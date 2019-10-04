package com.sayzen.campfiresdk.screens.chat

import android.view.View
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.requests.chat.RChatMessageCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.chat.EventChatMemberStatusChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class CardSending(
        val screen: SChat,
        val request: RChatMessageCreate
) : Card(R.layout.screen_chat_card_sending) {

    private var sending = false
    private var sendingDone = false
    private var hided = true

    init {
        send()
        ToolsThreads.main(1000) {
            hided = false
            update()
        }
    }

    private fun send() {
        sending = true
        ApiRequestsSupporter.execute(request) { r ->
            sendingDone = true
            afterSend(r.message)
            EventBus.post(EventChatMemberStatusChanged(screen.tag, ControllerApi.account.id, API.CHAT_MEMBER_STATUS_ACTIVE))
        }
                .onApiError(RChatMessageCreate.E_BLACK_LIST) {
                    ToolsToast.show(R.string.error_black_list)
                    adapter?.remove(this)
                }
                .onApiError(RChatMessageCreate.E_IS_IGNORE_VOICE_MESSAGES) {
                    ToolsToast.show(R.string.error_ignore_voice_messages)
                    adapter?.remove(this)
                }
                .onApiError(API.ERROR_ACCESS) {
                    ToolsToast.show(R.string.error_chat_access)
                    adapter?.remove(this)
                }
                .onFinish {
                    sending = false
                    ToolsThreads.main(true) { update() }
                }
    }


    private fun afterSend(message: UnitChatMessage) {
        EventBus.post(EventUpdateChats())
        screen.addMessage(message, true, this)
    }


    override fun bindView(view: View) {
        super.bindView(view)

        view.visibility = if (hided) View.GONE else View.VISIBLE

        val vRetry: Button = view.findViewById(R.id.vRetry)
        val vProgress: View = view.findViewById(R.id.vProgress)

        vRetry.visibility = if (sending || sendingDone) View.GONE else View.VISIBLE
        vProgress.visibility = if (sending && !sendingDone) View.VISIBLE else View.GONE
        vRetry.setOnClickListener {
            vProgress.visibility = View.GONE
            send()
            update()
        }
    }

}