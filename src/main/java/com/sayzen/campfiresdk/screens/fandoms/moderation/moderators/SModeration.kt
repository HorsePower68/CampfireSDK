package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators

import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.moderators.SModerators
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.prison.SPrision
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.privilege.SPrivilege
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.reports.SReports
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.Settings

class SModeration(
        fandomId: Long,
        languageId: Long
) : Screen(R.layout.screen_fandom_moderators) {

    private val vModerators: Settings = findViewById(R.id.vModerators)
    private val vPrivilege: Settings = findViewById(R.id.vPrivilege)
    private val vPrison: Settings = findViewById(R.id.vPrison)
    private val vReports: Settings = findViewById(R.id.vReports)

    init {
        vModerators.setOnClickListener { Navigator.to(SModerators(fandomId, languageId)) }
        vPrivilege.setOnClickListener { Navigator.to(SPrivilege(fandomId, languageId)) }
        vPrison.setOnClickListener { Navigator.to(SPrision(fandomId, languageId)) }
        vReports.setOnClickListener {
            if (!ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_BLOCK))
                ToolsToast.show(R.string.error_low_lvl_or_karma)
            else Navigator.to(SReports(fandomId, languageId))
        }

    }

}
