package com.sayzen.campfiresdk.screens.fandoms.story_quest

import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

open class SStoryQuest(r:Int) : Screen(r){

    companion object{

        fun to(questIndex:Long){
            when(questIndex){
                API.QUEST_STORY_START.index -> Navigator.to(SStoryQuest_1())
                API.QUEST_STORY_KARMA.index -> Navigator.to(SStoryQuest_2())
            }
        }

    }

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
    }

}