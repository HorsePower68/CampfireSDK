package com.sayzen.campfiresdk.screens.fandoms.tags

import android.view.View
import android.widget.Button
import android.widget.ImageView

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.tags.RTagsChange
import com.dzen.campfire.api.requests.tags.RTagsCreate
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class WidgetTagCreate private constructor(
        private val tag: PublicationTag?,
        private val parentId: Long,
        private val fandomId: Long,
        private val languageId: Long
) : Widget(R.layout.widget_tag_create) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageIcon: View = findViewById(R.id.vImageIcon)

    private var removeImage: Boolean = false
    private var image: ByteArray? = null

    constructor(tag: PublicationTag) : this(tag, tag.parentPublicationId, tag.fandomId, tag.languageId) {}

    constructor(parentId: Long, fandomId: Long, languageId: Long) : this(null, parentId, fandomId, languageId) {}

    init {
        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { onActionClicked() }
        vImage.setOnClickListener { chooseImage() }
        vName.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        if (tag != null) {
            vName.setText(tag.name)
            vEnter.setText(R.string.app_change)
            if (tag.imageId != 0L) {
                ToolsImagesLoader.load(tag.imageId).into(vImage)
                vImageIcon.visibility = View.GONE
            }
        }

        asSheetShow()
        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val t = vName.getText()
        vEnter.isEnabled = ToolsText.inBounds(t, API.TAG_NAME_MIN_L, API.TAG_NAME_MAX_L) && vComment.getText().isNotEmpty()
    }

    override fun setEnabled(enabled: Boolean): WidgetTagCreate {
        super.setEnabled(enabled)
        vName.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        vComment.isEnabled = enabled
        vImage.isEnabled = enabled
        return this
    }

    private fun chooseImage() {
        if (image != null || tag != null && tag.imageId != 0L && !removeImage) {
            image = null
            removeImage = true
            vImageIcon.visibility = View.VISIBLE
            vImage.setImageBitmap(null)
        } else {
            removeImage = false
            hide()
            WidgetChooseImage()
                    .setOnSelectedBitmap { _, b ->
                        Navigator.to(SCrop(b, API.TAG_IMAGE_SIDE, API.TAG_IMAGE_SIDE) { screen, b2, _, _, _, _ ->
                            this.image = ToolsBitmap.toBytes(ToolsBitmap.resize(b2, API.TAG_IMAGE_SIDE), API.TAG_IMAGE_WEIGHT)
                            vImage.setImageBitmap(b2)
                            vImageIcon.visibility = View.GONE
                            screen.back()
                        }
                                .setAutoBackOnCrop(false)
                                .setOnHide { ToolsThreads.main(100) { asSheetShow() }}
                        )
                    }
                    .asSheetShow()
        }
    }

    private fun onActionClicked() {
        if (tag == null)
            sendCreate()
        else
            sendChange()
    }

    private fun sendCreate() {
        ApiRequestsSupporter.executeEnabled(this, RTagsCreate(vName.getText(), vComment.getText(), fandomId, languageId, parentId, image)
        ) {
            ToolsToast.show(R.string.app_done)
            STags.instance(fandomId, languageId, Navigator.REPLACE)
        }
    }

    private fun sendChange() {
        ApiRequestsSupporter.executeEnabled(this, RTagsChange(tag!!.id, vName.getText(), vComment.getText(), image, removeImage)
        ) {
            ToolsToast.show(R.string.app_done)
            STags.instance(fandomId, languageId, Navigator.REPLACE)
        }
    }
}
