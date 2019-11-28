package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.requests.activities.RActivitiesGetCounts
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceMember
import com.dzen.campfire.api.requests.activities.RActivitiesRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.activities.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRaceCreate
import com.sayzen.campfiresdk.screens.activities.user_activities.WidgetReject
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerActivities {

    private val eventBus = EventBus
            .subscribe(EventFandomAccepted::class) { setFandomsCount(suggestedFandomsCount - 1) }
            .subscribe(EventNotification::class) { if (it.notification is NotificationActivitiesRelayRaceTurn) reloadActivities() }
            .subscribe(EventActivitiesRelayRaceRejected::class) { setRelayRacesCount(relayRacesCount - 1) }

    fun init() {

    }


    fun clear() {
        clearUser()
        clearAdmins()
    }


    //
    //  User
    //

    private var relayRacesCount = 0L
    private var rubricsCount = 0L

    fun setRelayRacesCount(relayRacesCount: Long) {
        this.relayRacesCount = relayRacesCount
        EventBus.post(EventActivitiesCountChanged())
    }

    fun setRubricsCount(rubricsCount: Long) {
        this.rubricsCount = rubricsCount
        EventBus.post(EventActivitiesCountChanged())
    }

    fun clearUser() {
        relayRacesCount = 0L
        rubricsCount = 0L
        EventBus.post(EventActivitiesCountChanged())
    }

    fun getActivitiesCount() = getRelayRacesCount() + getRubricsCount()

    fun getRelayRacesCount() = relayRacesCount
    fun getRubricsCount() = rubricsCount

    fun showMenu(userActivity: UserActivity) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToActivity(userActivity.id));ToolsToast.show(R.string.app_copied) }
                .add(R.string.app_change) { _, _ -> Navigator.to(SRelayRaceCreate(userActivity)) }.condition(ControllerApi.can(userActivity.fandomId, userActivity.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .add(R.string.app_remove) { _, _ -> removeActivity(userActivity) }.condition(ControllerApi.can(userActivity.fandomId, userActivity.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .asSheetShow()
    }

    fun removeActivity(userActivity: UserActivity) {
        ControllerApi.moderation(R.string.activities_relay_race_remove_title, R.string.app_remove, { RActivitiesRemove(userActivity.id, it) }) {
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventActivitiesRemove(userActivity.id))
        }
    }

    //
    //  Administration
    //

    private var administrationLoadInProgress = false
    private var suggestedFandomsCount = 0L
    private var reportsCount = 0L
    private var reportsUserCount = 0L
    private var blocksCount = 0L

    fun setFandomsCount(count: Long) {
        suggestedFandomsCount = count
        EventBus.post(EventActivitiesAdminCountChanged())
    }

    fun getSuggestedFandomsCount() = suggestedFandomsCount
    fun getReportsCount() = reportsCount
    fun getReportsUserCount() = reportsUserCount
    fun getBlocksCount() = blocksCount
    fun isAdministrationLoadInProgress() = administrationLoadInProgress

    fun clearAdmins() {
        suggestedFandomsCount = 0
        reportsCount = 0
        reportsUserCount = 0
        blocksCount = 0
        EventBus.post(EventActivitiesAdminCountChanged())
    }

    //
    //  Reload
    //

    fun reloadActivities() {
        administrationLoadInProgress = true
        clearAdmins()
        RActivitiesGetCounts(ControllerSettings.adminReportsLanguages)
                .onComplete {
                    setRelayRacesCount(it.relayRacesCount)
                    setRubricsCount(it.rubricsCount)
                    suggestedFandomsCount = it.suggestedFandomsCount
                    reportsCount = it.reportsCount
                    reportsUserCount = it.reportsUserCount
                    blocksCount = it.blocksCount
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                }
                .onError {
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                    err(it)
                }
                .send(api)
    }

    //
    //  Relay race
    //

    fun reject(userActivityId: Long) {
        WidgetReject(userActivityId).asSheetShow()
    }

    fun member(userActivityId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.activities_relay_race_member_text, R.string.app_participate, RActivitiesRelayRaceMember(userActivityId, true)) { r ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventActivitiesRelayRaceMemberStatusChanged(userActivityId, 1, r.myIsCurrentMember))
        }
    }

    fun no_member(userActivityId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.activities_relay_race_member_text_no, R.string.app_participate_no, RActivitiesRelayRaceMember(userActivityId, false)) { r ->
            ToolsToast.show(R.string.app_done)
            setRelayRacesCount(getActivitiesCount() - 1)
            EventBus.post(EventActivitiesRelayRaceMemberStatusChanged(userActivityId, 0, false))
        }
    }

    //
    //  Video Ad
    //

    fun showVideoAd() {
        //    ControllerAdsVideoReward.loadAd()
        //    showVideoAdNow(10, ToolsView.showProgressDialog(R.string.achi_video_loading))
    }

    private fun showVideoAdNow(tryCount: Int, vDialog: Widget) {
        // info("XAd", "onRewardedAdFailedToLoad " + ControllerAdsVideoReward.isCahShow())
        // if (ControllerAdsVideoReward.isCahShow()) {
        //     vDialog.hide()
        //     ControllerAdsVideoReward.show {
        //         RVideoAdView().onComplete {
        //             if (it.achi) EventBus.post(EventAchiProgressIncr(API.ACHI_VIDEO_AD.index))
        //             EventBus.post(EventVideoAdView())
        //         }.send(api)
        //     }
        // } else if (tryCount > 0 && ControllerAdsVideoReward.isLoading()) {
        //     ToolsThreads.main(1000) { showVideoAdNow(tryCount - 1, vDialog) }
        // } else {
        //     vDialog.hide()
        //     ToolsToast.show(R.string.achi_video_not_loaded)
        // }
    }

}