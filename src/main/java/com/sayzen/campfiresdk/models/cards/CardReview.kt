package com.sayzen.campfiresdk.models.cards

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.UnitReview
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewRemoved
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewTextRemoved
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsClear
import com.sayzen.campfiresdk.models.widgets.WidgetModerationBlock
import com.sayzen.campfiresdk.models.widgets.WidgetReview
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsThreads

class CardReview(override val unit: UnitReview) : CardUnit(unit) {

    private val eventBus = EventBus
            .subscribe(EventUnitReportsClear::class) { this.onEventUnitReportsClear(it) }
            .subscribe(EventUnitReportsAdd::class) { this.onEventUnitReportsAdd(it) }
            .subscribe(EventFandomReviewChanged::class) { this.onEventFandomReviewChanged(it) }
            .subscribe(EventFandomReviewTextRemoved::class) { this.onEventFandomReviewTextRemoved(it) }

    private val xFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xAccount: XAccount = XAccount(unit, unit.dateCreate) { update() }
    private val xKarma = XKarma(unit) { update() }
    private var flash: Boolean = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null

    override fun getLayout() = R.layout.card_review

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatarTitle: ViewAvatarTitle = view.findViewById(R.id.vAvatarTitle)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vReports: TextView = view.findViewById(R.id.vReports)
        val vKarma: ViewKarma = view.findViewById(R.id.vKarma)
        val vTopContainer: View = view.findViewById(R.id.vTopContainer)
        val vStar1: ImageView = view.findViewById(R.id.vStar1)
        val vStar2: ImageView = view.findViewById(R.id.vStar2)
        val vStar3: ImageView = view.findViewById(R.id.vStar3)
        val vStar4: ImageView = view.findViewById(R.id.vStar4)
        val vStar5: ImageView = view.findViewById(R.id.vStar5)

        if(unit.rate == 5L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.green_700)))
        if(unit.rate == 4L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.green_700)))
        if(unit.rate == 3L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.yellow_700)))
        if(unit.rate == 2L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.red_700)))
        if(unit.rate == 1L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.red_700)))


        vMenu.setOnClickListener {
            WidgetMenu()
                    .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                    .add(R.string.app_change) { w, card -> WidgetReview(unit.fandomId, unit.languageId, unit.rate, unit.text) {}.asSheetShow() }.condition(unit.isPublic)
                    .add(R.string.app_remove) { w, card -> ControllerApi.removeUnit(unit.id, R.string.review_remove_confirm, R.string.review_error_gone) { EventBus.post(EventFandomReviewRemoved(unit.fandomId, unit.languageId, unit.id, unit.rate)) } }
                    .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId) && unit.isPublic)
                    .add(R.string.app_report) { w, card -> ControllerApi.reportUnit(unit.id, R.string.review_report_confirm, R.string.review_error_gone) }
                    .clearGroupCondition()
                    .add(R.string.app_share) { w, card -> ControllerApi.shareReview(unit.id) }.condition(unit.isPublic)
                    .add(R.string.app_copy_link) { w, card ->
                        ToolsAndroid.setToClipboard(ControllerApi.linkToReview(unit.id))
                        ToolsToast.show(R.string.app_copied)
                    }
                    .add(R.string.app_clear_reports) { w, card -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                    .add(R.string.app_remove_text) { w, card -> WidgetModerationBlock.show(unit, {}){it.setActionText(R.string.app_remove).setAlertText(R.string.review_remove_text_confirm, R.string.app_remove).setToastText(R.string.app_removed)} }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_REVIEW_REMOVE_TEXT) && !ControllerApi.isCurrentAccount(unit.creatorId))
                    .asSheetShow()
        }

        vReports.text = unit.reportsCount.toString() + ""
        vReports.visibility = if (unit.reportsCount > 0 && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE

        if (showFandom) xFandom.setView(vAvatarTitle)
        else xAccount.setView(vAvatarTitle)

        xKarma.setView(vKarma)

        vText.text = unit.text
        vText.visibility = if (unit.text.isEmpty()) View.GONE else View.VISIBLE


        vStar1.setImageResource(ToolsResources.getDrawableAttrId(if(unit.rate >= 1) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar2.setImageResource(ToolsResources.getDrawableAttrId(if(unit.rate >= 2) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar3.setImageResource(ToolsResources.getDrawableAttrId(if(unit.rate >= 3) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar4.setImageResource(ToolsResources.getDrawableAttrId(if(unit.rate >= 4) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar5.setImageResource(ToolsResources.getDrawableAttrId(if(unit.rate >= 5) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (animationFlash != null) {
                view.foreground = ColorDrawable(animationFlash!!.color)
            } else
                view.foreground = ColorDrawable(0x00000000)
        } else {
            if (animationFlash != null) {
                view.background = ColorDrawable(animationFlash!!.color)
            } else
                view.background = ColorDrawable(0x00000000)
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
                        ToolsThreads.main { update() }
                    },
                    {
                        ToolsThreads.main {
                            animationFlash = null
                            update()
                        }
                    })
        }

    }

    override fun notifyItem() {

    }

    fun flash() {
        flash = true
        update()
    }

    //
    //  EventBus
    //

    private fun onEventUnitReportsClear(e: EventUnitReportsClear) {
        if (e.unitId == unit.id) {
            unit.reportsCount = 0
            update()
        }
    }

    private fun onEventUnitReportsAdd(e: EventUnitReportsAdd) {
        if (e.unitId == unit.id) {
            unit.reportsCount++
            update()
        }
    }

    private fun onEventFandomReviewChanged(e: EventFandomReviewChanged) {
        if (e.fandomId == unit.fandomId && e.languageId == unit.languageId && ControllerApi.isCurrentAccount(unit.creatorId)) {
            unit.rate = e.rateNew
            unit.text = e.text
            update()
        }
    }
    private fun onEventFandomReviewTextRemoved(e: EventFandomReviewTextRemoved) {
        if (e.unitId == unit.id) {
            unit.text = ""
            update()
        }
    }


}
