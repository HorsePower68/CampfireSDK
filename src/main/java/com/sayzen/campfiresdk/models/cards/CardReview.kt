package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.UnitReview
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewRemoved
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewTextRemoved
import com.sayzen.campfiresdk.models.widgets.WidgetModerationBlock
import com.sayzen.campfiresdk.models.widgets.WidgetReview
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor

class CardReview(
        unit: UnitReview
) : CardUnit(R.layout.card_unit_review, unit) {

    private val eventBus = EventBus
            .subscribe(EventFandomReviewChanged::class) { this.onEventFandomReviewChanged(it) }
            .subscribe(EventFandomReviewTextRemoved::class) { this.onEventFandomReviewTextRemoved(it) }


    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitReview

        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTopContainer: View = view.findViewById(R.id.vTopContainer)
        val vStar1: ImageView = view.findViewById(R.id.vStar1)
        val vStar2: ImageView = view.findViewById(R.id.vStar2)
        val vStar3: ImageView = view.findViewById(R.id.vStar3)
        val vStar4: ImageView = view.findViewById(R.id.vStar4)
        val vStar5: ImageView = view.findViewById(R.id.vStar5)

        if (unit.rate == 5L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.green_700)))
        if (unit.rate == 4L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.green_700)))
        if (unit.rate == 3L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.yellow_700)))
        if (unit.rate == 2L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.red_700)))
        if (unit.rate == 1L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.red_700)))

        vMenu.setOnClickListener { onMenuClicked() }

        vText.text = unit.text
        vText.visibility = if (unit.text.isEmpty()) View.GONE else View.VISIBLE

        vStar1.setImageResource(ToolsResources.getDrawableAttrId(if (unit.rate >= 1) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar2.setImageResource(ToolsResources.getDrawableAttrId(if (unit.rate >= 2) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar3.setImageResource(ToolsResources.getDrawableAttrId(if (unit.rate >= 3) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar4.setImageResource(ToolsResources.getDrawableAttrId(if (unit.rate >= 4) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar5.setImageResource(ToolsResources.getDrawableAttrId(if (unit.rate >= 5) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
    }

    override fun updateAccount() {
        if (getView() == null) return
        val vAvatarTitle: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatarTitle)
        if (showFandom) xUnit.xFandom.setView(vAvatarTitle)
        else xUnit.xAccount.setView(vAvatarTitle)
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateKarma() {
        if (getView() == null) return
        xUnit.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    override fun updateReports() {
        if(getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun onMenuClicked() {
        val unit = xUnit.unit as UnitReview

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
                .add(R.string.app_clear_reports) { w, card -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_remove_text) { w, card -> WidgetModerationBlock.show(unit, {}) { it.setActionText(R.string.app_remove).setAlertText(R.string.review_remove_text_confirm, R.string.app_remove).setToastText(R.string.app_removed) } }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_REVIEW_REMOVE_TEXT) && !ControllerApi.isCurrentAccount(unit.creatorId))
                .asSheetShow()
    }


    override fun notifyItem() {

    }

    //
    //  EventBus
    //


    private fun onEventFandomReviewChanged(e: EventFandomReviewChanged) {
        val unit = xUnit.unit as UnitReview
        if (e.fandomId == unit.fandomId && e.languageId == unit.languageId && ControllerApi.isCurrentAccount(unit.creatorId)) {
            unit.rate = e.rateNew
            unit.text = e.text
            update()
        }
    }

    private fun onEventFandomReviewTextRemoved(e: EventFandomReviewTextRemoved) {
        val unit = xUnit.unit as UnitReview
        if (e.unitId == unit.id) {
            unit.text = ""
            update()
        }
    }


}
