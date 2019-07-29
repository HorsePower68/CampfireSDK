package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerVoiceMessages {

    enum class State{
        NONE, LOADING, PLAY, PAUSE
    }

    private val unitsAudioPlayer = UtilsAudioPlayer()
    private var currentId = 0L
    private var state = State.NONE
    private var playTimeMs = 0L

    init {
        unitsAudioPlayer.onStep = {
            playTimeMs = it
            EventBus.post(EventVoiceMessageStep(currentId))
        }
    }

    fun play(id: Long) {
        if (currentId == id) {
            if(isPause(currentId)) resume(id)
            return
        }

        playTimeMs = 0L
        currentId = id

        unitsAudioPlayer.stop()
        setState(State.LOADING)
        RResourcesGet(id)
                .onComplete { startPlay(it.bytes) }
                .onError {
                    setState(State.NONE)
                    ToolsToast.show(R.string.error_unknown)
                }
                .send(apiMedia)
    }

    private fun startPlay(bytes:ByteArray){
        setState(State.PLAY)
        playTimeMs = 0L
        unitsAudioPlayer.play(bytes) {
            currentId = 0L
            setState(State.NONE)
        }
    }

    fun pause(id: Long) {
        if (currentId != id) return

        setState(State.PAUSE)
        unitsAudioPlayer.pause()
    }

    fun resume(id: Long) {
        if (currentId != id) return

        setState(State.PLAY)
        unitsAudioPlayer.resume()
    }

    private fun setState(state:State){
        this.state = state
        EventBus.post(EventVoiceMessageStateChanged())
    }

    fun isPlay(id: Long): Boolean {
        if (currentId != id) return false
        return state == State.PLAY
    }

    fun isPause(id: Long): Boolean {
        if (currentId != id) return false
        return state == State.PAUSE
    }

    fun isLoading(id: Long): Boolean {
        if (currentId != id) return false
        return state == State.LOADING
    }

    fun getPlayTimeMs(id:Long):Long{
        if (currentId != id) return 0L
        return playTimeMs
    }

}