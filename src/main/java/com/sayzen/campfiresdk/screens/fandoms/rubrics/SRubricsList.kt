package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.requests.rubrics.RRubricsGetAll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.rubrics.EventRubricCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SRubricsList constructor(
        private val fandomId: Long,
        private val languageId: Long,
        private val ownerId: Long,
        private val onSelected: ((Rubric)->Unit)? = null
) : SLoadingRecycler<CardRubric, Rubric>(R.layout.screen_fandoms_search) {

    val eventBus = EventBus.subscribe(EventRubricCreate::class){
        if(it.rubric.fandomId == fandomId && it.rubric.languageId == languageId) reload()
    }

    init {
        setTitle(R.string.app_rubrics)
        if(ownerId == 0L) setTextEmpty(R.string.rubric_empty)
        else if(ControllerApi.isCurrentAccount(ownerId) ) setTextEmpty(R.string.rubric_empty_my)
        else  setTextEmpty(R.string.rubric_empty_other)
        setTextProgress(R.string.rubric_loading)
        setBackgroundImage(R.drawable.bg_7)

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_RUBRIC)) (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {Navigator.to(SRubricsCreate(fandomId, languageId))}
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardRubric, Rubric> {
        return RecyclerCardAdapterLoading<CardRubric, Rubric>(CardRubric::class) {
            val card = CardRubric(it)
            if(ownerId > 0) card.showFandom = true
            if(onSelected != null) card.onClick = {
                onSelected.invoke(it)
                Navigator.remove(this)
            }
            card
        }
                .setBottomLoader { onLoad, cards ->
                    RRubricsGetAll(fandomId, languageId, ownerId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.rubrics) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
