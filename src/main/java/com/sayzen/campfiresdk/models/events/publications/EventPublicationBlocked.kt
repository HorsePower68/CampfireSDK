package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage

class EventPublicationBlocked(
        val unitId:Long,
        val firstBlockPublicationId:Long,
        val publicationChatMessage: PublicationChatMessage?
)