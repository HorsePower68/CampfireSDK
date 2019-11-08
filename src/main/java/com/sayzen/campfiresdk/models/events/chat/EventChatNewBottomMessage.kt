package com.sayzen.campfiresdk.models.events.chat

import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage

class EventChatNewBottomMessage(var tag: ChatTag, var unitChatMessage: PublicationChatMessage)
