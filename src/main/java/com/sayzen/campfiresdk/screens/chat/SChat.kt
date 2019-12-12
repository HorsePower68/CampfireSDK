package com.sayzen.campfiresdk.screens.chat


import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.chat.CardChatMessage
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChangedModeration
import com.sayzen.campfiresdk.models.ScreenShare
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.api_simple.client.ApiClient
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.*

class SChat private constructor(
        val tag: ChatTag,
        var subscribed: Boolean,
        var chatName: String,
        var chatParams: Json,
        var chatImageId: Long,
        var chatBackgroundImageId: Long,
        var chatInfo_1: Long,
        val chatInfo_2: Long,
        val chatInfo_3: Long,
        val chatInfo_4: Long,
        var scrollToMessageId: Long,
        var memberStatus: Long?)
    : SLoadingRecycler<CardChatMessage, PublicationChatMessage>(R.layout.screen_chat), ScreenShare {

    companion object {

        fun instance(messageId: Long, setStack: Boolean, action: NavigationAction) {
            if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            ApiRequestsSupporter.executeInterstitial(action, RChatGet(ChatTag(), messageId)) { r ->
                for (i in ToolsCollections.copy(Navigator.currentStack.stack)) if (i is SChat && i.tag == r.tag) Navigator.remove(i)
                onChatLoaded(r, messageId, {})
            }
        }

        fun instance(tag: ChatTag, scrollToMessageId: Long, setStack: Boolean, action: NavigationAction, onShow: (SChat) -> Unit = {}) {
            tag.targetSubId = if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT || tag.targetSubId != 0L) tag.targetSubId else ControllerApi.getLanguageId()

            if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            if (tryOpenFromBackStack(tag, scrollToMessageId)) return

            ApiRequestsSupporter.executeInterstitial(action, RChatGet(tag, 0)) { r ->
                onChatLoaded(r, scrollToMessageId, onShow)
            }
        }

        private fun tryOpenFromBackStack(tag: ChatTag, messageId: Long): Boolean {
            for (i in Navigator.currentStack.stack) {
                if (i is SChat && i.tag == tag) {
                    Navigator.reorder(i)
                    if (messageId > 0) i.scrollTo(messageId)
                    return true
                }
            }
            return false
        }

        private fun onChatLoaded(r: RChatGet.Response, messageId: Long, onShow: (SChat) -> Unit): SChat {
            ControllerChats.putRead(r.tag, r.anotherReadDate)
            val screen = SChat(r.tag, r.subscribed, r.chatName, r.chatParams, r.chatImageId, r.chatBackgroundImageId, r.chatInfo_1, r.chatInfo_2, r.chatInfo_3, r.chatInfo_4, messageId, r.memberStatus)
            onShow.invoke(screen)
            return screen
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.eventOnChatTypingChanged(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventFandomBackgroundImageChangedModeration::class) { this.onEventFandomBackgroundImageChangedModeration(it) }
            .subscribe(EventFandomBackgroundImageChanged::class) { this.onEventFandomBackgroundImageChanged(it) }
            .subscribe(EventChatChanged::class) { this.onEventChatChanged(it) }
            .subscribe(EventChatMemberStatusChanged::class) { this.onEventChatMemberStatusChanged(it) }

    private val vLine: View = findViewById(R.id.vLine)
    private val vMenu: ViewIcon
    private val vNotifications: ViewIcon
    private val vTypingText: TextView = findViewById(R.id.vTypingText)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vFandomBackground: ImageView = findViewById(R.id.vFandomBackground)

    private val fieldLogic = FieldLogic(this)

    private val carSpace = CardSpace(16)
    private var needUpdate = false
    private var loaded = false
    private var scrollToMessageWasLoaded = false
    private val addAfterLoadList = ArrayList<PublicationChatMessage>()

    init {
        isNavigationShadowAvailable = false
        SActivityTypeBottomNavigation.setShadow(vLine)

        vAvatarTitle.setTitleColor(ToolsResources.getColorAttr(R.attr.toolbar_content_color))
        vAvatarTitle.setSubtitleColor(ToolsResources.getColorAttr(R.attr.toolbar_content_color))

        vNotifications = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_notifications_24dp)) { sendSubscribe(!subscribed) }

        vNotifications.visibility = View.GONE
        if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) vNotifications.visibility = View.VISIBLE
        if (tag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == 1L) vNotifications.visibility = View.VISIBLE

        if (tag.chatType == API.CHAT_TYPE_CONFERENCE) vAvatarTitle.setOnClickListener { SChatCreate.instance(tag.targetId, Navigator.TO) }

        vMenu = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_more_vert_24dp)) {
            ControllerChats.instanceChatPopup(tag, chatParams, chatImageId, memberStatus) { Navigator.remove(this) }.asSheetShow()
        }

        setBackgroundImage(R.drawable.bg_5)
        setTextEmpty(if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) R.string.chat_empty_fandom else R.string.chat_empty_private)
        setTextProgress(R.string.chat_loading)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        vRecycler.layoutManager = layoutManager

        updateSubscribed()
        update()
        updateTyping()
        updateBackground()

        if (tag.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            if (!ControllerSettings.viewedChats.contains(tag.targetId)) {
                ToolsThreads.main(100) { ControllerChats.showFandomChatInfo(tag, chatParams, chatImageId) }
            }
        }
    }

    private fun scrollTo(messageId: Long) {
        var found = false
        val cards = adapter!!.get(CardChatMessage::class)
        for (i in cards) {
            if (i.xPublication.publication.id == messageId) {
                ToolsView.scrollRecycler(vRecycler, adapter!!.indexOf(i) + 1)
                ToolsThreads.main(500) { i.flash() }
                found = true
                break
            }
        }
        if (!found) {
            scrollToMessageId = messageId
            if (messageId > cards.get(cards.size - 1).xPublication.publication.id) {
                adapter!!.loadBottom()
            } else {
                adapter!!.loadTop()
            }
        }
    }

    private fun update() {
        if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            val xFandom = XFandom(tag.targetId, tag.targetSubId, chatName, chatImageId) { update() }
            xFandom.setView(vAvatarTitle)
            vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            vAvatarTitle.setSubtitle(ToolsResources.s(R.string.app_subscribers) + ": $chatInfo_1")
            vAvatarTitle.setOnClickListener { Navigator.to(SChatSubscribers(tag.targetId, tag.targetSubId, chatName)) }
        } else if (tag.chatType == API.CHAT_TYPE_PRIVATE) {
            val anotherId = if (tag.targetId == ControllerApi.account.id) tag.targetSubId else tag.targetId
            val xAccount = XAccount(anotherId, chatName, chatImageId, chatInfo_1, chatInfo_4, chatInfo_2) { update() }
            xAccount.setView(vAvatarTitle)

            if (!xAccount.isOnline()) {
                vAvatarTitle.setSubtitle(ToolsResources.sCap(R.string.app_was_online, ToolsResources.sex(chatInfo_3, R.string.he_was, R.string.she_was), ToolsDate.dateToString(xAccount.getLastOnlineTime())))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            } else {
                vAvatarTitle.setSubtitle(ToolsResources.s(R.string.app_online))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.green_700))
            }
        } else {
            ToolsImagesLoader.load(chatImageId).into(vAvatarTitle.vAvatar.vImageView)
            vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            vAvatarTitle.setSubtitle(ToolsResources.s(R.string.app_subscribers) + ": $chatInfo_1")
            vAvatarTitle.setTitle(chatName)
        }
    }

    private fun updateSubscribed() {
        vNotifications.setFilter(if (subscribed) ToolsResources.getAccentColor(context) else ToolsResources.getColorAttr(R.attr.toolbar_content_color))
    }

    private fun updateBackground() {
        if (chatBackgroundImageId > 0 && ControllerSettings.fandomBackground) {
            vFandomBackground.visibility = View.VISIBLE
            ToolsImagesLoader.load(chatBackgroundImageId).holder(0x00000000).into(vFandomBackground)
            vFandomBackground.setColorFilter(ToolsColor.setAlpha(210, ToolsResources.getColorAttr(R.attr.window_background)))
        } else {
            vFandomBackground.setImageBitmap(null)
            vFandomBackground.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        ControllerNotifications.hide(ControllerNotifications.TYPE_CHAT)
        if (needUpdate) {
            needUpdate = false
            ControllerChats.readRequest(tag)
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardChatMessage, PublicationChatMessage> {
        val adapter = RecyclerCardAdapterLoading<CardChatMessage, PublicationChatMessage>(CardChatMessage::class) { u -> instanceCard(u) }
                .setBottomLoader { onLoad, cards ->
                    if (loaded) {
                        onLoad.invoke(emptyArray())
                    } else {
                        subscription = RChatMessageGetAll(tag,
                                if (cards.isEmpty()) 0 else cards[cards.size - 1].xPublication.publication.dateCreate,
                                false,
                                scrollToMessageId)
                                .onComplete { r ->

                                    if (scrollToMessageId > 0) {
                                        onLoad.invoke(r.publications)
                                    } else {
                                        if (loaded) {
                                            onLoad.invoke(emptyArray())
                                            return@onComplete
                                        }
                                        adapter!!.remove(carSpace)
                                        onLoad.invoke(r.publications)
                                        adapter!!.add(carSpace)
                                        if (r.publications.isNotEmpty()) EventBus.post(EventChatNewBottomMessage(r.publications[r.publications.size - 1]))
                                        ToolsThreads.main(true) {
                                            while (addAfterLoadList.isNotEmpty()) addMessage(addAfterLoadList.removeAt(0), true)
                                        }
                                        if (r.publications.isEmpty() || r.publications.size < RChatMessageGetAll.COUNT) {
                                            loaded = true
                                            adapter!!.setShowLoadingCardBottom(false)
                                            EventBus.post(EventChatRead(tag))
                                            adapter!!.lockBottom()
                                        }
                                    }
                                }
                                .onNetworkError { onLoad.invoke(null) }
                                .send(api)
                    }
                }
                .setTopLoader { onLoad, cards ->
                    subscription = RChatMessageGetAll(tag, if (cards.isEmpty()) 0 else cards[0].xPublication.publication.dateCreate, true, 0)
                            .onComplete { r -> onLoad.invoke(r.publications) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
        adapter.setRemoveSame(true)
        adapter.setShowLoadingCardBottom(true)
        adapter.setShowLoadingCardTop(true)
        adapter.addOnLoadedNotEmpty {
            if (scrollToMessageId != 0L) {
                for (c in adapter.get(CardChatMessage::class)) {
                    if (c.xPublication.publication.id == scrollToMessageId) {
                        scrollToMessageId = 0
                        ToolsView.scrollRecycler(vRecycler, adapter.indexOf(c) + 1)
                        ToolsThreads.main(500) { c.flash() }
                    }
                }
                if (scrollToMessageId != 0L) {
                    if (scrollToMessageWasLoaded) {
                        adapter.loadBottom()
                        scrollToMessageWasLoaded = false
                        scrollToMessageId = 0
                    } else {
                        RChatMessageGet(tag, scrollToMessageId)
                                .onComplete {
                                    adapter.loadBottom()
                                    scrollToMessageWasLoaded = true
                                }
                                .onApiError(ApiClient.ERROR_GONE) {
                                    if (it.messageError == RChatMessageGet.GONE_BLOCKED) ControllerApi.showBlockedDialog(it, R.string.chat_error_gone_block)
                                    else if (it.messageError == RChatMessageGet.GONE_REMOVE) WidgetAlert().setText(R.string.chat_error_gone_remove).setOnEnter(R.string.app_ok).asSheetShow()
                                    else WidgetAlert().setText(R.string.chat_error_gone).setOnEnter(R.string.app_ok).asSheetShow()
                                    scrollToMessageWasLoaded = false
                                    scrollToMessageId = 0
                                }
                                .send(api)
                    }
                }
            }
        }
        adapter.addOnLoadedEmptyPack {
            if (scrollToMessageId > 0 && scrollToMessageWasLoaded) {
                scrollToMessageId = 0
                scrollToMessageWasLoaded = false
                WidgetAlert().setText(R.string.chat_error_gone_remove).setOnEnter(R.string.app_ok).asSheetShow()
            }
        }
        return adapter
    }

    private fun instanceCard(u: PublicationChatMessage): CardChatMessage {
        return CardChatMessage.instance(u,
                onClick = { publication ->
                    if (ControllerApi.isCurrentAccount(publication.creatorId)) {
                        false
                    } else {
                        fieldLogic.setAnswer(publication, true)
                        true
                    }
                },
                onChange = { publication -> fieldLogic.setChange(publication) },
                onQuote = { publication ->
                    if (!ControllerApi.isCurrentAccount(publication.creatorId)) fieldLogic.setAnswer(publication, false)
                    fieldLogic.setQuote(publication)
                    ToolsView.showKeyboard(fieldLogic.vText)
                },
                onGoTo = { id ->
                    if (adapter == null) return@instance
                    for (i in adapter!!.get(CardChatMessage::class)) {
                        if (i.xPublication.publication.id == id) {
                            i.flash()
                            ToolsView.scrollRecycler(vRecycler, adapter!!.indexOf(i) + 1)
                            break
                        }
                    }
                },
                onBlocked = {
                    addMessage(it, false)
                }
        )
    }

    //
    //  Methods
    //

    private fun updateTyping() {
        val text = ControllerChats.getTypingText(tag)

        if (text == null) {
            vTypingText.text = ""
            vTypingText.visibility = View.GONE
            return
        }

        vTypingText.visibility = View.VISIBLE
        vTypingText.text = text
    }

    fun sendSubscribe(subscribed: Boolean) {
        if (tag.chatType != API.CHAT_TYPE_FANDOM_ROOT && tag.chatType != API.CHAT_TYPE_CONFERENCE) return
        ApiRequestsSupporter.executeProgressDialog(RChatSubscribe(tag, subscribed)) { _ ->
            EventBus.post(EventChatSubscriptionChanged(tag, subscribed))
        }
    }

    fun isNeedScrollAfterAdd(): Boolean {
        if (vRecycler.layoutManager !is LinearLayoutManager || adapter == null) return false
        return (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == vRecycler.adapter!!.itemCount - 1
    }


    override fun onBackPressed(): Boolean {
        if (fieldLogic.publicationChange != null) {
            fieldLogic.setChange(null)
            return true
        }
        return super.onBackPressed()
    }

    fun addCard(card: CardSending) {
        if (adapter != null) {
            adapter!!.remove(carSpace)
            adapter!!.add(card)
            adapter!!.add(carSpace)
            ToolsView.scrollRecycler(vRecycler, vRecycler.adapter!!.itemCount - 1)
        }
    }

    fun addMessage(message: PublicationChatMessage, forceScroll: Boolean, replaceCard: Card? = null) {
        if (!loaded) {
            addAfterLoadList.add(message)
            return
        }
        val b = isNeedScrollAfterAdd()
        if (adapter != null) {
            val card = instanceCard(message)
            if (replaceCard == null || !adapter!!.contains(replaceCard)) {
                if (!adapter!!.containsSame(card)) {
                    adapter!!.remove(carSpace)
                    adapter!!.add(card)
                    adapter!!.add(carSpace)
                }
            } else {
                adapter!!.replace(adapter!!.indexOf(replaceCard), card)
            }

            val index = adapter!!.indexOf(card)
            if (index > 0) {
                val cardX = adapter!!.get(index - 1)
                if (cardX is CardChatMessage) {
                    cardX.updateSameCrds()
                    ToolsThreads.main(2000) { cardX.updateSameCrds() }
                }
            }

            if (forceScroll)
                ToolsView.scrollRecycler(vRecycler, vRecycler.adapter!!.itemCount - 1)
            else if (b)
                ToolsView.scrollRecyclerSmooth(vRecycler, vRecycler.adapter!!.itemCount - 1)
        }
        setState(State.NONE)
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (tag == n.tag && !ControllerApi.isCurrentAccount(n.publicationChatMessage.creatorId)) {
                addMessage(n.publicationChatMessage, false)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) ControllerChats.readRequest(tag)
                else needUpdate = true


            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (tag == n.tag) {
                addMessage(n.publicationChatMessage, false)
                ControllerChats.readRequest(tag)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) ControllerChats.readRequest(tag)
                else needUpdate = true

            }
        }
    }

    private fun onEventChatSubscriptionChanged(e: EventChatSubscriptionChanged) {
        if (e.tag == tag) {
            this.subscribed = e.subscribed
            updateSubscribed()
        }
    }

    private fun onEventFandomBackgroundImageChangedModeration(e: EventFandomBackgroundImageChangedModeration) {
        if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            if (tag.targetId == e.fandomId && tag.targetSubId == e.languageId) {
                this.chatBackgroundImageId = e.imageId
                updateBackground()
            }
            if (tag.targetId == 0L && tag.targetSubId == 0L) {
                updateBackground()
            }
        }
    }

    private fun onEventFandomBackgroundImageChanged(e: EventFandomBackgroundImageChanged) {
        if (tag.chatType == API.CHAT_TYPE_FANDOM_SUB || tag.chatType == API.CHAT_TYPE_CONFERENCE) {
            if (tag.targetId == e.chatId) {
                this.chatBackgroundImageId = e.imageId
                updateBackground()
            }
            if (tag.targetId == 0L && tag.targetSubId == 0L) {
                updateBackground()
            }
        }
    }

    private fun onEventChatChanged(e: EventChatChanged) {
        if (tag.chatType == API.CHAT_TYPE_CONFERENCE && e.chatId == tag.targetId) {
            chatName = e.name
            chatImageId = e.imageId
            chatInfo_1 = e.accountCount.toLong()
            update()
        }
    }

    private fun onEventChatMemberStatusChanged(e: EventChatMemberStatusChanged) {
        if (tag.chatType == e.tag.chatType && e.tag.targetId == tag.targetId && e.tag.targetSubId == tag.targetSubId) {
            if (e.accountId == ControllerApi.account.id) memberStatus = e.status
            update()
        }
    }

    private fun eventOnChatTypingChanged(e: EventChatTypingChanged) {
        if (e.tag == tag) updateTyping()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBus.unsubscribe()
    }

    //
    //  Share
    //

    override fun addText(text: String, postAfterAdd: Boolean) {
        fieldLogic.setText(text)
    }

    override fun addImage(image: Uri, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            ToolsBitmap.getFromUri(image, {
                if (it == null) {
                    dialog.hide()
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@getFromUri
                }

                fieldLogic.attach.setImageBitmapNow(it, dialog)
            }, {
                dialog.hide()
                ToolsToast.show(R.string.error_cant_load_image)
            })
        }
    }

    override fun addImage(image: Bitmap, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        fieldLogic.attach.setImageBitmapNow(image, dialog)
    }


}
