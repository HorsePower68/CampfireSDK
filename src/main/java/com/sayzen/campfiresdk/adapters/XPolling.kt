package com.sayzen.campfiresdk.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPolling
import com.sayzen.campfiresdk.models.events.publications.EventPollingChanged
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewProgressCircle
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class XPolling(
        val page: PagePolling,
        val pagesContainer: PagesContainer?,
        val isEditModeProvider: ()->Boolean,
        val ispostIsDraftProvider: ()->Boolean,
        var onChanged: () -> Unit
) {

    val eventBud = EventBus.subscribe(EventPollingChanged::class) {
        if (it.pollingId == page.pollingId) onChanged.invoke()
    }

    fun setView(view: View) {
        updateLimits(view)
        updateTitle(view)
        updateVotes(view)
    }

    private fun updateVotes(view: View){
        val vContainer: ViewGroup = view.findViewById(R.id.vContainer)
        val tag = System.currentTimeMillis()
        vContainer.tag = tag
        vContainer.removeAllViews()

        for (s in page.options) {
            val vItem: View = ToolsView.inflate(R.layout.card_page_polling_item)
            val vText: TextView = vItem.findViewById(R.id.vText)
            vText.text = s
            vContainer.addView(vItem)
        }

        if (!isEditModeProvider.invoke() && !ispostIsDraftProvider.invoke()) {
            ControllerPolling.get(page.pollingId) { result ->

                if (vContainer.tag != tag) return@get

                val showResults = result.voted || !canVote()

                var percentSum = 0
                for (i in 0 until vContainer.childCount) {
                    val vItem: View = vContainer.getChildAt(i)
                    val vCount: TextView = vItem.findViewById(R.id.vCount)
                    val vPercent: TextView = vItem.findViewById(R.id.vPercent)
                    val vTouch: View = vItem.findViewById(R.id.vTouch)
                    val vLine1: View = vItem.findViewById(R.id.vLine1)
                    val vLine2: View = vItem.findViewById(R.id.vLine2)
                    val vProgress: ViewProgressCircle = vItem.findViewById(R.id.vProgress)

                    vCount.visibility = if (showResults) View.VISIBLE else View.INVISIBLE
                    vPercent.visibility = if (showResults) View.VISIBLE else View.INVISIBLE
                    vProgress.visibility = View.INVISIBLE

                    val percent = (result.count(i.toLong()).toFloat() / result.totalVotes * 100).toInt()
                    percentSum += percent
                    if (i == vContainer.childCount - 1) {
                        for (n in 0 until vContainer.childCount) {
                            val p = (result.count(n.toLong()).toFloat() / result.totalVotes * 100).toInt()
                            if (p > 0) {
                                (vContainer.getChildAt(n).findViewById(R.id.vPercent) as TextView).text = "${p + (100 - percentSum)}%"
                                break
                            }
                        }
                    }

                    if (showResults) {
                        vCount.text = "(${result.count(i.toLong())})"
                        if (result.totalVotes == 0L) vPercent.text = "0%"
                        else vPercent.text = "$percent%"
                        (vLine1.layoutParams as LinearLayout.LayoutParams).weight = 100 - percent.toFloat()
                        (vLine2.layoutParams as LinearLayout.LayoutParams).weight = percent.toFloat()
                        vLine1.requestLayout()
                    }

                    if (result.myVoteItemId == i.toLong()) {
                        vLine1.setBackgroundColor(ToolsResources.getAccentColor(vLine1.context))
                    } else {
                        vLine1.setBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
                    }

                    if (!showResults) {
                        vTouch.setOnClickListener {
                            if (pagesContainer != null)
                                ControllerPolling.vote(pagesContainer.getSourceType(), pagesContainer.getSourcId(), pagesContainer.getSourceIdSub(), page.pollingId, i.toLong())
                        }
                    }

                    val startTime = ControllerPolling.getStartTime(page.pollingId)
                    val startItem = ControllerPolling.getStartItem(page.pollingId)

                    if(startTime > 0 && startItem == i.toLong()){

                        val itemTag = System.currentTimeMillis()
                        vItem.tag = itemTag
                        ToolsThreads.timerMain(10){
                            if(vItem.tag != itemTag || ControllerPolling.getStartTime(page.pollingId) <= 0L || ControllerPolling.getStartItem(page.pollingId) != i.toLong()){
                                it.unsubscribe()
                                return@timerMain
                            }
                            vProgress.visibility = View.VISIBLE
                            vProgress.setProgress(System.currentTimeMillis() - startTime, CampfireConstants.VOTE_TIME)
                        }
                    }

                }
            }
        }

    }

    private fun updateTitle(view: View){
        val vTitle: ViewTextLinkable = view.findViewById(R.id.vTitle)

        ControllerLinks.makeLinkable(vTitle)

        vTitle.visibility = if (page.title.isEmpty()) View.GONE else View.VISIBLE
        vTitle.text = page.title

    }

    private fun updateLimits(view: View){
        val vLimit: ViewTextLinkable = view.findViewById(R.id.vLimit)

        if (page.minKarma <= 0 && page.minLevel <= 0) {
            vLimit.visibility = View.GONE
        } else {
            vLimit.visibility = View.VISIBLE
            vLimit.text = "${ToolsResources.s(R.string.app_limitations)}: "
            if (page.minLevel > 0) vLimit.text = "${vLimit.text} ${ToolsResources.s(R.string.app_level)} ${((page.minLevel / 100).toInt())}  "
            if (page.minKarma > 0) vLimit.text = "${vLimit.text} ${ToolsResources.s(R.string.app_karma)} ${((page.minKarma / 100).toInt())}"
            vLimit.setTextColor(ToolsResources.getColor(if (!canVote()) R.color.red_700 else R.color.green_700))
        }

    }

    private fun canVote() = ControllerApi.account.lvl >= page.minLevel && ControllerApi.account.karma30 >= page.minKarma



}
