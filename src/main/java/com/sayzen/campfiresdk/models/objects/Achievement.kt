package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.AchievementInfo
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources

class Achievement(
        val info: AchievementInfo,
        private val text: String,
        val colorRes: Int,
        val clickable: Boolean,
        val image: Int
) {

    constructor(info: AchievementInfo, text: Int, colorRes: Int, clickable: Boolean, image: Int) : this(info, ToolsResources.s(text), colorRes, clickable, image)

    init {
    }

    fun getText(includePress: Boolean): String {
        return if (clickable && includePress)
            text + " " + ToolsResources.s(R.string.achi_click)
        else
            text
    }

}
