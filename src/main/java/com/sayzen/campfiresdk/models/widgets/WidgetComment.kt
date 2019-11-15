package com.sayzen.campfiresdk.models.widgets

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.PublicationComment
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.units.RUnitsCommentChange
import com.dzen.campfire.api.requests.units.RUnitsCommentCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.publications.EventCommentAdd
import com.sayzen.campfiresdk.models.events.publications.EventCommentChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationCommentWatchChange
import com.sayzen.campfiresdk.models.support.Attach
import com.sayzen.campfiresdk.screens.other.rules.SGoogleRules
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
        private val publicationId: Long,
        private val answer: PublicationComment?,
        private val changeComment: PublicationComment?,
        private var quoteId: Long,
        private var quoteText: String,
        private val onCreated: ((PublicationComment) -> Unit)?
) : Widget(R.layout.widget_comment_input) {

    private val vSend: ViewIcon = findViewById(R.id.vSend)
    private val vAttach: ViewIcon = findViewById(R.id.vAttach)
    private val vAttachRecycler: RecyclerView = findViewById(R.id.vAttachRecycler)
    private val vText: ViewEditTextMedia = findViewById(R.id.vText)
    private val vQuoteText: ViewTextLinkable = findViewById(R.id.vQuoteText)

    private var attach = Attach(vAttach, vAttachRecycler,
            { updateSendEnabled() },
            { ToolsThreads.main(100) { asSheetShow() } }, // Нужна задержка, иначе откроется и сразу закроется из-за смены экранов
            { sendSticker(it) })

    constructor(changeComment: PublicationComment) : this(0, null, changeComment, 0, "", null)

    constructor(publicationId: Long, onCreated: (PublicationComment) -> Unit) : this(publicationId, null, null, 0, "", onCreated)

    constructor(publicationId: Long, answer: PublicationComment?, onCreated: (PublicationComment) -> Unit) : this(publicationId, answer, null, 0, "", onCreated)

    init {

        if (changeComment != null) {
            vText.setText(changeComment.text)
            vText.setSelection(vText.text!!.length)
            quoteText = changeComment.quoteText
            quoteId = changeComment.quoteId
        } else if (answer != null && quoteId == 0L) {
            vText.setText(answer.creatorName + ", ")
            vText.setSelection(vText.text!!.length)
        }

        vQuoteText.visibility = if (quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = quoteText
        ControllerApi.makeTextHtml(vQuoteText)

        if (changeComment == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        vText.addTextChangedListener(TextWatcherChanged { updateSendEnabled() })

        vSend.setOnClickListener { onSendClicked() }
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
                || (getText().isNotEmpty() && answer != null && getText() != answer.creatorName + ",")
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


    private fun afterSend(comment: PublicationComment) {
        ToolsToast.show(R.string.app_published)
        onCreated?.invoke(comment)
        EventBus.post(EventCommentAdd(publicationId, comment))
        if (ControllerSettings.watchPost) EventBus.post(EventPublicationCommentWatchChange(publicationId, true))
        hide()
    }

    private fun getText() = vText.text!!.toString().trim { it <= ' ' }

    private fun getParentId(): Long {
        if (quoteId != 0L) return quoteId
        if (answer != null && getText().startsWith(answer.creatorName + ", ")) return answer.id
        return 0L
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
        SGoogleRules.acceptRulesDialog() {
            ApiRequestsSupporter.executeEnabled(this, RUnitsCommentCreate(publicationId, text, null, null, parentId, ControllerSettings.watchPost, quoteId, 0)) { r ->
                afterSend(r.comment)
            }
        }

    }

    private fun sendChange(text: String) {
        SGoogleRules.acceptRulesDialog() {
            ApiRequestsSupporter.executeEnabled(
                this,
                RUnitsCommentChange(changeComment!!.id, text, quoteId)
            ) {
                ToolsToast.show(R.string.app_changed)
                EventBus.post(EventCommentChange(changeComment.id, text, quoteId, quoteText))
            }
        }
    }

    //
    //  Link
    //

    private fun sendLink(text: String, parentId: Long, send: Boolean) {
        SGoogleRules.acceptRulesDialog() {
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
    }

    private fun sendImage(text: String, parentId: Long) {
        SGoogleRules.acceptRulesDialog() {
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
                ApiRequestsSupporter.executeProgressDialog(
                    RUnitsCommentCreate(
                        publicationId,
                        text,
                        bytes,
                        gif,
                        parentId,
                        ControllerSettings.watchPost,
                        quoteId,
                        0
                    )
                ) { r -> afterSend(r.comment) }
                    .onApiError(RUnitsCommentCreate.E_BAD_PUBLICATION_STATUS) { ToolsToast.show(R.string.error_gone) }
                    .onFinish { setEnabled(true) }
            }

        }
    }

    private fun sendSticker(sticker: PublicationSticker) {
        SGoogleRules.acceptRulesDialog() {
            setEnabled(false)

            ApiRequestsSupporter.executeProgressDialog(
                RUnitsCommentCreate(
                    publicationId,
                    "",
                    null,
                    null,
                    answer?.id?:0,
                    ControllerSettings.watchPost,
                    quoteId,
                    sticker.id
                )
            ) { r -> afterSend(r.comment) }
                .onApiError(RUnitsCommentCreate.E_BAD_PUBLICATION_STATUS) { ToolsToast.show(R.string.error_gone) }
                .onFinish { setEnabled(true) }
        }
    }

}
