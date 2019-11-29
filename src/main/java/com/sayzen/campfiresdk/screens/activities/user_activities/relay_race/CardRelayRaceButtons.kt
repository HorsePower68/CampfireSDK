package com.sayzen.campfiresdk.screens.activities.user_activities.relay_race

import android.view.View
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.Settings

class CardRelayRaceButtons(
        val activityId:Long,
        val postsCount:Long,
        val waitMembersCount:Long,
        val rejectedMembersCount:Long
) : Card(R.layout.screen_relay_race_buttons){


    override fun bindView(view: View) {
        super.bindView(view)

        val vMembersCount : Settings = view.findViewById(R.id.vMembersCount)
        val vRejectedCount : Settings = view.findViewById(R.id.vRejectedCount)
        val vPostsCount : Settings = view.findViewById(R.id.vPostsCount)

        vMembersCount.setSubtitle("$waitMembersCount")
        vRejectedCount.setSubtitle("$rejectedMembersCount")
        vPostsCount.setSubtitle("$postsCount")

        vMembersCount.setOnClickListener { Navigator.to(SRelayRaceInfoMembers(activityId)) }
        vRejectedCount.setOnClickListener { Navigator.to(SRelayRaceInfoRejected(activityId))  }
    }



}