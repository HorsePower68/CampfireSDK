package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.requests.units.RUnitsKarmaAdd
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaStateChanged
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
            EventBus.post(EventUnitKarmaStateChanged(unitId))
        }
    }

    fun rate(unitId: Long, up:Boolean) {
        stop(unitId)
        rates[unitId] = Rate(unitId, up)
        EventBus.post(EventUnitKarmaStateChanged(unitId))
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
            val rateStartTime: Long = System.currentTimeMillis()
    ) {

        private val subscription: Subscription

        init {
            subscription = ToolsThreads.main(CampfreConstants.RATE_TIME) {
                ApiRequestsSupporter.execute(RUnitsKarmaAdd(unitId, up, ControllerApi.getLanguageId())) { r ->
                    EventBus.post(EventUnitKarmaAdd(unitId, r.myKarmaCount))
                }
                    .onApiError(RUnitsKarmaAdd.E_SELF_UNIT) { ToolsToast.show(R.string.error_rate_self_unit) }
                    .onFinish {
                        if(rates[unitId] == this) rates.remove(unitId)
                        clearRate()
                    }
            }
        }

        fun clearRate() {
            subscription.unsubscribe()
            EventBus.post(EventUnitKarmaStateChanged(unitId))
        }


    }


}