package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.widgets.WidgetChooseImage

class CardCreateTitle(
        val myLvl: Long,
        val changeName: String,
        val changeImageId: Long,
        val params: ChatParamsConf,
        val updateFinish: () -> Unit
) : Card(R.layout.screen_chat_create_card_title) {

    var image: ByteArray? = null
    var text = changeName

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vImageIcon: View = view.findViewById(R.id.vImageIcon)
        val vUsers: TextView = view.findViewById(R.id.vUsers)
        val vName: SettingsField = view.findViewById(R.id.vName)
        val vNameTitle: TextView = view.findViewById(R.id.vNameTitle)
        val vAllowInvites: SettingsCheckBox = view.findViewById(R.id.vAllowInvites)
        val vAllowEdit: SettingsCheckBox = view.findViewById(R.id.vAllowEdit)

        vName.vField.imeOptions = EditorInfo.IME_ACTION_DONE    //  Вылетало при нажатии Enter
        ToolsView.onFieldEnterKey(vName.vField){
            ToolsView.hideKeyboard(vName.vField)
        }

        vAllowInvites.isEnabled = myLvl == API.CHAT_MEMBER_LVL_ADMIN
        vAllowEdit.isEnabled = myLvl == API.CHAT_MEMBER_LVL_ADMIN

        vAllowInvites.setChecked(params.allowUserInvite)
        vAllowEdit.setChecked(params.allowUserNameAndImage)

        vAllowInvites.setOnClickListener { params.allowUserInvite = vAllowInvites.isChecked() }
        vAllowEdit.setOnClickListener { params.allowUserNameAndImage = vAllowEdit.isChecked() }

        vName.setText(changeName)
        vNameTitle.setText(changeName)

        vName.setMaxLength(API.CHAT_NAME_MAX)
        vName.addOnTextChanged {
            text = it
            updateFinish.invoke()
        }

        if (changeImageId != 0L) {
            ToolsImagesLoader.load(changeImageId).into(vImage)
            vImageIcon.visibility = View.GONE
        }

        if( params.allowUserNameAndImage || myLvl != API.CHAT_MEMBER_LVL_USER){
            vImage.isEnabled = true
            vName.visibility = View.VISIBLE
            vNameTitle.visibility = View.GONE
        } else{
            vImage.isEnabled = false
            vName.visibility = View.GONE
            vNameTitle.visibility = View.VISIBLE
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