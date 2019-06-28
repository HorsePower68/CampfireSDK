package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.widgets.WidgetAlert

object ControllerClosedFandoms {

    fun showAlertIfNeed(screen: Screen, fandomId: Long, isPost: Boolean) {

        val key = "ControllerClosedFandoms_$fandomId"

        if(WidgetAlert.check(key)){
            return
        }

        val w = WidgetAlert()
                .setTopTitleText(R.string.app_attention)
                .setCancelable(false)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setChecker(key, R.string.message_closed_fandom_check)
                .setOnEnter(R.string.app_continue)
                .setOnCancel(R.string.app_cancel){
                    Navigator.remove(screen)
                }

        if (isPost) {
            w.setText(R.string.message_closed_fandom_post)
        } else {
            w.setText(R.string.message_closed_fandom)
        }

        w.asSheetShow()
    }

}