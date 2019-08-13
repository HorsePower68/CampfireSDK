package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardStickersPack(
        override val unit: UnitStickersPack
) : CardUnit(unit) {

    private val eventBus = EventBus
            .subscribe(EventStickersPackChanged::class) {
                if (it.stickersPack.id == unit.id) {
                    unit.imageId = it.stickersPack.imageId
                    unit.name = it.stickersPack.name
                    update()
                }
            }

    private val xKarma = XKarma(unit) { update() }

    override fun getLayout() = R.layout.card_stickers_pack

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitleContainer: View = view.findViewById(R.id.vTitleContainer)

        ToolsImagesLoader.load(unit.imageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.setTitle(unit.name)
        vAvatar.setSubtitle(unit.creatorName)

        vMenu.setOnClickListener { ControllerUnits.showStickerPackPopup(vMenu, unit) }

        view.setOnClickListener { Navigator.to(SStickersView(unit, 0)) }
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(unit.imageId).intoCash()
    }


}
