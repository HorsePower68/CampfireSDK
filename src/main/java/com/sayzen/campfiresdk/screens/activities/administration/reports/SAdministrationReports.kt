package com.sayzen.campfiresdk.screens.activities.administration.reports

import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsReportedGetAll
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SAdministrationReports : SLoadingRecycler<CardPublication, Publication>() {

    private var languages = arrayListOf(*ControllerSettings.adminReportsLanguages)

    private val eventBus = EventBus.subscribe(EventPublicationReportsClear::class) {
        if (adapter != null) for (c in adapter!!.get(CardPublication::class)) if (c.xPublication.publication.id == it.publicationId) adapter?.remove(c)
    }

    init {
        vScreenRoot?.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_15)
        setTitle(R.string.moderation_screen_reports)
        setTextEmpty(R.string.moderation_screen_reports_empty)
        addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_translate_24dp)){
            ControllerCampfireSDK.createLanguageCheckMenu(languages)
                    .setOnEnter(R.string.app_save)
                    .setOnHide {
                        ControllerSettings.adminReportsLanguages = languages.toTypedArray()
                        reload()
                    }
                    .asSheetShow()
        }
    }

    override fun reload() {
        adapter?.clear()
        super.reload()
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { CardPublication.instance(it, null, false, false, isShowReports= true) }
                .setBottomLoader { onLoad, cards ->
                    RPublicationsReportedGetAll(0, languages.toTypedArray(), cards.size.toLong())
                            .onComplete { r ->
                                onLoad.invoke(r.publications)
                                ToolsThreads.main { afterPackLoaded() }
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun afterPackLoaded() {
        var i = 0
        while (i < adapter!!.size()) {
            if (adapter!![i] is CardPublication) {
                if (i != adapter!!.size() - 1) {
                    if (adapter!![i + 1] is CardPublication) {
                        adapter!!.add(i + 1, CardUnitReport(adapter!![i] as CardPublication))
                        i++
                    }
                } else {
                    adapter!!.add(i + 1, CardUnitReport((adapter!![i] as CardPublication)))
                    i++
                }
            }
            i++
        }
    }


}