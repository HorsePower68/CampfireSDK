package com.sayzen.campfiresdk.screens.post.create

import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.activities.user_activities.SUserActivitiesList
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.splash.Sheet
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseDate
import com.sup.dev.android.views.widgets.WidgetChooseTime
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

class WidgetTagsAdditional(
        private val fandomId: Long,
        private val language: Long,
        private val closed: Boolean,
        private val publicationTag3: Long
) : Widget(R.layout.screen_post_create_tags_widget) {

    private var pendingDate = 0L
    private var rubricId = 0L
    private var userActivityId = 0L
    private var userActivityIdNextUserId = 0L
    var needReShow = false

    private val vNotifyFollowers: SettingsCheckBox = findViewById(R.id.vNotifyFollowers)
    private val vPending: SettingsCheckBox = findViewById(R.id.vPending)
    private val vClose: SettingsCheckBox = findViewById(R.id.vClose)
    private val vRubric: Settings = findViewById(R.id.vRubric)
    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)

    init {
        vNotifyFollowers.isEnabled = publicationTag3 == 0L
        vNotifyFollowers.setChecked(false)
        vPending.setChecked(false)
        vClose.setChecked(closed)

        vPending.setOnClickListener { onPendingClicked() }
        vRubric.setOnClickListener { onRubricClicked() }
        vRelayRace.setOnClickListener { onRelayRaceClicked() }
    }

    override fun asSheetShow(): Sheet {
        needReShow = false
        return super.asSheetShow()
    }


    private fun onPendingClicked() {
        if (!vPending.isChecked()/*После нажатия положение меняется*/) setPendingDate(0)
        else {
            WidgetChooseDate()
                    .setOnEnter(R.string.app_choose) { _, date ->
                        WidgetChooseTime()
                                .setOnEnter(R.string.app_choose) { _, h, m ->
                                    setPendingDate(ToolsDate.getStartOfDay(date) + (h * 60L * 60 * 1000) + (m * 60L * 1000))
                                }
                                .asSheetShow()
                    }
                    .asSheetShow()
        }
    }

    private fun clearRubric() {
        vRubric.setTitle(R.string.post_create_rubric)
        rubricId = 0
    }

    private fun clearRelay() {
        vRelayRace.setTitle(R.string.post_create_relay_race)
        userActivityId = 0
    }

    private fun onRubricClicked() {
        if (rubricId > 0) {
            clearRubric()
        } else {
            needReShow = true
            Navigator.to(SRubricsList(fandomId, language, ControllerApi.account.id) {
                clearRelay()
                vRubric.setTitle(ToolsResources.s(R.string.app_rubric) + ": " + it.name)
                rubricId = it.id
            })
        }
    }

    private fun onRelayRaceClicked() {
        if (userActivityId > 0) {
            clearRelay()
        } else {
            Navigator.to(SUserActivitiesList(fandomId, language) { userActivity ->
                ToolsThreads.main(200) {
                    WidgetTagsRelayRaceNextUser(userActivity.id) {
                        needReShow = true
                        clearRubric()
                        vRelayRace.setTitle(ToolsResources.s(R.string.app_relay_race) + ": " + userActivity.name)
                        userActivityId = userActivity.id
                        userActivityIdNextUserId = it
                    }
                            .asSheetShow()

                }
            })
        }
    }

    private fun setPendingDate(date: Long) {
        var dateV = date
        if (dateV != 0L && dateV < System.currentTimeMillis()) {
            ToolsToast.show(R.string.post_create_pending_error)
            dateV = 0L
        }

        pendingDate = dateV
        if (dateV > 0) {
            vPending.setTitle(ToolsResources.s(R.string.post_create_pending) + " (${ToolsDate.dateToString(dateV)})")
            vPending.setChecked(true)
        } else {
            vPending.setTitle(R.string.post_create_pending)
            vPending.setChecked(false)
        }

    }

    fun isNotifyFollowers() = vNotifyFollowers.isChecked()

    fun getPendingDate() = pendingDate

    fun isClosed() = vClose.isChecked()

    fun getRubricId() = rubricId

    fun getUserActivityId() = userActivityId

    fun getUserActivityIdNextUserId() = userActivityIdNextUserId

}