package com.sayzen.campfiresdk.screens.activities

import android.view.View
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesAdminCountChanged
import com.sayzen.campfiresdk.screens.activities.administration.api_errors.SAdministrationErrors
import com.sayzen.campfiresdk.screens.activities.administration.api_query.SAdministrationQuery
import com.sayzen.campfiresdk.screens.activities.administration.api_statistic.SAdministrationRequests
import com.sayzen.campfiresdk.screens.activities.administration.block.SAdministrationBlock
import com.sayzen.campfiresdk.screens.activities.administration.fandoms.SAdministrationFandoms
import com.sayzen.campfiresdk.screens.activities.administration.reports.SAdministrationReports
import com.sayzen.campfiresdk.screens.activities.administration.reports.SAdministrationUserReports
import com.sayzen.campfiresdk.screens.activities.user_activities.SUserActivitiesList
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.java.libs.eventBus.EventBus

class SActivities : Screen(R.layout.screen_activities) {

    private val eventBus = EventBus
            .subscribe(EventActivitiesAdminCountChanged::class) { updateCounters() }

    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vVideoAd: Settings = findViewById(R.id.vVideoAd)
    private val vTitleAdmins: Settings = findViewById(R.id.vTitleAdmins)
    private val vFandoms: Settings = findViewById(R.id.vFandoms)
    private val vUserReports: Settings = findViewById(R.id.vUserReports)
    private val vReports: Settings = findViewById(R.id.vReports)
    private val vBlock: Settings = findViewById(R.id.vBlock)
    private val vTitleProtoadmins: Settings = findViewById(R.id.vTitleProtoadmins)
    private val vRequests: Settings = findViewById(R.id.vRequests)
    private val vQuery: Settings = findViewById(R.id.vQuery)
    private val vErrors: Settings = findViewById(R.id.vErrors)

    private val vFandomsChip = ViewChip.instanceMini(vFandoms, "")
    private val vUserReportsChip = ViewChip.instanceMini(vUserReports, "")
    private val vReportsChip = ViewChip.instanceMini(vReports, "")
    private val vBlockChip = ViewChip.instanceMini(vBlock, "")

    init {
        vRelayRace.setOnClickListener { Navigator.to(SUserActivitiesList()) }
        vFandoms.setOnClickListener { SAdministrationFandoms.instance(Navigator.TO) }
        vUserReports.setOnClickListener { Navigator.to(SAdministrationUserReports()) }
        vReports.setOnClickListener { Navigator.to(SAdministrationReports()) }
        vBlock.setOnClickListener { Navigator.to(SAdministrationBlock()) }
        vRequests.setOnClickListener { SAdministrationRequests.instance(Navigator.TO) }
        vQuery.setOnClickListener { SAdministrationQuery.instance(Navigator.TO) }
        vErrors.setOnClickListener { SAdministrationErrors.instance(Navigator.TO) }
        vVideoAd.setOnClickListener { ControllerActivities.showVideoAd() }

        vTitleAdmins.visibility = if (ControllerApi.can(API.LVL_ADMIN_MODER)) View.VISIBLE else View.GONE
        vUserReports.visibility = if (ControllerApi.can(API.LVL_ADMIN_BAN)) View.VISIBLE else View.GONE
        vReports.visibility = if (ControllerApi.can(API.LVL_ADMIN_MODER)) View.VISIBLE else View.GONE
        vFandoms.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOMS_ACCEPT)) View.VISIBLE else View.GONE
        vBlock.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_ADMIN)) View.VISIBLE else View.GONE
        vTitleProtoadmins.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vRequests.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vQuery.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vErrors.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE

        vFandoms.setSubView(vFandomsChip)
        vUserReports.setSubView(vUserReportsChip)
        vReports.setSubView(vReportsChip)
        vBlock.setSubView(vBlockChip)
    }

    override fun onResume() {
        super.onResume()
        ControllerActivities.reloadAdministration()
    }

    private fun updateCounters() {
        if(ControllerActivities.isAdministrationLoadInProgress()){
            vFandomsChip.text = "-"
            vUserReportsChip.text = "-"
            vReportsChip.text = "-"
            vBlockChip.text = "-"
        }else{
            vFandomsChip.text = "${ControllerActivities.getSuggestedFandomsCount()}"
            vUserReportsChip.text = "${ControllerActivities.getReportsUserCount()}"
            vReportsChip.text = "${ControllerActivities.getReportsCount()}"
            vBlockChip.text = "${ControllerActivities.getBlocksCount()}"
        }
    }


}