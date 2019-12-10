package com.sayzen.campfiresdk.screens.activities.quests

class QuestItem(){

    var imageId = 0L

    var label = ""
    var text = ""

    var button_1 = ""
    var button_2 = ""
    var button_3 = ""
    var button_4 = ""

    var action_1:()->Unit = {}
    var action_2:()->Unit = {}
    var action_3:()->Unit = {}
    var action_4:()->Unit = {}

    constructor(imageId:Long, text:String):this(){
        this.imageId = imageId
        this.text = text
    }

    fun setLabel(label:String):QuestItem{
        this.label = label
        return this
    }

    fun setButton_1(text:String, action:()->Unit):QuestItem{
        button_1 = text
        action_1 = action
        return this
    }

    fun setButton_2(text:String, action:()->Unit):QuestItem{
        button_2 = text
        action_2 = action
        return this
    }

    fun setButton_3(text:String, action:()->Unit):QuestItem{
        button_3 = text
        action_3 = action
        return this
    }

    fun setButton_4(text:String, action:()->Unit):QuestItem{
        button_4 = text
        action_4 = action
        return this
    }

}