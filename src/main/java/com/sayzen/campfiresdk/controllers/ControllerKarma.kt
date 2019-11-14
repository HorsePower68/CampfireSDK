package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.units.RUnitsKarmaAdd
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

    fun stop(unitId: Long){
        val rate = rates[unitId]
        if (rate != null) {
            rate.clearRate()
            rates.remove(unitId)
            EventBus.post(EventPublicationKarmaStateChanged(unitId))
        }
    }

    fun rate(unitId: Long, up:Boolean, anon:Boolean) {
        stop(unitId)
        rates[unitId] = Rate(unitId, up, anon)
        EventBus.post(EventPublicationKarmaStateChanged(unitId))
    }

    fun getStartTime(unitId: Long):Long{
        val rate = rates[unitId]
        return if(rate == null) 0L else rate.rateStartTime
    }

    fun getIsUp(unitId: Long):Boolean{
        val rate = rates[unitId]
        return if(rate == null) false else rate.up
    }

    private class Rate(
            val unitId: Long,
            val up: Boolean,
            val anon:Boolean,
            val rateStartTime: Long = System.currentTimeMillis()
    ) {

        private val subscription: Subscription

        init {
            subscription = ToolsThreads.main(CampfireConstants.RATE_TIME) {
                ApiRequestsSupporter.execute(RUnitsKarmaAdd(unitId, up, ControllerApi.getLanguageId(), anon)) { r ->
                    EventBus.post(EventPublicationKarmaAdd(unitId, r.myKarmaCount))
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_KARMA)
                }
                    .onApiError(RUnitsKarmaAdd.E_SELF_PUBLICATION) { ToolsToast.show(R.string.error_rate_self_unit) }
                    .onApiError(RUnitsKarmaAdd.E_CANT_DOWN) { ToolsToast.show(R.string.error_rate_cant_down) }
                    .onFinish {
                        if(rates[unitId] == this) rates.remove(unitId)
                        clearRate()
                    }
            }
        }

        fun clearRate() {
            subscription.unsubscribe()
            EventBus.post(EventPublicationKarmaStateChanged(unitId))
        }


    }


}