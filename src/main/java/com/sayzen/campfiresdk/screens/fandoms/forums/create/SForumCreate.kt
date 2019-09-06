package com.sayzen.campfiresdk.screens.fandoms.forums.create

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationForumChange
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationForumCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventForumChanged
import com.sayzen.campfiresdk.models.events.fandom.EventForumCreated
import com.sayzen.campfiresdk.screens.fandoms.forums.view.SForumView
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.ScreenProtected
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus

class SForumCreate(
        val fandomId: Long,
        val languageId: Long,
        val forumId: Long,
        val text: String?,
        val imageId: Long?,
        val name: String,
        val image: ByteArray?
) : Screen(R.layout.screen_forums_create), ScreenProtected {

    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)

    init {

        vField.setSingleLine(false)
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { update() })

        if (text != null) {
            vField.setText(text)
            vField.setSelection(vField.text.length)
        }
        if (image != null) {
            vAvatarTitle.vAvatar.setImage(ToolsBitmap.decode(image))
        } else {
            ToolsImagesLoader.load(imageId ?: 0).into(vAvatarTitle.vAvatar.vImageView)
        }

        vAvatarTitle.setTitle(name)

        vFab.setOnClickListener { onEnter() }
        update()
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    private fun update() {
        val s = vField.text.toString()

        vField.textSize = if (s.length < 200) 22f else 16f

        ToolsView.setFabEnabledR(vFab, !s.isEmpty() && s.length <= API.FORUM_TEXT_MAX_L, R.color.green_700)
    }

    private fun onEnter() {
        WidgetField()
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(if (forumId == 0L) R.string.app_create else R.string.app_change) { _, comment ->
                    if (forumId == 0L) {
                        ApiRequestsSupporter.executeProgressDialog(RFandomsModerationForumCreate(fandomId, languageId, name, vField.text.toString(), comment, image)) { r ->
                            ToolsToast.show(R.string.app_done)
                            Navigator.remove(this)
                            EventBus.post(EventForumCreated(fandomId, languageId, r.unitId))
                            SForumView.instance(r.unitId, Navigator.TO)
                        }
                    } else {
                        ApiRequestsSupporter.executeProgressDialog(RFandomsModerationForumChange(forumId, name, vField.text.toString(), comment, image)) { _ ->
                            ToolsToast.show(R.string.app_done)
                            Navigator.remove(this)
                            EventBus.post(EventForumChanged(forumId, name, vField.text.toString()))
                        }
                    }
                }
                .asSheetShow()
    }

    override fun onBackPressed(): Boolean {
        if (notChanged())
            return false
        else {
            showConfirmCancelDialog()
            return true
        }
    }

    override fun onProtectedClose(onClose: () -> Unit) {
        if (notChanged())
            onClose.invoke()
        else {
            showConfirmCancelDialog()
        }
    }

    private fun showConfirmCancelDialog() {
        WidgetAlert()
                .setText(R.string.post_create_cancel_alert)
                .setOnEnter(R.string.app_yes) { Navigator.remove(this) }
                .setOnCancel(R.string.app_no)
                .asSheetShow()
    }

    private fun notChanged(): Boolean {
        return text == vField.text.toString()
    }

}
