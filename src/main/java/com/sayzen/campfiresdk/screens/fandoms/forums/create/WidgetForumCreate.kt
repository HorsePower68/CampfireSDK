package com.sayzen.campfiresdk.screens.fandoms.forums.create

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class WidgetForumCreate constructor(
        private val fandomId: Long,
        private val languageId: Long,
        private val forumId: Long,
        private val name: String?,
        private val text: String?,
        private val imageId: Long?
) : Widget(R.layout.widget_forums_create) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageIcon: View = findViewById(R.id.vImageIcon)

    private var image: ByteArray? = null

    init {
        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { onActionClicked() }
        vImage.setOnClickListener { chooseImage() }
        vName.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        if (name != null) {
            vName.setText(name)
        }
        if (imageId != null) {
            ToolsImagesLoader.load(imageId).into(vImage)
            vImageIcon.visibility = View.GONE
        }

        if(forumId != 0L) vEnter.setText(R.string.app_change)

        updateFinishEnabled()
        asSheetShow()
    }

    private fun updateFinishEnabled() {
        vEnter.isEnabled = ToolsText.inBounds(vName.getText(), API.FORUM_NAME_L_MIN, API.FORUM_NAME_L_MAX) && (forumId != 0L || image != null)
    }

    private fun chooseImage() {
        hide()
        WidgetChooseImage()
                .setOnSelected { _, bytes,_ ->


                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.FORUM_IMG_SIDE_GIF else API.FORUM_IMG_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.FORUM_IMG_SIDE_GIF, API.FORUM_IMG_SIDE_GIF, x, y, w, h)

                                        ToolsThreads.main {
                                            d.hide()
                                            if (bytesSized.size > API.FORUM_IMG_WEIGHT_GIF) {
                                                ToolsToast.show(R.string.error_too_long_file)
                                            } else {
                                                afterSelectImage(bytesSized, b2)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.FORUM_IMG_WEIGHT, API.FORUM_IMG_SIDE, API.FORUM_IMG_SIDE) {
                                        d.hide()
                                        if(it == null) ToolsToast.show(R.string.error_cant_load_image)
                                        else afterSelectImage(it, b2)
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()
    }

    private fun afterSelectImage(bytes:ByteArray, bitmap:Bitmap){
        this.image = bytes
        vImage.setImageBitmap(bitmap)
        vImageIcon.visibility = View.GONE
        ToolsThreads.main(100) { asSheetShow() }
        updateFinishEnabled()
    }

    private fun onActionClicked() {
        hide()
        Navigator.to(SForumCreate(fandomId, languageId, forumId, text, imageId, vName.getText(), image))
    }

}
