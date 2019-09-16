package com.sayzen.campfiresdk.models.events.units

import com.dzen.campfire.api.models.units.chat.UnitChatMessage

class EventUnitBlocked(
        val unitId:Long,
        val firstBlockUnitId:Long,
        val unitChatMessage: UnitChatMessage?
)