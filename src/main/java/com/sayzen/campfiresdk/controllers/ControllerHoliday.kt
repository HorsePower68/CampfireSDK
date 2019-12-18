package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.tools.ToolsDate

object ControllerHoliday {

    fun onAppStart() {
        if (isNewYear()) ControllerScreenAnimations.snow()
    }

    fun getProfileBackgroundImage():Long?{
        if (isNewYear()) {
            return API_RESOURCES.IMAGE_NEW_YEAR_LIGHT_GIF
        }
        return null
    }

    fun getAvatar(accountId:Long, accountLvl:Long, karma30:Long): Long? {
        if (isNewYear()) {

            if(ControllerApi.isProtoadmin(accountId, accountLvl)) return API_RESOURCES.IMAGE_NEW_YEAR_SANTA
            if(ControllerApi.isAdmin(accountLvl, karma30)){
                val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_DEER_1, API_RESOURCES.IMAGE_NEW_YEAR_DEER_2, API_RESOURCES.IMAGE_NEW_YEAR_DEER_3, API_RESOURCES.IMAGE_NEW_YEAR_DEER_4, API_RESOURCES.IMAGE_NEW_YEAR_DEER_5)
                return array[(accountId % array.size).toInt()]
            }
            if(ControllerApi.isModerator(accountLvl, karma30)){
                val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_ELF_1, API_RESOURCES.IMAGE_NEW_YEAR_ELF_2, API_RESOURCES.IMAGE_NEW_YEAR_ELF_3, API_RESOURCES.IMAGE_NEW_YEAR_ELF_4)
                return array[(accountId % array.size).toInt()]
            }
            val array = arrayOf(API_RESOURCES.IMAGE_NEW_YEAR_KID_1, API_RESOURCES.IMAGE_NEW_YEAR_KID_2, API_RESOURCES.IMAGE_NEW_YEAR_KID_3, API_RESOURCES.IMAGE_NEW_YEAR_KID_4, API_RESOURCES.IMAGE_NEW_YEAR_KID_5, API_RESOURCES.IMAGE_NEW_YEAR_KID_6, API_RESOURCES.IMAGE_NEW_YEAR_KID_7, API_RESOURCES.IMAGE_NEW_YEAR_KID_8, API_RESOURCES.IMAGE_NEW_YEAR_KID_9, API_RESOURCES.IMAGE_NEW_YEAR_KID_10)
            return array[(accountId % array.size).toInt()]
        }
        return null
    }

    fun getBackground(accountId:Long): Int? {
        if (isNewYear()) {
            val array = arrayOf(R.color.blue_500,R.color.light_blue_500,
                    R.color.cyan_500, R.color.indigo_300, R.color.red_500,
                    R.color.yellow_500, R.color.lime_500, R.color.light_green_500,
                    R.color.teal_500, R.color.deep_purple_300, R.color.purple_300,
                    R.color.pink_400, R.color.deep_orange_400)
             return array[(accountId % array.size).toInt()]
        }
        return null
    }

    fun isNewYear(): Boolean {

        return (ToolsDate.getCurrentMonthOfYear() == 12 && ToolsDate.getCurrentDayOfMonth() > 28) || (ToolsDate.getCurrentMonthOfYear() == 1 && ToolsDate.getCurrentDayOfMonth() < 2)
    }

}