package com.sayzen.campfiresdk.models.events.chat

import com.dzen.campfire.api.models.ChatTag

class EventChatSubscriptionChanged(
        var tag : ChatTag,
        var subscribed: Boolean
)
