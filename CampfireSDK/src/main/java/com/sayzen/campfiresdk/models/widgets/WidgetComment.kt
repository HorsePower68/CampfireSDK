package com.sayzen.campfiresdk.models.widgets

import android.support.v7.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.requests.units.RUnitsCommentChange
import com.dzen.campfire.api.requests.units.RUnitsCommentCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.sayzen.campfiresdk.models.events.units.EventCommentChange
import com.sayzen.campfiresdk.models.events.units.EventUnitCommentWatchChange
import com.sayzen.campfiresdk.models.support.Attach
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewEditTextMedia
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class WidgetComment constructor(
        private val unitId: Long,
        private val answer: UnitComment?,
        private val changeComment: UnitComment?,
        private var quoteId: Long,
        private var quoteText: String,
        private val onCreated: ((Long) -> Unit)?
) : Widget(R.layout.widget_comment_input) {

    private val vSend: ViewIcon = findViewById(R.id.vSend)
    private val vAttach: ViewIcon = findViewById(R.id.vAttach)
    private val vAttachRecycler: RecyclerView = findViewById(R.id.vAttachRecycler)
    private val vText: ViewEditTextMedia = findViewById(R.id.vText)
    private val vQuoteText: ViewTextLinkable = findViewById(R.id.vQuoteText)

    private var attach = Attach(vAttach, vAttachRecycler, { updateSendEnabled() }) { ToolsThreads.main(100) { asSheetShow() } } // Нужна задержка, иначе откроется и сразу закроется из-за смены экранов

    constructor(changeComment: UnitComment) : this(0, null, changeComment, 0, "", null)

    constructor(unitId: Long, onCreated: (Long) -> Unit) : this(unitId, null, null, 0, "", onCreated)

    constructor(unitId: Long, answer: UnitComment?, onCreated: (Long) -> Unit) : this(unitId, answer, null, 0, "", onCreated)

    init {

        if (changeComment != null) {
            vText.setText(changeComment.text)
            vText.setSelection(vText.text!!.length)
            quoteText = changeComment.quoteText
            quoteId = changeComment.quoteId
        } else if (answer != null) {
            vText.setText(answer.creatorName + ", ")
            vText.setSelection(vText.text!!.length)
        }

        vQuoteText.visibility = if (quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = quoteText
        ControllerApi.makeTextHtml(vQuoteText)

        if (changeComment == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        vText.addTextChangedListener(TextWatcherChanged { updateSendEnabled() })

        vSend.setOnClickListener { v -> onSendClicked() }
        vSend.setImageResource(ToolsResources.getDrawableAttrId(if (changeComment == null) R.attr.ic_send_24dp else R.attr.ic_done_24dp))
        vAttach.visibility = if (changeComment == null) View.VISIBLE else View.GONE
        updateSendEnabled()
    }

    private fun updateSendEnabled() {
        vSend.isEnabled = (vText.isEnabled && vText.text!!.isNotEmpty() && vText.text!!.length < API.COMMENT_MAX_L) || attach.isHasContent()
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vText)
    }

    override fun setEnabled(enabled: Boolean): Widget {
        attach.setEnabled(enabled)
        vText.isEnabled = enabled
        vQuoteText.isEnabled = enabled
        updateSendEnabled()
        return super.setEnabled(enabled)
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

    private fun onCancel() {
        if (attach.isHasContent()
                || (answer != null && getText() != answer.creatorName + ",")
                || (changeComment != null && getText() != changeComment.text.trim())
                || (answer == null && changeComment == null && getText().isNotEmpty())
        ) {
            WidgetAlert()
                    .setText(R.string.comments_cancel_confirm)
                    .setOnCancel(R.string.app_do_cancel)
                    .setOnEnter(R.string.app_close) {
                        hide()
                    }
                    .asSheetShow()
        } else {
            hide()
        }
    }


    private fun afterSend(commentId: Long) {
        ToolsToast.show(R.string.app_published)
        onCreated?.invoke(commentId)
        EventBus.post(EventCommentAdd(unitId, commentId))
        if (ControllerSettings.watchPost) EventBus.post(EventUnitCommentWatchChange(unitId, true))
        hide()
    }

    private fun getText() = vText.text!!.toString().trim { it <= ' ' }

    private fun getParentId(): Long {
        var parentId: Long = 0
        if (answer != null && getText().startsWith(answer.creatorName + ", "))
            parentId = answer.id
        return parentId

    }

    private fun onSendClicked() {
        val text = getText()
        val parentId = getParentId()

        if (text.isEmpty() && !attach.isHasContent()) return

        if (changeComment == null) {
            if (attach.isHasContent()) sendImage(text, parentId)
            else if (ToolsText.isWebLink(text)) sendLink(text, parentId, true)
            else sendText(text, parentId)
        } else sendChange(text)
    }


    //
    //  Text
    //


    private fun sendText(text: String, parentId: Long) {
        ApiRequestsSupporter.executeEnabled(this, RUnitsCommentCreate(unitId, text, null, null, parentId, ControllerSettings.watchPost, quoteId)) { r ->
            afterSend(r.commentId)
        }
    }

    private fun sendChange(text: String) {
        ApiRequestsSupporter.executeEnabled(this, RUnitsCommentChange(changeComment!!.id, text, quoteId)) { r ->
            ToolsToast.show(R.string.app_changed)
            EventBus.post(EventCommentChange(changeComment.id, text, quoteId, quoteText))
        }
    }

    //
    //  Link
    //

    private fun sendLink(text: String, parentId: Long, send: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        ToolsNetwork.getBytesFromURL(text, 10) { bytes ->
            if (bytes == null || !ToolsBytes.isImage(bytes)) {
                dialog.hide()
                if (send) sendText(text, parentId)
                else vText.setText(text)
            } else {
                attach.attachUrl(text, dialog) {
                    if (send) sendText(text, parentId)
                    else vText.setText(text)
                }
            }

        }
    }


    //
    //  Image
    //

    private fun sendImage(text: String, parentId: Long) {
        setEnabled(false)

        ToolsThreads.thread {
            val bytes = attach.getBytes()
            val gif = if (bytes.size == 1 && ToolsBytes.isGif(bytes[0])) bytes[0] else null
            if (gif != null) {
                val bt = ToolsBitmap.decode(bytes[0])
                if (bt == null) {
                    setEnabled(true)
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                val byt = ToolsBitmap.toBytes(bt, API.CHAT_MESSAGE_IMAGE_WEIGHT)
                if (byt == null) {
                    setEnabled(true)
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                bytes[0] = byt
            }
            ApiRequestsSupporter.executeProgressDialog(RUnitsCommentCreate(unitId, text, bytes, gif, parentId, ControllerSettings.watchPost, quoteId)) { r -> afterSend(r.commentId) }
                    .onApiError(RUnitsCommentCreate.E_BAD_UNIT_STATUS) { ToolsToast.show(R.string.error_gone) }
                    .onFinish { setEnabled(true) }
        }


    }

}
