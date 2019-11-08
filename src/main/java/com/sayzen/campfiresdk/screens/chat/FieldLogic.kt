package com.sayzen.campfiresdk.screens.chat

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.chat.RChatMessageChange
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.dzen.campfire.api.requests.chat.RChatTyping
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.support.Attach
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.android.views.ViewVoiceRecord
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewEditTextMedia
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class FieldLogic(
        val screen: SChat
) {

    val vSend: ViewIcon = screen.findViewById(R.id.vSend)
    val vAttach: ViewIcon = screen.findViewById(R.id.vAttach)
    val vAttachRecycler: RecyclerView = screen.findViewById(R.id.vAttachRecycler)
    val vText: ViewEditTextMedia = screen.findViewById(R.id.vText)
    val vQuoteContainer: ViewGroup = screen.findViewById(R.id.vQuoteContainer)
    val vQuoteText: ViewTextLinkable = screen.findViewById(R.id.vQuoteText)
    val vQuoteRemove: ViewIcon = screen.findViewById(R.id.vQuoteRemove)
    val vVoiceRecorder: ViewVoiceRecord = screen.findViewById(R.id.vVoiceRecorder)
    val vFieldContainer: ViewGroup = screen.findViewById(R.id.vFieldContainer)
    val vVoiceContainer: ViewGroup = screen.findViewById(R.id.vVoiceContainer)
    val vVoicePlay: ViewIcon = screen.findViewById(R.id.vVoicePlay)
    val vVoiceRemove: ViewIcon = screen.findViewById(R.id.vVoiceRemove)
    val vVoiceLabel: TextView = screen.findViewById(R.id.vVoiceLabel)

    val attach = Attach(vAttach, vAttachRecycler, { updateAction() }, {}, { sendSticker(it) })
    private val utilsAudioPlayer = UtilsAudioPlayer()

    private var lastTypingSent = 0L
    private var isRecording = false
    private var unitAnswer: PublicationChatMessage? = null
    var unitChange: PublicationChatMessage? = null
    private var quoteText = ""
    private var quoteId = 0L
    private var voiceBytes: ByteArray? = null

    init {
        vVoiceContainer.visibility = View.GONE
        vQuoteContainer.visibility = View.GONE
        vQuoteRemove.setOnClickListener { setQuote("") }
        vSend.setOnClickListener { onSendClicked() }
        vText.addTextChangedListener(TextWatcherChanged { onTextChanged() })

        vVoiceRecorder.maxRecordingTimeMs = API.CHAT_MESSAGE_VOICE_MAX_MS
        vVoiceRecorder.onRecordingProgress = { vVoiceLabel.text = ToolsText.toTime(it) }
        vVoiceRecorder.onRecordingStart = {
            stopMyVoice()
            vVoiceLabel.text = ToolsText.toTime(0)
            isRecording = true
            updateAction()
            if (ControllerSettings.voiceMessagesAutoLock) vVoiceRecorder.lock()
        }
        vVoiceRecorder.onRecordingStop = {
            voiceBytes = it
            isRecording = false
            updateAction()
            if (voiceBytes != null && ControllerSettings.voiceMessagesAutoSend) sendVoice()
        }

        vVoiceRemove.setOnClickListener {
            stopMyVoice()
            voiceBytes = null
            updateAction()
        }
        vVoicePlay.setOnClickListener {
            if (utilsAudioPlayer.isPlaying()) stopMyVoice()
            else startMyVoice()
        }

        updateMedieEditText()
        onTextChanged()
    }

    private fun stopMyVoice() {
        utilsAudioPlayer.stop()
        vVoicePlay.setImageDrawable(ToolsResources.getDrawableAttr(R.attr.ic_play_arrow_24dp))
    }

    private fun startMyVoice() {
        utilsAudioPlayer.play(voiceBytes!!) {
            vVoicePlay.setImageDrawable(ToolsResources.getDrawableAttr(R.attr.ic_play_arrow_24dp))
        }
        vVoicePlay.setImageDrawable(ToolsResources.getDrawableAttr(R.attr.ic_pause_24dp))
    }

    fun setQuote(unit: PublicationChatMessage) {
        var text = unit.creatorName + ": "
        if (unit.text.isNotEmpty()) text += unit.text
        else if (unit.resourceId != 0L || unit.imageIdArray.isNotEmpty()) text += ToolsResources.s(R.string.app_image)
        else if (unit.stickerId != 0L) text += ToolsResources.s(R.string.app_sticker)
        setQuote(text, unit.id)
    }

    fun setQuote(quoteText: String, quoteId: Long = 0) {
        this.quoteText = quoteText
        if (this.quoteText.length > API.CHAT_MESSAGE_QUOTE_MAX_SIZE) this.quoteText = this.quoteText.substring(0, API.CHAT_MESSAGE_QUOTE_MAX_SIZE) + "..."
        this.quoteId = quoteId
        vQuoteContainer.visibility = if (this.quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = this.quoteText
        ControllerApi.makeTextHtml(vQuoteText)
        updateAction()
    }

    fun setAnswer(unitAnswer: PublicationChatMessage, withName: Boolean): Boolean {
        setChange(null)
        if (ControllerApi.isCurrentAccount(unitAnswer.creatorId)) return false
        var text = vText.text!!.toString()
        if (this.unitAnswer != null && text.startsWith(this.unitAnswer!!.creatorName + ", ")) {
            text = text.substring((this.unitAnswer!!.creatorName + ", ").length)
        }
        this.unitAnswer = unitAnswer
        if (withName) vText.setText(unitAnswer.creatorName + ", " + text)
        vText.setSelection(vText.text!!.length)
        ToolsView.showKeyboard(vText)
        return true
    }

    private fun onTextChanged() {
        sendTyping()
        updateAction()
    }

    private fun updateAction() {
        vSend.visibility = if (vText.text.toString().isNotEmpty() || attach.isHasContent() || quoteId != 0L || unitChange != null || voiceBytes != null) View.VISIBLE else View.GONE
        vVoiceRecorder.visibility = if (vText.text.toString().isNotEmpty() || attach.isHasContent() || quoteId != 0L || unitChange != null || voiceBytes != null) View.GONE else View.VISIBLE

        if (isRecording || voiceBytes != null) vFieldContainer.visibility = View.GONE
        else vFieldContainer.visibility = View.VISIBLE

        vVoiceContainer.visibility = if (vFieldContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        vVoicePlay.visibility = if (voiceBytes == null) View.INVISIBLE else View.VISIBLE
        vVoiceRemove.visibility = if (voiceBytes == null) View.INVISIBLE else View.VISIBLE
    }

    //
    //  Send
    //

    private fun getText() = vText.text!!.toString().trim { it <= ' ' }

    private fun getParentId(): Long {
        if (quoteId != 0L) return quoteId
        if (unitAnswer != null && getText().startsWith(unitAnswer!!.creatorName + ", ")) return unitAnswer!!.id
        return 0L

    }

    private fun onSendClicked() {

        if (voiceBytes != null) {
            sendVoice()
            return
        }

        val text = getText()
        val parentId = getParentId()

        if (text.isEmpty() && !attach.isHasContent()) return

        if (unitChange == null) {
            if (attach.isHasContent()) sendImage(text, parentId)
            else if (ToolsText.isWebLink(text)) sendLink(text, parentId, true)
            else sendText(text, parentId)
        } else sendChange(text)
    }

    private fun beforeSend() {
        voiceBytes = null
        setQuote("")
        attach.clear()
        setChange(null)
        setText("")
        updateAction()
    }

    fun setText(text: String) {
        vText.setText(text)
    }

    fun setChange(unitChange: PublicationChatMessage?) {
        if (this.unitChange != null && unitChange == null) vText.setText(null)
        this.unitChange = unitChange

        updateMedieEditText()

        vSend.setImageResource(ToolsResources.getDrawableAttrId(if (unitChange == null) R.attr.ic_send_24dp else R.attr.ic_done_24dp))
        vAttach.visibility = if (unitChange == null) View.VISIBLE else View.GONE
        if (unitChange != null) {
            vText.setText(unitChange.text)
            vText.setSelection(vText.text!!.length)
            ToolsView.showKeyboard(vText)
            setQuote(unitChange.quoteText, unitChange.quoteId)
        }
    }

    fun updateMedieEditText() {
        if (unitChange == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        else vText.setCallback(null)
    }

    fun sendVoice() {
        val quoteIdV = quoteId
        val voiceBytes = this.voiceBytes
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.tag, "", null, null, voiceBytes, 0L, quoteIdV, 0)))
    }

    private fun sendText(text: String, parentId: Long) {
        val quoteIdV = quoteId
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.tag, text, null, null, null, parentId, quoteIdV, 0)))
    }

    private fun sendChange(text: String) {
        val quoteIdV = quoteId
        val quoteTextV = quoteText
        val unitChangeId = unitChange!!.id
        beforeSend()
        ToolsToast.show(R.string.app_changed)
        ApiRequestsSupporter.execute(RChatMessageChange(unitChangeId, quoteIdV, text)) {
            EventBus.post(EventChatMessageChanged(unitChangeId, text, quoteIdV, quoteTextV))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(R.string.error_chat_access) }

    }

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

    private fun sendImage(text: String, parentId: Long) {
        ToolsThreads.thread {
            val bytes = attach.getBytes()
            val gif = if (bytes.size == 1 && ToolsBytes.isGif(bytes[0])) bytes[0] else null
            if (gif != null) {
                val bt = ToolsBitmap.decode(bytes[0])
                if (bt == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                val byt = ToolsBitmap.toBytes(bt, API.CHAT_MESSAGE_IMAGE_WEIGHT)
                if (byt == null) {
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                bytes[0] = byt
            }
            ToolsThreads.main {
                val quoteIdV = quoteId
                beforeSend()
                screen.addCard(CardSending(screen, RChatMessageCreate(screen.tag, text, bytes, gif, null, parentId, quoteIdV, 0)))
            }
        }
    }

    private fun sendSticker(sticker: PublicationSticker) {
        val quoteIdV = quoteId
        val parentId = getParentId()
        beforeSend()
        screen.addCard(CardSending(screen, RChatMessageCreate(screen.tag, "", null, null, null, parentId, quoteIdV, sticker.id)))
    }

    private fun sendTyping() {
        if (lastTypingSent > System.currentTimeMillis() - 5000) return
        val t = vText.text
        if (t == null || t.isEmpty()) return
        lastTypingSent = System.currentTimeMillis()
        RChatTyping(screen.tag).send(api)
    }


}