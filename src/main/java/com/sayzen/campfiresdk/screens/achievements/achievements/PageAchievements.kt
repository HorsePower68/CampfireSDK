package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.notifications.NotificationAchievement
import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.achievements.CardInfo
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
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
    private val cardInfo: CardInfo = CardInfo(R.string.achi_karma_hint, R.string.app_level, r.karmaForce, true, CampfreConstants.getLvlImage(r.karmaForce))
    private var scrollToIndex: Int = 0

    init {

        adapterSub.add(cardInfo)

        val spoiler1 = CardSpoilerAchi()
                .setTitle(R.string.achi_spoiler_instruction)
                .addAchi(CardAchievement(this, API.ACHI_RULES_USER))
                .addAchi(CardAchievement(this, API.ACHI_LOGIN))
                .addAchi(CardAchievement(this, API.ACHI_CHAT))
                .addAchi(CardAchievement(this, API.ACHI_CHAT_SUBSCRIBE))
                .addAchi(CardAchievement(this, API.ACHI_COMMENT))
                .addAchi(CardAchievement(this, API.ACHI_ANSWER))
                .addAchi(CardAchievement(this, API.ACHI_RATE))
                .addAchi(CardAchievement(this, API.ACHI_CHANGE_PUBLICATION))
                .addAchi(CardAchievement(this, API.ACHI_CHANGE_COMMENT))
                .addAchi(CardAchievement(this, API.ACHI_REVIEW))
                .addAchi(CardAchievement(this, API.ACHI_FIRST_POST))
                .addAchi(CardAchievement(this, API.ACHI_SUBSCRIBE))
                .addAchi(CardAchievement(this, API.ACHI_TAGS_SEARCH))
                .addAchi(CardAchievement(this, API.ACHI_LANGUAGE))
                .addAchi(CardAchievement(this, API.ACHI_TITLE_IMAGE))

        val spoiler2 = CardSpoilerAchi()
                .setTitle(R.string.achi_spoiler_sharing)
                .addAchi(CardAchievement(this, API.ACHI_APP_SHARE))
                .addAchi(CardAchievement(this, API.ACHI_CONTENT_SHARE))
                .addAchi(CardAchievement(this, API.ACHI_ADD_RECRUITER))
                .addAchi(CardAchievement(this, API.ACHI_REFERRALS_COUNT))
                .addAchi(CardAchievement(this, API.ACHI_FOLLOWERS))

        val spoiler3 = CardSpoilerAchi()
                .setTitle(R.string.achi_spoiler_publications)
                .addAchi(CardAchievement(this, API.ACHI_POSTS_COUNT))
                .addAchi(CardAchievement(this, API.ACHI_COMMENTS_COUNT))
                .addAchi(CardAchievement(this, API.ACHI_POST_KARMA).setValueMultiplier(0.01))
                .addAchi(CardAchievement(this, API.ACHI_COMMENTS_KARMA).setValueMultiplier(0.01))
                .addAchi(CardAchievement(this, API.ACHI_KARMA_COUNT).setValueMultiplier(0.01))
                .addAchi(CardAchievement(this, API.ACHI_KARMA_30).setValueMultiplier(0.01))
                .addAchi(CardAchievement(this, API.ACHI_UP_RATES))
                .addAchi(CardAchievement(this, API.ACHI_UP_RATES_OVER_DOWN))

        val spoiler6 = CardSpoilerAchi()
                .setTitle(R.string.app_moderation)
                .addAchi(CardAchievement(this, API.ACHI_CREATE_TAG))
                .addAchi(CardAchievement(this, API.ACHI_RULES_MODERATOR))
                .addAchi(CardAchievement(this, API.ACHI_MODER_CHANGE_POST_TAGS))
                .addAchi(CardAchievement(this, API.ACHI_MAKE_MODER))
                .addAchi(CardAchievement(this, API.ACHI_CREATE_FORUM))
                .addAchi(CardAchievement(this, API.ACHI_REVIEW_MODER_ACTION))
                .addAchi(CardAchievement(this, API.ACHI_ACCEPT_FANDOM))
                .addAchi(CardAchievement(this, API.ACHI_MODERATOR_COUNT))
                .addAchi(CardAchievement(this, API.ACHI_MODERATOR_ACTION_KARMA).setValueMultiplier(0.01))

        val spoiler5 = CardSpoilerAchi()
                .setTitle(R.string.achi_spoiler_other)
                .addAchi(CardAchievement(this, API.ACHI_RATES_COUNT))
                .addAchi(CardAchievement(this, API.ACHI_ENTERS))
                .addAchi(CardAchievement(this, API.ACHI_QUESTS))
                .addAchi(CardAchievement(this, API.ACHI_FANDOMS))
                .addAchi(CardAchievement(this, API.ACHI_FIREWORKS))

        adapterSub.add(spoiler1)
        adapterSub.add(spoiler2)
        adapterSub.add(spoiler3)
        adapterSub.add(spoiler6)
        adapterSub.add(spoiler5)

        spoiler1.updateAchi()
        spoiler2.updateAchi()
        spoiler3.updateAchi()
        spoiler6.updateAchi()
        spoiler5.updateAchi()

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

    fun achiProgress(achi: AchievementInfo): Long {
        for (i in 0 until r.indexes.size) if (r.indexes[i] == achi.index) return r.progress[i]
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
