package com.sayzen.campfiresdk.models.events.chat

class EventChatMemberStatusChanged(
        val chatId: Long,
        val accountId: Long,
        val status: Long
)