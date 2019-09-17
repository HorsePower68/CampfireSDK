package com.sayzen.campfiresdk.screens.other.about

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast

class SAboutApp : Screen(R.layout.screen_other_abount_app){

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vVersion: TextView = findViewById(R.id.vVersion)

    init {
        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_ABOUT)
            ToolsToast.show(R.string.app_copied)
        }
        vVersion.text = "Campfire ${ToolsAndroid.getVersion()}"
    }

}