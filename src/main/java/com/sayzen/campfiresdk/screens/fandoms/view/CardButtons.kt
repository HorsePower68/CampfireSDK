package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemoveModerator
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.forums.SForums
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.SModeration
import com.sayzen.campfiresdk.screens.fandoms.rating.SRating
import com.sayzen.campfiresdk.screens.fandoms.subscribers.SSubscribers
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.chat.EventChatRead
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sayzen.campfiresdk.screens.wiki.SWikiList
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardButtons(
        private val xFandom: XFandom,
        private var newChatMessagesCount: Long,
        private val chatOnlineCount: Long,
        private val forumsCount: Long,
        private val tagsCount: Long,
        private var subscribersCount: Long,
        private var modersCount: Long,
        private var subscribed: Boolean
) : Card(R.layout.screen_fandom_card_buttons) {

    private val eventBus = EventBus
            .subscribe(EventChatRead::class) { this.onChatRead(it) }
            .subscribe(EventFandomSubscribe::class) { onEventFandomSubscribe(it) }
            .subscribe(EventFandomRemoveModerator::class) { onEventFandomRemoveModerator(it)}

    override fun bindView(view: View) {
        super.bindView(view)
        val vChatsButton: View = view.findViewById(R.id.vChatsButton)
        val vChatsCount: TextView = view.findViewById(R.id.vChatsCount)
        val vRatingsButton: View = view.findViewById(R.id.vRatingsButton)
        val vRatingsText: TextView = view.findViewById(R.id.vRatingsText)
        val vTagsButton: View = view.findViewById(R.id.vTagsButton)
        val vTagsText: TextView = view.findViewById(R.id.vTagsText)
        val vModerationButton: View = view.findViewById(R.id.vModerationButton)
        val vModerationText: TextView = view.findViewById(R.id.vModerationText)
        val vForumsButton: View = view.findViewById(R.id.vForumsButton)
        val vForumsText: TextView = view.findViewById(R.id.vForumsText)
        val vSubscribersButton: View = view.findViewById(R.id.vSubscribersButton)
        val vSubscribersText: TextView = view.findViewById(R.id.vSubscribersText)
        val vWikiButton: View = view.findViewById(R.id.vWikiButton)

        vChatsButton.setOnClickListener { SChat.instance(API.CHAT_TYPE_FANDOM, xFandom.fandomId, xFandom.languageId, false, Navigator.REORDER) }
        vChatsCount.text = "$chatOnlineCount"

        vWikiButton.visibility = View.GONE

        val karma30 = ControllerApi.getKarmaCount(xFandom.fandomId, xFandom.languageId)/100
        vRatingsButton.setOnClickListener { SRating.instance(xFandom.fandomId, xFandom.languageId, Navigator.TO) }
        vRatingsText.text = "$karma30"
        vRatingsText.setTextColor(ToolsResources.getColor(if (karma30 > -1) R.color.green_700 else R.color.red_700))

        vTagsButton.setOnClickListener { STags.instance(xFandom.fandomId, xFandom.languageId, Navigator.TO) }
        vTagsText.text = "$tagsCount"

        vModerationButton.setOnClickListener { Navigator.to(SModeration(xFandom.fandomId, xFandom.languageId)) }
        vModerationText.text = "$modersCount"

        vForumsButton.setOnClickListener { Navigator.to(SForums(xFandom.fandomId, xFandom.languageId)) }
        vForumsText.text = "$forumsCount"

        vSubscribersButton.setOnClickListener { SSubscribers.instance(xFandom.fandomId, xFandom.languageId, Navigator.TO) }
        vSubscribersText.text = "$subscribersCount"

        vWikiButton.setOnClickListener { Navigator.to(SWikiList(xFandom.fandomId, 0, "")) }
    }

    //
    //  EventBus
    //

    private fun onChatRead(e: EventChatRead) {
        if (e.tag.chatType == API.CHAT_TYPE_FANDOM && e.tag.targetId == xFandom.fandomId && e.tag.targetSubId == xFandom.languageId) {
            newChatMessagesCount = 0
            update()
        }
    }

    private fun onEventFandomRemoveModerator(e: EventFandomRemoveModerator) {
        if(e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId){
            modersCount--
            update()
        }
    }

    private fun onEventFandomSubscribe(e: EventFandomSubscribe) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId) {
            if(subscribed && e.subscriptionType == API.UNIT_IMPORTANT_NONE){
                subscribed = false
                subscribersCount--
                update()
                return
            }
            if(!subscribed && e.subscriptionType != API.UNIT_IMPORTANT_NONE){
                subscribed = true
                subscribersCount++
                update()
                return
            }
        }
    }

}
