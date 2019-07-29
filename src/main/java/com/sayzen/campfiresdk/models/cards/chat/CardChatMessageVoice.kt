package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerVoiceMessages
import com.sayzen.campfiresdk.controllers.apiMedia
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewSoundLine
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardChatMessageVoice(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardChatMessage(unit, onClick, onChange, onQuote, onGoTo) {

    val eventBus = EventBus
            .subscribe(EventVoiceMessageStateChanged::class) { update() }
            .subscribe(EventVoiceMessageStep::class) { if (it.id == unit.voiceResourceId) updatePlayTime() }

    init {
        changeEnabled = false
    }

    override fun getLayout() = R.layout.card_chat_message_voice

    override fun bindView(view: View) {
        super.bindView(view)

        val vTimeLabel: TextView = view.findViewById(R.id.vTimeLabel)
        val vPlay: ViewIcon = view.findViewById(R.id.vPlay)
        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)

        for (i in 0 until unit.voiceMask.size) log("i[$i] " + unit.voiceMask[i])
        vSoundLine.setSoundMask(unit.voiceMask)

        vTimeLabel.text = ToolsText.toTime(unit.voiceMs)

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

        val vSoundLine: ViewSoundLine = view.findViewById(R.id.vSoundLine)
        vSoundLine.setProgress(ControllerVoiceMessages.getPlayTimeMs(unit.voiceResourceId).toFloat(), unit.voiceMs.toFloat())
    }


}