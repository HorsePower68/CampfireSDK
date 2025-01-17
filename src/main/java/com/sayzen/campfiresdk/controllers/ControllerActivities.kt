package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.requests.activities.*
import com.dzen.campfire.api.requests.project.RVideoAdInfo
import com.dzen.campfire.api.requests.project.RVideoAdView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.activities.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.project.EventAchiProgressIncr
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRaceCreate
import com.sayzen.campfiresdk.screens.activities.user_activities.WidgetReject
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

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
                .add(R.string.app_subscription) { _, _ -> subscribtion(userActivity) }
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerLinks.linkToActivity(userActivity.id));ToolsToast.show(R.string.app_copied) }
                .add(R.string.app_change) { _, _ -> Navigator.to(SRelayRaceCreate(userActivity)) }.condition(ControllerApi.can(userActivity.fandomId, userActivity.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .add(R.string.app_remove) { _, _ -> removeActivity(userActivity) }.condition(ControllerApi.can(userActivity.fandomId, userActivity.languageId, API.LVL_MODERATOR_RELAY_RACE)).textColorRes(R.color.white).backgroundRes(R.color.blue_700)
                .asSheetShow()
    }

    fun subscribtion(userActivity: UserActivity){
        ApiRequestsSupporter.executeProgressDialog(RActivitiesSubscribeGet(userActivity.id)){ r->
            ApiRequestsSupporter.executeEnabledConfirm(
                    if(r.subscribed)R.string.activities_unsubscribe_alert else R.string.activities_subscribe_alert,
                    if(r.subscribed) R.string.app_unsubscribe else  R.string.app_subscribe,
                    RActivitiesSubscribe(userActivity.id, !r.subscribed)
            ){
                ToolsToast.show(R.string.app_done)
            }
        }
    }

    fun removeActivity(userActivity: UserActivity) {
        ControllerApi.moderation(R.string.activities_relay_race_remove_title, R.string.app_remove, { RActivitiesRemove(userActivity.id, it) }) {
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventActivitiesRemove(userActivity.id))
            reloadActivities()
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

    private var videoAchiLocked = false

    fun showVideoAd() {
        if (videoAchiLocked) {
            ToolsToast.show(R.string.achi_video_device_max)
            return
        }
        ControllerAppodeal.cashVideoReward()

        val v =  ToolsView.showProgressDialog(R.string.achi_video_loading)
        RVideoAdInfo()
                .onComplete {
                    videoAchiLocked = it.countToday > 2
                    if (videoAchiLocked) {
                        v.hide()
                        ToolsToast.show(R.string.achi_video_device_max)
                        return@onComplete
                    }
                    showVideoAdNow(10, v)
                }
                .onError{
                    ToolsToast.show(R.string.error_unknown)
                    v.hide()
                }
                .send(api)
    }

    private fun showVideoAdNow(tryCount: Int, vDialog: Widget) {
        if (ControllerAppodeal.isLoadedVideoReward()) {
            vDialog.hide()
            ControllerAppodeal.showVideoReward {
                RVideoAdView().onComplete {
                    if (it.achi) EventBus.post(EventAchiProgressIncr(API.ACHI_VIDEO_AD.index))
                    else if (!it.achi) {
                        videoAchiLocked = true
                        ToolsToast.show(R.string.achi_video_achi_max)
                    } else{
                        videoAchiLocked = it.countToday > 2
                    }
                    EventBus.post(EventVideoAdView())
                }.send(api)
            }
        } else if (tryCount > 0) {
            ToolsThreads.main(1000) { showVideoAdNow(tryCount - 1, vDialog) }
        } else {
            vDialog.hide()
            ToolsToast.show(R.string.achi_video_not_loaded)
        }
    }

}