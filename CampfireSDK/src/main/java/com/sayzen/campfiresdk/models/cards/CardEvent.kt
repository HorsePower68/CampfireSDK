package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.events.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsDate

class CardEvent(override val unit: UnitEvent) : CardUnit(unit) {

    private val xFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xAccount = XAccount(unit, unit.dateCreate) { update() }
    private val xAccountOwner: XAccount
    private val xAccountTarget: XAccount

    init {
        val e = unit.event!!
        xAccountOwner = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
        xAccountTarget = XAccount(e.targetAccountId, e.targetAccountName, e.targetAccountImageId, 0, 0) { update() }
    }

    override fun getLayout() = R.layout.card_event

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vDate.text = ToolsDate.dateToStringCustom(unit.dateCreate)
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = unit.event!!
        var text = ""
        var imageRedId = 0

        xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventUnitBlocked -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_blocked, ToolsResources.sex(e.targetAccountSex, R.string.he_baned, R.string.she_baned), e.fandomName, ToolsDate.dateToStringFull(e.blockDate), ControllerApi.linkToUser(e.ownerAccountName))
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_blocked_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(e.targetAccountName), "" + e.fandomName + " (" + ControllerApi.linkToFandom(unit.fandomId, unit.languageId) + ")", ToolsDate.dateToStringFull(e.blockDate))
                }
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventBan -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_blocked_app, ToolsResources.sex(e.targetAccountSex, R.string.he_baned, R.string.she_baned), ToolsDate.dateToStringFull(e.blockDate), ControllerApi.linkToUser(e.ownerAccountName))
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_blocked_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(e.targetAccountName), ToolsDate.dateToStringFull(e.blockDate))
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventWarn -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_warn_app, ControllerApi.linkToUser(e.ownerAccountName))
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_warn_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUnitWarn -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_unit_warn_app, ToolsResources.sex(e.targetAccountSex, R.string.he_warned, R.string.she_warned), ControllerApi.linkToUser(e.ownerAccountName), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId, e.fandomLanguageId) + ")")
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_unit_warn_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(e.targetAccountName), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId, e.fandomLanguageId) + ")")
                    view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventAchievement -> {
                text = ToolsResources.sCap(R.string.unit_event_achievement, ToolsResources.sex(e.ownerAccountSex, R.string.he_gained, R.string.she_gained), CampfreConstants.getAchievement(e.achievementIndex).getText(false))
                imageRedId = CampfreConstants.getAchievement(e.achievementIndex).image
                vAvatarTitle.vImageView.tag = null
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfreConstants.getAchievement(e.achievementIndex).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(unit.creatorId, unit.creatorName, unit.creatorLvl, e.achievementIndex, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(unit.creatorId, unit.creatorName, unit.creatorLvl, e.achievementIndex, false, Navigator.TO) }
            }
            is ApiEventFandomSuggest -> {
                view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.fandomId, ControllerApi.getLanguageId(), Navigator.TO) }
                if (e.fandomCreatorId == e.administratorId) {
                    text = ToolsResources.sCap(R.string.unit_event_fandom_suggested_self, ToolsResources.sex(e.ownerAccountSex, R.string.he_suggest, R.string.she_suggest), ToolsResources.sex(e.targetAccountSex, R.string.he_accept, R.string.she_accept), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                } else if (!e.acceptEvent) {
                    text = ToolsResources.sCap(R.string.unit_event_fandom_suggested, ToolsResources.sex(e.targetAccountSex, R.string.he_suggest, R.string.she_suggest), e.fandomName)
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, R.string.he_accept, R.string.she_accept), e.fandomName)
                }
            }
            is ApiEventChangeName -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_change_name, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_change_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), e.oldName, ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUserRemoveName -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_name, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_name_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUserRemoveTitleImage -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_titile_image, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_titile_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUserRemoveImage -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_image, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_image_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventFandomRemove -> {
                text = "" + ToolsResources.sCap(R.string.unit_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), e.fandomName)
            }
            is ApiEventFandomChangeAvatar -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomRename -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, R.string.he_rename, R.string.she_rename), e.oldName, "" + e.newName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")

                view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomChangeParams -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_new) + " " + CampfreConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfreConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_remove) + " " + CampfreConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfreConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventUserRemoveStatus -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_status, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_status_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUserRemoveDescription -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_description, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_description_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventUserRemoveLink -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_link, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_link_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventFandomMakeModerator -> {
                log("targetAccountId=${e.targetAccountId} targetAccountName=${e.targetAccountName} ownerAccountId=${e.ownerAccountId} ownerAccountName=${e.ownerAccountName} creatorId=${unit.creatorId}")
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_make_moderator, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventFandomRemoveModerator -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_moderator, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.languageId)})")
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventPunishmentRemove -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_remove_punishment, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.ownerAccountId, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_remove_punishment_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
                }
            }
            is ApiEventModerationRejected -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_moderation_rejected, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_reject, R.string.she_reject))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_moderation_rejected_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_reject, R.string.she_reject), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
                }
            }
            is ApiEventUnitRestore -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_unit_restore, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_unit_restore_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_restore, R.string.she_restore), ControllerApi.linkToUser(e.targetAccountName))
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.moderationId, 0, Navigator.TO) }
                }
            }
            is ApiEventAdminPostChangeFandom -> {
                if (e.targetAccountId == unit.creatorId) {
                    text = ToolsResources.sCap(R.string.unit_event_post_fandom_change, ControllerApi.linkToUser(e.ownerAccountName), ToolsResources.sex(e.ownerAccountSex, R.string.he_move, R.string.she_move), e.oldFandomName, e.newFandomName)
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToPostClicked(e.unitId, 0, Navigator.TO) }
                } else {
                    text = ToolsResources.sCap(R.string.unit_event_post_fandom_change_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_move, R.string.she_move), ControllerApi.linkToUser(e.targetAccountName), e.oldFandomName, e.newFandomName)
                    view.setOnClickListener { v -> ControllerCampfireSDK.onToPostClicked(e.unitId, 0, Navigator.TO) }
                }
            }
            is ApiEventFandomChangeCategory -> {
                text = ToolsResources.sCap(R.string.unit_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed),
                        e.fandomName,
                        CampfreConstants.getCategory(e.oldCategory).name,
                        CampfreConstants.getCategory(e.newCategory).name)
                view.setOnClickListener { v -> ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerApi.makeLinkable(vText)

        if (imageRedId != 0) {
            vAvatarTitle.vImageView.tag = null
            vAvatarTitle.vImageView.setImageResource(imageRedId)
        } else if (showFandom && unit.fandomId > 0) {
            xFandom.setView(vAvatarTitle)
            vName.text = xFandom.name
        } else if (xAccountTarget.accountId == 0L) {
            xAccount.setView(vAvatarTitle)
            vName.text = xAccount.name
        } else if (e.targetAccountId == unit.creatorId) {
            xAccountOwner.setView(vAvatarTitle)
            vName.text = xAccountOwner.name
        } else {
            xAccountTarget.setView(vAvatarTitle)
            vName.text = xAccountTarget.name
        }
    }


    override fun notifyItem() {

    }

}
