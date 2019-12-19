package com.sayzen.campfiresdk.screens.activities.quests

class QuestItem() {

    var imageId = 0L

    var label = ""
    var text = ""

    var button_1 = ""
    var button_2 = ""
    var button_3 = ""
    var button_4 = ""

    var onStart: () -> Unit = {}
    var onFinish: () -> Unit = {}

    var action_1: () -> Unit = {}
    var action_2: () -> Unit = {}
    var action_3: () -> Unit = {}
    var action_4: () -> Unit = {}

    constructor(text: String) : this() {
        this.text = text
    }

    fun addText(text:String): QuestItem{
        this.text +="\n$text"
        return this
    }
    fun addTextJump(text:String): QuestItem{
        this.text +="\n\n$text"
        return this
    }

    fun setImage(imageId: Long): QuestItem {
        this.imageId = imageId
        return this
    }

    fun setLabel(label: String): QuestItem {
        this.label = label
        return this
    }

    fun addButton(text: String, action: () -> Unit): QuestItem {
        if (button_1.isEmpty()) setButton_1(text, action)
        else if (button_2.isEmpty()) setButton_2(text, action)
        else if (button_3.isEmpty()) setButton_3(text, action)
        else if (button_4.isEmpty()) setButton_4(text, action)
        return this
    }

    fun setButton_1(text: String, action: () -> Unit): QuestItem {
        button_1 = text
        action_1 = action
        return this
    }

    fun setButton_2(text: String, action: () -> Unit): QuestItem {
        button_2 = text
        action_2 = action
        return this
    }

    fun setButton_3(text: String, action: () -> Unit): QuestItem {
        button_3 = text
        action_3 = action
        return this
    }

    fun setButton_4(text: String, action: () -> Unit): QuestItem {
        button_4 = text
        action_4 = action
        return this
    }

    fun setOnStart(onStart: () -> Unit): QuestItem {
        this.onStart = onStart
        return this
    }

    fun setOnFinish(onFinish: () -> Unit): QuestItem {
        this.onFinish = onFinish
        return this
    }

}