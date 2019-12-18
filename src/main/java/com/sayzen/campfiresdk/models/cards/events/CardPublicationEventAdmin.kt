package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.publications.events_admins.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardPublicationEventAdmin(
        publication: PublicationEventAdmin
) : CardPublication(R.layout.card_event, publication) {

    private val xAccount: XAccount

    init {
        val e = publication.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationEventAdmin

        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vDate.text = ToolsDate.dateToString(publication.dateCreate)
        vName.text = ""
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = publication.event!!
        var text = ""

        xPublication.xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {

            is ApiEventAdminBan -> {
                text = ToolsResources.sCap(R.string.publication_event_blocked_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), ControllerLinks.linkToAccount(e.targetAccountName), ToolsDate.dateToStringFull(e.blockDate))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminBlockPublication -> {
                val publicationName = ControllerPublications.getName(e.publicationType)
                text = ToolsResources.sCap(R.string.publication_event_admin_blocked_publication, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), publicationName, ControllerLinks.linkToAccount(e.targetAccountName))
                if (e.blockAccountDate > 0 && e.blockedInApp && e.blockFandomId < 1) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (e.blockAccountDate > 0 && !e.blockedInApp && e.blockFandomId > 0) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName}")
                if (e.warned) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_warn)
                if (e.lastPublicationsBlocked) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_last_publications)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminChangeName -> {
                text = ToolsResources.sCap(R.string.publication_event_change_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeAvatar -> {
                text = ToolsResources.sCap(R.string.publication_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeCategory -> {
                text = ToolsResources.sCap(R.string.publication_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed),
                        e.fandomName,
                        CampfireConstants.getCategory(e.oldCategory).name,
                        CampfireConstants.getCategory(e.newCategory).name)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeParams -> {
                text = ToolsResources.sCap(R.string.publication_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName)

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.publication_event_fandom_genres_new) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.publication_event_fandom_genres_remove) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomClose -> {
                text = ToolsResources.sCap(R.string.publication_event_fandom_close, if (e.closed) ToolsResources.sex(e.ownerAccountSex, R.string.he_close, R.string.she_close) else ToolsResources.sex(e.ownerAccountSex, R.string.he_open, R.string.she_open), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomKarmaCofChanged -> {
                text = "" + ToolsResources.sCap(R.string.publication_event_fandom_karma_cof, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.fandomName, ToolsText.numToStringRound(e.oldCof / 100.0, 2), ToolsText.numToStringRound(e.newCof / 100.0, 2))
            }
            is ApiEventAdminFandomMakeModerator -> {
                text = ToolsResources.sCap(R.string.publication_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRemove -> {
                text = "" + ToolsResources.sCap(R.string.publication_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), e.fandomName)
            }
            is ApiEventAdminFandomRemoveModerator -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRename -> {
                text = ToolsResources.sCap(R.string.publication_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, R.string.he_rename, R.string.she_rename), e.oldName, "" + e.newName)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminModerationRejected -> {
                text = ToolsResources.sCap(R.string.publication_event_moderation_rejected_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_reject, R.string.she_reject), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminPostChangeFandom -> {
                text = ToolsResources.sCap(R.string.publication_event_post_fandom_change_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_move, R.string.she_move), ControllerLinks.linkToAccount(e.targetAccountName), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventAdminPunishmentRemove -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_punishment_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminPublicationRestore -> {
                text = ToolsResources.sCap(R.string.publication_event_publication_restore_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveDescription -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_description_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveImage -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveLink -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_link_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveName -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveStatus -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_status_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveTitleImage -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_titile_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminWarn -> {
                text = ToolsResources.sCap(R.string.publication_event_warn_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_warn, R.string.she_warn), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomAccepted -> {
                text = ToolsResources.sCap(R.string.publication_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, R.string.he_accept, R.string.she_accept), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomViceroyAssign -> {
                text = if(e.oldAccountName.isEmpty()) ToolsResources.sCap(R.string.publication_event_admin_viceroy_assign, ToolsResources.sex(e.ownerAccountSex, R.string.he_assign, R.string.she_assign), ControllerLinks.linkToAccount(e.newAccountName), e.fandomName)
                else ToolsResources.sCap(R.string.publication_event_admin_viceroy_assign_instead, ToolsResources.sex(e.ownerAccountSex, R.string.he_assign, R.string.she_assign), ControllerLinks.linkToAccount(e.newAccountName), e.fandomName, ControllerLinks.linkToAccount(e.oldAccountName))
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO)}
            }
            is ApiEventAdminFandomViceroyRemove -> {
                text = ToolsResources.sCap(R.string.publication_event_admin_viceroy_remove, ToolsResources.sex(e.ownerAccountSex, R.string.he_denied, R.string.she_denied), ControllerLinks.linkToAccount(e.oldAccountName), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerLinks.makeLinkable(vText)

        if (showFandom && publication.fandomId > 0) {
            xPublication.xFandom.setView(vAvatarTitle)
            vName.text = xPublication.xFandom.name
        } else {
            xAccount.setView(vAvatarTitle)
            vName.text = xAccount.name
        }


        when (e) {
            is ApiEventAdminFandomClose -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminBlockPublication -> ToolsView.addLink(vText, e.blockFandomName) { SFandom.instance(e.blockFandomId, Navigator.TO) }
            is ApiEventAdminFandomChangeAvatar -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomChangeParams -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomKarmaCofChanged -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomMakeModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomRemoveModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomRename -> ToolsView.addLink(vText, e.newName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomViceroyAssign -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventAdminFandomViceroyRemove -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
        }

    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        update()
    }

    override fun updateAccount() {
        update()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        update()
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {

    }

}
