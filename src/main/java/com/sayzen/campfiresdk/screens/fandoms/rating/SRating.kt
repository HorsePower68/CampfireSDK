package com.sayzen.campfiresdk.screens.fandoms.rating

import com.dzen.campfire.api.requests.fandoms.RFandomsRatingGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SRating private constructor(
        val r: RFandomsRatingGet.Response
) : SLoadingRecycler<CardRating, CardRating>() {

    companion object {

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RFandomsRatingGet(fandomId, languageId)) { r ->
                SRating(r)
            }
        }
    }

    init {
        setTitle(R.string.app_karma_count_30_days)
        setTextEmpty(R.string.app_empty)
        setBackgroundImage(R.drawable.bg_10)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardRating, CardRating> {
        return RecyclerCardAdapterLoading<CardRating, CardRating>(CardRating::class) { it }
                .setBottomLoader { onLoad, cards ->
                    if (cards.isNotEmpty()) {
                        onLoad.invoke(emptyArray())
                        return@setBottomLoader
                    }
                    val list = ArrayList<CardRating>()
                    var index = 1
                    for (i in r.karmaAccounts.indices) {
                        val c = CardRating(r.karmaAccounts[i], (r.karmaCounts[i] / 100).toString() + "")
                        c.setTextColor(ToolsResources.getColor(R.color.green_700))
                        c.setIndex(index++)
                        list.add(c)
                    }
                    onLoad.invoke(list.toTypedArray())
                }
    }




}
