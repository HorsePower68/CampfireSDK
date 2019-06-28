package com.sayzen.campfiresdk.models.cards.comments

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.notifications.NotificationComment
import com.dzen.campfire.api.models.notifications.NotificationCommentAnswer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.units.EventCommentChange
import com.sayzen.campfiresdk.models.events.units.EventCommentRemove
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsClear
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsThreads

abstract class CardComment protected constructor(
        override val unit: UnitComment,
        private val dividers: Boolean,
        protected val miniSize: Boolean,
        private val onClick: ((UnitComment) -> Boolean)? = null,
        private val onQuote: ((UnitComment) -> Unit)? = null,
        private var onGoTo: ((Long) -> Unit)? = null
) : CardUnit(unit) {


    companion object {

        fun instance(unit: UnitComment, dividers: Boolean, miniSize: Boolean, onClick: ((UnitComment) -> Boolean)? = null, onQuote: ((UnitComment) -> Unit)? = null, onGoTo: ((Long) -> Unit)? = null): CardComment {
            when (unit.type) {
                UnitComment.TYPE_TEXT -> return CardCommentText(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                UnitComment.TYPE_IMAGE, UnitComment.TYPE_GIF -> return CardCommentImage(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                UnitComment.TYPE_IMAGES -> return CardCommentImages(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                else -> throw RuntimeException("Unknown type ${unit.type}")
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventCommentChange::class) { e: EventCommentChange -> this.onCommentChange(e) }
            .subscribe(EventUnitReportsClear::class) { this.onEventUnitReportsClear(it) }
            .subscribe(EventUnitReportsAdd::class) { this.onEventUnitReportsAdd(it) }

    private val xKarma: XKarma = XKarma(unit) { updateKarma() }
    protected val xFandom: XFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xAccount: XAccount = XAccount(unit, unit.dateCreate) { update() }
    private var flash = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null
    protected var popup: Widget? = null

    init {
        ControllerNotifications.removeNotificationFromNew(NotificationComment::class, unit.id)
        ControllerNotifications.removeNotificationFromNew(NotificationCommentAnswer::class, unit.id)
    }

    protected abstract fun bind(view: View)

    override fun bindView(view: View) {
        super.bindView(view)
        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val vAvatar: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vLabel: TextView? = view.findViewById(R.id.vLabel)
        val vLabelName: TextView? = view.findViewById(R.id.vLabelName)
        val vLabelDate: TextView? = view.findViewById(R.id.vLabelDate)
        val vDivider: View? = view.findViewById(R.id.vDivider)
        val vText: ViewTextLinkable = view.findViewById(R.id.vCommentText)
        val vReports: TextView? = view.findViewById(R.id.vReports)
        val vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vQuoteText: ViewTextLinkable? = view.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesSwipe? = view.findViewById(R.id.vQuoteImage)

        if (vSwipe != null && onQuote != null) {
            popup = createPopup(vSwipe)
            vSwipe.onClick = { x, y ->
                if (ControllerApi.isCurrentAccount(unit.creatorId)) popup?.asSheetShow()
                else onClick()
            }
            vSwipe.onLongClick = { x, y -> popup?.asSheetShow() }
            vSwipe.onSwipe = {
                if (unit.type == UnitComment.TYPE_TEXT || unit.type == UnitComment.TYPE_IMAGE || unit.type == UnitComment.TYPE_GIF || unit.type == UnitComment.TYPE_IMAGES) {
                    onQuote.invoke(unit)
                } else {
                    onClick?.invoke(unit)
                }
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

        vText.text = unit.text
        ControllerApi.makeLinkable(vText) {
            val myName = ControllerApi.account.name + ","
            if (unit.text.startsWith(myName)) vText.text = Html.fromHtml(ToolsHTML.font_color(myName, "#FF6D00") + unit.text.substring(myName.length))
        }

        if (!showFandom) xAccount.setView(vAvatar)
        else xFandom.setView(vAvatar)

        if (vLabelName != null) vLabelName.text = unit.creatorName
        if (vLabelDate != null) vLabelDate.text = ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
        if (vLabel != null) vLabel.text = unit.creatorName + "   " + ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
        if (vDivider != null) vDivider.visibility = if (dividers) View.VISIBLE else View.GONE

        bind(view)
        updateKarma()
        updateFlash()
    }

    private fun updateFlash() {
        if (getView() == null) return
        val vRootContainer: View = getView()!!.findViewById(R.id.vRootContainer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (animationFlash != null) {
                vRootContainer.foreground = ColorDrawable(animationFlash!!.color)
            } else
                vRootContainer.foreground = ColorDrawable(0x00000000)
        } else {
            if (animationFlash != null) {
                vRootContainer.background = ColorDrawable(animationFlash!!.color)
            } else
                vRootContainer.background = ColorDrawable(0x00000000)
        }


        if (flash) {
            flash = false
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus_dark)), ToolsResources.getColor(R.color.focus_dark), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash?.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    { subscription ->
                        animationFlash?.update()
                        ToolsThreads.main { updateFlash() }
                    },
                    {
                        ToolsThreads.main {
                            animationFlash = null
                            updateFlash()
                        }
                    })
        }

    }

    private fun updateKarma() {
        if (getView() == null) return
        val vKarma: ViewKarma? = getView()!!.findViewById(R.id.vKarma)
        if (vKarma != null) xKarma.setView(vKarma)
    }

    private fun onClick(): Boolean {
        if (onClick == null) {
            if (unit.parentUnitType == 0L) {
                ToolsToast.show(R.string.post_error_gone)
            } else {
                ControllerUnits.toUnit(unit.parentUnitType, unit.parentUnitId, unit.id)
            }
            return false
        } else {
            return !onClick.invoke(unit)
        }
    }

    fun createPopup(vTouch: View): Widget {
        val w = WidgetMenu()
                .add(R.string.app_copy_link) { w, card ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToComment(unit))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_remove) { w, c -> ControllerApi.removeUnit(unit.id, R.string.comment_remove_confirm, R.string.comment_error_gone) { EventBus.post(EventCommentRemove(unit.id, unit.parentUnitId)) } }
                .add(R.string.app_change) { w, c -> WidgetComment(unit).asSheetShow() }
                .clearGroupCondition()
                .add(R.string.app_copy) { w, c ->
                    ToolsAndroid.setToClipboard(unit.text)
                    ToolsToast.show(R.string.app_copied)
                }
                .add(R.string.app_quote) { w, c -> onQuote?.invoke(unit) }.condition(onQuote != null && (unit.type == UnitComment.TYPE_TEXT || unit.type == UnitComment.TYPE_IMAGE || unit.type == UnitComment.TYPE_GIF || unit.type == UnitComment.TYPE_IMAGES))
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_report) { w, c -> ControllerApi.reportUnit(unit.id, R.string.comment_report_confirm, R.string.comment_error_gone) }
                .add(R.string.app_clear_reports) { w, c -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { w, c -> ControllerUnits.block(unit) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .showPopupWhenClickAndLongClick(vTouch) { onClick() }

        vTouch.setOnLongClickListener {
            w.asSheetShow()
            true
        }

        return w
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(unit.creatorImageId).intoCash()
    }

    //
    //  Methods
    //

    fun flash() {
        flash = true
        updateFlash()
    }

    override fun equals(other: Any?): Boolean {
        if (other is CardComment) return other.unit.id == unit.id
        return super.equals(other)
    }

    //
    //  Event Bus
    //

    private fun onCommentChange(e: EventCommentChange) {
        if (e.unitId == unit.id) {
            unit.text = e.text
            unit.quoteId = e.quoteId
            unit.quoteText = e.quoteText
            unit.changed = true
            update()
        }
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


}
