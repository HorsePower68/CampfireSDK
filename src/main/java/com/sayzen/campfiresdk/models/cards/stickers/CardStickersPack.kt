package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardStickersPack(
        unit: UnitStickersPack,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = true
) : CardUnit(R.layout.card_stickers_pack, unit) {

    private val eventBus = EventBus
            .subscribe(EventStickersPackChanged::class) {
                if (it.stickersPack.id == unit.id) {
                    unit.imageId = it.stickersPack.imageId
                    unit.name = it.stickersPack.name
                    update()
                }
            }

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitStickersPack

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vComments: TextView = view.findViewById(R.id.vComments)

        vComments.text = unit.subUnitsCount.toString() + ""

        vTitle.visibility = if(isShowFullInfo) View.VISIBLE else View.GONE
        vTitle.text = ToolsResources.sCap(R.string.sticker_event_create_stickers_pack, ToolsResources.sex(unit.creatorSex, R.string.he_created, R.string.she_created))

        ToolsImagesLoader.load(unit.imageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.setTitle(unit.name)
        vAvatar.setSubtitle(unit.creatorName)

        vComments.setOnClickListener {
            SComments.instance(unit.id, 0, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            WidgetComment(unit.id, null) { }.asSheetShow()
            true
        }

        vMenu.setOnClickListener { ControllerUnits.showStickerPackPopup(unit) }

        view.setOnClickListener { Navigator.to(SStickersView(unit, 0)) }
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

    override fun updateReports() {
        if(getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    override fun updateKarma() {
        if (getView() == null) return
        val viewKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xUnit.xKarma.setView(viewKarma)
    }

    override fun notifyItem() {
        val unit = xUnit.unit as UnitStickersPack
        ToolsImagesLoader.load(unit.imageId).intoCash()
    }


}
