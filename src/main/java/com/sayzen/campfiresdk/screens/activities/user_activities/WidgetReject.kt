package com.sayzen.campfiresdk.screens.activities.user_activities

import android.widget.Button
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceReject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceRejected
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class WidgetReject(
        val userActivityId: Long
) : Widget(R.layout.screen_activities_reject) {

    val vUser: ViewAvatarTitle = findViewById(R.id.vUser)
    val vCancel: Button = findViewById(R.id.vCancel)
    val vEnter: Button = findViewById(R.id.vEnter)

    var nextAccountId = 0L

    init {

        vUser.setTitle(R.string.app_choose_user)
        vUser.vAvatar.vImageView.setImageResource(R.color.focus_dark)

        vUser.setOnClickListener {
            Navigator.to(SAccountSearch(true, true) {
                nextAccountId = it.id
                vUser.setTitle(it.name)
                ToolsImagesLoader.load(it.imageId).into(vUser.vAvatar.vImageView)
                ToolsThreads.main(true) { asSheetShow() }
            })
        }

        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { send() }
    }

    private fun send() {
        ApiRequestsSupporter.executeProgressDialog(RActivitiesRelayRaceReject(userActivityId, nextAccountId)) { r ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventActivitiesRelayRaceRejected(userActivityId,
                    r.currentOwnerId,
                    r.currentOwnerTime,
                    r.currentOwnerName,
                    r.currentOwnerImageId
                    ))
            hide()
        }
                .onApiError(RActivitiesRelayRaceReject.E_HAS_POST) { ToolsToast.show(R.string.activities_relay_race_error_has_post) }
                .onApiError(RActivitiesRelayRaceReject.E_HAS_REJECT) { ToolsToast.show(R.string.activities_relay_race_error_has_rejected) }
    }

}