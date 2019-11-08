package com.sayzen.campfiresdk.models.widgets

import android.view.View
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetInfo
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewTextRemoved
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlocked
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsArrow
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor

class WidgetModerationBlock(
        private val unit: com.dzen.campfire.api.models.publications.Publication,
        private val bansCount: Long,
        private val warnsCount: Long,
        private val onBlock: () -> Unit = {}
) : Widget(R.layout.widget_moderation_block) {

    private var alertText = R.string.moderation_widget_block_confirm
    private var alertAction = R.string.app_block
    private var finishToast = R.string.app_blocked

    private val vTemplate: Button = findViewById(R.id.vTemplate)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vBlockLast: SettingsCheckBox = findViewById(R.id.vRemoveUnits)
    private val vBlockUser: SettingsSelection = findViewById(R.id.vBlockUser)
    private val vPunishments: SettingsArrow = findViewById(R.id.vPunishments)
    private val vBlockInApp: SettingsCheckBox = findViewById(R.id.vBlockInApp)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    private var banTime = 0L

    companion object {

        fun show(unit: com.dzen.campfire.api.models.publications.Publication, onBlock: () -> Unit = {}, onShow: (WidgetModerationBlock) -> Unit = {}) {

            ApiRequestsSupporter.executeProgressDialog(RAccountsPunishmentsGetInfo(unit.creatorId)) { r ->
                val w = WidgetModerationBlock(unit, r.bansCount, r.warnsCount, onBlock)
                onShow.invoke(w)
                w.asSheetShow()
            }
        }

    }

    init {
        if (unit.unitType == API.PUBLICATION_TYPE_REVIEW) vBlockLast.visibility = View.GONE

        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        if (bansCount > 0 || warnsCount > 2) vPunishments.setBackgroundColor(ToolsColor.setAlpha(100, ToolsResources.getColor(R.color.red_700)))
        vPunishments.setTitle(ToolsResources.s(R.string.moderation_widget_block_user_punishments, bansCount, warnsCount))
        vPunishments.setOnClickListener {
            val screen = SPunishments(unit.creatorId, unit.creatorName)
            screen.setOnBackPressed {
                Navigator.back()
                asSheetShow()
                true
            }
            Navigator.to(screen)
            hide()
        }

        vBlockInApp.visibility = if (ControllerApi.can(API.LVL_ADMIN_BAN)) View.VISIBLE else View.GONE
        if(unit.unitType == API.PUBLICATION_TYPE_STICKERS_PACK
                ||unit.unitType == API.PUBLICATION_TYPE_STICKER){
            vBlockInApp.visibility = View.GONE
            vBlockInApp.setChecked(true)
        }

        vEnter.setOnClickListener { block() }
        vCancel.setOnClickListener { hide() }

        vBlockUser.add(R.string.moderation_widget_ban_no) { setBanTime(0) }
        vBlockUser.add(R.string.moderation_widget_ban_warn) { setBanTime(-1) }
        vBlockUser.add(R.string.time_hour) { setBanTime(1000L * 60 * 60) }
        vBlockUser.add(R.string.time_8_hour) { setBanTime(1000L * 60 * 60 * 8) }
        vBlockUser.add(R.string.time_day) { setBanTime(1000L * 60 * 60 * 24) }
        vBlockUser.add(R.string.time_week) { setBanTime(1000L * 60 * 60 * 24 * 7) }
        vBlockUser.add(R.string.time_month) { setBanTime(1000L * 60 * 60 * 24 * 30) }
        vBlockUser.add(R.string.time_6_month) { setBanTime(1000L * 60 * 60 * 24 * 30 * 6) }
        vBlockUser.add(R.string.time_year) { setBanTime(1000L * 60 * 60 * 24 * 365) }

        vBlockUser.setSubtitle(R.string.moderation_widget_ban_no)
        setBanTime(0)

        vTemplate.setOnClickListener {
            val w = WidgetMenu()
            CampfireConstants.RULES_USER
            for (i in CampfireConstants.RULES_USER)
                w.add(ToolsResources.sLang(ControllerApi.getLanguage(unit.languageId).code, i.title))
                { _, _ -> vComment.setText(ToolsResources.sLang(ControllerApi.getLanguage(unit.languageId).code, i.text)) }
            w.asSheetShow()
        }

        updateFinishEnabled()
    }


    private fun setBanTime(banTime: Long) {
        this.banTime = banTime
        vBlockInApp.isEnabled = banTime > 0
        if (banTime <= 0) vBlockInApp.setChecked(false)
    }

    private fun updateFinishEnabled() {
        if (vComment.getText().length < API.MODERATION_COMMENT_MIN_L || vComment.getText().length > API.MODERATION_COMMENT_MAX_L) {
            vEnter.isEnabled = false
        } else {
            vEnter.isEnabled = isEnabled
        }
    }

    override fun setEnabled(enabled: Boolean): WidgetModerationBlock {
        super.setEnabled(enabled)
        vComment.isEnabled = enabled
        vPunishments.isEnabled = enabled
        vTemplate.isEnabled = enabled
        vBlockLast.isEnabled = enabled
        vBlockUser.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        updateFinishEnabled()

        return this
    }

    private fun block() {

        val blockInApp = if (ControllerApi.can(API.LVL_ADMIN_BAN)) vBlockInApp.isChecked() else false

        ApiRequestsSupporter.executeEnabledConfirm(alertText, alertAction,
                RFandomsModerationBlock(unit.id, banTime, vBlockLast.isChecked(), vComment.getText().trim { it <= ' ' }, blockInApp, ControllerApi.getLanguageId())) { r ->
            onBlock.invoke()
            afterBlock(r.blockedUnitsIds, r.unitChatMessage)
            ToolsToast.show(finishToast)
            hide()
        }
                .onApiError(RFandomsModerationBlock.E_LOW_KARMA_FORCE) {
                    ToolsToast.show(R.string.moderation_low_karma)
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_ALREADY) {
                    ToolsToast.show(R.string.error_already_blocked)
                    afterBlock(emptyArray(), null)
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_DRAFT) {
                    ToolsToast.show(R.string.error_already_returned_to_drafts)
                    afterBlock(emptyArray(), null)
                    hide()
                }
    }

    private fun afterBlock(blockedUnitsIds: Array<Long>, unitChatMessage: PublicationChatMessage?) {
        if (unit.unitType == API.PUBLICATION_TYPE_REVIEW) {
            EventBus.post(EventFandomReviewTextRemoved(unit.id))
        } else {
            for (id in blockedUnitsIds) EventBus.post(EventPublicationRemove(id))
            for (id in blockedUnitsIds) EventBus.post(EventPublicationBlocked(id, unit.id, unitChatMessage))
        }
    }

    fun setActionText(text: Int): WidgetModerationBlock {
        vEnter.setText(text)
        return this
    }

    fun setAlertText(text: Int, action: Int): WidgetModerationBlock {
        alertText = text
        alertAction = action
        return this
    }

    fun setToastText(text: Int): WidgetModerationBlock {
        finishToast = text
        return this
    }

}
