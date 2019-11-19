package com.sayzen.campfiresdk.screens.activities.administration.api_query

import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.project.StatisticQuery
import com.dzen.campfire.api.requests.project.RProjectStatisticQuery
import com.dzen.campfire.api.requests.project.RProjectStatisticRequests
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.widgets.WidgetMenu

class SAdministrationQuery private constructor(

) : SLoadingRecycler<CardQuery, StatisticQuery>() {

    companion object {

        private val KEY = "SAdministrationRequests_Sort"

        fun instance(action: NavigationAction) {
            Navigator.action(action, SAdministrationQuery())
        }
    }

    init {
        setTitle(R.string.administration_requests)

        addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_tune_24dp)) {
            WidgetMenu()
                    .add(R.string.administration_requests_f_middle) { _, _ ->
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_AVG)
                        reload()
                    }
                    .add(R.string.administration_requests_f_total) { _, _ ->
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_TOTAL)
                        reload()
                    }
                    .add(R.string.administration_requests_f_count) { _, _ ->
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_COUNT)
                        reload()
                    }
                    .add(R.string.administration_requests_f_max) { _, _ ->
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_MAX)
                        reload()
                    }
                    .asSheetShow()
        }

    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardQuery, StatisticQuery> {
        return RecyclerCardAdapterLoading<CardQuery, StatisticQuery>(CardQuery::class) { CardQuery(it) }
                .setBottomLoader { onLoad, cards ->
                    RProjectStatisticQuery(ToolsStorage.getLong(KEY, RProjectStatisticRequests.SORT_AVG), cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.statistic) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}
