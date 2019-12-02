package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationReview
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
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
        publication: PublicationReview
) : CardPublication(R.layout.card_unit_review, publication) {

    private val eventBus = EventBus
            .subscribe(EventFandomReviewChanged::class) { this.onEventFandomReviewChanged(it) }
            .subscribe(EventFandomReviewTextRemoved::class) { this.onEventFandomReviewTextRemoved(it) }


    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationReview

        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTopContainer: View = view.findViewById(R.id.vTopContainer)
        val vStar1: ImageView = view.findViewById(R.id.vStar1)
        val vStar2: ImageView = view.findViewById(R.id.vStar2)
        val vStar3: ImageView = view.findViewById(R.id.vStar3)
        val vStar4: ImageView = view.findViewById(R.id.vStar4)
        val vStar5: ImageView = view.findViewById(R.id.vStar5)

        if (publication.rate == 5L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.green_700)))
        if (publication.rate == 4L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.green_700)))
        if (publication.rate == 3L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.yellow_700)))
        if (publication.rate == 2L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(30, ToolsResources.getColor(R.color.red_700)))
        if (publication.rate == 1L) vTopContainer.setBackgroundColor(ToolsColor.setAlpha(60, ToolsResources.getColor(R.color.red_700)))

        vMenu.setOnClickListener { onMenuClicked() }

        vText.text = publication.text
        vText.visibility = if (publication.text.isEmpty()) View.GONE else View.VISIBLE

        vStar1.setImageResource(ToolsResources.getDrawableAttrId(if (publication.rate >= 1) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar2.setImageResource(ToolsResources.getDrawableAttrId(if (publication.rate >= 2) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar3.setImageResource(ToolsResources.getDrawableAttrId(if (publication.rate >= 3) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar4.setImageResource(ToolsResources.getDrawableAttrId(if (publication.rate >= 4) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
        vStar5.setImageResource(ToolsResources.getDrawableAttrId(if (publication.rate >= 5) R.attr.ic_star_24dp else R.attr.ic_star_border_24dp))
    }

    override fun updateAccount() {
        if (getView() == null) return
        val vAvatarTitle: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatarTitle)
        if (showFandom) xPublication.xFandom.setView(vAvatarTitle)
        else xPublication.xAccount.setView(vAvatarTitle)
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateKarma() {
        if (getView() == null) return
        xPublication.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    override fun updateReports() {
        if(getView() == null) return
        xPublication.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun onMenuClicked() {
        val publication = xPublication.publication as PublicationReview

        WidgetMenu()
                .groupCondition(ControllerApi.isCurrentAccount(publication.creatorId))
                .add(R.string.app_change) { _, _ -> WidgetReview(publication.fandomId, publication.languageId, publication.rate, publication.text) {}.asSheetShow() }.condition(publication.isPublic)
                .add(R.string.app_remove) { _, _ -> ControllerApi.removePublication(publication.id, R.string.review_remove_confirm, R.string.review_error_gone) { EventBus.post(EventFandomReviewRemoved(publication.fandomId, publication.languageId, publication.id, publication.rate)) } }
                .groupCondition(!ControllerApi.isCurrentAccount(publication.creatorId) && publication.isPublic)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportPublication(publication.id, R.string.review_report_confirm, R.string.review_error_gone) }
                .clearGroupCondition()
                .add(R.string.app_share) { _, _ -> ControllerApi.shareReview(publication.id) }.condition(publication.isPublic)
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerLinks.linkToReview(publication.id))
                    ToolsToast.show(R.string.app_copied)
                }
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearReportsPublication(publication.id, publication.publicationType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_BLOCK) && publication.reportsCount > 0)
                .add(R.string.app_remove_text) { _, _ -> WidgetModerationBlock.show(publication, {}) { it.setActionText(R.string.app_remove).setAlertText(R.string.review_remove_text_confirm, R.string.app_remove).setToastText(R.string.app_removed) } }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_REVIEW_REMOVE_TEXT) && !ControllerApi.isCurrentAccount(publication.creatorId))
                .asSheetShow()
    }


    override fun notifyItem() {

    }

    //
    //  EventBus
    //


    private fun onEventFandomReviewChanged(e: EventFandomReviewChanged) {
        val publication = xPublication.publication as PublicationReview
        if (e.fandomId == publication.fandomId && e.languageId == publication.languageId && ControllerApi.isCurrentAccount(publication.creatorId)) {
            publication.rate = e.rateNew
            publication.text = e.text
            update()
        }
    }

    private fun onEventFandomReviewTextRemoved(e: EventFandomReviewTextRemoved) {
        val publication = xPublication.publication as PublicationReview
        if (e.publicationId == publication.id) {
            publication.text = ""
            update()
        }
    }


}
