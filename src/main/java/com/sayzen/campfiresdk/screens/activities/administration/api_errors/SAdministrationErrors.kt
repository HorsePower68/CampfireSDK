package com.sayzen.campfiresdk.screens.activities.administration.api_errors

import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.project.StatisticError
import com.dzen.campfire.api.requests.project.RProjectStatisticErrors
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SAdministrationErrors private constructor(

) : SLoadingRecycler<CardError, StatisticError>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SAdministrationErrors())
        }
    }

    init {
        setTitle(R.string.administration_errors)
        setTextEmpty(R.string.administration_errors_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardError, StatisticError> {
        return RecyclerCardAdapterLoading<CardError, StatisticError>(CardError::class) { error -> CardError(error) }
                .setBottomLoader { onLoad, cards ->
                    RProjectStatisticErrors(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.errors) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}
