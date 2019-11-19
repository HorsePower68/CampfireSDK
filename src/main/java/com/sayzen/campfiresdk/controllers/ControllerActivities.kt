package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesAdministrationGetCounts
import com.dzen.campfire.api.requests.project.RVideoAdGetCount
import com.dzen.campfire.api.requests.project.RVideoAdView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccepted
import com.sayzen.campfiresdk.models.events.project.EventAchiProgressIncr
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesAdminCountChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesCountChanged
import com.sayzen.devsupandroidgoogle.ControllerAdsVideoReward
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

object ControllerActivities {

    private val eventBus = EventBus
            .subscribe(EventFandomAccepted::class) { setFandomsCount(suggestedFandomsCount - 1) }

    fun init() {

    }


    fun clear() {
        clearUser()
        clearAdmins()
    }


    //
    //  User
    //

    private var activitiesCount = 0L

    fun setActivitiesCount(activitiesCount: Long) {
        this.activitiesCount = activitiesCount
        EventBus.post(EventActivitiesCountChanged())
    }

    fun clearUser() {
        EventBus.post(EventActivitiesCountChanged())
    }

    fun getActivitiesCount() = activitiesCount

    //
    //  Administration
    //

    private var administrationLoadInProgress = false
    private var suggestedFandomsCount = 0L
    private var reportsCount = 0L
    private var reportsUserCount = 0L
    private var blocksCount = 0L

    fun reloadAdministration() {
        administrationLoadInProgress = true
        clearAdmins()
        RActivitiesAdministrationGetCounts(ControllerSettings.adminReportsLanguages)
                .onComplete{
                    suggestedFandomsCount = it.suggestedFandomsCount
                    reportsCount = it.reportsCount
                    reportsUserCount = it.reportsUserCount
                    blocksCount = it.blocksCount
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                }
                .onError{
                    administrationLoadInProgress = false
                    EventBus.post(EventActivitiesAdminCountChanged())
                    err(it)
                }
                .send(api)
    }

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
    //  Video Ad
    //

    fun showVideoAd() {
        ControllerAdsVideoReward.loadAd()
        ApiRequestsSupporter.executeProgressDialog(RVideoAdGetCount()) { w, r ->
            if (r.count < 1) {
                w.hide()
                ToolsToast.show(R.string.achi_video_not_available)
            } else {
                showVideoAdNow(10, w)
            }
        }
    }

    private fun showVideoAdNow(tryCount: Int, vDialog: Widget) {
        if (ControllerAdsVideoReward.isCahShow()) {
            vDialog.hide()
            ControllerAdsVideoReward.show {
                EventBus.post(EventAchiProgressIncr(API.ACHI_VIDEO_AD.index))
                RVideoAdView().send(api)
            }
        } else if (tryCount > 0 && ControllerAdsVideoReward.isLoading()) {
            ToolsThreads.main(1000) { showVideoAdNow(tryCount - 1, vDialog) }
        } else {
            vDialog.hide()
            ToolsToast.show(R.string.achi_video_not_loaded)
        }
    }

}