package com.sayzen.campfiresdk.screens.fandoms.view


import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminChangeParams
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomParamsChanged
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.objects.FandomParam
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus

class CardParams(
        private val xFandom: XFandom,
        private var params: Array<Long>,
        private val categoryId: Long,
        private val paramsPosition: Int
) : Card(R.layout.screen_fandom_card_params) {

    private val eventBus = EventBus
            .subscribe(EventFandomParamsChanged::class) { onEventFandomParamsChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vContainer: ViewGroup = view.findViewById(R.id.vContainer)
        val vAdd: ViewIcon = view.findViewById(R.id.vAdd)
        val vEmptyText: TextView = view.findViewById(R.id.vEmptyText)

        vContainer.removeAllViews()

        vAdd.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_PARAMS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { change() }

        vEmptyText.text = if (params.isEmpty() && CampfireConstants.getParamTitle(categoryId, paramsPosition) != null) CampfireConstants.getParamTitle(categoryId, paramsPosition)!! else ""

        for (i in params) {
            val v = ViewChip.instance(view.context)
            v.text = getParam(i).name
            v.setOnClickListener { onClick(i) }
            vContainer.addView(v)
        }
    }

    private fun change() {
        WidgetParams(CampfireConstants.getParamTitle(categoryId, paramsPosition)!!, CampfireConstants.getParams(categoryId, paramsPosition)!!, params) { newParams, comment ->
            ApiRequestsSupporter.executeEnabledConfirm(R.string.fandoms_menu_change_params_confirms, R.string.app_change, RFandomsAdminChangeParams(xFandom.fandomId, categoryId, paramsPosition, newParams, comment)) { r ->
                EventBus.post(EventFandomParamsChanged(xFandom.fandomId, xFandom.languageId, categoryId, paramsPosition, newParams))
                ToolsToast.show(R.string.app_done)
            }
        }.asSheetShow()
    }

    fun resetParams(params: Array<Long>) {
        this.params = params
        update()
    }

    fun getParam(index: Long): FandomParam {
        for (a in CampfireConstants.getParams(categoryId, paramsPosition)!!)
            if (a.index == index) return a
        throw java.lang.RuntimeException("Unknown index $index")
    }

    //
    //  EventBus
    //

    private fun onEventFandomParamsChanged(e: EventFandomParamsChanged) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId && e.categoryId == categoryId && e.paramsPosition == paramsPosition) {
            params = e.params
            update()
        }
    }

    private fun onClick(paramIndex: Long) {
        when (paramsPosition) {
            1 -> SFandomsSearch.instance("", categoryId, arrayOf(paramIndex), emptyArray(), emptyArray(), emptyArray(), Navigator.TO)
            2 -> SFandomsSearch.instance("", categoryId, emptyArray(), arrayOf(paramIndex), emptyArray(), emptyArray(), Navigator.TO)
            3 -> SFandomsSearch.instance("", categoryId, emptyArray(), emptyArray(), arrayOf(paramIndex), emptyArray(), Navigator.TO)
            4 -> SFandomsSearch.instance("", categoryId, emptyArray(), emptyArray(), emptyArray(), arrayOf(paramIndex), Navigator.TO)
            else -> throw java.lang.RuntimeException("Unknown paramsPosition $paramsPosition")
        }
    }

}
