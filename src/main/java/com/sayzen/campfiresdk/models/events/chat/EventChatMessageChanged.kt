package com.sayzen.campfiresdk.models.events.chat

class EventChatMessageChanged(
        val unitId: Long,
        val text: String,
        val quoteId:Long,
        val quoteText:String
)