package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.models.notifications.NotificationAchievement
import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.achievements.CardInfo
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class PageAchievements(
        val accountId: Long,
        scrollToId: Long,
        val r: RAchievementsInfo.Response
) : Card(0) {

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { e: EventNotification -> this.onNotification(e) }

    private val adapterSub: RecyclerCardAdapter = RecyclerCardAdapter()
    private val cardInfo: CardInfo = CardInfo(R.string.achi_karma_hint, R.string.app_level, r.karmaForce, true, CampfireConstants.getLvlImage(r.karmaForce))
    private var scrollToIndex: Int = 0

    private val indexes = ArrayList<Long>()
    private val progress = ArrayList<Long>()

    init {

        adapterSub.add(cardInfo)

        val spoiler1 = CardSpoilerAchi(this, 1).setTitle(R.string.achi_spoiler_instruction)
        val spoiler2 = CardSpoilerAchi(this, 2).setTitle(R.string.achi_spoiler_sharing)
        val spoiler3 = CardSpoilerAchi(this, 3).setTitle(R.string.achi_spoiler_publications)
        val spoiler6 = CardSpoilerAchi(this, 4).setTitle(R.string.app_moderation)
        val spoiler5 = CardSpoilerAchi(this, 5).setTitle(R.string.achi_spoiler_other)

        adapterSub.add(spoiler1)
        adapterSub.add(spoiler2)
        adapterSub.add(spoiler3)
        adapterSub.add(spoiler6)
        adapterSub.add(spoiler5)


        if (scrollToId != 0L) {
            for (card in adapterSub.get(CardSpoilerAchi::class))
                for (c in card.cards)
                    if ((c as CardAchievement).achievement.index == scrollToId) {
                        card.setExpanded(true)
                        c.flash()
                        scrollToIndex = adapterSub.indexOf(c) + 3
                        if (scrollToIndex > adapterSub.size() - 1) scrollToIndex = adapterSub.size() - 1
                        break
                    }
        }
    }

    fun loadPack(index:Int, cardSpoiler:CardSpoilerAchi){
        cardSpoiler.cardLoading.setState(CardLoading.State.LOADING)
        cardSpoiler.cardLoading.setOnRetry { loadPack(index, cardSpoiler) }
        RAchievementsPack(accountId, index)
                .onComplete { r->
                    for(i in r.indexes) indexes.add(i)
                    for(i in r.progress) progress.add(i)
                    cardSpoiler.onLoaded()
                }
                .onError { cardSpoiler.cardLoading.setState(CardLoading.State.RETRY) }
                .send(api)
    }

    fun achiProgress(index:Long): Long {
        for (i in 0 until indexes.size) if (indexes[i] == index) return progress[i]
        return 0
    }
    fun achiLvl(index:Long): Long {
        for (i in 0 until r.indexes.size) if (r.indexes[i] == index) return r.lvls[i]
        return 0
    }

    override fun instanceView(): View {
        val v = RecyclerView(SupAndroid.activity!!)
        v.layoutManager = LinearLayoutManager(SupAndroid.activity)
        ToolsView.setRecyclerAnimation(v)
        return v
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vRecycler = view as RecyclerView
        vRecycler.adapter = adapterSub
        if (scrollToIndex > 0) vRecycler.smoothScrollToPosition(scrollToIndex)
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationAchievement) {
            val n = e.notification
            val l = adapterSub.get(CardAchievement::class)
            for (card in l)
                if ((n as NotificationAchievement).achiIndex == card.achievement.index) {
                    cardInfo.count = cardInfo.count + card.achievement.force * (n.achiLvl - card.lvl)
                    card.lvl = n.achiLvl
                    card.update()
                }
        }
    }

}
