package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesGetAllForAccount
import com.dzen.campfire.api.requests.activities.RActivitiesGetAllNotForAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SUserActivitiesList constructor(
        private val onSelected: ((UserActivity) -> Unit)? = null
) : SLoadingRecycler<CardUserActivity, UserActivity>(R.layout.screen_activities_user_activities) {

    private val eventBus = EventBus.subscribe(EventActivitiesCreate::class){reload()}

    private var subscribedLoaded = false
    private var lockOnEmpty = false
    private var myCount = 0L

    init {
        setTitle(R.string.app_relay_races)
        setTextEmpty(R.string.activities_empty)
        setTextProgress(R.string.activities_loading)
        setBackgroundImage(R.drawable.bg_7)

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        (vFab as View).visibility = if (ControllerApi.account.lvl >= API.LVL_MODERATOR_RELAY_RACE.lvl) View.VISIBLE else View.GONE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            Navigator.to(SRelayRaceCreate(null))
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUserActivity, UserActivity> {
        val adapterX = RecyclerCardAdapterLoading<CardUserActivity, UserActivity>(CardUserActivity::class) { CardUserActivity(it) }
                .setBottomLoader { onLoad, cards -> load(onLoad, cards) }

        return adapterX
    }

    override fun reload() {
        adapter!!.remove(CardDividerTitle::class)
        lockOnEmpty = true
        subscribedLoaded = false
        myCount = 0
        super.reload()
    }

    private fun load(onLoad: (Array<UserActivity>?) -> Unit, cards: ArrayList<CardUserActivity>) {
        lockOnEmpty = false
        if (!subscribedLoaded) {

            subscription = RActivitiesGetAllForAccount(ControllerApi.account.id, cards.size.toLong())
                    .onComplete {
                        onLoad.invoke(it.userActivities)
                        myCount += it.userActivities.size
                        if(it.userActivities.isEmpty()){
                            subscribedLoaded = true
                            if (adapter!!.size() > 0) adapter!!.add(CardDividerTitle(R.string.activities_all).setDividerBottom(false))
                            adapter!!.loadBottom()
                        }
                    }
                    .onError { onLoad.invoke(null) }
                    .send(api)

        } else {
            subscription = RActivitiesGetAllNotForAccount(ControllerApi.account.id, cards.size - myCount)
                    .onComplete { onLoad.invoke(it.userActivities) }
                    .onError { onLoad.invoke(null) }
                    .send(api)


        }
    }


}
