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
import com.sayzen.campfiresdk.screens.activities.support.SSupport
import com.sayzen.campfiresdk.screens.activities.user_activities.SUserActivitiesList
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.java.libs.eventBus.EventBus

class SActivities : Screen(R.layout.screen_activities) {

    private val eventBus = EventBus
            .subscribe(EventActivitiesAdminCountChanged::class) { updateCounters() }

    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vRubrics: Settings = findViewById(R.id.vRubrics)
    private val vSupport: Settings = findViewById(R.id.vSupport)
    private val vTitleAdmins: Settings = findViewById(R.id.vTitleAdmins)
    private val vFandoms: Settings = findViewById(R.id.vFandoms)
    private val vUserReports: Settings = findViewById(R.id.vUserReports)
    private val vReports: Settings = findViewById(R.id.vReports)
    private val vBlock: Settings = findViewById(R.id.vBlock)
    private val vTitleProtoadmins: Settings = findViewById(R.id.vTitleProtoadmins)
    private val vRequests: Settings = findViewById(R.id.vRequests)
    private val vQuery: Settings = findViewById(R.id.vQuery)
    private val vErrors: Settings = findViewById(R.id.vErrors)

    private val vRelayRaceChip = ViewChip.instanceMini(vRelayRace, "")
    private val vRubricsChip = ViewChip.instanceMini(vRubrics, "")
    private val vFandomsChip = ViewChip.instanceMini(vFandoms, "")
    private val vUserReportsChip = ViewChip.instanceMini(vUserReports, "")
    private val vReportsChip = ViewChip.instanceMini(vReports, "")
    private val vBlockChip = ViewChip.instanceMini(vBlock, "")

    init {
        vRelayRace.setOnClickListener { Navigator.to(SUserActivitiesList()) }
        vRubrics.setOnClickListener { Navigator.to(SRubricsList(0, 0, ControllerApi.account.id)) }
        vFandoms.setOnClickListener { SAdministrationFandoms.instance(Navigator.TO) }
        vUserReports.setOnClickListener { Navigator.to(SAdministrationUserReports()) }
        vReports.setOnClickListener { Navigator.to(SAdministrationReports()) }
        vBlock.setOnClickListener { Navigator.to(SAdministrationBlock()) }
        vRequests.setOnClickListener { SAdministrationRequests.instance(Navigator.TO) }
        vQuery.setOnClickListener { SAdministrationQuery.instance(Navigator.TO) }
        vErrors.setOnClickListener { SAdministrationErrors.instance(Navigator.TO) }
        vSupport.setOnClickListener { SSupport.instance(Navigator.TO) }

        vSupport.visibility = View.GONE

        vTitleProtoadmins.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vRequests.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vQuery.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vErrors.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        if (!ControllerApi.isProtoadmin()) vBlock.setLineVisible(false)

        vRelayRaceChip.setBackground(ToolsResources.getAccentColor(context))
        vRubricsChip.setBackground(ToolsResources.getAccentColor(context))

        vRelayRace.setSubView(vRelayRaceChip)
        vRubrics.setSubView(vRubricsChip)
        vFandoms.setSubView(vFandomsChip)
        vUserReports.setSubView(vUserReportsChip)
        vReports.setSubView(vReportsChip)
        vBlock.setSubView(vBlockChip)

        if (!ControllerApi.can(API.LVL_ADMIN_BAN)) {
            vUserReports.isEnabled = false
            vUserReports.setSubtitle(R.string.activities_low_lvl)
            vUserReportsChip.visibility = View.GONE
        } else {
            vUserReportsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_MODER)) {
            vReports.isEnabled = false
            vReports.setSubtitle(R.string.activities_low_lvl)
            vReportsChip.visibility = View.GONE
        } else {
            vReportsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_FANDOMS_ACCEPT)) {
            vFandoms.isEnabled = false
            vFandoms.setSubtitle(R.string.activities_low_lvl)
            vFandomsChip.visibility = View.GONE
        } else {
            vFandomsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_FANDOM_ADMIN)) {
            vBlock.isEnabled = false
            vBlock.setSubtitle(R.string.activities_low_lvl)
            vBlockChip.visibility = View.GONE
        } else {
            vBlockChip.visibility = View.VISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        ControllerActivities.reloadActivities()
    }

    private fun updateCounters() {
        if (ControllerActivities.isAdministrationLoadInProgress()) {
            vRelayRaceChip.text = ""
            vRubricsChip.text = ""
            if (vFandomsChip.visibility == View.VISIBLE) vFandomsChip.text = "-"
            if (vUserReportsChip.visibility == View.VISIBLE) vUserReportsChip.text = "-"
            if (vReportsChip.visibility == View.VISIBLE) vReportsChip.text = "-"
            if (vBlockChip.visibility == View.VISIBLE) vBlockChip.text = "-"
        } else {
            vRelayRaceChip.text = "${if(ControllerActivities.getRelayRacesCount() == 0L) "" else ControllerActivities.getRelayRacesCount()}"
            vRubricsChip.text =  "${if(ControllerActivities.getRubricsCount() == 0L) "" else ControllerActivities.getRubricsCount()}"
            if (vFandomsChip.visibility == View.VISIBLE) vFandomsChip.text = "${ControllerActivities.getSuggestedFandomsCount()}"
            if (vUserReportsChip.visibility == View.VISIBLE) vUserReportsChip.text = "${ControllerActivities.getReportsUserCount()}"
            if (vReportsChip.visibility == View.VISIBLE) vReportsChip.text = "${ControllerActivities.getReportsCount()}"
            if (vBlockChip.visibility == View.VISIBLE) vBlockChip.text = "${ControllerActivities.getBlocksCount()}"
        }
    }


}