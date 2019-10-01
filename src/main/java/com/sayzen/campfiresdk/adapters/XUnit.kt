package com.sayzen.campfiresdk.adapters

import com.dzen.campfire.api.models.units.Unit
import com.sayzen.campfiresdk.models.events.units.*
import com.sup.dev.java.libs.eventBus.EventBus

class XUnit(
        val unit: Unit,
        val onChangedAccount: () -> kotlin.Unit,
        val onChangedFandom: () -> kotlin.Unit,
        val onChangedKarma: () -> kotlin.Unit,
        val onChangedComments: () -> kotlin.Unit,
        val onChangedReports: () -> kotlin.Unit,
        val onChangedImportance: () -> kotlin.Unit,
        val onRemove: () -> kotlin.Unit
) {

    val eventBus = EventBus
            .subscribe(EventPostCloseChange::class) { if (it.unitId == unit.id) unit.closed = it.closed }
            .subscribe(EventPostNotifyFollowers::class) { if (it.unitId == unit.id) unit.tag_3 = 1 }
            .subscribe(EventUnitRemove::class) { if (it.unitId == unit.id) onRemove.invoke() }
            .subscribe(EventPostMultilingualChange::class) {
                if (it.unitId == unit.id) {
                    unit.languageId = it.languageId
                    unit.tag_5 = it.tag5
                    xFandom.languageId = it.languageId
                    onChangedFandom.invoke()
                }
            }
            .subscribe(EventUnitImportantChange::class) {
                if (it.unitId == unit.id) {
                    unit.important = it.important
                    onChangedImportance.invoke()
                }
            }

    val xAccount = XAccount(unit, unit.dateCreate) { onChangedAccount.invoke() }
    val xKarma = XKarma(unit) { onChangedKarma.invoke() }
    var xFandom = XFandom(unit, unit.dateCreate) { onChangedFandom.invoke() }
    val xComments = XComments(unit) { onChangedComments.invoke() }
    val xReports = XReports(unit) { onChangedReports.invoke() }


}
