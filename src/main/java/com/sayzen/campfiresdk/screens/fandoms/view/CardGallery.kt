package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGalleryAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGalleryRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomInfoChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class CardGallery(
        private val xFandom: XFandom,
        private var gallery: Array<Long>
) : Card(R.layout.screen_fandom_card_gallery) {

    private val eventBus = EventBus
            .subscribe(EventFandomInfoChanged::class) { this.onEventFandomInfoChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vImages: ViewImagesSwipe = view.findViewById(R.id.vImages)
        val vText: TextView = view.findViewById(R.id.vText)
        val vAdd: ViewIcon = view.findViewById(R.id.vAdd)

        vAdd.visibility = if (ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_FANDOM_IMAGE)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { addGallery() }

        vImages.clear()
        for (i in gallery) vImages.add(i, onClick = null) { onGalleryImageClicked(i) }

        vText.visibility = if(gallery.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    private fun onGalleryImageClicked(i: Long) {
        if (!ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_GALLERY)) {
            ToolsToast.show(R.string.error_low_lvl)
            return
        }
        WidgetField()
                .setTitle(R.string.app_remove_image)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationGalleryRemove(xFandom.fandomId, xFandom.languageId, i, comment)) {
                        val list = ArrayList<Long>()
                        for (id in gallery) if (id != i) list.add(id)
                        EventBus.post(EventFandomInfoChanged(xFandom.fandomId, xFandom.languageId, list.toTypedArray()))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }


    private fun addGallery() {
        if (gallery.size >= API.FANDOM_GALLERY_MAX) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        WidgetChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b) { _, bitmap,_,_,_,_ ->
                        WidgetField()
                                .setHint(R.string.moderation_widget_comment)
                                .setOnCancel(R.string.app_cancel)
                                .setMin(API.MODERATION_COMMENT_MIN_L)
                                .setMax(API.MODERATION_COMMENT_MAX_L)
                                .setOnEnter(R.string.app_add) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(bitmap, API.FANDOM_GALLERY_MAX_SIDE), API.FANDOM_GALLERY_MAX_WEIGHT)
                                        ToolsThreads.main {
                                            ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsModerationGalleryAdd(xFandom.fandomId, xFandom.languageId, image, comment)) { r ->
                                                val array = Array(gallery.size + 1) {
                                                    if (gallery.size == it) r.imageId
                                                    else gallery[it]
                                                }
                                                EventBus.post(EventFandomInfoChanged(xFandom.fandomId, xFandom.languageId, array))
                                                ToolsToast.show(R.string.app_done)
                                            }
                                        }
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    //
    //  EventBus
    //

    private fun onEventFandomInfoChanged(e: EventFandomInfoChanged) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId) {
            if (e.gallery.isNotEmpty()) this.gallery = e.gallery
            update()
        }
    }


}
