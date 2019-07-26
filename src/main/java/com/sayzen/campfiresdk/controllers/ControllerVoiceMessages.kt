package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerVoiceMessages {

    private val unitsAudioPlayer = UtilsAudioPlayer()
    private var currentId = 0L
    private var isLoading = false
    private var isPlay = false
    private var isPause = false

    fun play(id: Long) {
        if (currentId == id) {
            if(isPause) resume(id)
            return
        }
        currentId = id

        unitsAudioPlayer.stop()
        isLoading = true
        isPlay = false
        isPause = false
        EventBus.post(EventVoiceMessageStateChanged())
        RResourcesGet(id)
                .onComplete {
                    isLoading = false
                    isPlay = true
                    isPause = false
                    EventBus.post(EventVoiceMessageStateChanged())
                    unitsAudioPlayer.play(it.bytes) {
                        currentId = 0L
                        isLoading = false
                        isPlay = false
                        isPause = false
                        EventBus.post(EventVoiceMessageStateChanged())
                    }
                }
                .onError { ToolsToast.show(R.string.error_unknown) }
                .send(apiMedia)
    }

    fun pause(id: Long) {
        if (currentId != id) return

        isLoading = false
        isPlay = false
        isPause = true
        EventBus.post(EventVoiceMessageStateChanged())
    }

    fun resume(id: Long) {
        if (currentId != id) return

        isLoading = false
        isPlay = true
        isPause = false
        EventBus.post(EventVoiceMessageStateChanged())
    }

    fun isPlay(id: Long): Boolean {
        if (currentId != id) return false
        return isPlay
    }

    fun isPause(id: Long): Boolean {
        if (currentId != id) return false
        return isPause
    }

    fun isLoading(id: Long): Boolean {
        if (currentId != id) return false
        return isLoading
    }

}