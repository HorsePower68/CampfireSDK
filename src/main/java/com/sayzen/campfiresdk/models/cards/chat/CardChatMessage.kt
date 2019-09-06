package com.sayzen.campfiresdk.models.cards.chat

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
import com.google.android.material.card.MaterialCardView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.events.chat.EventChatReadDateChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.units.EventUnitBlocked
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoadingInterface
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate

abstract class CardChatMessage constructor(
        layout: Int,
        unit: UnitChatMessage,
        var onClick: ((UnitChatMessage) -> Boolean)? = null,
        var onChange: ((UnitChatMessage) -> Unit)? = null,
        var onQuote: ((UnitChatMessage) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null,
        var onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardUnit(layout, unit) {

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
                UnitChatMessage.TYPE_STICKER -> return CardChatMessageSticker(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
                else -> return CardChatMessageUnknowm(unit, onClick, onChange, onQuote, onGoTo, onBlocked)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { onNotification(it) }
            .subscribe(EventChatMessageChanged::class) { onEventChanged(it) }
            .subscribe(EventChatReadDateChanged::class) { onEventChatReadDateChanged(it) }
            .subscribe(EventStyleChanged::class) { update() }
            .subscribe(EventUnitBlocked::class) { onEventUnitBlocked(it) }

    var changeEnabled = true
    var useMessageContainerBackground = true
    var quoteEnabled = true
    var copyEnabled = true

    init {
        updateFandomOnBind = false
        useBackgroundToFlash = true
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitChatMessage

        val vNotRead: View? = view.findViewById(R.id.vNotRead)
        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val vText: ViewTextLinkable? = view.findViewById(R.id.vCommentText)
        val vRootContainer: ViewGroup? = view.findViewById(R.id.vRootContainer)
        val vMessageContainer: MaterialCardView? = view.findViewById(R.id.vMessageContainer)
        val vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vQuoteText: ViewTextLinkable? = view.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesSwipe? = view.findViewById(R.id.vQuoteImage)

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, unit.id)
        }
        if (vSwipe != null) {
            vSwipe.swipeEnabled = this !is CardChatMessageVoice
        }

        if (vSwipe != null && onQuote != null) {
            vSwipe.onClick = { _, _ ->
                if (ControllerApi.isCurrentAccount(unit.creatorId)) showMenu()
                else if(!onClick()) showMenu()
            }
            vSwipe.onLongClick = { _, _ -> showMenu() }
            vSwipe.onSwipe = { onQuote?.invoke(unit) }
            vSwipe.swipeEnabled = quoteEnabled
        } else {
            if (vSwipe != null) {
                vSwipe.swipeEnabled = quoteEnabled
            }
        }

        if (vQuoteContainer != null) {
            vQuoteContainer.visibility = if (unit.quoteText.isEmpty() && unit.quoteImages.isEmpty() && unit.quoteStickerId < 1L) View.GONE else View.VISIBLE
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
            vQuoteImage.visibility = View.VISIBLE
            if (unit.quoteStickerId > 0) {
                vQuoteImage.add(unit.quoteStickerImageId, onClick = { SStickersView.instanceBySticker(unit.quoteStickerId, Navigator.TO) })
            } else if (unit.quoteImages.isNotEmpty()) {
                for (i in unit.quoteImages) vQuoteImage.add(i)
            } else {
                vQuoteImage.visibility = View.GONE
            }
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
                if (useMessageContainerBackground) {
                    if (ToolsColor.red(ToolsResources.getColorAttr(R.attr.widget_background)) < 0x60)
                        vMessageContainer.setCardBackgroundColor(ToolsColor.add(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
                    else
                        vMessageContainer.setCardBackgroundColor(ToolsColor.remove(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
                } else {
                    vMessageContainer.setCardBackgroundColor(0x00000000)
                }
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(12).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(48).toInt()
            }
        } else {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(48).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(12).toInt()
                if (useMessageContainerBackground) vMessageContainer.setCardBackgroundColor(ToolsResources.getColorAttr(R.attr.widget_background))
                else vMessageContainer.setCardBackgroundColor(0x00000000)
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(0).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
            }
        }
    }

    fun showMenu() {
        val unit = xUnit.unit as UnitChatMessage
        WidgetMenu()
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_remove) { _, _ ->
                    ControllerApi.removeUnit(unit.id, R.string.chat_remove_confirm, R.string.chat_error_gone) {
                        EventBus.post(EventUpdateChats())
                    }
                }
                .add(R.string.app_change) { _, _ -> onChange?.invoke(unit) }.condition(changeEnabled)
                .clearGroupCondition()
                .add(R.string.app_copy) { _, _ ->
                    ToolsAndroid.setToClipboard(unit.text)
                    ToolsToast.show(R.string.app_copied)
                }.condition(copyEnabled)
                .add(R.string.app_quote) { _, _ -> onQuote!!.invoke(unit) }.condition(quoteEnabled && onQuote != null)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.chat_report_confirm, R.string.chat_error_gone) }.condition(unit.chatType == API.CHAT_TYPE_FANDOM)
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(unit.chatType == API.CHAT_TYPE_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) { if (adapter != null && adapter!! is RecyclerCardAdapterLoadingInterface) (adapter!! as RecyclerCardAdapterLoadingInterface).loadBottom() } }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(unit.chatType == API.CHAT_TYPE_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .asSheetShow()
    }


    override fun updateAccount() {
        if (getView() == null) return
        val unit = xUnit.unit as UnitChatMessage

        val vAvatar: ViewAvatar? = getView()!!.findViewById(R.id.vAvatar)
        val vLabel: TextView? = getView()!!.findViewById(R.id.vLabel)

        if (vAvatar != null) {
            vAvatar.visibility = if (ControllerApi.isCurrentAccount(unit.creatorId)) View.GONE else View.VISIBLE
            if (unit.chatTag().chatType == API.CHAT_TYPE_PRIVATE) vAvatar.visibility = View.GONE
            if (!showFandom) xUnit.xAccount.setView(vAvatar)
            else xUnit.xFandom.setView(vAvatar)
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
                    vLabel.text = xUnit.xAccount.name + "  " + ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
            }
        }
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports: TextView? = getView()!!.findViewById(R.id.vReports)
        if (vReports != null) xUnit.xReports.setView(vReports)

    }

    fun onClick(): Boolean {
        val unit = xUnit.unit as UnitChatMessage
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
        val unit = xUnit.unit as UnitChatMessage
        ToolsImagesLoader.load(unit.creatorImageId).intoCash()
    }

    //
    //  Event Bus
    //

    private fun onNotification(e: EventNotification) {
        val unit = xUnit.unit as UnitChatMessage
        if (e.notification is NotificationChatMessageChange) {
            val n = e.notification
            if (n.unitId == unit.id) {
                unit.text = n.text
                unit.changed = true
                update()
            }
        } else if (e.notification is NotificationChatMessageRemove) {
            if (e.notification.unitId == unit.id && adapter != null) adapter!!.remove(this)

        }
    }

    private fun onEventChanged(e: EventChatMessageChanged) {
        val unit = xUnit.unit as UnitChatMessage
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
        val unit = xUnit.unit as UnitChatMessage
        if (e.tag == unit.chatTag()) update()
    }

    private fun onEventUnitBlocked(e: EventUnitBlocked) {
        val unit = xUnit.unit as UnitChatMessage
        if (e.unitId == unit.id) {
            if (onBlocked != null && e.unitChatMessage != null) onBlocked!!.invoke(e.unitChatMessage)

        }
    }

    override fun equals(other: Any?): Boolean {
        val unit = xUnit.unit as UnitChatMessage
        return if (other is CardChatMessage) unit.id == other.xUnit.unit.id
        else super.equals(other)
    }

}