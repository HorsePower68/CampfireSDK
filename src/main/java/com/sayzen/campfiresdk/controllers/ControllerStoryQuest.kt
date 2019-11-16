package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.models.events.project.EventStoryQuestUpdated
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerStoryQuest {

    fun finishQuest(){
        if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_START){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_KARMA
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 3
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_KARMA){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_ACHI_SCREEN
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_ACHI_SCREEN){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_CHAT
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_CHAT){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_FANDOM
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        }  else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_FANDOM){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_PROFILE
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_PROFILE){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_FILTERS
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_FILTERS){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_POST
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 1
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_POST){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_FINISH
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 0
        } else if(ControllerSettings.storyQuestIndex == API.QUEST_STORY_FINISH){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_FUTURE
            ControllerSettings.storyQuestProgress = 0
            ControllerSettings.storyQuestProgressNeed = 0
        }
        EventBus.post(EventStoryQuestUpdated())
    }


    fun incrQuest(index:Long){
        if(index == ControllerSettings.storyQuestIndex) {
            ControllerSettings.storyQuestProgress++
            EventBus.post(EventStoryQuestUpdated())
        }
    }



}