package com.sayzen.campfiresdk.models.cards

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PagePolling
import com.dzen.campfire.api.models.units.post.PageSpoiler
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.cards.post_pages.*
import com.sayzen.campfiresdk.models.events.units.*
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.layouts.LayoutMaxSizes
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CardPost constructor(
        private val vRecycler: RecyclerView?,
        unit: UnitPost,
        var onClick: ((UnitPost) -> Unit)? = null
) : CardUnit(R.layout.card_unit_post, unit) {

    companion object {

        private val pagesCash = HashMap<Long, ArrayList<View>>()
        private val cashSize = 5

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
            .subscribe(EventUnitBlockedRemove::class) { onEventUnitBlockedRemove(it) }
            .subscribe(EventUnitDeepBlockRestore::class) { onEventUnitDeepBlockRestore(it) }

    private val pages = ArrayList<CardPage>()
    private var isShowFull = false
    private var onBack: () -> Boolean = { false }

    init {
        xUnit.xFandom.allViewIsClickable = true
        updateFandomOnBind = false
        updatePages()
        onBack = {
            if (Navigator.getCurrent() is PostList && (Navigator.getCurrent() as PostList).contains(this)) {
                if (isShowFull) {
                    toggleShowFull()
                    getView() != null
                } else {
                    Navigator.removeOnBack(onBack)
                    false
                }
            } else {
                false
            }
        }
    }

    private fun updatePages() {
        val unit = xUnit.unit as UnitPost

        pages.clear()

        if (unit.pages.isNotEmpty()) {

            if (isShowFull) {
                var i = 0
                while (i < unit.pages.size) {
                    val pageView = CardPage.instance(unit, unit.pages[i])
                    pages.add(pageView)
                    if (pageView is CardPageSpoiler) {
                        pageView.pages = pages
                        pageView.onClick = { update() }
                    }
                    i++
                }
                ControllerPost.updateSpoilers(pages)
            } else {

                addPage(unit.pages[0])
                if (unit.pages.size > 1)
                    if (unit.pages[0].getType() != API.PAGE_TYPE_SPOILER)
                        addPage(unit.pages[1])
                    else {
                        var leftCount = (unit.pages[0] as PageSpoiler).count
                        for (i in 1 until unit.pages.size) {
                            if (leftCount == 0) {
                                if (unit.pages.size > i) addPage(unit.pages[i])
                                break
                            }
                            if (unit.pages[i].getType() == API.PAGE_TYPE_SPOILER) leftCount += (unit.pages[i] as PageSpoiler).count
                            leftCount--
                        }

                    }
            }
        }

        update()
    }

    private fun addPage(page: Page) {
        val unit = xUnit.unit as UnitPost

        val card = CardPage.instance(unit, page)
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
        val unit = xUnit.unit as UnitPost

        val vPagesContainer: ViewGroup = view.findViewById(R.id.vPagesContainer)
        val vTitleContainer: ViewGroup = view.findViewById(R.id.vTitleContainer)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vContainerInfo: View = view.findViewById(R.id.vInfoContainer)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vPagesCount: TextView = view.findViewById(R.id.vPagesCount)
        val vBestCommentRootContainer: ViewGroup = view.findViewById(R.id.vBestCommentRootContainer)
        val vBestCommentContainer: ViewGroup = view.findViewById(R.id.vBestCommentContainer)

        vPagesContainer.removeAllViews()

        vContainerInfo.visibility = if (unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        vMenu.setOnClickListener { ControllerPost.showPostMenu(unit) }

        view.setOnClickListener {
            if (onClick != null)
                onClick!!.invoke(unit)
            else if (unit.status == API.STATUS_DRAFT)
                ControllerCampfireSDK.onToDraftClicked(unit.id, Navigator.TO)
            else if (unit.status == API.STATUS_PUBLIC)
                ControllerCampfireSDK.onToPostClicked(unit.id, 0, Navigator.TO)
        }

        for (page in pages) {
            page.clickable = isShowFull || (page is CardPageSpoiler) || (page is CardPageImage) || (page is CardPageImages)
            page.postIsDraft = unit.isDraft
            val v = Companion.getView(page, vPagesContainer)
            page.bindCardView(v)
            vPagesContainer.addView(v)
        }

        if (unit.isPined) vTitleContainer.setBackgroundColor(ToolsResources.getColor(R.color.lime_700))
        else if (unit.important == API.UNIT_IMPORTANT_IMPORTANT) vTitleContainer.setBackgroundColor(ToolsResources.getColor(R.color.blue_700))
        else vTitleContainer.setBackgroundColor(0x00000000)

        vPagesCount.setOnClickListener { toggleShowFull() }

        vBestCommentContainer.removeAllViews()
        vBestCommentRootContainer.visibility = if (unit.bestComment == null) View.GONE else View.VISIBLE

        if (unit.bestComment != null) {
            val cardComment = CardComment.instance(unit.bestComment!!, false, true)
            val cardCommentView = cardComment.instanceView(vBestCommentContainer)
            cardComment.bindCardView(cardCommentView)
            vBestCommentContainer.addView(cardCommentView)
        }
        vComments.setOnClickListener {
            if (onClick == null && unit.status == API.STATUS_PUBLIC)
                ControllerCampfireSDK.onToPostClicked(unit.id, -1, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            WidgetComment(unit.id, null) { }.asSheetShow()
            true
        }

        updateShowAll()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateAccount() {
        if (getView() == null) return
        val unit = xUnit.unit as UnitPost
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        val vKarmaCof: ViewChipMini = getView()!!.findViewById(R.id.vKarmaCof)

        if (showFandom) {
            vKarmaCof.setText("x${ToolsText.numToStringRoundAndTrim(unit.fandomKarmaCof / 100.0, 2)}")
            vKarmaCof.setBackgroundRes(if (unit.fandomKarmaCof < 100L) R.color.red_700 else R.color.green_700)
            vKarmaCof.visibility = if (unit.fandomKarmaCof > 0 && unit.fandomKarmaCof != 100L) View.VISIBLE else View.GONE
            xUnit.xFandom.setView(vAvatar)
        } else {
            vKarmaCof.visibility = View.GONE
            xUnit.xAccount.setView(vAvatar)
        }
        if (unit.status == API.STATUS_PENDING) vAvatar.setSubtitle(ToolsDate.dateToString(unit.tag_4))
        if (unit.rubricId > 0) {
            vAvatar.vSubtitle.text = vAvatar.getSubTitle() + "  " + unit.rubricName
            ToolsView.addLink(vAvatar.vSubtitle, unit.rubricName) {
                Navigator.to(SRubricPosts(unit.rubricId, unit.rubricName))
            }
        }

    }

    override fun updateComments() {
        if (getView() == null) return
        xUnit.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    override fun updateKarma() {
        if (getView() == null) return
        val viewKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xUnit.xKarma.setView(viewKarma)
    }

    override fun updateReports() {
        if (getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun toggleShowFull() {
        isShowFull = !isShowFull

        if (isShowFull) Navigator.addOnBack(onBack)
        else Navigator.removeOnBack(onBack)

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
        val unit = xUnit.unit as UnitPost

        val vPagesCount: TextView = getView()!!.findViewById(R.id.vPagesCount)
        val vMaxSizes: LayoutMaxSizes = getView()!!.findViewById(R.id.vMaxSizes)
        val vPagesContainer: ViewGroup = getView()!!.findViewById(R.id.vPagesContainer)

        if (isShowFull) vPagesCount.text = ToolsResources.s(R.string.app_hide)
        else vPagesCount.text = "${ToolsResources.s(R.string.app_show_all)} (${unit.pages.size})"

        vMaxSizes.setMaxHeight(if (isShowFull) 50000 else 300)

        vPagesCount.tag = this
        updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
        ToolsThreads.main(100) {
            if (vPagesCount.tag == this) {
                updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
                ToolsThreads.main(100) {
                    if (vPagesCount.tag == this) {
                        updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
                        ToolsThreads.main(100) {
                            if (vPagesCount.tag == this) {
                                updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
                            }
                        }
                    }
                }
            }
        }

    }


    private fun updateShowAll(vPagesCount: TextView, vMaxSizes: LayoutMaxSizes, vPagesContainer: ViewGroup) {
        val unit = xUnit.unit as UnitPost
        vPagesCount.visibility = if (unit.pages.size > 2 || vMaxSizes.isCroppedH() || vPagesContainer.measuredHeight > ToolsView.dpToPx(300)) View.VISIBLE else View.INVISIBLE
    }

    override fun notifyItem() {
        for (page in pages)
            page.notifyItem()
    }

    //
    //  Event Bus
    //

    private fun onPostChange(e: EventPostChanged) {
        val unit = xUnit.unit as UnitPost
        if (e.unitId == unit.id) {
            unit.pages = e.pages
            updatePages()
        }
    }

    private fun onEventUnitBlockedRemove(e: EventUnitBlockedRemove) {
        val unit = xUnit.unit as UnitPost
        if (unit.bestComment != null && e.unitId == unit.bestComment!!.id) {
            unit.bestComment = null
            update()
        }
    }

    private fun onEventUnitDeepBlockRestore(e: EventUnitDeepBlockRestore) {
        if (e.unitId == xUnit.unit.id && xUnit.unit.status == API.STATUS_DEEP_BLOCKED) {
            adapter?.remove(this)
        }
    }

    private fun onEventCommentRemove2(e: EventCommentRemove) {
        val unit = xUnit.unit as UnitPost
        if (e.parentUnitId == unit.id && unit.bestComment != null && unit.bestComment!!.id == e.commentId) {
            unit.bestComment = null
            update()
        }
    }

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        val unit = xUnit.unit as UnitPost
        if (e.unitId == unit.id) {
            if (e.status != API.STATUS_DRAFT && unit.status == API.STATUS_DRAFT && adapter != null) adapter!!.remove(this)
            if (e.status != API.STATUS_PUBLIC && unit.status == API.STATUS_PUBLIC && adapter != null) adapter!!.remove(this)
            if (e.status != API.STATUS_PENDING && unit.status == API.STATUS_PENDING && adapter != null) adapter!!.remove(this)
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
