package com.sayzen.campfiresdk.screens.punishments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsAdminPunishmentsRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountPunishmentRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardPunishment(
        val punishment: AccountPunishment
) : CardAvatar(), NotifyItem {

    private val eventBus = EventBus
            .subscribe(EventAccountPunishmentRemove::class) { onEventAccountPunishmentRemove(it) }

    init {
        var text: String

        if (punishment.fandomId == 0L) {
            if (punishment.banDate > 0) text = ToolsResources.sCap(R.string.profile_punishment_card_ban_admin, ControllerApi.linkToUser(punishment.fromAccountName), ToolsResources.sex(punishment.fromAccountSex, R.string.he_blocked, R.string.she_blocked), ToolsDate.dateToStringFull(punishment.banDate))
            else text = ToolsResources.sCap(R.string.profile_punishment_card_warn_admin,
                    ControllerApi.linkToUser(punishment.fromAccountName),
                    ToolsResources.sex(punishment.fromAccountSex, R.string.he_warn, R.string.she_warn)
            )
            setOnClick { ControllerCampfireSDK.onToAccountClicked(punishment.fromAccountId, Navigator.TO) }
        } else {
            if (punishment.banDate > 0) text = ToolsResources.sCap(R.string.profile_punishment_card_ban,
                    ControllerApi.linkToUser(punishment.fromAccountName),
                    ToolsResources.sex(punishment.fromAccountSex, R.string.he_blocked, R.string.she_blocked),
                    "" + punishment.fandomName + " (" + ControllerApi.linkToFandom(punishment.fandomId, punishment.languageId) + ")",
                    ToolsDate.dateToStringFull(punishment.banDate))
            else text = ToolsResources.sCap(R.string.profile_punishment_card_warn,
                    ControllerApi.linkToUser(punishment.fromAccountName),
                    ToolsResources.sex(punishment.fromAccountSex, R.string.he_warn, R.string.she_warn),
                    "" + punishment.fandomName + " (" + ControllerApi.linkToFandom(punishment.fandomId, punishment.languageId) + ")")
            setOnClick { ControllerCampfireSDK.onToFandomClicked(punishment.fandomId, punishment.languageId, Navigator.TO) }
        }

        if (punishment.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + punishment.comment


        if ((ControllerApi.isCurrentAccount(punishment.fromAccountId) || ControllerApi.can(API.LVL_ADMIN_USER_PUNISHMENTS_REMOVE))
                && !ControllerApi.isCurrentAccount(punishment.ownerId))
            setOnLongClick { _, _, _, _ ->
                WidgetMenu()
                        .add(R.string.app_remove) { _, _ -> removePunishment() }.backgroundRes(R.color.red_700).textColorRes(R.color.white)
                        .asSheetShow()
            }

        setTitle(text)
        setSubtitle(ToolsDate.dateToString(punishment.dateCreate))
        setDividerVisible(true)

        if (punishment.fandomId > 0) setOnCLickAvatar { ControllerCampfireSDK.onToFandomClicked(punishment.fandomId, punishment.languageId, Navigator.TO) }
        else setOnCLickAvatar { ControllerCampfireSDK.onToAccountClicked(punishment.fromAccountId, Navigator.TO) }
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        ControllerApi.makeLinkable(vAvatar.vTitle)

        if (punishment.fandomImageId > 0) ToolsImagesLoader.load(punishment.fandomImageId).into(vAvatar.vAvatar.vImageView)
        else ToolsImagesLoader.load(punishment.fromAccountImageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun notifyItem() {
        if (punishment.fandomImageId > 0) ToolsImagesLoader.load(punishment.fandomImageId).intoCash()
        else ToolsImagesLoader.load(punishment.fromAccountImageId).intoCash()
    }

    //
    //  EventBus
    //

    private fun onEventAccountPunishmentRemove(e: EventAccountPunishmentRemove) {
        if (e.punishmentId == punishment.id && adapter != null) {
            adapter!!.remove(this)
        }
    }

    //
    //  Api
    //

    private fun removePunishment() {
        WidgetField()
                .setTitle(R.string.profile_remove_punishment)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsAdminPunishmentsRemove(punishment.id, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountPunishmentRemove(punishment.id))
                        if (it.fandomId == 0L || it.languageId == 0L)
                            EventBus.post(EventAccountBaned(punishment.ownerId, it.newBlockDate))
                        else
                            EventBus.post(EventFandomAccountBaned(punishment.ownerId, it.fandomId, it.languageId, it.newBlockDate))
                    }
                }
                .asSheetShow()
    }

}
