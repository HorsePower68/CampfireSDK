package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.events_user.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.tools.ToolsDate

class CardPublicationEventUser(
        publication: PublicationEventUser
) : CardPublication(R.layout.card_event, publication) {

    private val xAccount: XAccount
    private val xAccountAdmin: XAccount

    init {
        val e = publication.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
        xAccountAdmin = XAccount(e.adminAccountId, e.adminAccountName, e.adminAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationEventUser

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
        var willResetimage = true

        xPublication.xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventUserAchievement -> {
                willResetimage = false
                text = ToolsResources.sCap(R.string.publication_event_achievement, ToolsResources.sex(e.ownerAccountSex, R.string.he_gained, R.string.she_gained), CampfireConstants.getAchievement(e.achievementIndex).getText(false))
                ToolsImagesLoader.load(CampfireConstants.getAchievement(e.achievementIndex).image).into(vAvatarTitle.vImageView)
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfireConstants.getAchievement(e.achievementIndex).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creatorId, publication.creatorName, e.achievementIndex, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creatorId, publication.creatorName, e.achievementIndex, false, Navigator.TO) }
            }
            is ApiEventUserQuestFinish -> {
                willResetimage = false
                text = ToolsResources.sCap(R.string.publication_event_quest_finish, ToolsResources.sex(e.ownerAccountSex, R.string.he_finished, R.string.she_finished)) + ":"
                text += "\n" + ToolsResources.s(CampfireConstants.getQuest(e.questIndex).text)
                ToolsImagesLoader.load(CampfireConstants.getAchievement(API.ACHI_QUESTS).image).into(vAvatarTitle.vImageView)
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfireConstants.getAchievement(API.ACHI_QUESTS).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creatorId, publication.creatorName, API.ACHI_QUESTS.index, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creatorId, publication.creatorName, API.ACHI_QUESTS.index, false, Navigator.TO) }
            }
            is ApiEventUserAdminBaned -> {
                text = ToolsResources.sCap(R.string.publication_event_blocked_app, ToolsResources.sex(e.adminAccountSex, R.string.he_baned, R.string.she_baned), ToolsDate.dateToStringFull(e.blockDate), ControllerLinks.linkToAccount(e.adminAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPublicationBlocked -> {
                val publicationName = ControllerPublications.getName(e.publicationType)
                text = ToolsResources.sCap(R.string.publication_event_blocked_publication, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_blocked, R.string.she_blocked), publicationName)
                if (e.blockAccountDate > 0 && e.blockFandomId < 1) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (e.blockAccountDate > 0 && e.blockFandomId > 0) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName}")
                if (e.warned) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_warn)
                if (e.lastPublicationsBlocked) text += "\n" + ToolsResources.s(R.string.publication_event_account_blocked_last_publications)
                view.setOnClickListener {
                    if (e.moderationId > 0) ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0L, Navigator.TO)
                    else ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO)
                }
            }
            is ApiEventUserAdminModerationRejected -> {
                text = ToolsResources.sCap(R.string.publication_event_user_moder_action_rejected, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_reject, R.string.she_reject), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminNameChanged -> {
                text = ToolsResources.sCap(R.string.publication_event_change_name, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, e.ownerAccountName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPunishmentRemove -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_punishment, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveDescription -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_description, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveImage -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_image, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveLink -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_link, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveName -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_name, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveStatus -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_status, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveTitleImage -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_titile_image, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPublicationRestored -> {
                text = ToolsResources.sCap(R.string.publication_event_publication_restore, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore))
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminWarned -> {
                text = ToolsResources.sCap(R.string.publication_event_warn_app, ControllerLinks.linkToAccount(e.adminAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomMakeModerator -> {
                text = ToolsResources.sCap(R.string.publication_event_make_moderator, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomRemoveModerator -> {
                text = ToolsResources.sCap(R.string.publication_event_remove_moderator, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomSuggest -> {
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, ControllerApi.getLanguageId(), Navigator.TO) }
                text = ToolsResources.sCap(R.string.publication_event_fandom_suggested, ToolsResources.sex(e.ownerAccountSex, R.string.he_suggest, R.string.she_suggest), e.fandomName)

            }
            is ApiEventUserAdminPostChangeFandom -> {
                text = ToolsResources.sCap(R.string.publication_event_post_fandom_change, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_move, R.string.she_move), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminViceroyAssign -> {
                text = ToolsResources.sCap(R.string.publication_event_user_viceroy_assign, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_assign, R.string.she_assign), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
            is ApiEventUserAdminViceroyRemove -> {
                text = ToolsResources.sCap(R.string.publication_event_user_viceroy_remove, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_denied, R.string.she_denied), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerLinks.makeLinkable(vText)

        if(willResetimage) {
             if (showFandom && publication.fandomId > 0) {
                xPublication.xFandom.setView(vAvatarTitle)
                vName.text = xPublication.xFandom.name
            } else if (e.adminAccountId > 0) {
                xAccountAdmin.setView(vAvatarTitle)
                vName.text = xAccountAdmin.name
            } else {
                xAccount.setView(vAvatarTitle)
                vName.text = xAccount.name
            }
        }

        when (e) {
            is ApiEventUserFandomMakeModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventUserFandomRemoveModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventUserAdminViceroyAssign -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventUserAdminViceroyRemove -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventUserAdminPublicationBlocked -> ToolsView.addLink(vText, e.blockFandomName) { SFandom.instance(e.blockFandomId, Navigator.TO) }
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
