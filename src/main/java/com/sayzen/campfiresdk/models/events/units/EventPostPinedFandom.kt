package com.sayzen.campfiresdk.models.events.units

import com.dzen.campfire.api.models.units.post.UnitPost

class EventPostPinedFandom(
        val fandomId:Long,
        val languageId:Long,
        val post:UnitPost?
)
