package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.AchievementInfo
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources

class Achievement(
        val info: AchievementInfo,
        val text: Int,
        val colorRes: Int,
        val clickable: Boolean,
        val image: Int,
        val textFormat: Array<String> = emptyArray()
) {

    fun getText(includePress: Boolean): String {
        return if (clickable && includePress)
            ToolsResources.s(text, *textFormat) + " " + ToolsResources.s(R.string.achi_click)
        else
            ToolsResources.s(text, *textFormat)
    }

}
