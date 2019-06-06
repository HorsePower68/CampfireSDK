package com.sayzen.campfiresdk.models.events.fandom

class EventFandomReviewChanged(
        val fandomId:Long,
        val languageId:Long,
        val rateBefore:Long,
        val rateNew:Long,
        val text:String
)