package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView

class CardSticker(
        unit: UnitSticker,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = false
) : CardUnit(if (isShowFullInfo) R.layout.card_sticker_info else R.layout.card_sticker, unit) {

    var onClick: (UnitSticker) -> Unit = {}

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitSticker

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vProgress: View = view.findViewById(R.id.vProgress)
        val vRootContainer: View = view.findViewById(R.id.vRootContainer)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vMenu: View = view.findViewById(R.id.vMenu)

        vTitle.visibility = if (isShowFullInfo) View.VISIBLE else View.GONE
        vMenu.visibility = if (isShowFullInfo || isShowReports) View.VISIBLE else View.GONE
        vTitle.text = ToolsResources.sCap(R.string.sticker_event_create_sticker, ToolsResources.sex(unit.creatorSex, R.string.he_add, R.string.she_add))

        vMenu.setOnClickListener { ControllerStickers.showStickerPopup(vMenu, 0, 0, unit) }

        if (isShowFullInfo) {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, _, _ ->

            }
            view.setOnClickListener { SStickersView.instanceBySticker(unit.id, Navigator.TO) }
        } else {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, x, y ->
                ControllerStickers.showStickerPopup(vRootContainer, x, y, unit)
            }
            view.setOnClickListener { onClick.invoke(unit) }
            vRootContainer.setBackgroundColor(0x00000000)
        }

        ToolsImagesLoader.loadGif(unit.imageId, unit.gifId, 0, 0, vImage, vProgress)
    }

    override fun updateAccount() {
        update()
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        update()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        if (getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    override fun notifyItem() {
        val unit = xUnit.unit as UnitSticker
        ToolsImagesLoader.load(unit.imageId).intoCash()
    }

}
