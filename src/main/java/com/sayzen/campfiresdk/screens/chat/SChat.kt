package com.sayzen.campfiresdk.screens.chat


import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.ChatTag
import com.dzen.campfire.api.models.notifications.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.NotificationChatMessage
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.requests.chat.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.chat.CardChatMessage
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.support.Attach
import com.sayzen.campfiresdk.models.ScreenShare
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.activity.SActivityBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewEditTextMedia
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.*

class SChat private constructor(
        val tag: ChatTag,
        var subscribed: Boolean,
        val chatName: String,
        val chatImageId: Long,
        var chatBackgroundImageId: Long,
        val chatInfo_1: Long,
        val chatInfo_2: Long,
        val chatInfo_3: Long)
    : SLoadingRecycler<CardChatMessage, UnitChatMessage>(R.layout.screen_chat), ScreenShare {

    companion object {

        fun instance(chatType: Long, targetId: Long, targetSubId: Long, action: NavigationAction) {
            val targetSubId = if (chatType != API.CHAT_TYPE_FANDOM || targetSubId != 0L) targetSubId else ControllerApi.getLanguageId()
            val tag = ChatTag(chatType, targetId, targetSubId)
            instance(tag, action)
        }

        fun instance(tag: ChatTag, action: NavigationAction, onShow: (SChat) -> Unit = {}) {
            ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            ApiRequestsSupporter.executeInterstitial(action, RChatGet(tag)) { r ->
                ControllerChats.putRead(tag, r.anotherReadDate)
                val screen = SChat(tag, r.subscribed, r.chatName, r.chatImageId, r.chatBackgroundImageId, r.chatInfo_1, r.chatInfo_2, r.chatInfo_3)
                onShow.invoke(screen)
                screen
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.eventOnChatTypingChanged(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventFandomBackgroundImageChanged::class) { this.onEventFandomBackgroundImageChanged(it) }

    private val vSend: ViewIcon = findViewById(R.id.vSend)
    private val vLine: View = findViewById(R.id.vLine)
    private val vAttach: ViewIcon = findViewById(R.id.vAttach)
    private val vAttachRecycler: RecyclerView = findViewById(R.id.vAttachRecycler)
    private val vText: ViewEditTextMedia = findViewById(R.id.vText)
    private val vMenu: ViewIcon
    private val vNotifications: ViewIcon
    private val vTypingText: TextView = findViewById(R.id.vTypingText)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vQuoteContainer: ViewGroup = findViewById(R.id.vQuoteContainer)
    private val vQuoteText: ViewTextLinkable = findViewById(R.id.vQuoteText)
    private val vQuoteRemove: ViewIcon = findViewById(R.id.vQuoteRemove)
    private val vFandomBackground: ImageView = findViewById(R.id.vFandomBackground)

    private var lastTypingSent = 0L
    private var scrollAfterLoad = false
    private var unitAnswer: UnitChatMessage? = null
    private var unitChange: UnitChatMessage? = null
    private var quoteText = ""
    private var quoteId = 0L
    private val attach = Attach(vAttach, vAttachRecycler)
    private val carSpace = CardSpace(16)
    private var needUpdate = false

    init {
        isBottomNavigationShadowAvailable = false
        SActivityBottomNavigation.setShadow(vLine)

        vNotifications = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_notifications_24dp)) { sendSubscribe(!subscribed) }
        if (tag.chatType != API.CHAT_TYPE_FANDOM) vNotifications.visibility = View.GONE
        vMenu = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_more_vert_24dp)) { v ->
            ControllerChats.instanceChatPopup(tag) { Navigator.remove(this) }.asSheetShow()
        }
        vQuoteContainer.visibility = View.GONE
        vQuoteRemove.setOnClickListener { setQuote("") }

        setBackgroundImage(R.drawable.bg_5)
        setTextEmpty(if (tag.chatType == API.CHAT_TYPE_FANDOM) R.string.chat_empty_fandom else R.string.chat_empty_private)
        vSend.setOnClickListener { v -> onSendClicked() }
        vText.addTextChangedListener(TextWatcherChanged { sendTyping() })

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        vRecycler.layoutManager = layoutManager

        updateSubscribed()
        update()
        updateTyping()
        updateBackground()
        updateMedieEditText()
    }

    private fun update() {
        if (tag.chatType == API.CHAT_TYPE_FANDOM) {
            val xFandom = XFandom(tag.targetId, tag.targetSubId, chatName, chatImageId) { update() }
            xFandom.setView(vAvatarTitle)
            vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            vAvatarTitle.setSubtitle(ToolsResources.s(R.string.app_subscribers) + ": $chatInfo_1")
            vAvatarTitle.setOnClickListener { Navigator.to(SChatSubscribers(tag.targetId, tag.targetSubId, chatName)) }
        } else {
            val anotherId = if (tag.targetId == ControllerApi.account.id) tag.targetSubId else tag.targetId
            val xAccount = XAccount(anotherId, chatName, chatImageId, chatInfo_1, chatInfo_2) { update() }
            xAccount.setView(vAvatarTitle)

            if (!xAccount.isOnline()) {
                vAvatarTitle.setSubtitle(ToolsResources.sCap(R.string.app_was_online, ToolsResources.sex(chatInfo_3, R.string.he_was, R.string.she_was), ToolsDate.dateToStringCustom(xAccount.getLastOnlineTime())))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.grey_500))
            } else {
                vAvatarTitle.setSubtitle(ToolsResources.s(R.string.app_online))
                vAvatarTitle.vSubtitle.setTextColor(ToolsResources.getColor(R.color.green_700))
            }
        }
    }

    private fun updateSubscribed() {
        vNotifications.setColorFilter(if (subscribed) ToolsResources.getAccentColor(context) else 0x00000000)
    }

    private fun updateBackground() {
        if (chatBackgroundImageId > 0 && ControllerSettings.fandomBackground) {
            vFandomBackground.visibility = View.VISIBLE
            ToolsImagesLoader.load(chatBackgroundImageId).holder(0x00000000).into(vFandomBackground)
            vFandomBackground.setColorFilter(ToolsColor.setAlpha(210, ToolsResources.getColorAttr(R.attr.background)))
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

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardChatMessage, UnitChatMessage> {
        val adapter = RecyclerCardAdapterLoading<CardChatMessage, UnitChatMessage>(CardChatMessage::class) { u -> instanceCard(u) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RChatMessageGetAll(tag, if (cards.isEmpty()) 0 else cards[cards.size - 1].unit.dateCreate, false)
                            .onComplete { r ->
                                adapter!!.remove(carSpace)
                                onLoad.invoke(r.units)
                                adapter!!.add(carSpace)
                                if (scrollAfterLoad) {
                                    scrollAfterLoad = false
                                    vRecycler.smoothScrollToPosition(vRecycler.adapter!!.itemCount)
                                }
                                EventBus.post(EventChatRead(tag))
                                if (r.units.isNotEmpty())
                                    EventBus.post(EventChatNewBottomMessage(tag, r.units[r.units.size - 1]))
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
                .setTopLoader { onLoad, cards ->
                    subscription = RChatMessageGetAll(tag, if (cards.isEmpty()) 0 else cards[0].unit.dateCreate, true)
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
        adapter.setRemoveSame(true)
        adapter.setShowLoadingCardBottom(false)
        adapter.setShowLoadingCardTop(true)
        return adapter
    }

    private fun instanceCard(u: UnitChatMessage): CardChatMessage {
        return CardChatMessage.instance(u,
                { unit ->
                    if (ControllerApi.isCurrentAccount(unit.creatorId)) {
                        false
                    } else {
                        setAnswer(unit)
                        true
                    }
                },
                { unit -> setChange(unit) },
                { unit ->
                    if (!ControllerApi.isCurrentAccount(unit.creatorId)) setAnswer(unit)
                    setQuote(unit.creatorName + ": " + unit.text, unit.id)
                    ToolsView.showKeyboard(vText)
                },
                { id ->
                    if (adapter == null) return@instance
                    for (i in adapter!!.get(CardChatMessage::class)) {
                        if (i.unit.id == id) {
                            i.flash()
                            vRecycler.scrollToPosition(adapter!!.indexOf(i))
                            break
                        }
                    }
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

    private fun sendTyping() {
        if (lastTypingSent > System.currentTimeMillis() - 5000) return
        val t = vText.text
        if (t == null || t.isEmpty()) return
        lastTypingSent = System.currentTimeMillis()
        RChatTyping(tag).send(api)
    }

    fun sendSubscribe(subscribed: Boolean) {
        if (tag.chatType != API.CHAT_TYPE_FANDOM) return
        RChatSubscribe(tag, subscribed)
                .onComplete { r ->
                    EventBus.post(EventChatSubscriptionChanged(tag, subscribed))
                }
                .onNetworkError { ToolsToast.show(R.string.error_network) }
                .send(api)
    }

    fun isNeedScrollAfterAdd() = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == vRecycler.adapter!!.itemCount - 1

    fun setLock(b: Boolean) {
        vAttach.isEnabled = !b
        vText.isEnabled = !b
        vSend.isEnabled = !b
        vQuoteRemove.isEnabled = !b
        vQuoteText.isEnabled = !b
    }

    fun updateMedieEditText() {
        if (unitChange == null) vText.setCallback { link -> sendLink(link, getParentId(), false) }
        else vText.setCallback(null)
    }

    private fun setChange(unitChange: UnitChatMessage?) {
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

    private fun setQuote(quoteText: String, quoteId: Long = 0) {
        this.quoteText = quoteText
        this.quoteId = quoteId
        vQuoteContainer.visibility = if (quoteText.isEmpty()) View.GONE else View.VISIBLE
        vQuoteText.text = quoteText
        ControllerApi.makeTextHtml(vQuoteText)
    }

    override fun onBackPressed(): Boolean {
        if (unitChange != null) {
            setChange(null)
            return true
        }
        return super.onBackPressed()
    }

    private fun clearInput() {
        setQuote("")
        attach.clear()
        vText.setText("")
        setChange(null)
    }

    private fun afterSend(message: UnitChatMessage) {
        clearInput()
        EventBus.post(EventUpdateChats())
        addMessage(message)
    }

    private fun addMessage(message: UnitChatMessage) {
        val b = isNeedScrollAfterAdd()
        if (adapter != null) {
            adapter!!.remove(carSpace)
            adapter!!.add(instanceCard(message))
            adapter!!.add(carSpace)
            if (b) vRecycler.smoothScrollToPosition(vRecycler.adapter!!.itemCount)
        }
        setState(State.NONE)
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
        val text = getText()
        val parentId = getParentId()

        if (text.isEmpty() && !attach.isHasContent()) return

        if (unitChange == null) {
            if (attach.isHasContent()) sendImage(text, parentId)
            else if (ToolsText.isWebLink(text)) sendLink(text, parentId, true)
            else sendText(text, parentId)
        } else sendChange(text)
    }

    //
    //  Text
    //

    private fun sendText(text: String, parentId: Long) {
        setLock(true)
        ApiRequestsSupporter.execute(RChatMessageCreate(tag, text, null, null, parentId, quoteId)) { r ->
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
            eventBus.post(EventChatMessageChanged(unitChangeId, text, quoteId, quoteText))
            clearInput()
        }, { enabled -> setLock(!enabled) })

    }

    //
    //  Link
    //

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

    //
    //  Image
    //

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
            ApiRequestsSupporter.executeProgressDialog(RChatMessageCreate(tag, text, bytes, gif, parentId, quoteId)) { r ->
                afterSend(r.message)
                setLock(false)
            }
                    .onApiError(RChatMessageCreate.E_BLACK_LIST) { ToolsToast.show(R.string.error_black_list) }
                    .onFinish { setLock(false) }
        }
    }


    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (tag == n.tag && !ControllerApi.isCurrentAccount(n.unitChatMessage.creatorId)) {
                addMessage(n.unitChatMessage)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) ControllerChats.readRequest(tag)
                else needUpdate = true


            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (tag == n.tag) {
                addMessage(n.unitChatMessage)
                ControllerChats.readRequest(tag)

                if (Navigator.getCurrent() == this  && SupAndroid.activityIsVisible) ControllerChats.readRequest(tag)
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

    private fun onEventFandomBackgroundImageChanged(e: EventFandomBackgroundImageChanged) {
        if (tag.chatType == API.CHAT_TYPE_FANDOM) {
            if (tag.targetId == e.fandomId && tag.targetSubId == e.languageId) {
                this.chatBackgroundImageId = e.imageId
                updateBackground()
            }
            if (tag.targetId == 0L && tag.targetSubId == 0L) {
                updateBackground()
            }
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
    //  Setters
    //

    private fun setAnswer(unitAnswer: UnitChatMessage): Boolean {
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


    //
    //  Share
    //

    override fun addText(text: String, postAfterAdd: Boolean) {
        vText.setText(text)
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

                attach.setImageBitmapNow(it, dialog)
            }, {
                dialog.hide()
                ToolsToast.show(R.string.error_cant_load_image)
            })
        }
    }

    override fun addImage(image: Bitmap, postAfterAdd: Boolean) {
        val dialog = ToolsView.showProgressDialog()
        attach.setImageBitmapNow(image, dialog)
    }


}
