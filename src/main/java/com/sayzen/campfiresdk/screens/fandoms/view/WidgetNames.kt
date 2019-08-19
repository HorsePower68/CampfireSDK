package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationNames
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomNamesChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus

internal class WidgetNames(
        private val fandomId: Long,
        private val languageId: Long,
        private val currentNames: Array<String>
) : Widget(R.layout.screen_fandom_widget_names) {

    private val vFlow: LayoutFlow = findViewById(R.id.vFlow)
    private val vNewText: SettingsField = findViewById(R.id.vNewText)
    private val vAdd: View = findViewById(R.id.vAdd)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {
        vNewText.addOnTextChanged { updateFinishEnabled() }
        vNewText.setMaxLength(API.FANDOM_NAMES_MAX_L)
        vNewText.setHint(R.string.fandom_info_links_names_add)

        vEnter.setOnClickListener { send() }
        vCancel.setOnClickListener { hide() }
        vAdd.setOnClickListener { add() }

        for (i in currentNames) {
            val v = ViewChip.instanceChoose(context, i, i)
            v.tag = 0
            v.isSelected = false
            v.setChipBackgroundColorResource(R.drawable.selector_style_dark_chip_red)
            v.setOnCheckedChangeListener { compoundButton, b -> updateFinishEnabled() }
            vFlow.addView(v)
        }

        updateFinishEnabled()

    }

    private fun updateFinishEnabled() {

        val newNames = getSelected()
        var changes = currentNames.size != newNames.size
        if (!changes) {
            for (i in 0 until newNames.size) {
                var b = false
                for (n in 0 until currentNames.size) b = b || newNames[i] == currentNames[n]
                if (!b) {
                    changes = true
                    break
                }
            }
        }
        vAdd.isEnabled = newNames.size < API.FANDOM_NAMES_MAX && vNewText.getText().isNotEmpty()
        vNewText.isEnabled = newNames.size < API.FANDOM_NAMES_MAX
        vEnter.isEnabled = newNames.size <= API.FANDOM_NAMES_MAX && changes
    }

    private fun add() {
        val v = ViewChip.instance(context, vNewText.getText())
        vNewText.setText("")
        v.tag = 1
        v.setBackgroundRes(R.color.green_700)
        v.setOnClickListener {
            vFlow.removeView(v)
            updateFinishEnabled()
        }
        vFlow.addView(v)
        updateFinishEnabled()
    }

    private fun getSelected(): Array<String> {
        val names = ArrayList<String>()
        for (i in 0 until vFlow.childCount) {
            val v = vFlow.getChildAt(i) as ViewChip
            if (v.tag == 0) {
                if (!v.isChecked) names.add(v.text.toString())
            } else {
                names.add(v.text.toString())
            }
        }
        return names.toTypedArray()
    }

    private fun send() {
        val names = getSelected()
        WidgetField()
                .setOnCancel(R.string.app_cancel)
                .setHint(R.string.moderation_widget_comment)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_change) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationNames(fandomId, languageId, names, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventFandomNamesChanged(fandomId, languageId, names))
                        hide()
                    }
                }
                .asSheetShow()
    }


}