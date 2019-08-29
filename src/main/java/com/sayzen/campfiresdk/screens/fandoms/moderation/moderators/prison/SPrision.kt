package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.prison

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPrison
import com.dzen.campfire.api.requests.fandoms.RFandomsPrisonGetAll
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationForgive
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccountBaned
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class SPrision(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardAccount, AccountPrison>() {

    init {
        setBackgroundImage(R.drawable.bg_8)
        setTitle(R.string.moderation_screen_prison)
        setTextEmpty(R.string.moderation_screen_prison_empty)
    }

    private val eventBus = EventBus.subscribe(EventFandomAccountBaned::class) {
        if (fandomId == it.fandomId && languageId == it.languageId && adapter != null) {
            for (i in adapter!!.get(CardAccount::class))
                if (i.xAccount.accountId == it.accountId){
                    if(it.date > 0) {
                        i.setSubtitle(ToolsResources.s(R.string.moderation_screen_prison_text, ToolsResources.sex(i.xAccount.sex, R.string.he_baned, R.string.she_baned), ToolsDate.dateToString(it.date)) + "\n" + ToolsResources.s(R.string.app_comment) + ": " + (i.tag as AccountPrison).comment)
                    }else{
                        adapter!!.remove(i)
                    }
                }
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, AccountPrison> {
        return RecyclerCardAdapterLoading<CardAccount, AccountPrison>(CardAccount::class) {
            val card = CardAccount(it.account)
            card.tag = it
            card.setSubtitle(ToolsResources.s(R.string.moderation_screen_prison_text, ToolsResources.sex(it.account.sex, R.string.he_baned, R.string.she_baned), ToolsDate.dateToString(it.banDate)) + "\n" + ToolsResources.s(R.string.app_comment) + ": " + it.comment)
            if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_BLOCK))
                card.setOnLongClick { card, view, x, y ->
                    WidgetMenu()
                            .add(R.string.app_forgive) { w, i -> forgive(it.account.id, card) }
                            .asSheetShow()
                }
            card
        }
                .setBottomLoader { onLoad, cards ->
                    RFandomsPrisonGetAll(fandomId, languageId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun forgive(accountId: Long, card: Card) {
        WidgetField()
                .setTitle(R.string.app_forgive_confirm)
                .setHint(R.string.moderation_widget_comment)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_forgive) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationForgive(fandomId, languageId, accountId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventFandomAccountBaned(accountId, fandomId, languageId, 0))
                    }
                }
                .asSheetShow()
    }


}