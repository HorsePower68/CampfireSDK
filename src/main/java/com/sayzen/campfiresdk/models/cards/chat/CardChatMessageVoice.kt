package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerVoiceMessages
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewSoundLine
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardChatMessageVoice(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_voice, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    val eventBus = EventBus
            .subscribe(EventVoiceMessageStateChanged::class) { update() }
            .subscribe(EventVoiceMessageStep::class) { if (it.id == publication.voiceResourceId) updatePlayTime() }

    init {
        changeEnabled = false
        quoteEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationChatMessage

        val vPlay: ViewIcon = view.findViewById(R.id.vPlay)
        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)

        vSoundLine.setSoundMask(publication.voiceMask)


        if (ControllerVoiceMessages.isLoading(publication.voiceResourceId)) {
            vPlay.isEnabled = false
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        } else if (ControllerVoiceMessages.isPlay(publication.voiceResourceId)) {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }

        vPlay.setOnClickListener {
            if (ControllerVoiceMessages.isPlay(publication.voiceResourceId))
                ControllerVoiceMessages.pause(publication.voiceResourceId)
            else
                ControllerVoiceMessages.play(publication.voiceResourceId)
        }

        updatePlayTime()

    }

    private fun updatePlayTime() {
        val view = getView()
        if (view == null) return
        val publication = xPublication.publication as PublicationChatMessage

        val vTimeLabel: TextView = view.findViewById(R.id.vTimeLabel)
        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)

        val time = ControllerVoiceMessages.getPlayTimeMs(publication.voiceResourceId)
        if (time < publication.voiceMs && ControllerVoiceMessages.isPlay(publication.voiceResourceId) || ControllerVoiceMessages.isPause(publication.voiceResourceId)) {
            vTimeLabel.text = ToolsText.toTime(publication.voiceMs - time)
            vSoundLine.setProgress(time.toFloat(), publication.voiceMs.toFloat())
        } else {
            vTimeLabel.text = ToolsText.toTime(publication.voiceMs)
            vSoundLine.setProgress(0f, publication.voiceMs.toFloat())
        }
    }


}