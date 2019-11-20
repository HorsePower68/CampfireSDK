package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.activities.UserActivity
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRemove
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemove
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardUserActivity(
        val userActivity: UserActivity
) : Card(R.layout.card_user_activity) {

    private val eventBus = EventBus
            .subscribe(EventActivitiesRemove::class) { if (it.userActivityId == userActivity.id) adapter?.remove(this) }
            .subscribe(EventActivitiesChanged::class) {
                if (it.userActivity.id == userActivity.id) {
                    userActivity.name = it.userActivity.name
                    userActivity.description = it.userActivity.description
                }
            }

    private val xFandom = XFandom(userActivity.fandomId, userActivity.languageId, userActivity.fandomName, userActivity.fandomImageId) { update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vDescription: TextView = view.findViewById(R.id.vDescription)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)

        vDescription.setText(userActivity.description)
        xFandom.setView(vAvatar)
        vAvatar.setSubtitle(userActivity.fandomName + " " + ToolsDate.dateToString(userActivity.dateCreate))
        vAvatar.setTitle(userActivity.name)
        vMenu.setOnClickListener { ControllerActivities.showMenu(userActivity) }
    }

}