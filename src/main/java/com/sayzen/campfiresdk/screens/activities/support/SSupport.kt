package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.requests.project.RProjectSupportGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerAppodeal
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.activities.EventVideoAdView
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class SSupport private constructor(
        var r: RProjectSupportGetInfo.Response
) : Screen(R.layout.screen_support) {

    companion object {

        fun instance(action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RProjectSupportGetInfo()) { r ->
                SSupport(r)
            }
        }

    }

    private val eventBus = EventBus.subscribe(EventVideoAdView::class){ reload() }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vShadow: View = findViewById(R.id.vShadow)
    private val vButton: Button = findViewById(R.id.vButton)
    private val adapter = RecyclerCardAdapter()

    init {
        isNavigationShadowAvailable = false

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        reset()
        ControllerAppodeal.cashVideo()

        vButton.setOnClickListener { ControllerActivities.showVideoAd(false) }

        SActivityTypeBottomNavigation.setShadow(vShadow)
    }

    private fun reset(){
        adapter.clear()
        adapter.add(CardSupportTotal(r.totalCount, 1000))
        for(i in r.accounts.indices) adapter.add(CardSupportUser(r.accounts[i], r.values[i]))
    }

    private fun reload(){
        RProjectSupportGetInfo()
                .onComplete{
                    r = it
                    reset()
                }
                .send(api)
    }

}