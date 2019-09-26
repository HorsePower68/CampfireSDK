package com.sayzen.campfiresdk.models.events.stickers

import com.dzen.campfire.api.models.units.stickers.UnitSticker

class EventStickerCollectionChanged(
        val sticker: UnitSticker,
        val inCollection:Boolean
)