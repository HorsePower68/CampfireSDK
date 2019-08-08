package com.sayzen.campfiresdk.models.cards.chat

import android.graphics.drawable.ColorDrawable
import com.google.android.material.card.MaterialCardView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.NotificationChatMessageChange
import com.dzen.campfire.api.models.notifications.NotificationChatMessageRemove
import com.dzen.campfire.api.models.notifications.NotificationMention
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsClear
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.events.chat.EventChatReadDateChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.units.EventUnitBlocked
import com.sayzen.campfiresdk.models.events.units.EventUnitBlockedRemove
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoadingInterface
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

abstract class CardChatMessage constructor(
        override var unit: UnitChatMessage,
        var onClick: ((UnitChatMessage) -> Boolean)? = null,
        var onChange: ((UnitChatMessage) -> Unit)? = null,
        var onQuote: ((UnitChatMessage) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null,
        var onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardUnit(unit) {

    var changeEnabled = true

    companion object {

        fun instance(unit: UnitChatMessage,
                     onClick: ((UnitChatMessage) -> Boolean)? = null,
                     onChange: ((UnitChatMessage) -> Unit)? = null,
                     onQuote: ((UnitChatMessage) -> Unit)? = null,
                     onGoTo: ((Long) -> Unit)? = null,
                     onBlocked: ((UnitChatMessage) -> Unit)? = null
        ): CardChatMessage {
            when (unit.type) {
                UnitChatMessage.TYPE_TEXT -> return CardChatMessageText(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                UnitChatMessage.TYPE_IMAGE, UnitChatMessage.TYPE_GIF -> return CardChatMessageImage(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                UnitChatMessage.TYPE_IMAGES -> return CardChatMessageImages(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                UnitChatMessage.TYPE_BLOCK -> return CardChatMessageModeration(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                UnitChatMessage.TYPE_VOICE -> return CardChatMessageVoice(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                else -> throw RuntimeException("Unknown type ${unit.type}")
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { onNotification(it) }
            .subscribe(EventChatMessageChanged::class) { onEventChanged(it) }
            .subscribe(EventChatReadDateChanged::class) { onEventChatReadDateChanged(it) }
            .subscribe(EventUnitReportsClear::class) { onEventUnitReportsClear(it) }
            .subscribe(EventUnitReportsAdd::class) { onEventUnitReportsAdd(it) }
            .subscribe(EventStyleChanged::class) { update() }
            .subscribe(EventUnitBlocked::class) { onEventUnitBlocked(it) }

    val xAccount = XAccount(unit) { updateAccount() }
    val xFandom = XFandom(unit, unit.dateCreate) { updateAccount() }
    protected var popup: Widget? = null
    private var flash: Boolean = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val vNotRead: View? = view.findViewById(R.id.vNotRead)
        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val vText: ViewTextLinkable? = view.findViewById(R.id.vCommentText)
        val vRootContainer: ViewGroup? = view.findViewById(R.id.vRootContainer)
        val vMessageContainer: MaterialCardView? = view.findViewById(R.id.vMessageContainer)
        val vReports: TextView? = view.findViewById(R.id.vReports)
        val vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vQuoteText: ViewTextLinkable? = view.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesSwipe? = view.findViewById(R.id.vQuoteImage)

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, unit.id)
        }
        if (vSwipe != null){
            vSwipe.swipeEnabled = this !is CardChatMessageVoice
        }

        if (vSwipe != null && onQuote != null) {
            popup = createPopup(vSwipe)

            vSwipe.onClick = { x, y ->
                if (ControllerApi.isCurrentAccount(unit.creatorId)) popup?.asSheetShow()
                else onClick()
            }
            vSwipe.onLongClick = { x, y ->
                Debug.printStack()
                popup?.asSheetShow()
            }
            vSwipe.onSwipe = {
                if (onQuote != null && (unit.type == UnitChatMessage.TYPE_TEXT || unit.type == UnitChatMessage.TYPE_IMAGE || unit.type == UnitChatMessage.TYPE_GIF || unit.type == UnitChatMessage.TYPE_IMAGES))
                    onQuote?.invoke(unit)
                else
                    onClick?.invoke(unit)
            }
        } else {
            if (vSwipe != null) popup = createPopup(vSwipe)
        }



        if (vQuoteContainer != null) {
            vQuoteContainer.visibility = if (unit.quoteText.isEmpty() && unit.quoteImages.isEmpty()) View.GONE else View.VISIBLE
            vQuoteContainer.setOnClickListener {
                if (onGoTo != null) onGoTo!!.invoke(unit.quoteId)
            }
        }

        if (vQuoteText != null) {
            vQuoteText.text = unit.quoteText
            ControllerApi.makeLinkable(vQuoteText)
        }

        if (vQuoteImage != null) {
            vQuoteImage.clear()
            if (unit.quoteImages.isEmpty()) {
                vQuoteImage.visibility = View.GONE
            } else {
                vQuoteImage.visibility = View.VISIBLE
                for (i in unit.quoteImages) vQuoteImage.add(i)
            }
        }

        if (vReports != null) {
            vReports.text = unit.reportsCount.toString() + ""
            vReports.visibility = if (unit.reportsCount > 0 && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE
        }

        if (vText != null) {
            vText.text = unit.text
            vText.visibility = if (unit.text.isEmpty()) View.GONE else View.VISIBLE

            ControllerApi.makeLinkable(vText) {
                val myName = ControllerApi.account.name + ","
                if (unit.text.startsWith(myName)) vText.text = "{ff6d00 $myName}" + unit.text.substring(myName.length)
            }
        }

        if (vNotRead != null) {

            if (!ControllerApi.isCurrentAccount(unit.creatorId) || unit.chatType != API.CHAT_TYPE_PRIVATE)
                vNotRead.visibility = View.GONE
            else if (ControllerChats.isRead(unit.chatTag(), unit.dateCreate))
                vNotRead.visibility = View.INVISIBLE
            else
                vNotRead.visibility = View.VISIBLE

        }



        if (flash) {
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus)), ToolsResources.getColor(R.color.focus), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash?.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    { subscription ->
                        animationFlash?.update()
                        ToolsThreads.main { update() }
                    },
                    {
                        ToolsThreads.main {
                            flash = false
                            animationFlash = null
                            update()
                        }
                    })
        } else {
            animationFlash = null
        }

        if (animationFlash != null) {
            view.background = ColorDrawable(animationFlash!!.color)
        } else
            view.background = ColorDrawable(0x00000000)

        if (vRootContainer != null) {
            vRootContainer.visibility = View.VISIBLE
            (vRootContainer.layoutParams as FrameLayout.LayoutParams).gravity = if (ControllerApi.isCurrentAccount(unit.creatorId)) Gravity.RIGHT else Gravity.LEFT
        }

        if (vMessageContainer != null) {
            vMessageContainer.radius = ToolsView.dpToPx(ControllerSettings.styleChatRounding)
        }

        if (ControllerApi.isCurrentAccount(unit.creatorId)) {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(0).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
                if (ToolsColor.red(ToolsResources.getColorAttr(R.attr.widget_background)) < 0x60)
                    vMessageContainer.setCardBackgroundColor(ToolsColor.add(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
                else
                    vMessageContainer.setCardBackgroundColor(ToolsColor.remove(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(12).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(48).toInt()
            }
        } else {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(48).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(12).toInt()
                vMessageContainer.setCardBackgroundColor(ToolsResources.getColorAttr(R.attr.widget_background))
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(0).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
            }
        }

        updateAccount()

    }

    private fun updateAccount() {
        if (getView() == null) return

        val vAvatar: ViewAvatar? = getView()!!.findViewById(R.id.vAvatar)
        val vLabel: TextView? = getView()!!.findViewById(R.id.vLabel)

        if (vAvatar != null) {
            vAvatar.visibility = if (ControllerApi.isCurrentAccount(unit.creatorId)) View.GONE else View.VISIBLE
            if (unit.chatTag().chatType == API.CHAT_TYPE_PRIVATE) vAvatar.visibility = View.GONE
            if (!showFandom) xAccount.setView(vAvatar)
            else xFandom.setView(vAvatar)
        }

        if (vLabel != null) {
            if (ControllerApi.isCurrentAccount(unit.creatorId)) {
                (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.RIGHT
                vLabel.text = ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
            } else {
                (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
                if (unit.chatTag().chatType == API.CHAT_TYPE_PRIVATE)
                    vLabel.text = ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
                else
                    vLabel.text = xAccount.name + "  " + ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
            }
        }
    }

    fun flash() {
        flash = true
        update()
    }

    fun onClick(): Boolean {
        if (unit.type == UnitChatMessage.TYPE_BLOCK) {
            ControllerCampfireSDK.onToModerationClicked(unit.blockModerationEventId, 0, Navigator.TO)
            return false
        }

        if (onClick == null) {
            SChat.instance(unit.chatType, unit.fandomId, unit.languageId, true, Navigator.TO)
            return false
        } else {
            return onClick!!.invoke(unit)
        }
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(unit.creatorImageId).intoCash()
    }

    fun createPopup(vTouch: View): Widget {
        return WidgetMenu()
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_remove) { w, c ->
                    ControllerApi.removeUnit(unit.id, R.string.chat_remove_confirm, R.string.chat_error_gone) {
                        EventBus.post(EventUpdateChats())
                    }
                }
                .add(R.string.app_change) { w, c -> onChange?.invoke(unit) }.condition(changeEnabled)
                .clearGroupCondition()
                .add(R.string.app_copy) { w, c ->
                    ToolsAndroid.setToClipboard(unit.text)
                    ToolsToast.show(R.string.app_copied)
                }
                .add(R.string.app_quote) { w, c -> onQuote!!.invoke(unit) }.condition(onQuote != null && (unit.type == UnitChatMessage.TYPE_TEXT || unit.type == UnitChatMessage.TYPE_IMAGE || unit.type == UnitChatMessage.TYPE_GIF || unit.type == UnitChatMessage.TYPE_IMAGES))
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_report) { w, c -> ControllerApi.reportUnit(unit.id, R.string.chat_report_confirm, R.string.chat_error_gone) }.condition(unit.chatType == API.CHAT_TYPE_FANDOM)
                .add(R.string.app_clear_reports) { w, c -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).condition(unit.chatType == API.CHAT_TYPE_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { w, c -> ControllerUnits.block(unit) { if (adapter != null && adapter!! is RecyclerCardAdapterLoadingInterface) (adapter!! as RecyclerCardAdapterLoadingInterface).loadBottom() } }.backgroundRes(R.color.blue_700).condition(unit.chatType == API.CHAT_TYPE_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .showPopupWhenClickAndLongClick(vTouch,
                        { onClick() },
                        { unit.type != UnitChatMessage.TYPE_BLOCK }
                )
    }

    //
    //  Event Bus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessageChange) {
            val n = e.notification
            if ((n as NotificationChatMessageChange).unitId == unit.id) {
                unit.text = n.text
                unit.changed = true
                update()
            }
        } else if (e.notification is NotificationChatMessageRemove) {
            if ((e.notification as NotificationChatMessageRemove).unitId == unit.id && adapter != null) adapter!!.remove(this)

        }
    }

    private fun onEventChanged(e: EventChatMessageChanged) {
        if (e.unitId == unit.id) {
            unit.text = e.text
            unit.quoteId = e.quoteId
            unit.quoteText = e.quoteText
            unit.changed = true
            if (adapter!!.isVisible(this))
                flash()
            else
                update()
        }
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        if (e.tag == unit.chatTag()) update()
    }

    private fun onEventUnitReportsAdd(e: EventUnitReportsAdd) {
        if (e.unitId == unit.id) {
            unit.reportsCount++
            update()
        }
    }

    private fun onEventUnitReportsClear(e: EventUnitReportsClear) {
        if (e.unitId == unit.id) {
            unit.reportsCount = 0
            update()
        }
    }

    private fun onEventUnitBlocked(e: EventUnitBlocked) {
        if (e.unitId == unit.id) {
            if (onBlocked != null && e.unitChatMessage != null) onBlocked!!.invoke(e.unitChatMessage)

        }
    }

    override fun equals(other: Any?): Boolean {
        return if(other is CardChatMessage) unit.id == other.unit.id
        else super.equals(other)
    }

}