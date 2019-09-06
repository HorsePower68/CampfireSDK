package com.sayzen.campfiresdk.screens.post.create.creator

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PageImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImage
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sup.dev.android.tools.*
import com.sup.dev.java.classes.items.Item

class WidgetPageImage(
        val screen: SPostCreate
) : WidgetChooseImage() {

    init {
        setMaxSelectCount(15)
        setCallbackInWorkerThread(true)
        setOnSelected { _, bytes, _ ->
            val sent = Item(false)
            createNow(bytes, null, screen, null) {
                sent.a = true
            }
            while (!sent.a) ToolsThreads.sleep(20)
        }
    }

    companion object {

        fun change(page: Page, screen: SPostCreate, card: CardPage) {
            if ((page as PageImage).gifId != 0L) {
                ToolsImagesLoader.load(page.gifId).into { bytes ->
                    parseChangeBytes(bytes, screen, card)
                }
            } else {
                ToolsImagesLoader.load(page.imageId).into { bytes ->
                    parseChangeBytes(bytes, screen, card)
                }
            }
        }

        private fun parseChangeBytes(bytes: ByteArray?, screen: SPostCreate, card: CardPage) {
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
                            createNow(bytesScaled, d, screen, card) {}
                        }
                    })
                }
            }
        }

        private fun createNow(bytes: ByteArray, d: Widget?, screen: SPostCreate, card: CardPage?, onCreateFinish: () -> Unit) {

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
                    screen.putPage(page, null, d, { page1 -> CardPageImage(null, page1) }, { onCreateFinish.invoke() }, true)
                else
                    screen.changePage(page, card, null, d)

            }


        }

    }

}
