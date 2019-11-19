package com.sayzen.campfiresdk.screens.activities.user_activities

import com.dzen.campfire.api.models.activities.UserActivity
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction

class SRelayRaceInfo(
        val userActivity: UserActivity
) : Screen(R.layout.screen_activities_relay_race){

    companion object{

        fun instance(activityId:Long, action:NavigationAction){
           // ApiRequestsSupporter.executeInterstitial(action, RChatGet(tag, 0)) { r ->
           //     onChatLoaded(r, 0, onShow)
           // }
        }
    }

}