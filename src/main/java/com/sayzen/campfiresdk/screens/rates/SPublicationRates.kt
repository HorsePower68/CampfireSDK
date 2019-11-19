package com.sayzen.campfiresdk.screens.rates

import android.view.View
import android.widget.CheckBox
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.post.RPostRatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SPublicationRates(
        val publicationId: Long,
        karmaCount: Long,
        myKarma: Long,
        val creatorId: Long,
        unitStatus: Long

) : SLoadingRecycler<CardRateText, Rate>(R.layout.screen_unit_rates) {

    val vMenuContainer: View = findViewById(R.id.vMenuContainer)
    val vAnon: CheckBox = findViewById(R.id.vAnon)
    val vKarma: ViewKarma = findViewById(R.id.vKarma)

    val xKarma = XKarma(publicationId, karmaCount, myKarma, creatorId, unitStatus) { updateKarma() }
    var lastReloadKarma = xKarma.karmaCount

    init {
        setTitle(R.string.app_rates)
        setTextEmpty(R.string.post_rates_empty)

        updateKarma()
    }

    fun updateKarma() {
        if (lastReloadKarma != xKarma.karmaCount) {
            lastReloadKarma = xKarma.karmaCount
            reload()
        }
        if (xKarma.myKarma == 0L && !ControllerApi.isCurrentAccount(creatorId)) {
            xKarma.setView(vKarma)
            vAnon.setOnClickListener {
                xKarma.anon = vAnon.isChecked
            }
            vAnon.isChecked = xKarma.anon
        } else {
            vMenuContainer.visibility = View.GONE
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardRateText, Rate> {
        return RecyclerCardAdapterLoading<CardRateText, Rate>(CardRateText::class) { ac -> CardRateText(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RPostRatesGetAll(publicationId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.rates) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
