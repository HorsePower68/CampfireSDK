package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.activities.UserActivity
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardUserActivity(
        val userActivity: UserActivity
) : Card(R.layout.card_user_activity){

    private val xFandom = XFandom(userActivity.fandomId, userActivity.languageId, userActivity.name, userActivity.imageId){ update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vName:TextView = view.findViewById(R.id.vName)
        val vDescription:TextView = view.findViewById(R.id.vDescription)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        vName.setText(userActivity.name)
        vDescription.setText(userActivity.description)
        xFandom.setView(vAvatar)
    }

}