package com.sayzen.campfiresdk.models.cards

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PagePolling
import com.dzen.campfire.api.models.units.post.PageSpoiler
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPagePolling
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageSpoiler
import com.sayzen.campfiresdk.models.events.units.*
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.layouts.LayoutMaxSizes
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads
import java.util.*

class CardPost constructor(
        private val vRecycler: RecyclerView?,
        override val unit: UnitPost,
        var onClick: ((UnitPost) -> Unit)? = null
) : CardUnit(unit) {

    private val eventBus = EventBus
            .subscribe(EventPostChanged::class) { onPostChange(it) }
            .subscribe(EventPostPublishedChange::class) { onPostPublicationChange(it) }
            .subscribe(EventPollingChanged::class) { onEventPollingChanged(it) }
            .subscribe(EventUnitReportsClear::class) { onEventUnitReportsClear(it) }
            .subscribe(EventUnitReportsAdd::class) { onEventUnitReportsAdd(it) }
            .subscribe(EventCommentRemove::class) { onEventCommentRemove2(it) }
            .subscribe(EventPostNotifyFollowers::class) { onEventPostNotifyFollowers(it) }
            .subscribe(EventUnitBlockedRemove::class) { onEventUnitBlockedRemove(it) }

    private val pages = ArrayList<CardPage>()
    private val xKarma = XKarma(unit) { updateKarma() }
    private val xAccount = XAccount(unit, unit.dateCreate) { update() }
    private var xFandom = XFandom(unit, unit.dateCreate) { update() }
    private var isShowFull = false
    private var onBack: () -> Boolean = { false }

    init {
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

    override fun getLayout() = R.layout.card_post

    private fun updatePages() {
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

    private fun addPage(page: Page){
        val card = CardPage.instance(unit, page)
        if(card is CardPageSpoiler && !isShowFull) {
            card.onClick = {
                toggleShowFull()
                update()
            }
        }
        pages.add(card)
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vPagesContainer: ViewGroup = view.findViewById(R.id.vPagesContainer)
        val vTitleContainer: ViewGroup = view.findViewById(R.id.vTitleContainer)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vReports: TextView = view.findViewById(R.id.vReports)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vContainerInfo: View = view.findViewById(R.id.vInfoContainer)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vPagesCount: TextView = view.findViewById(R.id.vPagesCount)
        val vMaxSizes: LayoutMaxSizes = view.findViewById(R.id.vMaxSizes)
        val vBestCommentRootContainer: ViewGroup = view.findViewById(R.id.vBestCommentRootContainer)
        val vBestCommentContainer: ViewGroup = view.findViewById(R.id.vBestCommentContainer)

        vComments.text = unit.subUnitsCount.toString() + ""
        vReports.text = unit.reportsCount.toString() + ""
        if (!showFandom) xAccount.setView(vAvatar)
        else xFandom.setView(vAvatar)

        vReports.visibility = if (unit.reportsCount > 0 && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE

        vPagesContainer.removeAllViews()

        vContainerInfo.visibility = if (unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        vMenu.setOnClickListener { v -> ControllerPost.showPostPopup(vMenu, unit) }

        view.setOnClickListener { v ->
            if (onClick != null)
                onClick!!.invoke(unit)
            else if (unit.status == API.STATUS_DRAFT)
                ControllerCampfireSDK.onToDraftClicked(unit.id, Navigator.TO)
            else
                ControllerCampfireSDK.onToPostClicked(unit.id, 0, Navigator.TO)
        }

        for (page in pages) {
            page.clickable = isShowFull || (page is CardPageSpoiler)
            page.postIsDraft = unit.isDraft
            val v = page.instanceView(view.context)
            page.bindView(v)
            vPagesContainer.addView(v)
        }

        if (unit.important == API.UNIT_IMPORTANT_IMPORTANT) vTitleContainer.setBackgroundColor(ToolsResources.getColor(R.color.blue_700))
        else vTitleContainer.setBackgroundColor(0x00000000)

        vPagesCount.setOnClickListener { toggleShowFull() }

        vBestCommentContainer.removeAllViews()
        vBestCommentRootContainer.visibility = if (unit.bestComment == null) View.GONE else View.VISIBLE

        if (unit.bestComment != null) {
            val cardComment = CardComment.instance(unit.bestComment!!, false, true)
            val cardCommentView = cardComment.instanceView(view.context)
            cardComment.bindView(cardCommentView)
            vBestCommentContainer.addView(cardCommentView)
        }

        vComments.setOnClickListener {
            if (onClick == null && unit.status == API.STATUS_PUBLIC)
                ControllerCampfireSDK.onToPostClicked(unit.id, -1, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            WidgetComment(unit.id, null) {  }.asSheetShow()
            true
        }

        updateShowAll()
        updateKarma()
    }

    private fun updateKarma() {
        if (getView() == null) return
        val viewKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xKarma.setView(viewKarma)
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

    private fun updateShowAll() {
        if (getView() == null) return
        val vPagesCount: TextView = getView()!!.findViewById(R.id.vPagesCount)
        val vMaxSizes: LayoutMaxSizes = getView()!!.findViewById(R.id.vMaxSizes)
        val vPagesContainer: ViewGroup = getView()!!.findViewById(R.id.vPagesContainer)

        if (isShowFull) vPagesCount.text = "${ToolsResources.s(R.string.app_hide)}"
        else vPagesCount.text = "${ToolsResources.s(R.string.app_show_all)} (${unit.pages.size})"

        vMaxSizes.setMaxHeight(if (isShowFull) 50000 else 480)

        vPagesCount.tag = this
        updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
        ToolsThreads.main(100) {
            if (vPagesCount.tag != this) return@main
            updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
            ToolsThreads.main(100) {
                if (vPagesCount.tag != this) return@main
                updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
                ToolsThreads.main(100) {
                    if (vPagesCount.tag != this) return@main
                    updateShowAll(vPagesCount, vMaxSizes, vPagesContainer)
                }
            }
        }

    }

    private fun updateShowAll(vPagesCount: TextView, vMaxSizes: LayoutMaxSizes, vPagesContainer: ViewGroup) {
        vPagesCount.visibility =  if (unit.pages.size > 2 || vMaxSizes.isCroppedH() || vPagesContainer.measuredHeight > ToolsView.dpToPx(480)) View.VISIBLE else  View.INVISIBLE
    }

    override fun notifyItem() {
        for (page in pages)
            page.notifyItem()
    }

    override fun onFandomChanged() {
        xFandom = XFandom(unit, unit.dateCreate) { update() }
    }

    //
    //  Event Bus
    //

    private fun onPostChange(e: EventPostChanged) {
        if (e.unitId == unit.id) {
            unit.pages = e.pages
            updatePages()
        }
    }

    private fun onEventUnitReportsClear(e: EventUnitReportsClear) {
        if (e.unitId == unit.id) {
            unit.reportsCount = 0
            update()
        }
    }

    private fun onEventUnitReportsAdd(e: EventUnitReportsAdd) {
        if (e.unitId == unit.id) {
            unit.reportsCount++
            update()
        }
    }

    private fun onEventPostNotifyFollowers(e: EventPostNotifyFollowers) {
        if (e.unitId == unit.id) {
            unit.tag_3 = 1
        }
    }

    private fun onEventUnitBlockedRemove(e: EventUnitBlockedRemove) {
        if (unit.bestComment != null && e.unitId == unit.bestComment!!.id) {
            unit.bestComment = null
            update()
        }
    }

    private fun onEventCommentRemove2(e: EventCommentRemove) {
        if (e.parentUnitId == unit.id && unit.bestComment != null && unit.bestComment!!.id == e.commentId) {
            unit.bestComment = null
            update()
        }
    }

    private fun onPostPublicationChange(e: EventPostPublishedChange) {
        if(e.unitId == unit.id) {
            if (e.published && unit.status == API.STATUS_DRAFT && adapter != null) adapter!!.remove(this)
            if (!e.published && unit.status == API.STATUS_PUBLIC && adapter != null) adapter!!.remove(this)
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
