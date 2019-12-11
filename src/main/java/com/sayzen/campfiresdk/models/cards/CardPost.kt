package com.sayzen.campfiresdk.models.cards

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.cards.post_pages.*
import com.sayzen.campfiresdk.models.events.publications.*
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.layouts.LayoutMaxSizes
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CardPost constructor(
        private val vRecycler: RecyclerView?,
        publication: PublicationPost,
        var onClick: ((PublicationPost) -> Unit)? = null
) : CardPublication(R.layout.card_publication_post, publication) {

    companion object {

        private val pagesCash = HashMap<Long, ArrayList<View>>()
        private val cashSize = 5

        init {
            SupAndroid.addOnLowMemory {
                pagesCash.clear()
            }
        }

        private fun getList(card: CardPage): ArrayList<View> {
            var list = pagesCash[card.page.getType()]
            if (list == null) {
                list = ArrayList()
                pagesCash[card.page.getType()] = list
            }
            return list
        }

        fun getView(card: CardPage, vParent: ViewGroup): View {
            val list = getList(card)
            if (list.isEmpty()) {
                return card.instanceView(vParent)
            } else {
                return ToolsView.removeFromParent(list.removeAt(0))
            }
        }

        fun putView(card: CardPage) {
            val view = card.getView()
            if (view != null) {
                val list = getList(card)
                if (list.size < cashSize) {
                    card.detachView()
                    getList(card).add(view)
                }
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventPostChanged::class) { onPostChange(it) }
            .subscribe(EventPostStatusChange::class) { onEventPostStatusChange(it) }
            .subscribe(EventPollingChanged::class) { onEventPollingChanged(it) }
            .subscribe(EventCommentRemove::class) { onEventCommentRemove2(it) }
            .subscribe(EventPublicationBlockedRemove::class) { onEventPublicationBlockedRemove(it) }
            .subscribe(EventPublicationDeepBlockRestore::class) { onEventPublicationDeepBlockRestore(it) }

    private val pages = ArrayList<CardPage>()
    private var isShowFull = false
    private var onBack: () -> Boolean = { false }

    init {
        xPublication.xFandom.allViewIsClickable = true
        updateFandomOnBind = false
        updatePages()
    }

    private fun updatePages() {
        val publication = xPublication.publication as PublicationPost

        val page_0 = if (pages.size > 0) pages[0] else null
        val page_1 = if (pages.size > 1) pages[1] else null
        pages.clear()

        if (publication.pages.isNotEmpty()) {

            if (isShowFull) {
                var i = 0
                while (i < publication.pages.size) {
                    val pageCard = if (i == 0 && page_0 != null) page_0 else if (i == 1 && page_1 != null) page_1 else CardPage.instance(publication, publication.pages[i])

                    pages.add(pageCard)
                    if (pageCard is CardPageSpoiler) {
                        pageCard.pages = pages
                        pageCard.onClick = { update() }
                    }
                    i++
                }
                ControllerPost.updateSpoilers(pages)
            } else {

                if (page_0 != null) addPage(page_0) else addPage(publication.pages[0])

                if (publication.pages.size > 1)
                    if (publication.pages[0].getType() != API.PAGE_TYPE_SPOILER)
                        if (page_1 != null) addPage(page_1) else addPage(publication.pages[1])
                    else {
                        var leftCount = (publication.pages[0] as PageSpoiler).count
                        for (i in 1 until publication.pages.size) {
                            if (leftCount == 0) {
                                if (publication.pages.size > i) {
                                    if (i == 1 && page_1 != null) addPage(page_1) else addPage(publication.pages[i])
                                }
                                break
                            }
                            if (publication.pages[i].getType() == API.PAGE_TYPE_SPOILER) leftCount += (publication.pages[i] as PageSpoiler).count
                            leftCount--
                        }

                    }
            }
        }

        update()
    }

    private fun addPage(page: Page) {
        addPage(CardPage.instance(xPublication.publication as PublicationPost, page))
    }

    private fun addPage(card: CardPage) {
        if (card is CardPageSpoiler && !isShowFull) {
            card.onClick = {
                toggleShowFull()
                update()
            }
        }
        pages.add(card)
    }

    override fun onDetachView() {
        for (page in pages) {
            putView(page)
        }
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationPost

        val vPagesContainer: ViewGroup = view.findViewById(R.id.vPagesContainer)
        val vTitleContainer: ViewGroup = view.findViewById(R.id.vTitleContainer)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vContainerInfo: View = view.findViewById(R.id.vInfoContainer)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vPagesCount: TextView = view.findViewById(R.id.vPagesCount)
        val vBestCommentRootContainer: ViewGroup = view.findViewById(R.id.vBestCommentRootContainer)
        val vBestCommentContainer: ViewGroup = view.findViewById(R.id.vBestCommentContainer)

        vPagesContainer.removeAllViews()

        vContainerInfo.visibility = if (publication.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        vMenu.setOnClickListener { ControllerPost.showPostMenu(publication) }

        view.setOnClickListener {
            if (onClick != null)
                onClick!!.invoke(publication)
            else if (publication.status == API.STATUS_DRAFT)
                ControllerCampfireSDK.onToDraftClicked(publication.id, Navigator.TO)
            else if (publication.status == API.STATUS_PUBLIC)
                ControllerCampfireSDK.onToPostClicked(publication.id, 0, Navigator.TO)
        }

        for (page in pages) {
            page.clickable = isShowFull || (page is CardPageSpoiler) || (page is CardPageImage) || (page is CardPageImages)
            page.postIsDraft = publication.isDraft
            val pageView = page.getView()
            val v = pageView ?: Companion.getView(page, vPagesContainer)
            page.bindCardView(v)
            vPagesContainer.addView(ToolsView.removeFromParent(v))
        }

        if (publication.isPined) vTitleContainer.setBackgroundColor(ToolsResources.getColor(R.color.lime_700))
        else if (publication.important == API.PUBLICATION_IMPORTANT_IMPORTANT) vTitleContainer.setBackgroundColor(ToolsResources.getColor(R.color.blue_700))
        else vTitleContainer.setBackgroundColor(0x00000000)

        vPagesCount.setOnClickListener { toggleShowFull() }

        vBestCommentContainer.removeAllViews()
        vBestCommentRootContainer.visibility = if (publication.bestComment == null) View.GONE else View.VISIBLE

        if (publication.bestComment != null) {
            val cardComment = CardComment.instance(publication.bestComment!!, false, true)
            val cardCommentView = cardComment.instanceView(vBestCommentContainer)
            cardComment.bindCardView(cardCommentView)
            vBestCommentContainer.addView(cardCommentView)
        }
        vComments.setOnClickListener {
            if (onClick == null && publication.status == API.STATUS_PUBLIC)
                ControllerCampfireSDK.onToPostClicked(publication.id, -1, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            WidgetComment(publication.id, null, true) { }.asSheetShow()
            true
        }

        updateShowAll()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateAccount() {
        if (getView() == null) return
        val publication = xPublication.publication as PublicationPost
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        val vKarmaCof: TextView = getView()!!.findViewById(R.id.vKarmaCof)

        if (showFandom) {
            vKarmaCof.setText("x${ToolsText.numToStringRoundAndTrim(publication.fandomKarmaCof / 100.0, 2)}")
            vKarmaCof.visibility = if (publication.fandomKarmaCof > 0 && publication.fandomKarmaCof != 100L) View.VISIBLE else View.GONE
            xPublication.xFandom.setView(vAvatar)
        } else {
            vKarmaCof.visibility = View.GONE
            xPublication.xAccount.setView(vAvatar)
        }
        if (publication.status == API.STATUS_PENDING) vAvatar.setSubtitle(ToolsDate.dateToString(publication.tag_4))
        if (publication.rubricId > 0) {
            vAvatar.vSubtitle.text = vAvatar.getSubTitle() + "  " + publication.rubricName
            ToolsView.addLink(vAvatar.vSubtitle, publication.rubricName) {
                SRubricPosts.instance(publication.rubricId, Navigator.TO)
            }
        }

    }

    override fun updateComments() {
        if (getView() == null) return
        xPublication.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    override fun updateKarma() {
        if (getView() == null) return
        val viewKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xPublication.xKarma.setView(viewKarma)
    }

    override fun updateReports() {
        if (getView() == null) return
        xPublication.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun toggleShowFull() {
        isShowFull = !isShowFull

        Navigator.removeOnBack(onBack)
        if (isShowFull) {
            onBack = {
                if (Navigator.getCurrent() is PostList && (Navigator.getCurrent() as PostList).contains(this)) {
                    if (isShowFull) {
                        toggleShowFull()
                        getView() != null && getView()!!.findViewById<View>(R.id.vPagesCount).tag == this
                    } else false
                } else false

            }
            Navigator.addOnBack(onBack)
        }

        updatePages()
        updateShowAll()

        if (!isShowFull && adapter != null && vRecycler != null) {
            val index = adapter!!.indexOf(this)
            if (index > -1 && index < adapter!!.size() - 1) vRecycler.scrollToPosition(index + 1)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateShowAll() {
        if (getView() == null) return
        val publication = xPublication.publication as PublicationPost

        val vPagesCount: TextView = getView()!!.findViewById(R.id.vPagesCount)
        val vMaxSizes: LayoutMaxSizes = getView()!!.findViewById(R.id.vMaxSizes)

        vPagesCount.tag = this
        vMaxSizes.onMeasureFinish = { updateShowAllUpdateCounter() }

        if (isShowFull) vPagesCount.text = ToolsResources.s(R.string.app_hide)
        else vPagesCount.text = "${ToolsResources.s(R.string.app_show_all)} (${publication.pages.size})"

        vMaxSizes.setMaxHeight(if (isShowFull) 50000 else 300)

        updateShowAllUpdateCounter()
    }

    private fun updateShowAllUpdateCounter() {
        if (getView() == null) return
        val publication = xPublication.publication as PublicationPost

        val vPagesCount: TextView = getView()!!.findViewById(R.id.vPagesCount)
        val vMaxSizes: LayoutMaxSizes = getView()!!.findViewById(R.id.vMaxSizes)

        ToolsThreads.main(true) {
            vPagesCount.visibility = if (publication.pages.size > 2 || vMaxSizes.isCroppedH() || isShowFull) View.VISIBLE else View.INVISIBLE
        }
    }


    override fun notifyItem() {
        for (page in pages)
            page.notifyItem()
    }

    //
    //  Event Bus
    //

    private fun onPostChange(e: EventPostChanged) {
        val publication = xPublication.publication as PublicationPost
        if (e.publicationId == publication.id) {
            publication.pages = e.pages
            pages.clear()
            updatePages()
        }
    }

    private fun onEventPublicationBlockedRemove(e: EventPublicationBlockedRemove) {
        val publication = xPublication.publication as PublicationPost
        if (publication.bestComment != null && e.publicationId == publication.bestComment!!.id) {
            publication.bestComment = null
            update()
        }
    }

    private fun onEventPublicationDeepBlockRestore(e: EventPublicationDeepBlockRestore) {
        if (e.publicationId == xPublication.publication.id && xPublication.publication.status == API.STATUS_DEEP_BLOCKED) {
            adapter?.remove(this)
        }
    }

    private fun onEventCommentRemove2(e: EventCommentRemove) {
        val publication = xPublication.publication as PublicationPost
        if (e.parentPublicationId == publication.id && publication.bestComment != null && publication.bestComment!!.id == e.commentId) {
            publication.bestComment = null
            update()
        }
    }

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        val publication = xPublication.publication as PublicationPost
        if (e.publicationId == publication.id) {
            if (e.status != API.STATUS_DRAFT && publication.status == API.STATUS_DRAFT && adapter != null) adapter!!.remove(this)
            if (e.status != API.STATUS_PUBLIC && publication.status == API.STATUS_PUBLIC && adapter != null) adapter!!.remove(this)
            if (e.status != API.STATUS_PENDING && publication.status == API.STATUS_PENDING && adapter != null) adapter!!.remove(this)
        }
    }

    private fun onEventPollingChanged(e: EventPollingChanged) {
        for (page in pages) {
            if (page is CardPagePolling && (page.page as PagePolling).pollingId == e.pollingId) {
                update()
                return
            }
        }
    }
}
