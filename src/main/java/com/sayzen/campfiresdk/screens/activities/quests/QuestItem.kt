package com.sayzen.campfiresdk.screens.activities.quests

class QuestItem() {

    var imageId = 0L

    var label = ""
    var index = ""
    var text = ""

    val buttons = ArrayList<QuestButton>()

    var onStart: () -> Unit = {}
    var onFinish: () -> Unit = {}


    constructor(index:String) : this() {
        this.index = index
    }

    fun text(text: String):QuestItem{
        this.text = text
        return this
    }

    fun addText(text: String): QuestItem {
        this.text += "\n$text"
        return this
    }

    fun addTextJump(text: String): QuestItem {
        this.text += "\n\n$text"
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

    fun addButtonParams(text: String, action: () -> Unit): QuestButton {
        val button = QuestButton()
        button.text = text
        button.action = action
        buttons.add(button)
        return button
    }

    fun addButton(text: String, action: () -> Unit): QuestItem {
        addButtonParams(text, action)
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

    //
    //  Support
    //

    inner class QuestButton {

        var text = ""
        var enabled : () -> Boolean = {true}
        var visible : () -> Boolean = {true}
        var action: () -> Unit = {}

        fun enabled(enabled:() -> Boolean):QuestButton{
            this.enabled = enabled
            return this
        }

        fun visible(visible:() -> Boolean):QuestButton{
            this.visible = visible
            return this
        }

        fun finish() = this@QuestItem

    }

}