package com.sayzen.campfiresdk.screens.chat


import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
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
import com.sayzen.campfiresdk.models.ScreenShare
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
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
import com.sup.dev.java.libs.debug.Debug
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
        val chatInfo_3: Long,
        val chatInfo_4: Long)
    : SLoadingRecycler<CardChatMessage, UnitChatMessage>(R.layout.screen_chat), ScreenShare {

    companion object {

        fun instance(chatType: Long, targetId: Long, targetSubId: Long, setStack: Boolean, action: NavigationAction) {
            val targetSubIdV = if (chatType != API.CHAT_TYPE_FANDOM || targetSubId != 0L) targetSubId else ControllerApi.getLanguageId()
            val tag = ChatTag(chatType, targetId, targetSubIdV)
            instance(tag, setStack, action)
        }

        fun instance(tag: ChatTag, setStack: Boolean, action: NavigationAction, onShow: (SChat) -> Unit = {}) {
            if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
            ApiRequestsSupporter.executeInterstitial(action, RChatGet(tag)) { r ->
                ControllerChats.putRead(tag, r.anotherReadDate)
                val screen = SChat(tag, r.subscribed, r.chatName, r.chatImageId, r.chatBackgroundImageId, r.chatInfo_1, r.chatInfo_2, r.chatInfo_3, r.chatInfo_4)
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

    private val vLine: View = findViewById(R.id.vLine)
    private val vMenu: ViewIcon
    private val vNotifications: ViewIcon
    private val vTypingText: TextView = findViewById(R.id.vTypingText)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vFandomBackground: ImageView = findViewById(R.id.vFandomBackground)

    private val fieldLogic = FieldLogic(this)

    private var scrollAfterLoad = false
    private val carSpace = CardSpace(16)
    private var needUpdate = false
    private var loaded = false
    private val addAfterLoadList = ArrayList<UnitChatMessage>()

    init {
        isNavigationShadowAvailable = false
        SActivityTypeBottomNavigation.setShadow(vLine)

        vAvatarTitle.setTitleColor(ToolsResources.getColorAttr(R.attr.toolbar_content_color))
        vAvatarTitle.setSubtitleColor(ToolsResources.getColorAttr(R.attr.toolbar_content_color))

        vNotifications = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_notifications_24dp)) { sendSubscribe(!subscribed) }
        if (tag.chatType != API.CHAT_TYPE_FANDOM) vNotifications.visibility = View.GONE
        vMenu = addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_more_vert_24dp)) {
            ControllerChats.instanceChatPopup(tag) { Navigator.remove(this) }.asSheetShow()
        }

        setBackgroundImage(R.drawable.bg_5)
        setTextEmpty(if (tag.chatType == API.CHAT_TYPE_FANDOM) R.string.chat_empty_fandom else R.string.chat_empty_private)
        setTextProgress(R.string.chat_loading)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        vRecycler.layoutManager = layoutManager

        ToolsThreads.main(100) { updateSubscribed() }  // Иначе иконка не красится.
        update()
        updateTyping()
        updateBackground()
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
            val xAccount = XAccount(anotherId, chatName, chatImageId, chatInfo_1, chatInfo_4, chatInfo_2) { update() }
            xAccount.setView(vAvatarTitle)

            if (!xAccount.isOnline()) {
                vAvatarTitle.setSubtitle(ToolsResources.sCap(R.string.app_was_online, ToolsResources.sex(chatInfo_3, R.string.he_was, R.string.she_was), ToolsDate.dateToString(xAccount.getLastOnlineTime())))
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

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardChatMessage, UnitChatMessage> {
        val adapter = RecyclerCardAdapterLoading<CardChatMessage, UnitChatMessage>(CardChatMessage::class) { u -> instanceCard(u) }
                .setBottomLoader { onLoad, cards ->
                    if (loaded) {
                        onLoad.invoke(emptyArray())
                    } else {
                        subscription = RChatMessageGetAll(tag, if (cards.isEmpty()) 0 else cards[cards.size - 1].xUnit.unit.dateCreate, false)
                                .onComplete { r ->
                                    if (loaded) {
                                        onLoad.invoke(emptyArray())
                                        return@onComplete
                                    }
                                    loaded = true
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
                                    ToolsThreads.main(true) {
                                        for (c in addAfterLoadList) addMessage(c, true)
                                    }
                                    adapter!!.lockBottom()
                                }
                                .onNetworkError { onLoad.invoke(null) }
                                .send(api)
                    }
                }
                .setTopLoader { onLoad, cards ->
                    subscription = RChatMessageGetAll(tag, if (cards.isEmpty()) 0 else cards[0].xUnit.unit.dateCreate, true)
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
                onClick = { unit ->
                    if (ControllerApi.isCurrentAccount(unit.creatorId)) {
                        false
                    } else {
                        fieldLogic.setAnswer(unit)
                        true
                    }
                },
                onChange = { unit -> fieldLogic.setChange(unit) },
                onQuote = { unit ->
                    if (!ControllerApi.isCurrentAccount(unit.creatorId)) fieldLogic.setAnswer(unit)
                    fieldLogic.setQuote(unit)
                    ToolsView.showKeyboard(fieldLogic.vText)
                },
                onGoTo = { id ->
                    if (adapter == null) return@instance
                    for (i in adapter!!.get(CardChatMessage::class)) {
                        if (i.xUnit.unit.id == id) {
                            i.flash()
                            vRecycler.scrollToPosition(adapter!!.indexOf(i))
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
        if (tag.chatType != API.CHAT_TYPE_FANDOM) return
        RChatSubscribe(tag, subscribed)
                .onComplete {
                    EventBus.post(EventChatSubscriptionChanged(tag, subscribed))
                }
                .onNetworkError { ToolsToast.show(R.string.error_network) }
                .send(api)
    }

    fun isNeedScrollAfterAdd(): Boolean {
        if (vRecycler.layoutManager !is LinearLayoutManager || adapter == null) return false
        return (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == vRecycler.adapter!!.itemCount - 1
    }


    override fun onBackPressed(): Boolean {
        if (fieldLogic.unitChange != null) {
            fieldLogic.setChange(null)
            return true
        }
        return super.onBackPressed()
    }

    fun addCard(card: CardSending) {
        if(adapter != null) {
            adapter!!.remove(carSpace)
            adapter!!.add(card)
            adapter!!.add(carSpace)
            vRecycler.smoothScrollToPosition(vRecycler.adapter!!.itemCount - 1)
        }
    }

    fun addMessage(message: UnitChatMessage, forceScroll: Boolean, replaceCard: Card? = null) {
        if (!loaded) {
            addAfterLoadList.add(message)
            return
        }
        val b = isNeedScrollAfterAdd()
        if (adapter != null) {
            if (replaceCard == null || !adapter!!.contains(replaceCard)) {
                adapter!!.remove(carSpace)
                adapter!!.add(instanceCard(message))
                adapter!!.add(carSpace)
            } else {
                adapter!!.replace(adapter!!.indexOf(replaceCard), instanceCard(message))
            }
            if (forceScroll)
                vRecycler.scrollToPosition(vRecycler.adapter!!.itemCount - 1)
            else if (b)
                vRecycler.smoothScrollToPosition(vRecycler.adapter!!.itemCount - 1)
        }
        setState(State.NONE)
    }


    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (tag == n.tag && !ControllerApi.isCurrentAccount(n.unitChatMessage.creatorId)) {
                addMessage(n.unitChatMessage, false)

                if (Navigator.getCurrent() == this && SupAndroid.activityIsVisible) ControllerChats.readRequest(tag)
                else needUpdate = true


            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (tag == n.tag) {
                addMessage(n.unitChatMessage, false)
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
