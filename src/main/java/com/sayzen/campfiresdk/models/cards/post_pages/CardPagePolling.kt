package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.models.PagesContainer
import com.dzen.campfire.api.models.units.post.PagePolling
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPolling
import com.sayzen.campfiresdk.models.events.units.EventPollingChanged
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable

import com.sup.dev.java.libs.eventBus.EventBus

class CardPagePolling(
        pagesContainer: PagesContainer?,
        page: PagePolling
) : CardPage(R.layout.card_page_polling, pagesContainer, page) {

    val eventBud = EventBus.subscribe(EventPollingChanged::class) {
        if (it.pollingId == page.pollingId) update()
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vContainer: ViewGroup = view.findViewById(R.id.vContainer)
        val vTitle: ViewTextLinkable = view.findViewById(R.id.vTitle)

        ControllerApi.makeLinkable(vTitle)

        vTitle.visibility = if((page as PagePolling).title.isEmpty()) View.GONE else View.VISIBLE
        vTitle.text = (page as PagePolling).title

        val tag = System.currentTimeMillis()
        vContainer.tag = tag
        vContainer.removeAllViews()

        for (s in (page as PagePolling).options) {
            val vItem: View = ToolsView.inflate(R.layout.card_page_polling_item)
            val vText: TextView = vItem.findViewById(R.id.vText)

            vText.text = s

            vContainer.addView(vItem)
        }
        if (!editMode && !postIsDraft) {
            ControllerPolling.get((page as PagePolling).pollingId) { result ->
                if (vContainer.tag != tag) return@get

                var percentSum = 0
                for (i in 0 until vContainer.childCount) {
                    val vItem: View = vContainer.getChildAt(i)
                    val vCount: TextView = vItem.findViewById(R.id.vCount)
                    val vPercent: TextView = vItem.findViewById(R.id.vPercent)
                    val vTouch: View = vItem.findViewById(R.id.vTouch)
                    val vLine1: View = vItem.findViewById(R.id.vLine1)
                    val vLine2: View = vItem.findViewById(R.id.vLine2)

                    vCount.visibility = if (result.voted) View.VISIBLE else View.GONE
                    vPercent.visibility = if (result.voted) View.VISIBLE else View.GONE

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

                    if (result.voted) {
                        vCount.text = "(${result.count(i.toLong())})"
                        if (result.totalVotes == 0L) vPercent.text = "0%"
                        else vPercent.text = "$percent%"
                        (vLine1.layoutParams as LinearLayout.LayoutParams).weight = 100 - percent.toFloat()
                        (vLine2.layoutParams as LinearLayout.LayoutParams).weight = percent.toFloat()
                    }

                    if (result.myVoteItemId == i.toLong()) {
                        vLine1.setBackgroundColor(ToolsResources.getAccentColor(vLine1.context))
                    } else {
                        vLine1.setBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
                    }

                    if (!result.voted)
                        vTouch.setOnClickListener {
                            ControllerPolling.vote((page as PagePolling).pollingId, i.toLong())
                        }

                }
            }
        }

    }


    override fun notifyItem() {}
}