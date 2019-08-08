package com.sayzen.campfiresdk.screens.chat

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.requests.chat.RChatMessageChange
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.dzen.campfire.api.requests.chat.RChatTyping
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
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

    val attach = Attach(vAttach, vAttachRecycler, { updateAction() }, {})
    private val utilsAudioPlayer = UtilsAudioPlayer()

    private var lastTypingSent = 0L
    private var isRecording = false
    private var unitAnswer: UnitChatMessage? = null
    var unitChange: UnitChatMessage? = null
    private var quoteText = ""
    private var quoteId = 0L
    private var voiceBytes: ByteArray? = null

    init {
        vVoiceContainer.visibility = View.GONE
        vQuoteContainer.visibility = View.GONE
        vQuoteRemove.setOnClickListener { setQuote("") }
        vSend.setOnClickListener { v -> onSendClicked() }
        vText.addTextChangedListener(TextWatcherChanged { onTextChanged() })

        vVoiceRecorder.maxRecordingTimeMs = API.CHAT_MESSAGE_VOICE_MAX_MS
        vVoiceRecorder.onRecordingProgress = { vVoiceLabel.text = ToolsText.toTime(it) }
        vVoiceRecorder.onRecordingStart = {
            vVoiceLabel.text = ToolsText.toTime(0)
            isRecording = true
            updateAction()
            if(ControllerSettings.voiceMessagesAutoLock) vVoiceRecorder.lock()
        }
        vVoiceRecorder.onRecordingStop = {
            voiceBytes = it
            isRecording = false
            updateAction()
            if(voiceBytes != null && ControllerSettings.voiceMessagesAutoSend) sendVoice()
        }

        vVoiceRemove.setOnClickListener {
            voiceBytes = null
            updateAction()
        }
        vVoicePlay.setOnClickListener {
            utilsAudioPlayer.stop()
            utilsAudioPlayer.play(voiceBytes!!)
        }

        updateMedieEditText()
        onTextChanged()
    }


    fun setQuote(quoteText: String, quoteId: Long = 0) {
        this.quoteText = quoteText
        this.quoteId = quoteId
        vQuoteContainer.visibility = if (quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = quoteText
        ControllerApi.makeTextHtml(vQuoteText)
        updateAction()
    }

    fun setAnswer(unitAnswer: UnitChatMessage): Boolean {
        setChange(null)
        if (ControllerApi.isCurrentAccount(unitAnswer.creatorId)) return false
        var text = vText.text!!.toString()
        if (this.unitAnswer != null && text.startsWith(this.unitAnswer!!.creatorName + ", ")) {
            text = text.substring((this.unitAnswer!!.creatorName + ", ").length)
        }
        this.unitAnswer = unitAnswer
        vText.setText(unitAnswer.creatorName + ", " + text)
        vText.setSelection(vText.text!!.length)
        ToolsView.showKeyboard(vText)
        return true
    }

    fun setLock(b: Boolean) {
        vAttach.isEnabled = !b
        vText.isEnabled = !b
        vSend.isEnabled = !b
        vQuoteRemove.isEnabled = !b
        vQuoteText.isEnabled = !b
        vVoicePlay.isEnabled = !b
        vVoiceRemove.isEnabled = !b
        vVoiceLabel.isEnabled = !b
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
        var parentId: Long = 0
        if (unitAnswer != null && getText().startsWith(unitAnswer!!.creatorName + ", "))
            parentId = unitAnswer!!.id
        return parentId

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

    private fun afterSend(message: UnitChatMessage) {
        clearInput()
        EventBus.post(EventUpdateChats())
        screen.addMessage(message, true)
        updateAction()
    }

    private fun clearInput() {
        setQuote("")
        attach.clear()
        setChange(null)
        setText("")
    }

    fun setText(text: String) {
        vText.setText(text)
    }

    fun setChange(unitChange: UnitChatMessage?) {
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
        setLock(true)
        ApiRequestsSupporter.execute(RChatMessageCreate(screen.tag, "", null, null, voiceBytes, 0L, quoteId)) { r ->
            voiceBytes = null
            afterSend(r.message)
        }
                .onApiError(RChatMessageCreate.E_BLACK_LIST) { ToolsToast.show(R.string.error_black_list) }
                .onApiError(RChatMessageCreate.E_IS_IGNORE_VOICE_MESSAGES) { ToolsToast.show(R.string.error_ignore_voice_messages) }
                .onFinish { setLock(false) }
    }

    private fun sendText(text: String, parentId: Long) {
        setLock(true)
        ApiRequestsSupporter.execute(RChatMessageCreate(screen.tag, text, null, null, null, parentId, quoteId)) { r ->
            afterSend(r.message)
        }
                .onApiError(RChatMessageCreate.E_BLACK_LIST) {
                    ToolsToast.show(R.string.error_black_list)
                }
                .onFinish { setLock(false) }
    }

    private fun sendChange(text: String) {
        val unitChangeId = unitChange!!.id
        ApiRequestsSupporter.executeEnabledCallback(RChatMessageChange(unitChangeId, quoteId, text), { r ->
            ToolsToast.show(R.string.app_changed)
            EventBus.post(EventChatMessageChanged(unitChangeId, text, quoteId, quoteText))
            clearInput()
        }, { enabled -> setLock(!enabled) })

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
        setLock(true)
        ToolsThreads.thread {
            val bytes = attach.getBytes()
            val gif = if (bytes.size == 1 && ToolsBytes.isGif(bytes[0])) bytes[0] else null
            if (gif != null) {
                val bt = ToolsBitmap.decode(bytes[0])
                if (bt == null) {
                    setLock(false)
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                val byt = ToolsBitmap.toBytes(bt, API.CHAT_MESSAGE_IMAGE_WEIGHT)
                if (byt == null) {
                    setLock(false)
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@thread
                }
                bytes[0] = byt
            }
            ApiRequestsSupporter.executeProgressDialog(RChatMessageCreate(screen.tag, text, bytes, gif, null, parentId, quoteId)) { r ->
                afterSend(r.message)
                setLock(false)
            }
                    .onApiError(RChatMessageCreate.E_BLACK_LIST) { ToolsToast.show(R.string.error_black_list) }
                    .onFinish { setLock(false) }
        }
    }

    private fun sendTyping() {
        if (lastTypingSent > System.currentTimeMillis() - 5000) return
        val t = vText.text
        if (t == null || t.isEmpty()) return
        lastTypingSent = System.currentTimeMillis()
        RChatTyping(screen.tag).send(api)
    }


}