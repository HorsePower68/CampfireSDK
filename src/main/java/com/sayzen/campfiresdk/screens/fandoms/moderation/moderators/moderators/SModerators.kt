package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.moderators

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminRemoveModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsModeratorsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemoveModerator
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

class SModerators(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardAccount, Account>() {

    private val eventBus = EventBus.subscribe(EventFandomRemoveModerator::class) {
        if (it.fandomId == fandomId && it.languageId == languageId && adapter != null) {
            val accounts = adapter!!.get(CardAccount::class)
            for (c in accounts) if (c.xAccount.accountId == it.accountId) adapter!!.remove(c)
        }
    }

    init {
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_14)
        setTitle(R.string.moderation_screen_moderators)
        setTextEmpty(R.string.moderation_screen_moderators_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) {
            val card = CardAccount(it)
            if (ControllerApi.can(API.LVL_ADMIN_REMOVE_MODERATOR)) card.setOnLongClick { _, _, _, _ ->
                WidgetMenu()
                        .add(R.string.app_deprive_moderator) { _, _ -> removeModerator(it.id) }
                        .asSheetShow()
            }
            card
        }
                .setBottomLoader { onLoad, cards ->
                    RFandomsModeratorsGetAll(fandomId, languageId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun removeModerator(accountId: Long) {
        WidgetField()
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_deprive) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminRemoveModerator(fandomId, languageId, accountId, comment)) {
                        EventBus.post(EventFandomRemoveModerator(fandomId, languageId, accountId))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

}