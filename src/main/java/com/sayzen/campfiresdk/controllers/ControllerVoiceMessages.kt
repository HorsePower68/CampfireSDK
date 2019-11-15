package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerVoiceMessages {

    enum class State {
        NONE, LOADING, PLAY, PAUSE
    }

    private val utilsAudioPlayer = UtilsAudioPlayer()
    private var currentId = 0L
    private var state = State.NONE
    private var playTimeMs = 0L

    init {
        utilsAudioPlayer.useProximity = true
        Navigator.addOnScreenChanged {
            stop(currentId)
            false
        }
    }

    fun play(id: Long) {
        if (currentId == id) {
            if (isPause(currentId)) resume(id)
            return
        }

        if (currentId != 0L) {
            stop(currentId)
        }

        playTimeMs = 0L
        currentId = id

        setState(id, State.LOADING)
        RResourcesGet(id)
                .onComplete {
                    startPlay(id, it.bytes)
                }
                .onError {
                    stop(id)
                    ToolsToast.show(R.string.error_unknown)
                }
                .send(apiMedia)
    }

    fun stop(id: Long) {
        if (currentId != id) return
        setState(id, State.NONE)
        utilsAudioPlayer.stop()
    }

    private fun startPlay(id: Long, bytes: ByteArray) {
        if (currentId != id) return
        setState(id, State.PLAY)
        playTimeMs = 0L
        utilsAudioPlayer.onStep = {
            if (currentId == id) playTimeMs = it
            EventBus.post(EventVoiceMessageStep(currentId))
        }
        utilsAudioPlayer.play(bytes) {
            setState(id, State.NONE)
            if (currentId == id) currentId = 0L
        }
    }

    fun pause(id: Long) {
        if (currentId != id) return

        setState(id, State.PAUSE)
        utilsAudioPlayer.pause()
    }

    fun resume(id: Long) {
        if (currentId != id) return

        setState(id, State.PLAY)
        utilsAudioPlayer.resume()
    }

    private fun setState(id: Long, state: State) {
        if (currentId != id) return
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

    fun getPlayTimeMs(id: Long): Long {
        if (currentId != id) return 0L
        return playTimeMs
    }

}