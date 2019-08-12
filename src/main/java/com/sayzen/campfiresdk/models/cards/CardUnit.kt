package com.sayzen.campfiresdk.models.cards

import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.UnitReview
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.UnitForum
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.models.units.events.UnitEvent
import com.dzen.campfire.api.models.units.moderations.UnitModeration
import com.dzen.campfire.api.models.units.stickers.UnitStricker
import com.dzen.campfire.api.models.units.stickers.UnitStrickersPack
import com.sayzen.campfiresdk.models.cards.chat.CardChatMessage
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sayzen.campfiresdk.models.events.units.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.java.libs.eventBus.EventBus

abstract class CardUnit(open val unit: Unit) : Card(), NotifyItem {

    companion object {
        fun instance(
                unit: Unit,
                vRecycler: RecyclerView? = null,
                showFandom: Boolean = false,
                dividers: Boolean = false,
                isFeedInFandom: Boolean = false
        ): CardUnit {

            val cardUnit = when (unit) {
                is UnitComment -> CardComment.instance(unit, dividers, false)
                is UnitPost -> CardPost(vRecycler, unit)
                is UnitChatMessage -> CardChatMessage.instance(unit)
                is UnitModeration -> CardModeration(unit)
                is UnitEvent -> CardEvent(unit, isFeedInFandom)
                is UnitReview -> CardReview(unit)
                is UnitForum -> CardForum(unit)
                is UnitStricker -> CardSticker(unit)
                is UnitStrickersPack -> CardStickersPack(unit)
                else -> throw RuntimeException("Unknown unit type [" + unit.unitType + "]")
            }

            cardUnit.showFandom = showFandom

            return cardUnit

        }
    }

    private val eventBus = EventBus
            .subscribe(EventUnitRemove::class) { this.onUnitRemoved(it) }
            .subscribe(EventCommentAdd::class) { this.onCommentAdd(it) }
            .subscribe(EventCommentRemove::class) { this.onEventCommentRemove(it) }
            .subscribe(EventUnitFandomChanged::class) { this.onEventUnitFandomChanged(it) }
            .subscribe(EventUnitImportantChange::class) { this.onEventUnitImportantChange(it) }
            .subscribe(EventUnitReportsClear::class) { this.onEventUnitReportsClear(it) }
            .subscribe(EventUnitReportsAdd::class) { this.onEventUnitReportsAdd(it) }

    var showFandom: Boolean = false

    open fun onFandomChanged(){

    }

    //
    //  EventBus
    //

    private fun onUnitRemoved(e: EventUnitRemove) {
        if (e.unitId == unit.id && adapter != null) adapter!!.remove(this)
    }

    private fun onCommentAdd(e: EventCommentAdd) {
        if (e.parentUnitId == unit.id) {
            unit.subUnitsCount++
            update()
        }
    }

    private fun onEventCommentRemove(e: EventCommentRemove) {
        if (e.parentUnitId == unit.id){
            unit.subUnitsCount--
            update()
        }
    }

    private fun onEventUnitFandomChanged(e: EventUnitFandomChanged) {
        if (e.unitId == unit.id) {
            unit.fandomId = e.fandomId
            unit.languageId = e.languageId
            unit.fandomName = e.fandomName
            unit.fandomImageId = e.fandomImageId
            onFandomChanged()
            update()
        }
    }

    private fun onEventUnitImportantChange(e: EventUnitImportantChange) {
        if (e.unitId == unit.id) {
            unit.important = e.important
            update()
        }
    }

    private fun onEventUnitReportsClear(e: EventUnitReportsClear) {
        if (e.unitId == unit.id) {
            unit.reportsCount = 0
            update()
        }
    }

    private fun onEventUnitReportsAdd(e: EventUnitReportsAdd) {
        if (e.unitId == unit.id) {
            unit.reportsCount++
            update()
        }
    }
}

