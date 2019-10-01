package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsDate

object ControllerHoliday {

    var IS_ENABLED = false

    fun getFeedMessageTitle():String?{
        if(isCampfireBirthday()) return ToolsResources.s(R.string.holiday_campfire_birthday_title)
        return null
    }

    fun getFeedMessageText():String?{
        if(isCampfireBirthday()) return ToolsResources.s(R.string.holiday_campfire_birthday_text)
        return null
    }

    fun isCampfireBirthday():Boolean{
        val time = ControllerApi.currentTime()
        return ToolsDate.getCurrentMonthOfYear(time) == 3 && ToolsDate.getCurrentDayOfMonth(time) == 10
    }

}