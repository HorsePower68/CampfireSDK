package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.fandom.EventFandomCategoryChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardTitle(
        val xFandom: XFandom,
        var category: Long,
        var subscriptionType: Long,
        var notifyImportant: Boolean
) : Card(R.layout.screen_fandom_card_title) {

    private val eventBus = EventBus
            .subscribe(EventFandomSubscribe::class) { onEventFandomSubscribe(it) }
            .subscribe(EventFandomCategoryChanged::class) { onEventFandomCategoryChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vName: TextView = view.findViewById(R.id.vName)
        val vSubscription: Button = view.findViewById(R.id.vSubscription)
        val vLanguage: Button = view.findViewById(R.id.vLanguage)
        val vIcon: ImageView = view.findViewById(R.id.vIcon)

        vIcon.setImageResource(CampfreConstants.getCategoryIcon(category))

        vName.text = xFandom.name
        vLanguage.text = ControllerApi.getLanguage(xFandom.languageId).name

        vSubscription.setOnClickListener { WidgetSubscription(xFandom.fandomId, xFandom.languageId, subscriptionType, notifyImportant).asSheetShow() }
        vLanguage.setOnClickListener { showLanguages() }
    }

    private fun showLanguages() {
        ControllerCampfireSDK.createLanguageMenu(xFandom.languageId) { languageId ->
            SFandom.instance(xFandom.fandomId, languageId, Navigator.REPLACE)
        }.asSheetShow()
    }

    //
    //  EventBus
    //

    private fun onEventFandomSubscribe(e: EventFandomSubscribe) {
        if (xFandom.fandomId == e.fandomId) {
            subscriptionType = e.subscriptionType
            notifyImportant = e.notifyImportant
            update()
        }
    }

    private fun onEventFandomCategoryChanged(e: EventFandomCategoryChanged) {
        if (xFandom.fandomId == e.fandomId) {
            category = e.newCategory
            update()
        }
    }

}