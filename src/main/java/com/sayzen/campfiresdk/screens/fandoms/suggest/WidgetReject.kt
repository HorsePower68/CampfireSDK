package com.sayzen.campfiresdk.screens.fandoms.suggest


import android.widget.Button
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.tools.ToolsText

internal class WidgetReject(val callback: (String) -> Unit) : Widget(R.layout.screen_fandom_suggest_widget_reject) {

    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vTemplate: Button = findViewById(R.id.vTemplate)

    init {


        vComment.vField.addTextChangedListener(TextWatcherChanged { t -> updateFinishEnabled() })

        vEnter.setOnClickListener {
            callback.invoke(vComment.getText())
            hide()
        }
        vTemplate.setOnClickListener {
            WidgetMenu()
                    .add(R.string.fandoms_suggest_tamplate_not_exist) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_not_exist_text)) }
                    .add(R.string.fandoms_suggest_tamplate_not_game) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_not_game_text)) }
                    .add(R.string.fandoms_suggest_tamplate_already) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_already_text)) }
                    .add(R.string.fandoms_suggest_tamplate_bad_name) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_bad_name_text)) }
                    .add(R.string.fandoms_suggest_tamplate_bad_image) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_bad_image_text)) }
                    .add(R.string.fandoms_suggest_tamplate_bad_image_title) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_bad_image_title_text)) }
                    .add(R.string.fandoms_suggest_tamplate_bad_genress) { w, c -> setText(ToolsResources.s(R.string.fandoms_suggest_tamplate_bad_genress_text)) }
                    .asSheetShow()
        }
        vCancel.setOnClickListener { v -> hide() }

        updateFinishEnabled()
    }

    private fun setText(text: String) {
        vComment.setText(text)
    }

    private fun updateFinishEnabled() {
        val commentCheck = ToolsText.isOnly(vComment.getText(), API.ENGLISH)
        vComment.setError(if (commentCheck) null else ToolsResources.s(R.string.error_use_english))
        vEnter.isEnabled = commentCheck && vComment.getText().isNotEmpty()
    }


}
