package com.sayzen.campfiresdk.adapters

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.EventUnitInstance
import com.dzen.campfire.api.models.units.Unit
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.units.*
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.eventBus.EventBus

class XReports(
        private val unit: Unit,
        var onChanged: () -> kotlin.Unit
) {

    companion object {
        private val eventBus = EventBus
                .subscribe(EventUnitInstance::class) { set(it.unit.id, it.unit.reportsCount, System.currentTimeMillis()) }
                .subscribe(EventUnitReportsAdd::class) { set(it.unitId, get(it.unitId) + 1, System.currentTimeMillis()) }
                .subscribe(EventUnitReportsClear::class) {  set(it.unitId, 0, System.currentTimeMillis()) }
        private val reports = HashMap<Long, Item2<Long, Long>>()

        fun set(unitId: Long, reportsCount: Long, unitInstanceDate: Long) {
            if (!reports.containsKey(unitId)) {
                reports[unitId] = Item2(reportsCount, unitInstanceDate)
                EventBus.post(EventReportsCountChanged(unitId, reportsCount, reportsCount))
            } else {
                if (reports[unitId]!!.a2 < unitInstanceDate && reports[unitId]!!.a1 != reportsCount) {
                    val old = reports[unitId]!!.a1
                    reports[unitId] = Item2(reportsCount, unitInstanceDate)
                    EventBus.post(EventReportsCountChanged(unitId, reportsCount, reportsCount - old))
                }
            }
        }

        fun get(unitId: Long) = if (reports.containsKey(unitId)) reports[unitId]!!.a1 else 0L

    }

    private val eventBus = EventBus.subscribe(EventReportsCountChanged::class) {
        if (it.unitId == unit.id) {
            unit.reportsCount = get(unit.id)
            onChanged.invoke()
        }
    }

    fun setView(view: TextView) {
        view.text = "${unit.reportsCount}"
        view.visibility = if (unit.reportsCount > 0 && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE
    }


}
