package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.requests.accounts.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedAge
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedDescription
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedLinks
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedSex
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.*
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardBio(
        private var accountId: Long,
        private var sex: Long,
        private var age: Long,
        private var description: String,
        private var links: AccountLinks
) : Card(R.layout.screen_account_card_bio) {

    private val eventBus = EventBus
            .subscribe(EventAccountBioChangedAge::class) { onEventAccountBioChangedAge(it) }
            .subscribe(EventAccountBioChangedDescription::class) { onEventAccountBioChangedDescription(it) }
            .subscribe(EventAccountBioChangedLinks::class) { onEventAccountBioChangedLinks(it) }
            .subscribe(EventAccountBioChangedSex::class) { onEventAccountBioChangedSex(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vSex: TextView = view.findViewById(R.id.vSex)
        val vAge: TextView = view.findViewById(R.id.vAge)
        val vDescription: ViewTextLinkable = view.findViewById(R.id.vInfo)
        val vDescriptionContainer: View = view.findViewById(R.id.vDescriptionContainer)
        val vLinksContainer: ViewGroup = view.findViewById(R.id.vLinksContainer)
        val vAddLink: View = view.findViewById(R.id.vAddLink)

        vSex.text = ToolsResources.s(R.string.profile_appeal, if (sex == 0L) ToolsResources.sCap(R.string.he) else ToolsResources.sCap(R.string.she))
        vAge.text = ToolsResources.s(R.string.profile_age, if (age == 0L) ToolsResources.s(R.string.profile_age_not_set) else age)
        vDescription.text = if (description.isEmpty()) ToolsResources.s(R.string.profile_bio_empty) else description

        if (ControllerApi.isCurrentAccount(accountId)) {
            vSex.setOnClickListener { onSexClicked() }
            vAge.setOnClickListener { onAgeClicked() }
            vDescriptionContainer.setOnClickListener { onDescriptionClicked() }
            vAddLink.setOnClickListener { onChangeLinkClicked(links.getEmptyIndex(), R.string.app_create) }
            vAddLink.visibility = if (links.count() == API.ACCOUNT_LINK_MAX) View.GONE else View.VISIBLE
        } else {
            vSex.setOnClickListener(null)
            vAge.setOnClickListener(null)
            vDescriptionContainer.setOnClickListener(null)
            vAddLink.setOnClickListener(null)
            vAddLink.visibility = View.GONE

            if (description.isNotEmpty() && ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_DESCRIPTION)) {
                vDescriptionContainer.setOnClickListener { onAdminRemoveDescriptionClicked() }
            }

        }

        ControllerLinks.makeLinkable(vDescription)

        vLinksContainer.visibility = if (links.count() > 0) View.VISIBLE else View.GONE
        vLinksContainer.removeAllViews()

        for (i in 0 until links.links.size) {
            val link = links.links[i]
            if (link.a1.isEmpty() || link.a2.isEmpty()) continue
            val v: View = ToolsView.inflate(R.layout.screen_account_card_bio_view_link)
            val vTitle: TextView = v.findViewById(R.id.vTitle)
            val vUrl: TextView = v.findViewById(R.id.vUrl)

            vTitle.text = link.a1
            vUrl.text = link.a2

            v.setOnClickListener { ControllerLinks.openLink(link.a2) }

            val w = WidgetMenu()
                    .add(R.string.app_copy) { _, _ ->
                        ToolsAndroid.setToClipboard(link.a2)
                        ToolsToast.show(R.string.app_copied)
                    }
                    .add(R.string.app_change) { _, _ -> onChangeLinkClicked(i, R.string.app_change, link.a1, link.a2) }.condition(ControllerApi.isCurrentAccount(accountId))
                    .add(R.string.app_remove) { _, _ -> onRemoveLinkClicked(i) }.condition(ControllerApi.isCurrentAccount(accountId))
                    .add(R.string.app_remove) { _, _ -> onAdminRemoveLinkClicked(i) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(!ControllerApi.isCurrentAccount(accountId) && ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_LINK))

            v.setOnLongClickListener {
                w.asSheetShow()
                true
            }

            vLinksContainer.addView(v)
        }
    }

    //
    //  Clicks
    //

    private fun onSexClicked() {
        WidgetMenu()
                .add(R.string.he) { w, _ -> ControllerCampfireSDK.setSex(0){ ToolsToast.show(R.string.app_done)} }
                .add(R.string.she) { w, _ -> ControllerCampfireSDK.setSex(1){ ToolsToast.show(R.string.app_done)} }
                .asSheetShow()
    }

    private fun onAgeClicked() {
        WidgetAge(age) { w, age ->
            setAge(w, age)
        }.asSheetShow()
    }

    private fun onDescriptionClicked() {
        WidgetField()
                .setAutoHideOnEnter(false)
                .setHint(R.string.app_description)
                .setText(description)
                .setMax(API.ACCOUNT_DESCRIPTION_MAX_L)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_change) { w, t -> setDescription(w, t) }
                .asSheetShow()
    }

    private fun onChangeLinkClicked(index: Int, enterText: Int, title: String = "", url: String = "") {
        WidgetFieldTwo()
                .setHint_1(R.string.app_naming)
                .setMax_1(API.ACCOUNT_LINK_TITLE_MAX_L)
                .setMin_1(1)
                .setText_1(title)
                .setLinesCount_1(1)
                .setHint_2(R.string.app_link)
                .addChecker_2 { ToolsText.isWebLink(it) }
                .setMin_2(2)
                .setText_2(url)
                .setMax_2(API.ACCOUNT_LINK_URL_MAX_L)
                .setLinesCount_2(1)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(enterText) { w, titleV, urlV -> setLink(w, index, titleV, urlV) }
                .asSheetShow()
    }

    private fun onRemoveLinkClicked(index: Int) {
        WidgetAlert()
                .setText(R.string.app_remove_link)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_remove) { w -> setLink(w, index, "", "") }
                .asSheetShow()
    }

    private fun onAdminRemoveDescriptionClicked() {
        WidgetField()
                .setTitle(R.string.profile_remove_description)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_remove) { w, comment -> adminRemoveDescription(w, comment) }
                .asSheetShow()
    }

    private fun onAdminRemoveLinkClicked(index: Int) {
        WidgetField()
                .setTitle(R.string.app_remove_link)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_remove) { w, comment -> adminRemoveLink(w, index, comment) }
                .asSheetShow()
    }

    //
    //  Api
    //

    private fun setAge(widget: Widget, age: Long) {
        ApiRequestsSupporter.executeEnabled(widget, RAccountsBioSetAge(age)) {
            EventBus.post(EventAccountBioChangedAge(accountId, age))
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun setDescription(widget: Widget, description: String) {
        ApiRequestsSupporter.executeEnabled(widget, RAccountsBioSetDescription(description)) {
            EventBus.post(EventAccountBioChangedDescription(accountId, description))
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun setLink(widget: Widget, index: Int, description: String, url: String) {
        ApiRequestsSupporter.executeEnabled(widget, RAccountsBioSetLink(index, description, url)) {
            links.set(index, description, url)
            EventBus.post(EventAccountBioChangedLinks(accountId, links))
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun adminRemoveDescription(widget: Widget, comment: String) {
        ApiRequestsSupporter.executeEnabled(widget, RAccountsAdminRemoveDescription(accountId, comment)) {
            EventBus.post(EventAccountBioChangedDescription(accountId, ""))
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun adminRemoveLink(widget: Widget, index: Int, comment: String) {
        ApiRequestsSupporter.executeEnabled(widget, RAccountsAdminRemoveLink(accountId, index, comment)) {
            links.set(index, "", "")
            EventBus.post(EventAccountBioChangedLinks(accountId, links))
            ToolsToast.show(R.string.app_done)
        }
    }

    //
    //  EventBus
    //

    private fun onEventAccountBioChangedAge(e: EventAccountBioChangedAge) {
        if (e.accountId == accountId) {
            age = e.age
            update()
        }
    }

    private fun onEventAccountBioChangedDescription(e: EventAccountBioChangedDescription) {
        if (e.accountId == accountId) {
            description = e.description
            update()
        }
    }

    private fun onEventAccountBioChangedLinks(e: EventAccountBioChangedLinks) {
        if (e.accountId == accountId) {
            links = e.links
            update()
        }
    }

    private fun onEventAccountBioChangedSex(e: EventAccountBioChangedSex) {
        if (e.accountId == accountId) {
            sex = e.sex
            update()
        }
    }

}