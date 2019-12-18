package com.sayzen.campfiresdk.screens.fandoms.view

import android.widget.Button
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget

internal class WidgetDescription(
        val name:String,
        val callback : (String) -> Unit
) : Widget(R.layout.screen_fandom_widget_description) {

    private val vDescription: SettingsField = findViewById(R.id.vDescription)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vDescription.vFieldLayout.counterMaxLength = API.FANDOM_DESCRIPTION_MAX_L
        vDescription.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vDescription.setText(name)

        vEnter.setOnClickListener {
            callback.invoke(vDescription.getText())
            hide()
        }
        vCancel.setOnClickListener { hide() }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val textCheck = vDescription.getText().length <= API.FANDOM_DESCRIPTION_MAX_L
        vDescription.setError(if (textCheck) null else ToolsResources.s(R.string.error_too_long_text))
        vEnter.isEnabled = textCheck && name != vDescription.getText()
    }


}
