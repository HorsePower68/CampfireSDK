package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsKarmaAdd
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaStateChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

object ControllerKarma {

    private val rates = HashMap<Long, Rate>()

    fun stop(publicationId: Long){
        val rate = rates[publicationId]
        if (rate != null) {
            rate.clearRate()
            rates.remove(publicationId)
            EventBus.post(EventPublicationKarmaStateChanged(publicationId))
        }
    }

    fun rate(publicationId: Long, up:Boolean, anon:Boolean) {
        stop(publicationId)
        rates[publicationId] = Rate(publicationId, up, anon)
        EventBus.post(EventPublicationKarmaStateChanged(publicationId))
    }

    fun getStartTime(publicationId: Long):Long{
        val rate = rates[publicationId]
        return if(rate == null) 0L else rate.rateStartTime
    }

    fun getIsUp(publicationId: Long):Boolean{
        val rate = rates[publicationId]
        return if(rate == null) false else rate.up
    }

    private class Rate(
            val publicationId: Long,
            val up: Boolean,
            val anon:Boolean,
            val rateStartTime: Long = System.currentTimeMillis()
    ) {

        private val subscription: Subscription

        init {
            subscription = ToolsThreads.main(CampfireConstants.RATE_TIME) {
                ApiRequestsSupporter.execute(RPublicationsKarmaAdd(publicationId, up, ControllerApi.getLanguageId(), anon)) { r ->
                    EventBus.post(EventPublicationKarmaAdd(publicationId, r.myKarmaCount))
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_KARMA)
                }
                    .onApiError(RPublicationsKarmaAdd.E_SELF_PUBLICATION) { ToolsToast.show(R.string.error_rate_self_publication) }
                    .onApiError(RPublicationsKarmaAdd.E_CANT_DOWN) { ToolsToast.show(R.string.error_rate_cant_down) }
                    .onFinish {
                        if(rates[publicationId] == this) rates.remove(publicationId)
                        clearRate()
                    }
            }
        }

        fun clearRate() {
            subscription.unsubscribe()
            EventBus.post(EventPublicationKarmaStateChanged(publicationId))
        }


    }


}