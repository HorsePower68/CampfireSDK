package com.sayzen.campfiresdk.screens.post.create.creators

import android.graphics.Bitmap
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PageImage
import com.dzen.campfire.api.models.units.post.PageImages
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImage
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImages
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.tools.ToolsCollections

class WidgetPageImage(
        private val requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page) -> Unit) -> Unit
) : WidgetChooseImage() {

    private var imagesCreator = ImagesListCreator(requestPutPage)
    private var createAsList = false

    init {
        setMaxSelectCount(15)
        setCallbackInWorkerThread(true)
        setOnSelected { w, bytes, index ->

            if (w.getSelectedCount() > 1) {

                if(index == 0) {
                    val waiting = Item(true)
                    ToolsThreads.main {
                        WidgetAlert()
                                .setText(R.string.post_page_image_create_as_list_text)
                                .setOnCancel(R.string.app_no)
                                .setOnEnter(R.string.app_yes) { ww -> createAsList = true }
                                .setOnHide { waiting.a = false }
                                .asSheetShow()
                    }
                    while (waiting.a) ToolsThreads.sleep(10)
                }

                if(createAsList){
                    imagesCreator.addImage(ToolsBitmap.decode(bytes)!!, bytes)
                    if(index == w.getSelectedCount() - 1){
                        val sent = Item(false)
                        imagesCreator.send{sent.a = true}
                        while (!sent.a) ToolsThreads.sleep(20)
                    }
                }else{
                    val sent = Item(false)
                    createNow(bytes, null, requestPutPage, requestChangePage, null) {
                        sent.a = true
                    }
                    while (!sent.a) ToolsThreads.sleep(20)
                }
            } else {
                ToolsThreads.main {
                    createNow(bytes, ToolsView.showProgressDialog(), requestPutPage, requestChangePage, null) {
                    }
                }
            }


        }
    }

    companion object {

        fun change(page: Page,
                   requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
                   requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page) -> Unit) -> Unit,
                   card: CardPage) {
            if ((page as PageImage).gifId != 0L) {
                ToolsImagesLoader.load(page.gifId).into { bytes ->
                    parseChangeBytes(bytes, requestPutPage, requestChangePage, card)
                }
            } else {
                ToolsImagesLoader.load(page.imageId).into { bytes ->
                    parseChangeBytes(bytes, requestPutPage, requestChangePage, card)
                }
            }
        }

        private fun parseChangeBytes(bytes: ByteArray?,
                                     requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
                                     requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page) -> Unit) -> Unit,
                                     card: CardPage) {
            val d = ToolsView.showProgressDialog()
            ToolsThreads.thread {
                if (bytes == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    d.hide()
                    return@thread
                }
                val bm = ToolsBitmap.decode(bytes)
                if (bm == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    d.hide()
                    return@thread
                }
                ToolsThreads.main {
                    Navigator.to(SCrop(bm) { _, bitmap, x, y, w, h ->
                        ToolsThreads.thread {
                            val bytesScaled =
                                    if (ToolsBytes.isGif(bytes))
                                        ToolsGif.resize(bytes, API.PAGE_IMAGE_SIDE_GIF, API.PAGE_IMAGE_SIDE_GIF, x, y, w, h, true)
                                    else
                                        ToolsBitmap.toBytes(bitmap, API.PAGE_IMAGE_WEIGHT)
                            if (bytesScaled == null) {
                                ToolsToast.show(R.string.error_cant_load_image)
                                d.hide()
                                return@thread
                            }
                            createNow(bytesScaled, d, requestPutPage, requestChangePage, card) {}
                        }
                    })
                }
            }
        }

        private fun createNow(bytes: ByteArray, d: Widget?,
                              requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
                              requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page) -> Unit) -> Unit,
                              card: CardPage?, onCreateFinish: () -> Unit) {

            if (ToolsBytes.isGif(bytes) && bytes.size > API.PAGE_IMAGE_GIF_WEIGHT) {
                d?.hide()
                ToolsToast.show(R.string.error_too_long_file)
                return
            }

            val page = PageImage()
            var size = API.PAGE_IMAGE_SIDE
            if (ToolsBytes.isGif(bytes)) {
                page.insertGifBytes = bytes
                size = API.PAGE_IMAGE_SIDE_GIF
            }
            val decode = ToolsBitmap.decode(bytes)
            if (decode == null) {
                d?.hide()
                ToolsToast.show(R.string.error_cant_load_image)
                return
            }

            page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(decode, size), API.PAGE_IMAGE_WEIGHT)

            ToolsThreads.main {
                if (card == null)
                    requestPutPage.invoke(page, null, d, { page1 -> CardPageImage(null, page1 as PageImage) }, { onCreateFinish.invoke() })
                else
                    requestChangePage.invoke(page, card, null, d) {}

            }


        }

        private class ImagesListCreator(
                val requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit
        ) {

            private val page = PageImages()

            fun addImage(bitmap: Bitmap, bytes: ByteArray) {
                val img = if (ToolsBytes.isGif(bytes)) ToolsGif.resize(bytes, API.PAGE_IMAGES_SIDE_GIF, API.PAGE_IMAGES_SIDE_GIF, null, null, null, null, true)
                else ControllerApi.toBytesNow(ToolsBitmap.keepMaxSides(bitmap, API.PAGE_IMAGES_SIDE), API.PAGE_IMAGES_WEIGHT)

                val imgMini = if (ToolsBytes.isGif(bytes)) ToolsGif.resize(bytes, API.PAGE_IMAGES_SIDE_GIF, API.PAGE_IMAGES_SIDE_GIF, null, null, null, null, true)
                else ControllerApi.toBytesNow(ToolsBitmap.keepMaxSides(bitmap, API.PAGE_IMAGES_MINI_SIDE), API.PAGE_IMAGES_MINI_WEIGHT)

                if (img == null || imgMini == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    return
                }

                if ((ToolsBytes.isGif(img) && img.size > API.PAGE_IMAGE_GIF_WEIGHT) || ToolsBytes.isGif(imgMini) && imgMini.size > API.PAGE_IMAGE_GIF_WEIGHT) {
                    ToolsToast.show(R.string.error_too_long_file)
                    return
                }

                synchronized(page.insertImages) {
                    page.insertImages = ToolsCollections.add(img, page.insertImages)
                    page.insertImagesMini = ToolsCollections.add(imgMini, page.insertImagesMini)
                }

            }

            fun send(onFinish:()->Unit){
                requestPutPage.invoke(page, null, null, {CardPageImages(null, it as PageImages)}){onFinish.invoke()}
            }
        }

    }

}
