package com.sayzen.campfiresdk.models.cards.chat

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageChange
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageRemove
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.google.android.material.card.MaterialCardView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.chat.EventChatMessageChanged
import com.sayzen.campfiresdk.models.events.chat.EventChatReadDateChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlocked
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoadingInterface
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate

abstract class CardChatMessage constructor(
        layout: Int,
        publication: PublicationChatMessage,
        var onClick: ((PublicationChatMessage) -> Boolean)? = null,
        var onChange: ((PublicationChatMessage) -> Unit)? = null,
        var onQuote: ((PublicationChatMessage) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null,
        var onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardPublication(layout, publication) {

    companion object {

        fun instance(publication: PublicationChatMessage,
                     onClick: ((PublicationChatMessage) -> Boolean)? = null,
                     onChange: ((PublicationChatMessage) -> Unit)? = null,
                     onQuote: ((PublicationChatMessage) -> Unit)? = null,
                     onGoTo: ((Long) -> Unit)? = null,
                     onBlocked: ((PublicationChatMessage) -> Unit)? = null
        ): CardChatMessage {
            when (publication.type) {
                PublicationChatMessage.TYPE_TEXT -> return CardChatMessageText(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                PublicationChatMessage.TYPE_IMAGE, PublicationChatMessage.TYPE_GIF -> return CardChatMessageImage(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                PublicationChatMessage.TYPE_IMAGES -> return CardChatMessageImages(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                PublicationChatMessage.TYPE_SYSTEM -> return CardChatMessageSystem(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                PublicationChatMessage.TYPE_VOICE -> return CardChatMessageVoice(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                PublicationChatMessage.TYPE_STICKER -> return CardChatMessageSticker(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
                else -> return CardChatMessageUnknowm(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { onNotification(it) }
            .subscribe(EventChatMessageChanged::class) { onEventChanged(it) }
            .subscribe(EventChatReadDateChanged::class) { onEventChatReadDateChanged(it) }
            .subscribe(EventStyleChanged::class) { update() }
            .subscribe(EventPublicationBlocked::class) { onEventPublicationBlocked(it) }
            .subscribe(EventPublicationDeepBlockRestore::class) { onEventPublicationDeepBlockRestore(it) }

    var changeEnabled = true
    var useMessageContainerBackground = true
    var quoteEnabled = true
    var copyEnabled = true
    var wasBlocked = false

    init {
        updateFandomOnBind = false
        useBackgroundToFlash = true
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationChatMessage

        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val vText: ViewTextLinkable? = view.findViewById(R.id.vCommentText)
        val vRootContainer: ViewGroup? = view.findViewById(R.id.vRootContainer)
        val vMessageContainer: MaterialCardView? = view.findViewById(R.id.vMessageContainer)
        val vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vQuoteText: ViewTextLinkable? = view.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesSwipe? = view.findViewById(R.id.vQuoteImage)

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, publication.id)
        }
        if (vSwipe != null) {
            vSwipe.onClick = { _, _ ->
                if (ControllerApi.isCurrentAccount(publication.creatorId)) {
                    showMenu()
                } else if (!onClick()) {
                    showMenu()
                }
            }
            vSwipe.onLongClick = { _, _ -> showMenu() }
            vSwipe.swipeEnabled = quoteEnabled && onQuote != null


            if (onQuote != null) {
                vSwipe.onSwipe = { onQuote?.invoke(publication) }
            }
        }

        if (vQuoteContainer != null) {
            vQuoteContainer.visibility = if (publication.quoteText.isEmpty() && publication.quoteImages.isEmpty() && publication.quoteStickerId < 1L) View.GONE else View.VISIBLE
            vQuoteContainer.setOnClickListener {
                if (onGoTo != null) onGoTo!!.invoke(publication.quoteId)
            }
        }

        if (vQuoteText != null) {
            vQuoteText.text = publication.quoteText
            if (publication.quoteCreatorName.isNotEmpty()) {
                val otherName = publication.quoteCreatorName + ":"
                if (publication.quoteText.startsWith(otherName)) {
                    vQuoteText.text = "{90A4AE $otherName}" + publication.quoteText.substring(otherName.length)
                }
            }
            ControllerLinks.makeLinkable(vQuoteText)
        }

        if (vQuoteImage != null) {
            vQuoteImage.clear()
            vQuoteImage.visibility = View.VISIBLE
            if (publication.quoteStickerId > 0) {
                vQuoteImage.add(publication.quoteStickerImageId, onClick = { SStickersView.instanceBySticker(publication.quoteStickerId, Navigator.TO) })
            } else if (publication.quoteImages.isNotEmpty()) {
                for (i in publication.quoteImages) vQuoteImage.add(i)
            } else {
                vQuoteImage.visibility = View.GONE
            }
        }

        if (vText != null) {
            vText.text = publication.text
            vText.visibility = if (publication.text.isEmpty()) View.GONE else View.VISIBLE

            ControllerLinks.makeLinkable(vText) {
                val myName = ControllerApi.account.name + ","
                if (publication.text.startsWith(myName)) {
                    vText.text = "{ff6d00 $myName}" + vText.text.toString().substring(myName.length)
                } else {
                    if (publication.answerName.isNotEmpty()) {
                        val otherName = publication.answerName + ","
                        if (publication.text.startsWith(otherName)) {
                            vText.text = "{90A4AE $otherName}" + vText.text.toString().substring(otherName.length)
                        }
                    }
                }
            }
        }

        if (vRootContainer != null) {
            vRootContainer.visibility = View.VISIBLE
            (vRootContainer.layoutParams as FrameLayout.LayoutParams).gravity = if (ControllerApi.isCurrentAccount(publication.creatorId)) Gravity.RIGHT else Gravity.LEFT
        }

        if (vMessageContainer != null) {
            vMessageContainer.radius = ToolsView.dpToPx(ControllerSettings.styleChatRounding)
        }

        if (ControllerApi.isCurrentAccount(publication.creatorId)) {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(0).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
                if (useMessageContainerBackground) {
                    if (ToolsColor.red(ToolsResources.getColorAttr(R.attr.widget_background)) < 0x60)
                        vMessageContainer.setCardBackgroundColor(ToolsColor.add(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
                    else
                        vMessageContainer.setCardBackgroundColor(ToolsColor.remove(ToolsResources.getColorAttr(R.attr.widget_background), 0xFF202020.toInt()))
                } else {
                    vMessageContainer.setCardBackgroundColor(0x00000000)
                }
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(12).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(48).toInt()
            }
        } else {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(48).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(12).toInt()
                if (useMessageContainerBackground) vMessageContainer.setCardBackgroundColor(ToolsResources.getColorAttr(R.attr.widget_background))
                else vMessageContainer.setCardBackgroundColor(0x00000000)
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(0).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
            }
        }

        updateRead()
        updateSameCrds()
    }

    fun updateSameCrds() {
        if (getView() == null) return

        val vPaddingContainer: ViewGroup? = getView()!!.findViewById(R.id.vPaddingContainer)
        val vTextContainer: ViewGroup? = getView()!!.findViewById(R.id.vTextContainer)
        val vRootContainer: ViewGroup? = getView()!!.findViewById(R.id.vRootContainer)
        val vMessageContainer: MaterialCardView? = getView()!!.findViewById(R.id.vMessageContainer)
        val vAvatar: ViewAvatar? = getView()!!.findViewById(R.id.vAvatar)
        val vLabel: TextView? = getView()!!.findViewById(R.id.vLabel)

        val topIsSameUser = isTopSameUser()
        val bottomsSameUser = isBottomsSameUser()

        val vPadding = if (vPaddingContainer != null) vPaddingContainer else if (vRootContainer != null) vRootContainer else null
        if (vPadding != null) vPadding.setPadding(0, if (topIsSameUser) ToolsView.dpToPx(1).toInt() else ToolsView.dpToPx(8).toInt(), 0, if (bottomsSameUser) ToolsView.dpToPx(1).toInt() else ToolsView.dpToPx(8).toInt())
        if (vMessageContainer != null) (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).topMargin = if (!topIsSameUser && bottomsSameUser) ToolsView.dpToPx(8).toInt() else 0

        if (vAvatar != null) {
            if (topIsSameUser) {
                vAvatar.layoutParams.height = 0
            } else {
                vAvatar.layoutParams.height = ToolsView.dpToPx(40).toInt()
            }
        }

        if (vLabel != null) {
            val bottomPublication = getBottomPublication()
            if (bottomsSameUser && bottomPublication != null && bottomPublication.dateCreate < xPublication.publication.dateCreate + 1000L * 60L * 5) {
                vLabel.visibility = View.GONE
            } else {
                vLabel.visibility = View.VISIBLE
            }
        }

        if (vTextContainer != null && vTextContainer.paddingBottom != 0 && (vLabel == null || vLabel.visibility == View.GONE))
            vTextContainer.setPadding(0, 0, 0, if (bottomsSameUser) ToolsView.dpToPx(8).toInt() else ToolsView.dpToPx(4).toInt())


    }

    fun isTopSameUser(): Boolean {
        if (adapter != null) {
            var myIndex = adapter!!.indexOf(this)
            myIndex--
            if (myIndex > -1) {
                val card = adapter!!.get(myIndex)
                if (card is CardChatMessage) return card.xPublication.publication.creatorId == xPublication.publication.creatorId
            }
        }
        return false
    }

    fun isBottomsSameUser(): Boolean {
        val u = getBottomPublication()
        return u != null && u.creatorId == xPublication.publication.creatorId
    }

    fun getBottomPublication(): PublicationChatMessage? {
        if (adapter != null) {
            var myIndex = adapter!!.indexOf(this)
            myIndex++
            if (myIndex < adapter!!.size()) {
                val card = adapter!!.get(myIndex)
                if (card is CardChatMessage) return card.xPublication.publication as PublicationChatMessage
            }
        }
        return null
    }

    fun updateRead() {
        val publication = xPublication.publication as PublicationChatMessage
        if (getView() == null) return
        val vNotRead: View? = getView()!!.findViewById(R.id.vNotRead)
        if (vNotRead != null) {

            if (!ControllerApi.isCurrentAccount(publication.creatorId) || publication.chatType != API.CHAT_TYPE_PRIVATE)
                vNotRead.visibility = View.GONE
            else if (ControllerChats.isRead(publication.chatTag(), publication.dateCreate))
                vNotRead.visibility = View.INVISIBLE
            else
                vNotRead.visibility = View.VISIBLE

        }

    }

    fun showMenu() {
        val publication = xPublication.publication as PublicationChatMessage
        WidgetMenu()
                .groupCondition(ControllerApi.isCurrentAccount(publication.creatorId))
                .add(R.string.app_remove) { _, _ ->
                    ControllerApi.removePublication(publication.id, R.string.chat_remove_confirm, R.string.chat_error_gone) {
                        EventBus.post(EventUpdateChats())
                    }
                }
                .add(R.string.app_change) { _, _ -> onChange?.invoke(publication) }.condition(changeEnabled)
                .clearGroupCondition()
                .add(R.string.app_copy) { _, _ ->
                    ToolsAndroid.setToClipboard(publication.text)
                    ToolsToast.show(R.string.app_copied)
                }.condition(copyEnabled)
                .add(R.string.app_quote) { _, _ -> onQuote!!.invoke(publication) }.condition(quoteEnabled && onQuote != null)
                .add(R.string.app_history) { _, _ -> Navigator.to(SPublicationHistory(publication.id)) }.condition(ControllerPost.ENABLED_HISTORY)
                .groupCondition(!ControllerApi.isCurrentAccount(publication.creatorId))
                .add(R.string.app_report) { _, _ -> ControllerApi.reportPublication(publication.id, R.string.chat_report_confirm, R.string.chat_error_gone) }.condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearReportsPublication(publication.id, publication.publicationType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_BLOCK) && publication.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerPublications.block(publication) { if (adapter != null && adapter!! is RecyclerCardAdapterLoadingInterface) (adapter!! as RecyclerCardAdapterLoadingInterface).loadBottom() } }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_BLOCK))
                .clearGroupCondition()
                .add("Востановить") { _, _ -> ControllerPublications.restoreDeepBlock(publication.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && publication.status == API.STATUS_DEEP_BLOCKED)
                .asSheetShow()
    }

    override fun updateAccount() {
        if (getView() == null) return
        val publication = xPublication.publication as PublicationChatMessage

        val vAvatar: ViewAvatar? = getView()!!.findViewById(R.id.vAvatar)
        val vLabel: TextView? = getView()!!.findViewById(R.id.vLabel)

        if (vAvatar != null) {
            vAvatar.visibility = if (ControllerApi.isCurrentAccount(publication.creatorId)) View.GONE else View.VISIBLE
            if (publication.chatTag().chatType == API.CHAT_TYPE_PRIVATE) vAvatar.visibility = View.GONE
            if (!showFandom) xPublication.xAccount.setView(vAvatar)
            else xPublication.xFandom.setView(vAvatar)
        }

        if (vLabel != null) {
            if (ControllerApi.isCurrentAccount(publication.creatorId)) {
                if (vLabel.layoutParams is FrameLayout.LayoutParams) (vLabel.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.BOTTOM
                if (vLabel.layoutParams is LinearLayout.LayoutParams) (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.RIGHT
                vLabel.text = ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + ToolsResources.s(R.string.app_edited) else "")
            } else {
                if (vLabel.layoutParams is FrameLayout.LayoutParams) (vLabel.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.BOTTOM
                if (vLabel.layoutParams is LinearLayout.LayoutParams) (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
                if (publication.chatTag().chatType == API.CHAT_TYPE_PRIVATE)
                    vLabel.text = ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + ToolsResources.s(R.string.app_edited) else "")
                else
                    vLabel.text = xPublication.xAccount.name + "  " + ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + ToolsResources.s(R.string.app_edited) else "")
            }
        }
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports: TextView? = getView()!!.findViewById(R.id.vReports)
        if (vReports != null) xPublication.xReports.setView(vReports)

    }

    fun onClick(): Boolean {
        val publication = xPublication.publication as PublicationChatMessage
        if (publication.type == PublicationChatMessage.TYPE_SYSTEM && publication.systemType == PublicationChatMessage.SYSTEM_TYPE_BLOCK) {
            ControllerCampfireSDK.onToModerationClicked(publication.blockModerationEventId, 0, Navigator.TO)
            return true
        }

        if (onClick == null) {
            SChat.instance(ChatTag(publication.chatType, publication.fandomId, publication.languageId), 0, true, Navigator.TO)
            return true
        } else {
            return onClick!!.invoke(publication)
        }
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationChatMessage
        ToolsImagesLoader.load(publication.creatorImageId).intoCash()
    }

    //
    //  Event Bus
    //

    private fun onEventPublicationDeepBlockRestore(e: EventPublicationDeepBlockRestore) {
        if (e.publicationId == xPublication.publication.id && xPublication.publication.status == API.STATUS_DEEP_BLOCKED) {
            adapter?.remove(this)
        }
    }

    private fun onNotification(e: EventNotification) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.notification is NotificationChatMessageChange) {
            val n = e.notification
            if (n.publicationId == publication.id) {
                publication.text = n.text
                publication.changed = true
                update()
            }
        } else if (e.notification is NotificationChatMessageRemove) {
            if (e.notification.publicationId == publication.id && adapter != null) adapter!!.remove(this)

        }
    }

    private fun onEventChanged(e: EventChatMessageChanged) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.publicationId == publication.id) {
            publication.text = e.text
            publication.quoteId = e.quoteId
            publication.quoteText = e.quoteText
            publication.changed = true
            flash()
            update()
        }
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.tag == publication.chatTag()) {
            updateRead()
        }
    }

    private fun onEventPublicationBlocked(e: EventPublicationBlocked) {
        val publication = xPublication.publication as PublicationChatMessage
        if (!wasBlocked && e.firstBlockPublicationId == publication.id) {
            wasBlocked = true
            if (onBlocked != null && e.publicationChatMessage != null) onBlocked!!.invoke(e.publicationChatMessage)
        }
    }

    override fun equals(other: Any?): Boolean {
        val publication = xPublication.publication as PublicationChatMessage
        return if (other is CardChatMessage) publication.id == other.xPublication.publication.id
        else super.equals(other)
    }

}