package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.fandom.EventFandomCategoryChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
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
        val vSubscriptionSettings: View = view.findViewById(R.id.vSubscriptionSettings)
        val vLanguage: Button = view.findViewById(R.id.vLanguage)
        val vIcon: ImageView = view.findViewById(R.id.vIcon)

        vSubscription.visibility = if(SFandom.SUBSCRIPE_ENABLED) View.VISIBLE else View.GONE
        vSubscriptionSettings.visibility = if(SFandom.SUBSCRIPE_ENABLED) View.VISIBLE else View.GONE

        vIcon.setImageResource(CampfireConstants.getCategoryIcon(category))

        vName.text = xFandom.name
        vLanguage.text = ControllerApi.getLanguage(xFandom.languageId).name

        vSubscriptionSettings.setOnClickListener { WidgetSubscription(xFandom.fandomId, xFandom.languageId, subscriptionType, notifyImportant).asSheetShow() }
        if (subscriptionType == API.UNIT_IMPORTANT_NONE) vSubscription.setText(R.string.app_follow) else vSubscription.setText(R.string.app_unfollow)
        vSubscription.setOnClickListener {
            val type = if (subscriptionType == API.UNIT_IMPORTANT_NONE) API.UNIT_IMPORTANT_DEFAULT else API.UNIT_IMPORTANT_NONE
            ApiRequestsSupporter.executeProgressDialog(RFandomsSubscribeChange(xFandom.fandomId, xFandom.languageId, type, true)) { _ ->
                EventBus.post(EventFandomSubscribe(xFandom.fandomId, xFandom.languageId, type, true))
                if(type != API.UNIT_IMPORTANT_NONE)ControllerApi.setHasFandomSubscribes(true)
                ToolsToast.show(R.string.app_done)
            }
        }
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