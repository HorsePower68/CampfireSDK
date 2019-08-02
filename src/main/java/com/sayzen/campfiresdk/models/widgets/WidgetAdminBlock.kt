package com.sayzen.campfiresdk.models.widgets

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetInfo
import com.dzen.campfire.api.requests.accounts.RAccountsAdminBan
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsArrow
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor

class WidgetAdminBlock(
        private val accountId: Long,
        private val accountName: String,
        private val bansCount: Long,
        private val warnsCount: Long
) : Widget(R.layout.widget_admin_block) {

    private val vTemplate: Button = findViewById(R.id.vTemplate)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vBlockUser: SettingsSelection = findViewById(R.id.vBlockUser)
    private val vPunishments: SettingsArrow = findViewById(R.id.vPunishments)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    private var banTime = 0L

    companion object {

        fun show(accountId: Long, accountName: String) {

            ApiRequestsSupporter.executeProgressDialog(RAccountsPunishmentsGetInfo(accountId)) { r ->
                WidgetAdminBlock(accountId, accountName, r.bansCount, r.warnsCount)
                        .asSheetShow()
            }
        }

    }

    init {
        isUseMoreScreenSpace = true

        vComment.vField.addTextChangedListener(TextWatcherChanged { t -> updateFinishEnabled() })

        if (bansCount > 0 || warnsCount > 2) vPunishments.setBackgroundColor(ToolsColor.setAlpha(100, ToolsResources.getColor(R.color.red_700)))
        vPunishments.setTitle(ToolsResources.s(R.string.moderation_widget_block_user_punishments, bansCount, warnsCount))
        vPunishments.setOnClickListener {
            val screen = SPunishments(accountId, accountName)
            screen.setOnBackPressed {
                Navigator.back()
                asSheetShow()
                true
            }
            Navigator.to(screen)
            hide()
        }

        vEnter.setOnClickListener { v -> punish() }
        vCancel.setOnClickListener { v -> hide() }

        vBlockUser.add(R.string.moderation_widget_ban_no) {  banTime = 0 }
        vBlockUser.add(R.string.moderation_widget_ban_warn) { banTime = -1 }
        vBlockUser.add(R.string.time_hour) {  banTime = 1000L * 60 * 60 }
        vBlockUser.add(R.string.time_8_hour) { banTime = 1000L * 60 * 60 * 8 }
        vBlockUser.add(R.string.time_day) { banTime = 1000L * 60 * 60 * 24 }
        vBlockUser.add(R.string.time_week) {  banTime = 1000L * 60 * 60 * 24 * 7 }
        vBlockUser.add(R.string.time_month) { banTime = 1000L * 60 * 60 * 24 * 30 }

        vBlockUser.setSubtitle(R.string.moderation_widget_ban_warn)
        banTime = -1

        vTemplate.setOnClickListener {
            val w = WidgetMenu()
            for (i in CampfreConstants.RULES_USER) w.add(i.title) { w, c -> setText(i.text) }
            w.asSheetShow()
        }

        updateFinishEnabled()
    }

    private fun setText(text: Int) {
        vComment.setText(ToolsResources.s(text))
    }

    private fun updateFinishEnabled() {
        if (vComment.getText().length < API.MODERATION_COMMENT_MIN_L || vComment.getText().length > API.MODERATION_COMMENT_MAX_L) {
            vEnter.isEnabled = false
        } else {
            vEnter.isEnabled = isEnabled
        }
    }

    override fun setEnabled(enabled: Boolean): WidgetAdminBlock {
        super.setEnabled(enabled)
        vComment.isEnabled = enabled
        vTemplate.isEnabled = enabled
        vBlockUser.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        updateFinishEnabled()

        return this
    }

    private fun punish() {

        ApiRequestsSupporter.executeEnabledConfirm(R.string.app_punish_confirm, R.string.app_punish, RAccountsAdminBan(accountId, banTime, vComment.getText().trim { it <= ' ' })) { r ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventAccountBaned(accountId, ControllerApi.currentTime() + banTime))
            hide()
        }.onApiError(RFandomsModerationBlock.E_LOW_KARMA_FORCE) { r ->
            ToolsToast.show(R.string.moderation_low_karma)
            hide()
        }

    }

}
