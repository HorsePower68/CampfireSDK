package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersAdd
import com.dzen.campfire.api.requests.stickers.RStickersGetAllByPackId
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XComments
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.events.stickers.EventStickerCreate
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.android.views.widgets.WidgetProgressTransparent
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads

class SStickersView(
        val stickersPack: PublicationStickersPack,
        val stickerId: Long
) : SLoadingRecycler<CardSticker, PublicationSticker>(R.layout.screen_stickers_view) {

    companion object {

        fun instanceBySticker(stickerId: Long, action: NavigationAction) {
            instance(0, stickerId, action)
        }

        fun instance(packId: Long, action: NavigationAction) {
            instance(packId, 0, action)
        }

        fun instance(packId: Long, stickerId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RStickersPacksGetInfo(packId, stickerId)) { r ->
                SStickersView(r.stickersPack, stickerId)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventStickerCreate::class) { onEventStickerCreate(it) }
            .subscribe(EventStickersPackChanged::class) { onEventStickersPackChanged(it) }
            .subscribe(EventPublicationRemove::class) { if (it.unitId == stickersPack.id) Navigator.remove(this) }

    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vCommentsCount: TextView = findViewById(R.id.vCommentsCount)
    private val vKarma: ViewKarma = findViewById(R.id.vKarma)
    private val vCommentsContainer: View = findViewById(R.id.vCommentsContainer)
    private var loaded = false
    private val xKarma = XKarma(stickersPack) { updateKarma() }
    private val xComments = XComments(stickersPack) { updateComments() }

    init {
        setTextEmpty(R.string.stickers_pack_view_empty)
        setBackgroundImage(R.drawable.bg_4)

        val spanCount = if (ToolsAndroid.isScreenPortrait()) 3 else 6
        vRecycler.layoutManager = GridLayoutManager(context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)
        addToolbarIcon(ToolsResources.getDrawableAttr(R.attr.ic_more_vert_24dp)!!) {
            ControllerStickers.showStickerPackPopup(stickersPack)
        }

        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { chooseImage() }
        if (stickersPack.creatorId == ControllerApi.account.id && ControllerApi.can(API.LVL_CREATE_STICKERS))   (vFab as View).visibility = View.VISIBLE



        vAvatarTitle.setOnClickListener {
            SAccount.instance(stickersPack.creatorId, Navigator.TO)
        }

        vCommentsContainer.setOnClickListener {
            Navigator.to(SComments(stickersPack.id, 0))
        }

        updateTitle()
        updateKarma()
        updateComments()
    }

    private fun updateComments() {
        xComments.setView(vCommentsCount)
    }

    private fun updateKarma() {
        xKarma.setView(vKarma)
    }

    private fun updateTitle() {
        vAvatarTitle.setTitle(stickersPack.name)
        vAvatarTitle.setSubtitle(stickersPack.creatorName)
        ToolsImagesLoader.load(stickersPack.imageId).into(vAvatarTitle.vAvatar.vImageView)

    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardSticker, PublicationSticker> {
        return RecyclerCardAdapterLoading<CardSticker, PublicationSticker>(CardSticker::class) {
            val card = CardSticker(it)
            if (it.id == stickerId) card.flash()
            card
        }
                .setShowLoadingCardBottom(false)
                .setBottomLoader { onLoad, _ ->
                    subscription = RStickersGetAllByPackId(stickersPack.id)
                            .onComplete { r ->
                                if (loaded) {
                                    onLoad.invoke(emptyArray())
                                } else {
                                    loaded = true
                                    onLoad.invoke(r.stickers)
                                }
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun chooseImage() {


        if (adapter!!.size(CardSticker::class) >= API.STICKERS_MAX_COUNT_IN_PACK){
            ToolsToast.show(R.string.stickers_message_too_many)
            return
        }

        WidgetChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.STICKERS_IMAGE_SIDE_GIF else API.STICKERS_IMAGE_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesGif = ToolsGif.resize(bytes, API.STICKERS_IMAGE_SIDE_GIF, API.STICKERS_IMAGE_SIDE_GIF, x, y, w, h, true)
                                        ControllerApi.toBytes(b2, API.STICKERS_IMAGE_WEIGHT, API.STICKERS_IMAGE_SIDE_GIF, API.STICKERS_IMAGE_SIDE_GIF, true) {
                                            if (it == null) d.hide()
                                            else {
                                                ToolsThreads.main {
                                                    if (bytesGif.size > API.STICKERS_IMAGE_WEIGHT_GIF) {
                                                        d.hide()
                                                        ToolsToast.show(R.string.error_too_long_file)
                                                    } else {
                                                        changeAvatarNow(d, it, bytesGif)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.STICKERS_IMAGE_WEIGHT, API.STICKERS_IMAGE_SIDE, API.STICKERS_IMAGE_SIDE, true) {
                                        if (it == null) d.hide()
                                        else changeAvatarNow(d, it, null)
                                    }
                                }
                            })

                        }


                    }


                }
                .asSheetShow()

    }

    private fun changeAvatarNow(dialog: WidgetProgressTransparent, bytes: ByteArray, gifBytes: ByteArray?) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RStickersAdd(stickersPack.id, bytes, gifBytes)) { r ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventStickerCreate(r.sticker))
        }
    }

    private fun onEventStickerCreate(e: EventStickerCreate) {
        if (e.sticker.tag_1 == stickersPack.id) {
            val card = CardSticker(e.sticker)
            adapter?.add(card)
            card.flash()
            setState(State.NONE)
        }
    }

    private fun onEventStickersPackChanged(e: EventStickersPackChanged) {
        if (e.stickersPack.id == stickerId) {
            stickersPack.name = e.stickersPack.name
            stickersPack.imageId = e.stickersPack.imageId
            updateTitle()
        }
    }


}