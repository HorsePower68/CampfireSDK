package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewCreated
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewRemoved
import com.sayzen.campfiresdk.screens.fandoms.reviews.SReviews
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText

class CardReview(
        val xFandom: XFandom,
        var rates: Array<Long>
) : Card(R.layout.screen_fandom_card_review) {

    private val eventBus = EventBus
            .subscribe(EventFandomReviewCreated::class) { onEventFandomReviewCreated(it) }
            .subscribe(EventFandomReviewRemoved::class) { onEventFandomReviewRemoved(it) }
            .subscribe(EventFandomReviewChanged::class) { onEventFandomReviewChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vReviews: TextView = view.findViewById(R.id.vReviews)
        val vTouch: View = view.findViewById(R.id.vTouch)

        if(rates.isNotEmpty()) {
            var rate = 0f
            for (i in rates) rate += i
            rate /= rates.size
            vTitle.text = "${ToolsText.numToStringRound(rate.toDouble(), 2)}"
            vReviews.text = "${rates.size} " + ToolsResources.getPlural(R.plurals.review, rates.size)
        }else{
            vTitle.text = "-"
            vReviews.setText(R.string.fandom_review_empty)
        }

        vTouch.setOnClickListener{ SReviews.instance(xFandom.fandomId, xFandom.languageId, Navigator.TO) }
    }

    private fun addRate(rate:Long){
        rates = ToolsCollections.add(rate, rates)
        update()
    }

    private fun removeRate(rate:Long){
        val list = ArrayList<Long>()
        var removed = false
        for (i in rates) {
            if (!removed && i == rate) {
                removed = true
            } else {
                list.add(i)
            }
        }
        rates = list.toTypedArray()
        update()
    }

    //
    //  EventBus
    //

    private fun onEventFandomReviewCreated(e: EventFandomReviewCreated) {
        if (xFandom.fandomId == e.fandomId && xFandom.languageId == e.languageId) {
            addRate(e.rate)
        }
    }

    private fun onEventFandomReviewRemoved(e: EventFandomReviewRemoved) {
        if (xFandom.fandomId == e.fandomId && xFandom.languageId == e.languageId) {
            removeRate(e.rate)
        }
    }
    private fun onEventFandomReviewChanged(e: EventFandomReviewChanged) {
        if (xFandom.fandomId == e.fandomId && xFandom.languageId == e.languageId) {
            removeRate(e.rateBefore)
            addRate(e.rateNew)
            update()
        }
    }

}
