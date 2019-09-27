package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.events_admins.*
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
import com.sup.dev.java.tools.ToolsText

class CardUnitEventAdmin(
        unit: UnitEventAdmin
) : CardUnit(R.layout.card_event, unit) {

    private val xAccount: XAccount

    init {
        val e = unit.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val unit = xUnit.unit as UnitEventAdmin

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

        xUnit.xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {

            is ApiEventAdminBan -> {
                text = ToolsResources.sCap(R.string.unit_event_blocked_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(e.targetAccountName), ToolsDate.dateToStringFull(e.blockDate))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminBlockUnit -> {
                val unitName = ControllerUnits.getName(e.unitType)
                text = ToolsResources.sCap(R.string.unit_event_admin_blocked_unit, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), unitName, ControllerApi.linkToUser(e.targetAccountName))
                if (e.blockAccountDate > 0 && e.blockedInApp && e.blockFandomId < 1) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (e.blockAccountDate > 0 && !e.blockedInApp && e.blockFandomId > 0) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName} (${ControllerApi.linkToFandom(e.blockFandomId, e.blockFandomLanguageId)})")
                if (e.warned) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_warn)
                if (e.lastUnitsBlocked) text += "\n" + ToolsResources.s(R.string.unit_event_account_blocked_last_units)
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminChangeName -> {
                text = ToolsResources.sCap(R.string.unit_event_change_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeAvatar -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeCategory -> {
                text = ToolsResources.sCap(R.string.unit_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed),
                        e.fandomName,
                        CampfireConstants.getCategory(e.oldCategory).name,
                        CampfireConstants.getCategory(e.newCategory).name)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeParams -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_new) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_remove) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomClose -> {
                text = ToolsResources.sCap(
                        R.string.unit_event_fandom_close,
                        if (e.closed) ToolsResources.sex(e.ownerAccountSex, R.string.he_close, R.string.she_close) else ToolsResources.sex(e.ownerAccountSex, R.string.he_open, R.string.she_open),
                        e.fandomName + "(" + ControllerApi.linkToFandom(e.fandomId) + ")")

                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminFandomKarmaCofChanged -> {
                text = "" + ToolsResources.sCap(R.string.unit_event_fandom_karma_cof, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId)})", ToolsText.numToStringRound(e.oldCof / 100.0, 2), ToolsText.numToStringRound(e.newCof / 100.0, 2))
            }
            is ApiEventAdminFandomMakeModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRemove -> {
                text = "" + ToolsResources.sCap(R.string.unit_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), e.fandomName)
            }
            is ApiEventAdminFandomRemoveModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRename -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, R.string.he_rename, R.string.she_rename), e.oldName, "" + e.newName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventAdminModerationRejected -> {
                text = ToolsResources.sCap(R.string.unit_event_moderation_rejected_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_reject, R.string.she_reject), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminPostChangeFandom -> {
                text = ToolsResources.sCap(R.string.unit_event_post_fandom_change_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_move, R.string.she_move), ControllerApi.linkToUser(e.targetAccountName), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.unitId, 0, Navigator.TO) }
            }
            is ApiEventAdminPunishmentRemove -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_punishment_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUnitRestore -> {
                text = ToolsResources.sCap(R.string.unit_event_unit_restore_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveDescription -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_description_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveImage -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveLink -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_link_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveName -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveStatus -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_status_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveTitleImage -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_titile_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminWarn -> {
                text = ToolsResources.sCap(R.string.unit_event_warn_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomAccepted -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, R.string.he_accept, R.string.she_accept), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerApi.makeLinkable(vText)

        if (showFandom && unit.fandomId > 0) {
            xUnit.xFandom.setView(vAvatarTitle)
            vName.text = xUnit.xFandom.name
        } else {
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
