package com.sayzen.campfiresdk.models.cards.history

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate

class CardHistoryUnit(
        val historyUnit: HistoryUnit
) : Card(R.layout.card_history_unit) {

    val history = historyUnit.history


    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        ToolsImagesLoader.load(history.userImageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.vAvatar.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(history.userId, Navigator.TO) }
        vAvatar.setTitle(history.userName + " " + ToolsDate.dateToString(historyUnit.date))
        vAvatar.setSubtitle("")

        when (history) {
            is HistoryCreate -> vAvatar.setSubtitle(R.string.history_created)
            is HistoryAdminBackDraft -> vAvatar.setSubtitle(R.string.history_admin_back_draft)
            is HistoryAdminBlock -> vAvatar.setSubtitle(R.string.history_admin_block)
            is HistoryAdminChangeFandom -> vAvatar.setSubtitle(ToolsResources.s(R.string.history_admin_change_fandom, "${history.oldFandomName} ( ${API.LINK_SHORT_FANDOM_ID + history.oldFandomId})", "${history.newFandomName} (${API.LINK_SHORT_FANDOM_ID + history.newFandomId})"))
            is HistoryAdminClearReports -> vAvatar.setSubtitle(R.string.history_admin_clear_reports)
            is HistoryAdminDeepBlock -> vAvatar.setSubtitle(R.string.history_admin_deep_block)
            is HistoryAdminNotBlock -> vAvatar.setSubtitle(R.string.history_admin_not_block)
            is HistoryAdminNotDeepBlock -> vAvatar.setSubtitle(R.string.history_admin_not_deep_block)
            is HistoryAdminNotMultilingual -> vAvatar.setSubtitle(R.string.history_admin_not_multilingual)
            is HistoryBackDraft -> vAvatar.setSubtitle(R.string.history_back_draft)
            is HistoryChangeFandom -> vAvatar.setSubtitle(ToolsResources.s(R.string.history_change_fandom, "${history.oldFandomName} ( ${API.LINK_SHORT_FANDOM_ID + history.oldFandomId})", "${history.newFandomName} (${API.LINK_SHORT_FANDOM_ID + history.newFandomId})"))
            is HistoryMultilingual -> vAvatar.setSubtitle(R.string.history_multilingual)
            is HistoryNotMultolingual -> vAvatar.setSubtitle(R.string.history_not_multilingual)
            is HistoryPublish -> vAvatar.setSubtitle(R.string.history_publish)
            is HistoryAdminChangeTags -> vAvatar.setSubtitle(R.string.history_admin_change_tags)
            is HistoryAdminImportant -> vAvatar.setSubtitle(R.string.history_admin_important)
            is HistoryAdminNotImportant -> vAvatar.setSubtitle(R.string.history_admin_not_important)
            is HistoryAdminPinFandom -> vAvatar.setSubtitle(R.string.history_admin_pin_fandom)
            is HistoryAdminUnpinFandom -> vAvatar.setSubtitle(R.string.history_admin_unpin_fandom)
            is HistoryChangeTags -> vAvatar.setSubtitle(R.string.history_change_tags)
            is HistoryPinProfile -> vAvatar.setSubtitle(R.string.history_pin_profile)
            is HistoryUnpinProfile -> vAvatar.setSubtitle(R.string.history_unpin_profile)
            is HistoryClose -> vAvatar.setSubtitle(R.string.history_close)
            is HistoryCloseNo -> vAvatar.setSubtitle(R.string.history_close_no)
            is HistoryAdminClose -> vAvatar.setSubtitle(R.string.history_admin_close)
            is HistoryAdminCloseNo -> vAvatar.setSubtitle(R.string.history_admin_close_no)
            is HistoryEditPublic -> {
                vAvatar.setSubtitle(R.string.history_edit_public)
                if (history.oldText.isNotEmpty())vAvatar.setSubtitle(vAvatar.getSubTitle() + "\n\"${history.oldText}\"")
            }
        }

        if (history.comment.isNotEmpty()) vAvatar.setSubtitle(vAvatar.getSubTitle() + "\n" + ToolsResources.s(R.string.app_comment) + ": " + history.comment)
        ControllerApi.makeLinkable(vAvatar.vSubtitle)


    }

}