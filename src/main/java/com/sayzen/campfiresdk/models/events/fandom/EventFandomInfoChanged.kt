package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.FandomLink

class EventFandomInfoChanged(
        val fandomId:Long,
        val languageId:Long,
        val gallery:Array<Long> = emptyArray(),
        val links:Array<FandomLink> = emptyArray()
)