package com.sayzen.campfiresdk.screens.fandoms.chats

import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.chat.RChatGet
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatChange
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatCreated
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SFandomChatsCreate(
        val fandomId: Long,
        val languageId: Long,
        val chatId: Long,
        val text: String,
        val imageId: Long,
        val name: String
) : Screen(R.layout.screen_fandom_chat_create) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageIcon: View = findViewById(R.id.vImageIcon)
    private val vField: EditText = findViewById(R.id.vField)
    private val vFinish: Button = findViewById(R.id.vFinish)

    private var image: ByteArray? = null

    companion object {

        fun instance(chatId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RChatGet(ChatTag(API.CHAT_TYPE_FANDOM_SUB, chatId, 0), 0)) { r ->
                val params = ChatParamsFandomSub(r.chatParams)
                SFandomChatsCreate(0, 0, chatId, params.text, r.chatImageId, r.chatName)
            }
        }

    }

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)

        vField.setSingleLine(false)
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { update() })
        vImage.setOnClickListener { chooseImage() }
        vName.vField.addTextChangedListener(TextWatcherChanged { update() })

        if (imageId > 0) {
            ImageLoader.load(imageId).into(vImage)
            vImageIcon.visibility = View.GONE
            vFinish.text = ToolsResources.s(R.string.app_change)
        }
        vName.setText(name)
        vField.setText(text)
        vField.setSelection(vField.text.length)

        vFinish.setOnClickListener { onEnter() }
        update()
    }

    private fun update() {
        val s = vField.text.toString()

        vField.textSize = if (s.length < 200) 22f else 16f

        val b = ToolsText.inBounds(vName.getText(), API.CHAT_NAME_MIN, API.CHAT_NAME_MAX) && (chatId != 0L || image != null)

        vFinish.isEnabled = b && s.isNotEmpty() && s.length <= API.FANDOM_CHAT_TEXT_MAX_L
    }

    private fun onEnter() {
        val name = vName.getText()
        WidgetField()
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(if (chatId == 0L) R.string.app_create else R.string.app_change) { _, comment ->
                    if (chatId == 0L) {
                        ApiRequestsSupporter.executeProgressDialog(RFandomsModerationChatCreate(fandomId, languageId, name, vField.text.toString(), comment, image)) { r ->
                            ToolsToast.show(R.string.app_done)
                            Navigator.remove(this)
                            EventBus.post(EventFandomChatCreated(fandomId, languageId, r.chatTag.targetId))
                            SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, r.chatTag.targetId, 0), 0, false, Navigator.TO)
                        }
                    } else {
                        ApiRequestsSupporter.executeProgressDialog(RFandomsModerationChatChange(chatId, name, vField.text.toString(), comment, image)) { _ ->
                            ToolsToast.show(R.string.app_done)
                            ImageLoader.clear(imageId)
                            Navigator.remove(this)
                            EventBus.post(EventFandomChatChanged(chatId, name, vField.text.toString()))
                        }
                    }
                }
                .asSheetShow()
    }


    override fun onBackPressed(): Boolean {
        if (notChanged()) return false

        WidgetAlert()
                .setText(R.string.post_create_cancel_alert)
                .setOnEnter(R.string.app_yes) { Navigator.remove(this) }
                .setOnCancel(R.string.app_no)
                .asSheetShow()

        return true
    }

    private fun notChanged(): Boolean {
        return text == vField.text.toString()
    }


    private fun chooseImage() {
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
                            val cropSize = if (isGif) API.CHAT_IMG_SIDE_GIF else API.CHAT_IMG_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.CHAT_IMG_SIDE_GIF, API.CHAT_IMG_SIDE_GIF, x, y, w, h)

                                        ToolsThreads.main {
                                            d.hide()
                                            if (bytesSized.size > API.CHAT_IMG_WEIGHT_GIF) {
                                                ToolsToast.show(R.string.error_too_long_file)
                                            } else {
                                                afterSelectImage(bytesSized, b2)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.CHAT_IMG_WEIGHT_GIF, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE) {
                                        d.hide()
                                        if (it == null) ToolsToast.show(R.string.error_cant_load_image)
                                        else ToolsThreads.main { afterSelectImage(it, b2) }
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()
    }

    private fun afterSelectImage(bytes: ByteArray, bitmap: Bitmap) {
        this.image = bytes
        vImage.setImageBitmap(bitmap)
        vImageIcon.visibility = View.GONE
        update()
    }


}
