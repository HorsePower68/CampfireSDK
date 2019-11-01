package com.sayzen.campfiresdk.screens.achievements.lvl

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.achievements.CardInfo
import com.sayzen.campfiresdk.screens.achievements.achievements.CardAchievement
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.objects.AppLevel
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.java.libs.eventBus.EventBus

class PageLvl(var accountId:Long, accountLvl: Long, karma30:Long) : Card(0) {

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { e: EventNotification -> this.onNotification(e) }

    private val adapterSub: RecyclerCardAdapter = RecyclerCardAdapter()
    private val cardInfo: CardInfo = CardInfo(R.string.achi_privilege_hint, R.string.app_karma_count_30_days, karma30, false)

    init {
        adapterSub.add(cardInfo)

        for(l in CampfireConstants.LVLS)  addLvl(CardLvl(accountLvl, karma30, l))

        addLvl(CardLvl(accountLvl, karma30, AppLevel(API.LVL_PROTOADMIN, R.string.lvl_protoadmin, R.color.orange_700)))
    }

    private fun addLvl(card: CardLvl) {
        if (ControllerApi.isCurrentAccount(accountId) && (ControllerApi.account.lvl < card.appLvl.lvl.lvl) && !adapterSub.contains(CardDividerTitle::class))
            adapterSub.add(CardDividerTitle().setText(R.string.achi_you_are_here).toCenter())
        adapterSub.add(card)
    }

    override fun instanceView(): View {
        val v = RecyclerView(SupAndroid.activity!!)
        v.layoutManager = LinearLayoutManager(SupAndroid.activity)
        ToolsView.setRecyclerAnimation(v)
        return v
    }

    override fun bindView(view: View) {
        super.bindView(view)
        (view as RecyclerView).adapter = adapterSub
    }


    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationAchievement) {
            val l = adapterSub.get(CardAchievement::class)
            for (card in l)
                card.update()
        }
    }
}