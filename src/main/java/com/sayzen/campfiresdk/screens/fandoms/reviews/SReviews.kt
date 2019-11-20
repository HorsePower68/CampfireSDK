package com.sayzen.campfiresdk.screens.fandoms.reviews

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationReview
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.fandoms.RFandomsReviewGet
import com.dzen.campfire.api.requests.fandoms.RFandomsReviewGetInfo
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardReview
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewCreated
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewRemoved
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.widgets.WidgetReview
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SReviews private constructor(
        private val fandomId: Long,
        private val languageId: Long,
        private var myReviewRate: Long?,
        private var myReviewText: String?,
        private var scrollToId: Long = 0
) : SLoadingRecycler<CardReview, Publication>() {

    companion object {

        fun instance(publicationId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsReviewGet(publicationId)) { r -> SReviews(r.publication.fandomId, r.publication.languageId, if (r.myReview == null) null else r.myReview!!.rate, if (r.myReview == null) null else r.myReview!!.text, r.publication.id) }
        }

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsReviewGetInfo(fandomId, languageId)) { r -> SReviews(fandomId, languageId, if (r.myReview == null) null else r.myReview!!.rate, if (r.myReview == null) null else r.myReview!!.text) }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventFandomReviewRemoved::class) {
                if (it.fandomId == fandomId && it.languageId == languageId) {
                    myReviewRate = null
                    myReviewText = null
                    update()
                }
            }
            .subscribe(EventFandomReviewCreated::class) {
                if (it.fandomId == fandomId && it.languageId == languageId) {
                    myReviewRate = it.rate
                    myReviewText = it.text
                    update()
                }
            }

    init {
        setBackgroundImage(R.drawable.bg_12)
        setTitle(R.string.app_reviews)
        setTextEmpty(R.string.fandom_review_empty)

        (vFab as View).visibility = View.VISIBLE
        update()
    }

    private fun update() {
        if (myReviewRate != null) {
            vFab.setImageResource(R.drawable.ic_mode_edit_white_24dp)
            vFab.setOnClickListener {
                WidgetReview(fandomId, languageId, myReviewRate, myReviewText) {
                    reload()
                }.asSheetShow()
            }
        } else {
            vFab.setImageResource(R.drawable.ic_add_white_24dp)
            vFab.setOnClickListener {
                WidgetReview(fandomId, languageId, null, null) {
                    reload()
                }.asSheetShow()
            }
        }


    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardReview, Publication> {
        return RecyclerCardAdapterLoading<CardReview, Publication>(CardReview::class) { CardReview(it as PublicationReview) }
                .setBottomLoader { onLoad, cards ->
                    val r = RUnitsGetAll()
                            .setCount(20)
                            .setFandomId(fandomId)
                            .setLanguageId(languageId)
                            .setPublicationTypes(API.PUBLICATION_TYPE_REVIEW)
                            .setOffset(cards.size.toLong())
                            .setOrder(RUnitsGetAll.ORDER_NEW)
                            .onComplete { r ->
                                var b = false
                                for (u in r.publications) b = b || ControllerApi.isCurrentAccount(u.creatorId)
                                onLoad.invoke(r.publications)
                                onPackLoaded()
                            }
                            .onNetworkError { onLoad.invoke(null) }
                    r.tokenRequired = false
                    r.send(api)
                }
                .addOnLoadedNotEmpty { onPackLoaded() }
    }

    private fun onPackLoaded() {
        adapter!!.remove(CardSpace::class)
        adapter!!.add(CardSpace(64))

        if (scrollToId != 0L) {
            for (c in adapter!!.get(CardReview::class)) {
                if (c.xPublication.publication.id == scrollToId) {
                    scrollToId = 0
                    ToolsThreads.main(600) {
                        vRecycler.scrollToPosition(adapter!!.indexOf(c) + 1)
                        c.flash()
                    }
                }
            }
            if (scrollToId != 0L) {
                ToolsToast.show(R.string.review_error_gone)
                scrollToId = 0
            }
        }

    }


}