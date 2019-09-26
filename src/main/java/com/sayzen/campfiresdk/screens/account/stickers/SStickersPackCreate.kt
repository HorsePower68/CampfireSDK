package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPackChange
import com.dzen.campfire.api.requests.stickers.RStickersPackCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText

class SStickersPackCreate(
        val unit: UnitStickersPack?
) : Screen(R.layout.screen_stickers_pack_create) {

    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageIcon: View = findViewById(R.id.vImageIcon)
    private val vName: SettingsField = findViewById(R.id.vName)
    private val vCreate: Button = findViewById(R.id.vCreate)

    private var image: ByteArray? = null

    init {
        vName.addOnTextChanged { updateFinishEnabled() }
        vImage.setOnClickListener { chooseImage() }
        vCreate.setOnClickListener { create() }

        if(unit != null){
            vName.setText(unit.name)
            ToolsImagesLoader.load(unit.imageId).into(vImage)
            vImageIcon.visibility = View.GONE
            vCreate.text = ToolsResources.s(R.string.app_change)
        }
    }

    private fun updateFinishEnabled() {
        var textCheck = ToolsText.isOnly(vName.getText(), API.ENGLISH)
        vName.setError(if (textCheck) null else ToolsResources.s(R.string.error_use_english))
        if (textCheck) {
            textCheck = vName.getText().length <= API.FANDOM_NAME_MAX
            vName.setError(if (textCheck) null else ToolsResources.s(R.string.error_too_long_text))
        }

        vCreate.isEnabled = textCheck && ToolsText.inBounds(vName.getText(), API.STICKERS_PACK_NAME_L_MIN, API.STICKERS_PACK_NAME_L_MAX)
                && (image != null || unit != null)
    }

    private fun chooseImage() {
        WidgetChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b, API.STICKERS_PACK_IMAGE_SIDE, API.STICKERS_PACK_IMAGE_SIDE) { _, b2, _, _, _, _ ->
                        this.image = ToolsBitmap.toBytes(ToolsBitmap.resize(b2, API.STICKERS_PACK_IMAGE_SIDE), API.STICKERS_PACK_IMAGE_WEIGHT)
                        vImage.setImageBitmap(b2)
                        vImageIcon.visibility = View.GONE
                        updateFinishEnabled()
                    }
                    )
                }
                .asSheetShow()
    }

    private fun create() {
        val name = vName.getText()

        if(unit == null) {
            ApiRequestsSupporter.executeProgressDialog(RStickersPackCreate(name, image)) { r ->
                Navigator.to(SStickersView(r.stickersPack, 0))
                Navigator.remove(this)
                EventBus.post(EventStickersPackCreate(r.stickersPack))
            }
        }else{
            ApiRequestsSupporter.executeProgressDialog(RStickersPackChange(unit.id, name, image)) { r ->
                Navigator.remove(this)
                EventBus.post(EventStickersPackChanged(r.stickersPack))
            }
        }
    }

}