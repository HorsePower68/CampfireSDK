package com.sayzen.campfiresdk.models.events.stickers

import com.dzen.campfire.api.models.units.stickers.UnitStickersPack

class EventStickersPackCollectionChanged(
        val stickersPack: UnitStickersPack,
        val inCollection:Boolean
)