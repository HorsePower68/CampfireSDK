package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView

import com.dzen.campfire.api.models.Fandom
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemove
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardFandom constructor(
        val fandom: Fandom,
        var onClick: (() -> Unit)? = null)
    : Card(R.layout.card_fandom) {

    private val eventBus = EventBus
            .subscribe(EventFandomRemove::class) { if(adapter != null) adapter!!.remove(this) }

    private val xFandom = XFandom(fandom){update()}
    private val subscribesCount = fandom.subscribesCount
    var subscribed = false
    var showLanguage = true
    var showSubscribes = true
    var avatarClickable = true

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vSubscribers: TextView = view.findViewById(R.id.vSubscribers)

        if (showSubscribes) {
            vSubscribers.text = "" + subscribesCount
            vSubscribers.visibility = View.VISIBLE
        } else {
            vSubscribers.visibility = View.INVISIBLE
        }

        view.setOnClickListener { onClick() }
        xFandom.setView(vAvatar)

        vAvatar.vAvatar.isClickable = avatarClickable

        if (showLanguage && xFandom.languageId > 0) vAvatar.vAvatar.setChipText(ControllerApi.getLanguage(xFandom.languageId).code)
        else vAvatar.vAvatar.setChipText("")


    }

    private fun onClick() {
        if (onClick != null) {
            onClick?.invoke()
            return
        }
        ControllerCampfireSDK.onToFandomClicked(xFandom.fandomId, xFandom.languageId, Navigator.TO)
    }

    //
    //  Setters
    //

    fun setSubscribed(subscribed: Boolean): CardFandom {
        this.subscribed = subscribed
        return this
    }

    fun setShowLanguage(showLanguage: Boolean): CardFandom {
        this.showLanguage = showLanguage
        return this
    }
    fun setShowSubscribes (showSubscribes: Boolean): CardFandom {
        this.showSubscribes = showSubscribes
        return this
    }

    fun getFandomId() = xFandom.fandomId
}
