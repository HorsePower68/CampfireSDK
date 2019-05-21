package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.requests.units.RUnitsKarmaAdd
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaStateChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
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
                ApiRequestsSupporter.execute(RUnitsKarmaAdd(unitId, up)) { r ->
                    if(rates[unitId] == this) rates.remove(unitId)
                    EventBus.post(EventUnitKarmaAdd(unitId, r.myKarmaCount))
                }.onFinish { clearRate() }
            }
        }

        fun clearRate() {
            subscription.unsubscribe()
        }


    }


}