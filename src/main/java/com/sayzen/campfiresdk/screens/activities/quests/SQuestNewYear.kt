package com.sayzen.campfiresdk.screens.activities.quests

import android.annotation.SuppressLint
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView

@SuppressLint("MissingPermission")
class SQuestNewYear : SQuest() {

    val animation = DrawAnimationSnow()

    var item_1_counter = 3
    val item_1 = QuestItem(261204, "27 декабря\nCampfire\nРасвет\n\n%user_name% спит у костра")
            .setLabel("Глухой лес, утро")
            .setButton_1("Поспать ещё") {
                ToolsView.fromAlpha(vSplash, 3000){
                    ToolsView.toAlpha(vSplash, 2000)
                }
            }
            .setButton_2("Проснуться") {
                item_1_counter--
                if(item_1_counter < 0) {
                    animation.inflate(4000)
                    setQuestItem(item_2)
                    ToolsVibration.vibrate(600)
                }
                else {
                    animation.inflate(2000)
                    ToolsVibration.vibrate(200)
                }
            }

    val item_2 = QuestItem(261204, "Спать в тяжелых желеных доспехах холодно, но чертовски удобно. Главное не проспать новогодний квест, чтобы ребята у следующего костране заспойлерили весь сюжет.")
            .setLabel("Глухой лес, утро")
            .setButton_1("Осмотреться возле костра") {}
            .setButton_2("Попытаться вспомнить кто я") {}


    init {
        ControllerScreenAnimations.addAnimationWithClear(animation)
        setQuestItem(item_1)
    }



}