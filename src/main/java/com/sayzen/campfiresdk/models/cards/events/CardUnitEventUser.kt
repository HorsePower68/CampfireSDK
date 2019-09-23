package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.events_user.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.tools.ToolsDate

class CardUnitEventUser(
        unit: UnitEventUser
) : CardUnit(R.layout.card_event, unit) {

    private val xAccount: XAccount
    private val xAccountAdmin: XAccount

    init {
        val e = unit.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
        xAccountAdmin = XAccount(e.adminAccountId, e.adminAccountName, e.adminAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val unit = xUnit.unit as UnitEventUser

        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vDate.text = ToolsDate.dateToString(unit.dateCreate)
        vName.text = ""
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = unit.event!!
        var text = ""
        var imageRedId = 0

        xUnit.xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventUserAchievement -> {
                text = ToolsResources.sCap(R.string.unit_event_achievement, ToolsResources.sex(e.ownerAccountSex, R.string.he_gained, R.string.she_gained), CampfireConstants.getAchievement(e.achievementIndex).getText(false))
                imageRedId = CampfireConstants.getAchievement(e.achievementIndex).image
                vAvatarTitle.vImageView.tag = null
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfireConstants.getAchievement(e.achievementIndex).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(unit.creatorId, unit.creatorName, e.achievementIndex, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(unit.creatorId, unit.creatorName, e.achievementIndex, false, Navigator.TO) }
            }
            is ApiEventUserAdminBaned -> {
                text = ToolsResources.sCap(R.string.unit_event_blocked_app, ToolsResources.sex(e.adminAccountSex, R.string.he_baned, R.string.she_baned), ToolsDate.dateToStringFull(e.blockDate), ControllerApi.linkToUser(e.adminAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminUnitBlocked -> {
                val unitName = ControllerUnits.getName(e.unitType)
                text = ToolsResources.sCap(R.string.unit_event_blocked_unit, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_blocked, R.string.she_blocked), unitName)
                if (e.blockAccountDate > 0 && e.blockFandomId < 1) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (e.blockAccountDate > 0 && e.blockFandomId > 0) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName} (${ControllerApi.linkToFandom(e.blockFandomId, e.blockFandomLanguageId)})")
                if (e.warned) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_warn)
                if (e.lastUnitsBlocked) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_last_units)
                view.setOnClickListener {
                    if(e.moderationId > 0) ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0L, Navigator.TO)
                    else ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO)
                }
            }
            is ApiEventUserAdminModerationRejected -> {
                text = ToolsResources.sCap(R.string.unit_event_user_moder_action_rejected, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_reject, R.string.she_reject), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminNameChanged -> {
                text = ToolsResources.sCap(R.string.unit_event_change_name, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, e.ownerAccountName)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPunishmentRemove -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_punishment, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveDescription -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_description, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveImage -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_image, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveLink -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_link, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveName -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_name, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveStatus -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_status, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveTitleImage -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_titile_image, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminUnitRestored -> {
                text = ToolsResources.sCap(R.string.unit_event_unit_restore, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore))
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminWarned -> {
                text = ToolsResources.sCap(R.string.unit_event_warn_app, ControllerApi.linkToUser(e.adminAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomMakeModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_make_moderator, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomRemoveModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_moderator, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomSuggest -> {
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, ControllerApi.getLanguageId(), Navigator.TO) }
                text = ToolsResources.sCap(R.string.unit_event_fandom_suggested, ToolsResources.sex(e.ownerAccountSex, R.string.he_suggest, R.string.she_suggest), e.fandomName)

            }
            is ApiEventUserAdminPostChangeFandom -> {
                text = ToolsResources.sCap(R.string.unit_event_post_fandom_change, ControllerApi.linkToUser(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, R.string.he_move, R.string.she_move), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.unitId, 0, Navigator.TO) }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerApi.makeLinkable(vText)

        if (imageRedId != 0) {
            vAvatarTitle.vImageView.tag = null
            vAvatarTitle.vImageView.setImageResource(imageRedId)
        } else if (showFandom && unit.fandomId > 0) {
            xUnit.xFandom.setView(vAvatarTitle)
            vName.text = xUnit.xFandom.name
        } else if(e.adminAccountId > 0){
            xAccountAdmin.setView(vAvatarTitle)
            vName.text = xAccountAdmin.name
        } else{
            xAccount.setView(vAvatarTitle)
            vName.text = xAccount.name
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

    override fun notifyItem() {

    }

}
