package com.sayzen.campfiresdk.screens.post.create

import android.widget.Button
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceCheckNextUser
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceReject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsThreads

class WidgetTagsRelayRaceNextUser(
        val userActivityId:Long,
        val onEnter:(Long)->Unit
) : Widget(R.layout.screen_post_create_tags_widget_relay_race_next_user){

    val vUser: ViewAvatarTitle = findViewById(R.id.vUser)
    val vCancel: Button = findViewById(R.id.vCancel)
    val vEnter:Button = findViewById(R.id.vEnter)

    var nextAccountId = 0L

    init {

        vUser.setTitle(R.string.app_choose_user)
        vUser.vAvatar.vImageView.setImageResource(R.color.focus_dark)

        vUser.setOnClickListener {
            Navigator.to(SAccountSearch(true, true) {
                nextAccountId = it.id
                vUser.setTitle(it.name)
                ImageLoader.load(it.imageId).into(vUser.vAvatar.vImageView)
                ToolsThreads.main(true) { asSheetShow() }
            })
        }

        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { send() }
    }

    private fun send(){
        ApiRequestsSupporter.executeProgressDialog(RActivitiesRelayRaceCheckNextUser(userActivityId, nextAccountId)){ _->
            onEnter.invoke(nextAccountId)
            hide()
        }
                .onApiError(RActivitiesRelayRaceReject.E_HAS_POST) { ToolsToast.show(R.string.activities_relay_race_error_has_post) }
                .onApiError(RActivitiesRelayRaceReject.E_HAS_REJECT) { ToolsToast.show(R.string.activities_relay_race_error_has_rejected) }
    }

}