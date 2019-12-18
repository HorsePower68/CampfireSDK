package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminSetCof
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomKarmaCofChanged
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.widgets.WidgetFieldTwo
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText

class CardKarmaCof(
        val fandom: Fandom
) : Card(R.layout.screen_fandom_card_karma_cof) {

    private val eventBus = EventBus
            .subscribe(EventFandomKarmaCofChanged::class) { if (it.fandomId == fandom.id) fandom.karmaCof = it.cof; update(); }

    override fun bindView(view: View) {
        super.bindView(view)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vTouch: View = view.findViewById(R.id.vTouch)

        var text = ToolsText.numToStringRound(fandom.karmaCof / 100.0, 2)
        if (fandom.karmaCof > 100) text = ToolsHTML.font_color(text, ToolsHTML.color_green)
        if (fandom.karmaCof < 100) text = ToolsHTML.font_color(text, ToolsHTML.color_red)
        vTitle.text = ToolsResources.s(R.string.app_coefficient_karma, text)
        ToolsView.makeTextHtml(vTitle)

        if (ControllerApi.can(API.LVL_ADMIN_FANDOM_SET_COF))
            vTouch.setOnClickListener { setCof() }
    }


    private fun setCof() {
        WidgetFieldTwo()
                .setTitle(ToolsResources.s(R.string.fandoms_menu_set_cof_hint, API.FANDOM_KARMA_COF_MIN / 100, API.FANDOM_KARMA_COF_MAX / 100))
                .setOnCancel(R.string.app_cancel)
                .setText_1(ToolsText.numToStringRound(fandom.karmaCof / 100.0, 2))
                .setHint_1(R.string.app_coefficient)
                .setLinesCount_1(1)
                .addChecker_1(R.string.error_incorrect_value) {
                    if (it.length > 4) {
                        return@addChecker_1 false
                    }
                    try {
                        val v = (it.toDouble() * 100).toLong()
                        if (v < API.FANDOM_KARMA_COF_MIN || v > API.FANDOM_KARMA_COF_MAX) {
                            return@addChecker_1 false
                        }
                    } catch (e: Exception) {
                        return@addChecker_1 false
                    }

                    return@addChecker_1 true
                }
                .addChecker_1("") {
                    try {
                        val v = (it.toDouble() * 100).toLong()
                        if (v == fandom.karmaCof) {
                            return@addChecker_1 false
                        }
                    } catch (e: Exception) {
                        return@addChecker_1 false
                    }

                    return@addChecker_1 true
                }
                .setMin_1(1)
                .setMax_1(4)
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_2(R.string.comments_hint)
                .addChecker_2(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(R.string.app_change) { w, cof, comment ->
                    val v = (cof.toDouble() * 100).toLong()

                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminSetCof(fandom.id, v, comment)) {
                        EventBus.post(EventFandomKarmaCofChanged(fandom.id, v))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

}
