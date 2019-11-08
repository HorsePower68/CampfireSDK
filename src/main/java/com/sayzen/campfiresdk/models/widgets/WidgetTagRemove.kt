package com.sayzen.campfiresdk.screens.fandoms.tags

import android.widget.Button

import com.dzen.campfire.api.requests.tags.RTagsRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget

class WidgetTagRemove(
        private val tag: PublicationTag
) : Widget(R.layout.widget_remove) {

    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { sendRemove() }
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        asSheetShow()
        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        vEnter.isEnabled = vComment.getText().isNotEmpty()
    }

    override fun setEnabled(enabled: Boolean): WidgetTagRemove {
        super.setEnabled(enabled)
        vEnter.isEnabled = enabled
        vComment.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }

    private fun sendRemove() {

        ApiRequestsSupporter.executeEnabledConfirm(R.string.fandom_tags_remove_conf, R.string.app_remove, RTagsRemove(vComment.getText(), tag.id)) {
            ToolsToast.show(R.string.app_done)
            STags.instance(tag.fandomId, tag.languageId, Navigator.REPLACE)
            hide()
        }
    }


}
