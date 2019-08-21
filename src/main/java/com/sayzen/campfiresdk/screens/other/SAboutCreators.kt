package com.sayzen.campfiresdk.screens.other

import android.view.View
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast

class SAboutCreators : Screen(R.layout.screen_other_abount_creators){

    private val vCopyLink:View = findViewById(R.id.vCopyLink)

    private val vCampfireZeon: View = findViewById(R.id.vCampfireZeon)
    private val vEmailZeon: View = findViewById(R.id.vEmailZeon)
    private val vVkZeon: View = findViewById(R.id.vVkZeon)

    private val vCampfireSaynok: View = findViewById(R.id.vCampfireSaynok)
    private val vEmailSaynok: View = findViewById(R.id.vEmailSaynok)
    private val vVkSaynok: View = findViewById(R.id.vVkSaynok)

    private val vCampfireEgor: View = findViewById(R.id.vCampfireEgor)
    private val vEmailEgor: View = findViewById(R.id.vEmailEgor)
    private val vVkEgor: View = findViewById(R.id.vVkEgor)

    private val vCampfireTurbo: View = findViewById(R.id.vCampfireTurbo)
    private val vEmailTurbo: View = findViewById(R.id.vEmailTurbo)
    private val vVkTurbo: View = findViewById(R.id.vVkTurbo)

    init {
        vCampfireZeon.setOnClickListener { SAccount.instance(1, Navigator.TO) }
        vEmailZeon.setOnClickListener { ToolsIntent.startMail("zeooon@ya.ru") }
        vVkZeon.setOnClickListener { ControllerCampfireSDK.openLink("https://vk.com/zeooon") }

        vCampfireSaynok.setOnClickListener { SAccount.instance(2720, Navigator.TO) }
        vEmailSaynok.setOnClickListener { ToolsIntent.startMail("saynokdeveloper@gmail.com") }
        vVkSaynok.setOnClickListener { ControllerCampfireSDK.openLink("vk.com/saynok") }

        vCampfireEgor.setOnClickListener { SAccount.instance(9447, Navigator.TO) }
        vEmailEgor.setOnClickListener { ToolsIntent.startMail("georgepro036@gmail.com") }
        vVkEgor.setOnClickListener { ControllerCampfireSDK.openLink("vk.com/id216069359") }

        vCampfireTurbo.setOnClickListener { SAccount.instance(8083, Navigator.TO) }
        vEmailTurbo.setOnClickListener { ToolsIntent.startMail("turboRO99@gmail.com") }
        vVkTurbo.setOnClickListener { ControllerCampfireSDK.openLink("vk.com/turboa99") }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_CREATORS)
            ToolsToast.show(R.string.app_copied)
        }
    }

}