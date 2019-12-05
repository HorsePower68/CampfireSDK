package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsChange
import com.dzen.campfire.api.requests.accounts.RAccountsStatusSet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.account.followers.SFollowers
import com.sayzen.campfiresdk.screens.account.followers.SFollows
import com.sayzen.campfiresdk.screens.account.karma.ScreenAccountKarma
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sayzen.campfiresdk.screens.account.rates.SRates
import com.sayzen.campfiresdk.screens.account.story.SStory
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountNoteChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountStatusChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.screens.account.black_list.SBlackList
import com.sayzen.campfiresdk.screens.account.fandoms.SAcounFandoms
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsModeration
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPacks
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardInfo(
        private val xAccount: XAccount,
        private val karma30: Long,
        private val dateCreate: Long,
        private val sex: Long,
        private var banDate: Long,
        private var isFollow: Boolean,
        private var followsCount: Long,
        private var followersCount: Long,
        private var moderateFandomsCount: Long,
        private var status: String,
        private var ratesCount: Long,
        private var bansCount: Long,
        private var warnsCount: Long,
        private var note: String,
        private var fandomsCount: Long,
        private var blackFandomsCount: Long,
        private var blackAccountCount: Long,
        private var stickersCount: Long
) : Card(R.layout.screen_account_card_info) {

    private val eventBus = EventBus
            .subscribe(EventAccountsFollowsChange::class) { this.onAccountsFollowChange(it) }
            .subscribe(EventAccountStatusChanged::class) { this.onEventAccountStatusChanged(it) }
            .subscribe(EventAccountNoteChanged::class) { this.onEventAccountNoteChanged(it) }
            .subscribe(EventAccountBaned::class) { this.onEventAccountBaned(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vLogin: TextView = view.findViewById(R.id.vLogin)
        val vOnline: TextView = view.findViewById(R.id.vOnlineText)
        val vBanText: TextView = view.findViewById(R.id.vBanText)
        val vButtonsContainerDivider: View = view.findViewById(R.id.vButtonsContainerDivider)
        val vButtonsContainer: ViewGroup = view.findViewById(R.id.vButtonsContainer)
        val vFollow: Button = view.findViewById(R.id.vFollow)
        val vMessage: Button = view.findViewById(R.id.vMessage)
        val vFollowsCount: TextView = view.findViewById(R.id.vFollowsCount)
        val vFollowsButton: View = view.findViewById(R.id.vFollowsButton)
        val vKarmaText: TextView = view.findViewById(R.id.vKarmaText)
        val vKarmaButton: View = view.findViewById(R.id.vKarmaButton)
        val vAchiText: TextView = view.findViewById(R.id.vAchievementsText)
        val vAchiButton: View = view.findViewById(R.id.vAchievementsButton)
        val vFollowersButton: View = view.findViewById(R.id.vFollowersButton)
        val vFollowersCount: TextView = view.findViewById(R.id.vFollowersCount)
        val vModeratorButton: View = view.findViewById(R.id.vModeratorButton)
        val vModeratorText: TextView = view.findViewById(R.id.vModeratorText)
        val vModeratorCount: TextView = view.findViewById(R.id.vModeratorCount)
        val vTimeText: TextView = view.findViewById(R.id.vTimeText)
        val vStatus: ViewTextLinkable = view.findViewById(R.id.vStatus)
        val vStatusContainer: View = view.findViewById(R.id.vStatusContainer)
        val vRatesButton: View = view.findViewById(R.id.vRatesButton)
        val vRatesCount: TextView = view.findViewById(R.id.vRatesCount)
        val vPunishmentsButton: View = view.findViewById(R.id.vPunishmentsButton)
        val vPunishmentsCount: TextView = view.findViewById(R.id.vPunishmentsCount)
        val vStory: View = view.findViewById(R.id.vStory)
        val vNote: ViewTextLinkable = view.findViewById(R.id.vNote)
        val vBlackList: View = view.findViewById(R.id.vBlackList)
        val vBlackListCount: TextView = view.findViewById(R.id.vBlackListCount)
        val vStickers: View = view.findViewById(R.id.vStickers)
        val vStickersCount: TextView = view.findViewById(R.id.vStickersCount)
        val vFandoms: View = view.findViewById(R.id.vFandoms)
        val vFandomsCount: TextView = view.findViewById(R.id.vFandomsCount)

        if (xAccount.isBot()) {
            vOnline.text = ToolsResources.s(R.string.app_bot)
            vOnline.setTextColor(ToolsResources.getColor(R.color.green_700))
        } else if (!xAccount.isOnline()) {
            vOnline.text = ToolsResources.sCap(R.string.app_was_online, ToolsResources.sex(sex, R.string.he_was, R.string.she_was), ToolsDate.dateToString(xAccount.getLastOnlineTime()))
            vOnline.setTextColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vOnline.text = ToolsResources.s(R.string.app_online)
            vOnline.setTextColor(ToolsResources.getColor(R.color.green_700))
        }

        if (banDate > ControllerApi.currentTime()) {
            vBanText.text = ToolsResources.s(R.string.error_account_baned, ToolsDate.dateToString(banDate))
            vBanText.visibility = VISIBLE
        } else {
            vBanText.visibility = GONE
        }

        vNote.text = note
        vNote.visibility = if (note.isEmpty()) GONE else VISIBLE

        vButtonsContainer.visibility = if (ControllerApi.isCurrentAccount(xAccount.accountId)) View.GONE else View.VISIBLE
        vButtonsContainerDivider.visibility = if (ControllerApi.isCurrentAccount(xAccount.accountId)) View.GONE else View.VISIBLE

        vFollowsCount.text = followsCount.toString()
        vRatesCount.text = ratesCount.toString()
        vPunishmentsCount.text = bansCount.toString() + " / " + warnsCount
        vLogin.text = xAccount.name
        vKarmaText.text = (karma30 / 100).toString()
        vFollow.setText(if (isFollow) R.string.app_unfollow else R.string.app_follow)
        vKarmaText.setTextColor(if (karma30 == 0L) ToolsResources.getColor(R.color.grey_600) else if (karma30 > 0) ToolsResources.getColor(R.color.green_700) else ToolsResources.getColor(R.color.red_700))
        vAchiText.text = ToolsText.numToStringRound(xAccount.lvl / 100.0, 2)
        vAchiText.setTextColor(ToolsResources.getColor(R.color.green_700))
        vFollowersCount.text = "" + followersCount
        vTimeText.text = "${((ControllerApi.currentTime() - dateCreate) / (1000L * 60 * 60 * 24)) + 1} ${ToolsResources.s(R.string.app_d)}"
        vFandomsCount.text = "$fandomsCount"
        vStickersCount.text = "$stickersCount"
        vBlackListCount.text = "$blackAccountCount/$blackFandomsCount"

        vStatusContainer.visibility = VISIBLE
        if (status.isEmpty()) {
            if (xAccount.isCurrentAccount()) vStatus.text = ToolsResources.s(R.string.profile_tap_to_change_status)
            else vStatusContainer.visibility = GONE
            vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vStatus.text = status
            vStatus.setTextColor(ToolsResources.getColorAttr(R.attr.revers_color))
        }

        if (xAccount.isCurrentAccount()) {
            vStatusContainer.setOnClickListener { changeStatus() }
        } else {
            vStatusContainer.setOnClickListener(null)
        }

        when {
            xAccount.isBot() -> {
                vModeratorCount.text = "-"
                vModeratorText.setTextColor(ToolsResources.getColorAttr(R.attr.revers_color))
                vModeratorText.setText(R.string.app_bot)
                vModeratorButton.setOnClickListener(null)
            }
            xAccount.isProtoadmin() -> {
                vModeratorCount.text = "∞"
                vModeratorText.setTextColor(ToolsResources.getColor(R.color.orange_a_700))
                vModeratorText.setText(R.string.app_protoadmin)
                vModeratorButton.setOnClickListener(null)
            }
            xAccount.isAdmin() -> {
                vModeratorCount.text = "∞"
                vModeratorText.setTextColor(ToolsResources.getColor(R.color.red_700))
                vModeratorText.setText(R.string.app_admin)
                vModeratorButton.setOnClickListener(null)
            }
            xAccount.isModerator() -> {
                vModeratorCount.text = "" + moderateFandomsCount
                vModeratorText.setTextColor(ToolsResources.getColor(R.color.blue_700))
                vModeratorText.setText(R.string.app_moderator)
                vModeratorButton.setOnClickListener { SFandomsModeration.instance(xAccount.accountId, Navigator.TO) }
            }
            else -> {
                vModeratorCount.text = "-"
                vModeratorText.setTextColor(ToolsResources.getColor(R.color.green_700))
                vModeratorText.setText(R.string.app_user)
                vModeratorButton.setOnClickListener(null)
            }
        }

        ControllerLinks.makeLinkable(vStatus)
        ControllerLinks.makeLinkable(vNote)

        vFollow.setOnClickListener { toggleFollows() }
        vMessage.setOnClickListener { SChat.instance(ChatTag(API.CHAT_TYPE_PRIVATE, xAccount.accountId, ControllerApi.account.id), 0, true, Navigator.TO) }
        vFollowsButton.setOnClickListener { Navigator.to(SFollows(xAccount.accountId, xAccount.name)) }
        vFollowersButton.setOnClickListener { Navigator.to(SFollowers(xAccount.accountId, xAccount.name)) }
        vRatesButton.setOnClickListener { Navigator.to(SRates(xAccount.accountId, xAccount.name)) }
        vPunishmentsButton.setOnClickListener { Navigator.to(SPunishments(xAccount.accountId, xAccount.name)) }
        vStory.setOnClickListener { SStory.instance(xAccount.accountId, xAccount.name, Navigator.TO) }
        vAchiButton.setOnClickListener { SAchievements.instance(xAccount.accountId, xAccount.name, 0, false, Navigator.TO) }
        vKarmaButton.setOnClickListener { Navigator.to(ScreenAccountKarma(xAccount.accountId, xAccount.name)) }
        vStickers.setOnClickListener { Navigator.to(SStickersPacks(xAccount.accountId)) }
        vBlackList.setOnClickListener { Navigator.to(SBlackList(xAccount.accountId, xAccount.name)) }
        vFandoms.setOnClickListener { Navigator.to(SAcounFandoms(xAccount.accountId)) }

    }

    //
    //  EventBus
    //

    private fun onAccountsFollowChange(e: EventAccountsFollowsChange) {
        if (xAccount.accountId == e.accountId) {
            isFollow = e.isFollow
            update()
        }
        if (ControllerApi.isCurrentAccount(xAccount.accountId)) {
            followsCount += (if (e.isFollow) 1 else -1).toLong()
            update()
        }
    }

    private fun onEventAccountStatusChanged(e: EventAccountStatusChanged) {
        if (e.accountId == xAccount.accountId) {
            status = e.status
            update()
        }
    }

    private fun onEventAccountNoteChanged(e: EventAccountNoteChanged) {
        if (e.accountId == xAccount.accountId) {
            note = e.note
            update()
        }
    }

    private fun onEventAccountBaned(e: EventAccountBaned) {
        if (e.accountId == xAccount.accountId) {
            banDate = e.date
            update()
        }
    }

    //
    //  Api
    //

    private fun changeStatus() {
        if (!xAccount.can(API.LVL_CAN_CHANGE_STATUS)) {
            ToolsToast.show(R.string.error_low_lvl)
            return
        }
        WidgetField()
                .setHint(R.string.app_status)
                .setAutoHideOnEnter(false)
                .setLinesCount(1)
                .setMax(API.ACCOUNT_STATUS_MAX_L)
                .setText(status)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_change) { w, status ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsStatusSet(status.trim())) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountStatusChanged(xAccount.accountId, status.trim()))
                    }
                }
                .asSheetShow()
    }

    private fun toggleFollows() {
        if (!isFollow)
            ApiRequestsSupporter.executeProgressDialog(RAccountsFollowsChange(xAccount.accountId, !isFollow)) { _ -> eventBus.post(EventAccountsFollowsChange(xAccount.accountId, !isFollow)) }
        else
            ApiRequestsSupporter.executeEnabledConfirm(R.string.profile_follows_remove_confirm, R.string.app_unfollow, RAccountsFollowsChange(xAccount.accountId, !isFollow)) { eventBus.post(EventAccountsFollowsChange(xAccount.accountId, !isFollow)) }
    }

}
