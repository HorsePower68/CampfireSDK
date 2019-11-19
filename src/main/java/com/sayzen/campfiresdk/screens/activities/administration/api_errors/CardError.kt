package com.sayzen.campfiresdk.screens.activities.administration.api_errors

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.project.StatisticError
import com.dzen.campfire.api.requests.project.RProjectStatisticErrorsRemove
import com.sayzen.campfiresdk.models.events.project.EventStatisticErrorRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus

class CardError(
        val error: StatisticError
) : Card(R.layout.screen_administration_card_error) {

    private val eventBus = EventBus
            .subscribe(EventStatisticErrorRemove::class) {
                if (error.key == it.key && adapter != null) adapter!!.remove(this)
            }

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: TextView = view.findViewById(R.id.vText)
        val vCount: TextView = view.findViewById(R.id.vCount)
        val vVersion: TextView = view.findViewById(R.id.vVersion)

        vText.text = error.key
        vCount.text = "${error.count}"
        vVersion.text = if (error.version.isEmpty()) "Old API" else error.version
        vVersion.setTextColor(ToolsResources.getColor(if (error.version == API.VERSION) R.color.grey_300 else R.color.red_700))

        view.setOnClickListener {
            WidgetAlert()
                    .setTitle(error.key)
                    .setText(error.stack)
                    .asScreenTo()
        }
        view.setOnLongClickListener {
            WidgetAlert()
                    .setText(R.string.app_remove)
                    .setOnCancel(R.string.app_cancel)
                    .setOnEnter(R.string.app_remove) {
                        ApiRequestsSupporter.executeEnabled(it, RProjectStatisticErrorsRemove(error.key)) {
                            ToolsToast.show(R.string.app_done)
                            EventBus.post(EventStatisticErrorRemove(error.key))
                        }
                    }
                    .asSheetShow()
            true
        }
    }
}