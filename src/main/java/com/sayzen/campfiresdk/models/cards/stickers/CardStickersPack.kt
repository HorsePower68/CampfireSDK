package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardStickersPack(
        publication: PublicationStickersPack,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = true
) : CardPublication(R.layout.card_stickers_pack, publication) {

    private val eventBus = EventBus
            .subscribe(EventStickersPackChanged::class) {
                if (it.stickersPack.id == publication.id) {
                    publication.imageId = it.stickersPack.imageId
                    publication.name = it.stickersPack.name
                    update()
                }
            }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationStickersPack

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vComments: TextView = view.findViewById(R.id.vComments)

        vComments.text = publication.subPublicationsCount.toString() + ""

        vTitle.visibility = if(isShowFullInfo) View.VISIBLE else View.GONE
        vTitle.text = ToolsResources.sCap(R.string.sticker_event_create_stickers_pack, ToolsResources.sex(publication.creatorSex, R.string.he_created, R.string.she_created))

        ToolsImagesLoader.load(publication.imageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.setTitle(publication.name)
        vAvatar.setSubtitle(publication.creatorName)

        vComments.setOnClickListener {
            SComments.instance(publication.id, 0, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            WidgetComment(publication.id, null, true) { }.asSheetShow()
            true
        }

        vMenu.setOnClickListener { ControllerStickers.showStickerPackPopup(publication) }

        view.setOnClickListener { Navigator.to(SStickersView(publication, 0)) }
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
        xPublication.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    override fun updateKarma() {
        if (getView() == null) return
        val viewKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xPublication.xKarma.setView(viewKarma)
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationStickersPack
        ToolsImagesLoader.load(publication.imageId).intoCash()
    }


}
