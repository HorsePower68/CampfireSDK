package com.sayzen.campfiresdk.models.support

import android.graphics.Bitmap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsGif
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsThreads

class Attach(
        val vAttach: ViewIcon,
        val vAttachRecycler: RecyclerView,
        val onUpdate: () -> Unit = {},
        val onSupportScreenHide: () -> Unit = {}
) {

    private var adapter = RecyclerCardAdapter()
    private var enabled = true
    private var initerd = false

    init {
        vAttach.setOnClickListener { onAttachClicked() }
        vAttachRecycler.layoutManager = LinearLayoutManager(vAttachRecycler.context, LinearLayoutManager.HORIZONTAL, false)
        clear()
        initerd = true
    }

    fun clear() {
        adapter.clear()
        adapter = RecyclerCardAdapter()
        adapter.setCardW(RecyclerView.LayoutParams.WRAP_CONTENT)
        vAttachRecycler.adapter = adapter
        updateAttach()
    }

    private fun updateAttach() {
        vAttachRecycler.visibility = if (isHasContent()) View.VISIBLE else View.GONE
        vAttach.isEnabled = adapter.size() < API.CHAT_MESSAGE_MAX_IMAGES_COUNT && enabled
        if(initerd)onUpdate.invoke()
    }

    private fun addBytes(bytes: ByteArray) {
        adapter.add(ItemCard(bytes))
        vAttachRecycler.scrollToPosition(adapter.size() - 1)
        updateAttach()
    }

    fun setImageBitmapNow(it: Bitmap, dialog: Widget) {

        val bitmap = ToolsBitmap.keepMaxSides(it, API.CHAT_MESSAGE_IMAGE_SIDE)
        val bytes = ControllerApi.toBytesNow(bitmap, API.CHAT_MESSAGE_IMAGE_WEIGHT)

        ToolsThreads.main {
            dialog.hide()
            if (bytes == null) {
                ToolsToast.show(R.string.error_cant_load_image)
            } else {
                addBytes(bytes)
            }
        }


    }

    fun attachUrl(text: String, dialog: Widget, onError: () -> Unit) {
        ToolsNetwork.getBytesFromURL(text, 0) { bytes ->
            try {
                ToolsThreads.thread {
                    parseAttachBytes(bytes, dialog)
                }
            } catch (e: Exception) {
                err(e)
                dialog.hide()
                onError.invoke()
            }
        }
    }

    private fun onAttachClicked() {
        WidgetChooseImage()
                .setMaxSelectCount(API.CHAT_MESSAGE_MAX_IMAGES_COUNT)
                .setCallbackInWorkerThread(true)
                .setOnSelected { w, bytes,index ->
                    parseAttachBytes(bytes, null)
                }
                .asSheetShow()
    }

    private fun parseAttachBytes(bytes: ByteArray?, dialog: Widget?) {
        if (bytes == null) {
            ToolsToast.show(R.string.error_cant_load_image)
            return
        }
        if (ToolsBytes.isGif(bytes)) {
            val bytesScaled = ToolsGif.resize(bytes, API.CHAT_MESSAGE_IMAGE_SIDE_GIF, API.CHAT_MESSAGE_IMAGE_SIDE_GIF, null, null, null, null, true)
            if (bytesScaled.size > API.CHAT_MESSAGE_GIF_MAX_WEIGHT) {
                ToolsToast.show(R.string.error_too_long_file)
                dialog?.hide()
                return
            }
            ToolsThreads.main {
                dialog?.hide()
                addBytes(bytesScaled)
            }
        } else {
            val bytes = ToolsBitmap.decode(bytes)
            if (bytes == null) {
                ToolsToast.show(R.string.error_cant_load_image)
                return
            }

            val bytesScaled = ToolsBitmap.toBytes(ToolsBitmap.keepSides(bytes, API.CHAT_MESSAGE_IMAGE_SIDE), API.CHAT_MESSAGE_IMAGE_WEIGHT)

            ToolsThreads.main {
                dialog?.hide()
                if (bytesScaled == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@main
                }
                addBytes(bytesScaled)
            }
        }
    }

    //
    //  Setters
    //

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        updateAttach()
    }

    //
    //  Getters
    //

    fun getBytes() = Array(adapter.size()) { (adapter[it] as ItemCard).bytes }

    fun isHasContent() = !adapter.isEmpty

    //
    //  Card
    //

    private inner class ItemCard(
            var bytes: ByteArray
    ) : Card() {

        override fun getLayout() = R.layout.view_attach_image

        override fun bindView(view: View) {
            super.bindView(view)

            val vImage: ImageView = view.findViewById(R.id.vImage)
            val vCrop: View = view.findViewById(R.id.vCrop)
            val vRemove: View = view.findViewById(R.id.vRemove)
            setImage(vImage)

            vImage.setOnClickListener { Navigator.to(SImageView(bytes).setOnHide(onSupportScreenHide)) }
            vCrop.setOnClickListener { crop(vImage) }
            vRemove.setOnClickListener {
                if (adapter != null) adapter!!.remove(this)
                updateAttach()
            }
        }

        private fun setImage(vImage: ImageView) {
            val dp = ToolsView.dpToPx(128).toInt()
            val bm = ToolsBitmap.decode(bytes, dp, dp, null, dp, dp)
            vImage.setImageBitmap(bm)
        }

        private fun crop(vImage: ImageView) {
            val dialog = ToolsView.showProgressDialog()

            ToolsThreads.thread {
                val decoded = ToolsBitmap.decode(bytes)
                dialog.hide()
                if (decoded == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                ToolsThreads.main {
                    Navigator.to(SCrop(decoded) { s, bitmap, x, y, w, h ->
                        val bytesScaled = ToolsBitmap.toBytes(ToolsBitmap.keepSides(bitmap, API.CHAT_MESSAGE_IMAGE_SIDE), API.CHAT_MESSAGE_IMAGE_WEIGHT)
                        if (bytesScaled == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@SCrop
                        }
                        this.bytes = bytesScaled
                        setImage(vImage)

                    }.setOnHide(onSupportScreenHide))
                }
            }
        }

    }

}