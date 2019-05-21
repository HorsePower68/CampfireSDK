package com.sayzen.campfiresdk.models.widgets

import android.widget.Button

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.tags.RTagsChange
import com.dzen.campfire.api.requests.tags.RTagsCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText

class WidgetCategoryCreate(
        private val tag: UnitTag?,
        private val fandomId: Long,
        private val languageId: Long
) : Widget(R.layout.widget_category_create) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    constructor(tag: UnitTag) : this(tag, tag.fandomId, tag.languageId)

    constructor(fandomId: Long, languageId: Long) : this(null, fandomId, languageId)

    init {

        vCancel.setOnClickListener { v -> hide() }
        vEnter.setOnClickListener { v -> onActionClicked() }
        vName.vField.addTextChangedListener(TextWatcherChanged { t -> updateFinishEnabled() })
        vComment.vField.addTextChangedListener(TextWatcherChanged { t -> updateFinishEnabled() })

        if (tag != null) {
            vName.setText(tag.name)
            vEnter.setText(R.string.app_change)
        }

        asSheetShow()
        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val t = vName.getText()
        vEnter.isEnabled = ToolsText.inBounds(t, API.TAG_NAME_MIN_L, API.TAG_NAME_MAX_L) && vComment.getText().isNotEmpty()
    }

    override fun setEnabled(enabled: Boolean): WidgetCategoryCreate {
        super.setEnabled(enabled)
        vName.isEnabled = enabled
        vEnter.isEnabled = enabled
        vComment.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }

    private fun onActionClicked() {
        if (tag == null)
            sendCreate()
        else
            sendChange()
    }

    private fun sendCreate() {
        ApiRequestsSupporter.executeEnabled(this, RTagsCreate(vName.getText(), vComment.getText(), fandomId, languageId, 0, null)) { r ->
            ToolsToast.show(R.string.app_done)
            ControllerCampfireSDK.onToTagsClicked(fandomId, languageId, Navigator.REPLACE)
        }
    }

    private fun sendChange() {
        ApiRequestsSupporter.executeEnabled(this, RTagsChange(tag!!.id, vName.getText(), vComment.getText(), null, false)) { r ->
            ToolsToast.show(R.string.app_done)
            ControllerCampfireSDK.onToTagsClicked(fandomId, languageId, Navigator.REPLACE)
        }
    }


}
