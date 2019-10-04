package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.java.tools.ToolsThreads

class CardCreateTitle(
        val changeName: String,
        val changeImageId: Long,
        val updateFinish:()->Unit
) : Card(R.layout.screen_chat_create_card_title) {

    var image: ByteArray? = null
    var text = changeName

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vImageIcon: View = view.findViewById(R.id.vImageIcon)
        val vUsers: TextView = view.findViewById(R.id.vUsers)
        val vName: SettingsField = view.findViewById(R.id.vName)

        vName.setText(changeName)

        vName.setMaxLength(API.CHAT_NAME_MAX)
        vName.addOnTextChanged {
            text = it
            updateFinish.invoke()
        }

        if(changeImageId != 0L){
            ToolsImagesLoader.load(changeImageId).into(vImage)
            vImageIcon.visibility = View.GONE
        }

        vImage.setOnClickListener { chooseImage(vImage, vImageIcon) }
        vUsers.text = ToolsResources.s(R.string.app_users) + ":"
    }

    private fun chooseImage(vImage: ImageView, vImageIcon: View) {
        WidgetChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE) { screen, b2, _, _, _, _ ->
                        this.image = ToolsBitmap.toBytes(ToolsBitmap.resize(b2, API.CHAT_IMG_SIDE), API.CHAT_IMG_WEIGHT)
                        vImage.setImageBitmap(b2)
                        vImageIcon.visibility = View.GONE
                        updateFinish.invoke()
                    }
                    )
                }
                .asSheetShow()
    }

}