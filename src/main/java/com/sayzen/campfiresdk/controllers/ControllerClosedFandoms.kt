package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.tools.ToolsCollections

object ControllerClosedFandoms {

    fun showAlertIfNeed(screen: Screen, fandomId: Long, isPost: Boolean) {

        if (!ControllerCampfireSDK.ENABLE_CLOSE_FANDOM_ALERT) return

        if (ControllerSettings.fandomNSFW.contains(fandomId)) return

        val w = WidgetAlert()
                .setTopTitleText(R.string.app_attention)
                .setCancelable(false)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setChecker(R.string.message_closed_fandom_check)
                .setOnEnter(R.string.app_continue)
                .setOnCancel(R.string.app_cancel) { Navigator.remove(screen) }
                .setOnChecker { if (it) ControllerSettings.fandomNSFW = ToolsCollections.add(fandomId, ControllerSettings.fandomNSFW) }

        if (isPost) {
            w.setText(R.string.message_closed_fandom_post)
        } else {
            w.setText(R.string.message_closed_fandom)
        }

        w.asSheetShow()
    }

}