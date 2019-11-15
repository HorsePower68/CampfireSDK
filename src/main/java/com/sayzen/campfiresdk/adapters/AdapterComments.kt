package com.sayzen.campfiresdk.adapters

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.dzen.campfire.api.models.PublicationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.requests.units.RCommentsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class AdapterComments(
        private val publicationId: Long,
        private var scrollToCommentId: Long,
        private val vRecycler: RecyclerView,
        private val startFromBottom: Boolean = false
) : RecyclerCardAdapterLoading<CardComment, PublicationComment>(CardComment::class, null) {


    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }

    private var needScrollToBottom = false
    private var extraScroll = 1

    init {
        setBottomLoader { onLoad, cards ->
            RCommentsGetAll(publicationId, if (cards.isEmpty()) 0 else cards.get(cards.size - 1).xPublication.publication.dateCreate, false, startFromBottom)
                    .onComplete { r ->
                        onLoad.invoke(r.publications)
                        remove(CardSpace::class)
                        if(!isEmpty)add(CardSpace(124))
                    }
                    .onError { onLoad.invoke(null) }
                    .send(api)
        }
        setAddToSameCards(true)
        setMapper { instanceCard(it) }
        setShowLoadingCardBottom(false)
        setShowLoadingCardTop(true)
        setRemoveSame(true)
        addOnLoadedNotEmpty { onCommentsPackLoaded() }
        setRetryMessage(R.string.error_network, R.string.app_retry)
        setEmptyMessage(R.string.comments_empty, R.string.app_comment) { showCommentDialog() }
        setNotifyCount(5)
        ToolsThreads.main(true) { this.loadBottom() }
    }

    fun setCommentButton(view: View) {
        view.setOnClickListener { showCommentDialog() }
    }

    fun showCommentDialog(comment: PublicationComment? = null, changeComment: PublicationComment? = null, quoteId: Long = 0, quoteText: String = "") {
        WidgetComment(publicationId, comment, changeComment, quoteId, quoteText) {
            val card = addComment(it)
            vRecycler.scrollToPosition(indexOf(card) + 1)
            card.flash()
        }.asSheetShow()
    }

    fun enableTopLoader() {
        setTopLoader { onLoad, cards ->
            RCommentsGetAll(publicationId, if (cards.isEmpty()) 0 else cards.get(0).xPublication.publication.dateCreate, true, startFromBottom)
                    .onComplete { r ->
                        onLoad.invoke(r.publications)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    private fun onCommentsPackLoaded() {
        if (scrollToCommentId == -1L) {
            scrollToCommentId = 0
            val v = get(CardComment::class)
            val index = if (v.isNotEmpty()) indexOf(v.get(0)) + 1 else size()
            ToolsThreads.main(600) {
                vRecycler.scrollToPosition(index)
            }
        } else if (scrollToCommentId != 0L) {
            for (c in get(CardComment::class)) {
                if (c.xPublication.publication.id == scrollToCommentId) {
                    scrollToCommentId = 0
                    ToolsThreads.main(600) {
                        vRecycler.scrollToPosition(indexOf(c) + extraScroll)
                        c.flash()
                    }
                }
            }
            if (scrollToCommentId != 0L) {
                ToolsToast.show(R.string.error_gone_comment)
                scrollToCommentId = 0
            }
        }

        if (needScrollToBottom) {
            needScrollToBottom = false
        }

    }

    fun addComment(publicationComment: PublicationComment): CardComment {
        val card = instanceCard(publicationComment)
        addWithHash(card)
        card.flash()
        return card
    }

    fun loadAndScrollTo(scrollToCommentId: Long) {
        this.scrollToCommentId = scrollToCommentId
        loadBottom()
    }

    private fun instanceCard(publication: PublicationComment): CardComment {
        return CardComment.instance(publication, true, false,
                { comment ->
                    if (ControllerApi.isCurrentAccount(comment.creatorId)) return@instance false
                    showCommentDialog(comment)
                    true
                },
                { comment ->
                    var quoteText = comment.creatorName + ": "
                    if (comment.text.isNotEmpty()) quoteText += comment.text
                    else if (comment.imageId != 0L || comment.imageIdArray.isNotEmpty()) quoteText += ToolsResources.s(R.string.app_image)
                    else if (comment.stickerId != 0L) quoteText += ToolsResources.s(R.string.app_sticker)

                    showCommentDialog(if (ControllerApi.isCurrentAccount(comment.creatorId)) null else comment, null, comment.id, quoteText)
                },
                { id ->
                    for (i in get(CardComment::class)) {
                        if (i.xPublication.publication.id == id) {
                            i.flash()
                            vRecycler.scrollToPosition(indexOf(i))
                            break
                        }
                    }
                }
        )
    }

    fun setExtraScroll(extraScroll:Int){
        this.extraScroll = extraScroll
    }

    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.publicationId == publicationId && e.change < 0) {
            if (get(CardComment::class).size == 0) {
                reloadBottom()
            }
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationComment)
            if (e.notification.publicationId == publicationId) {
                needScrollToBottom = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1
                loadBottom()
            }

        if (e.notification is NotificationCommentAnswer)
            if (e.notification.publicationId == publicationId) {
                needScrollToBottom = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1
                loadBottom()
            }

    }

}
