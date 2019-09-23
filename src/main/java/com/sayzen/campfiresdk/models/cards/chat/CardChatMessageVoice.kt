package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerVoiceMessages
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewSoundLine
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardChatMessageVoice(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_voice, unit, onClick, onChange, onQuote, onGoTo, onBlocked) {

    val eventBus = EventBus
            .subscribe(EventVoiceMessageStateChanged::class) { update() }
            .subscribe(EventVoiceMessageStep::class) { if (it.id == unit.voiceResourceId) updatePlayTime() }

    init {
        changeEnabled = false
        quoteEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitChatMessage

        val vPlay: ViewIcon = view.findViewById(R.id.vPlay)
        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)

        vSoundLine.setSoundMask(unit.voiceMask)


        if (ControllerVoiceMessages.isLoading(unit.voiceResourceId)) {
            vPlay.isEnabled = false
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        } else if (ControllerVoiceMessages.isPlay(unit.voiceResourceId)) {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }

        vPlay.setOnClickListener {
            if (ControllerVoiceMessages.isPlay(unit.voiceResourceId))
                ControllerVoiceMessages.pause(unit.voiceResourceId)
            else
                ControllerVoiceMessages.play(unit.voiceResourceId)
        }

        updatePlayTime()

    }

    private fun updatePlayTime() {
        val view = getView()
        if (view == null) return
        val unit = xUnit.unit as UnitChatMessage

        val vTimeLabel: TextView = view.findViewById(R.id.vTimeLabel)
        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)

        val time = ControllerVoiceMessages.getPlayTimeMs(unit.voiceResourceId)
        if (time < unit.voiceMs && ControllerVoiceMessages.isPlay(unit.voiceResourceId) || ControllerVoiceMessages.isPause(unit.voiceResourceId)) {
            vTimeLabel.text = ToolsText.toTime(unit.voiceMs - time)
            vSoundLine.setProgress(time.toFloat(), unit.voiceMs.toFloat())
        } else {
            vTimeLabel.text = ToolsText.toTime(unit.voiceMs)
            vSoundLine.setProgress(0f, unit.voiceMs.toFloat())
        }
    }


}