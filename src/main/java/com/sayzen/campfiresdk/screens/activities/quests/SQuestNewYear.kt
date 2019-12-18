package com.sayzen.campfiresdk.screens.activities.quests

import android.annotation.SuppressLint
import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView

@SuppressLint("MissingPermission")
class SQuestNewYear : SQuest() {

    /*

        Что делать на слайде 5?
        Что делать на слайде 8?
        Выбрать товарищей?

     */

    val animation = DrawAnimationSnow()

    var item_1_counter = 3
    val item_1 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1, "%user_name% спит у костра.")
            .setLabel("Ранее утро")
            .setButton_1("Поспать ещё") {
                ToolsView.fromAlpha(vSplash, 3000){
                    ToolsView.toAlpha(vSplash, 4000)
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

    val item_2 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2, "Спать в тяжёлых железных доспехах холодно, но чертовски удобно. Главное не проспать новогодний квест, чтобы ребята у следующего костра не заспойлерили весь сюжет.")
            .setLabel("Глухой лес, утро")
            .setButton_1("Осмотреться возле костра") {setQuestItem(item_3)}
            .setButton_2("Попытаться вспомнить кто я") {setQuestItem(item_4)}

    val item_3 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_3, "Кажется огонь горел всю ночь и растопил весь снег вокруг. Но с боку на снегу видны следы от доспехов. Буд-то кто-то так же как и я спал у костра, но встал раньше и ушёл.\n\nВы вспоминаете, что как раз ночью получили Новогодний квест, скорее всего все остальные кемповцы пошли выполнять и забыли вас разбудить.")
            .setLabel("Глухой лес, утро")
            .setButton_1("Отказаться выполнять квест.") {setQuestItem(item_5)}
            .setButton_2("Прочитать квест.") {setQuestItem(item_6)}

    val item_4 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_3, "Хм… Я %user_name%, у меня есть доспехи, меч и симпатичный шлем с тремя дырками. Боже мой, да я же Рыцарь! Кажется, этой ночью мы сидели у костра с другими рыцарями.\n\nВы вспоминаете, что как раз ночью получили Новогодний квест, скорее всего все остальные кемповцы пошли выполнять его и забыли вас разбудить.")
            .setLabel("Глухой лес, утро")
            .setButton_1("Отказаться выполнять квест") {setQuestItem(item_5)}
            .setButton_2("Прочитать квест") {setQuestItem(item_6)}

    val item_5 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_5, "Квесты? Звучит как что-то страшное, опасное и совсем не весёлое. Лучше останусь здесь, у костра, вон тут как раз лес рядом, там грибы, ягоды, шишки разные. Квесты это точно не для меня, вдруг там драконы? Боже, драконы, я же зажарюсь в этой броне как картошечка в мундире. Ха. Смешно. Картошечка…")
            .setLabel("Глухой лес, утро")

    val item_6 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_4, "…")
            .setLabel("Глухой лес, утро")
            .setButton_1("Подумать над планом") {setQuestItem(item_7)}
            .setButton_2("К чёрту план - %user_name% страшен в своей импровизации") {setQuestItem(item_8)}
            .setButton_2("Чёрт. Да кто такой этот Староста?") {setQuestItem(item_9)}

    val item_7 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_4, "Если остановиться и подумать. Хм. Путь до домика старосты не близкий. Кругом снег. Да ещё и эти боксы, явно не самый лёгкий груз, думаю один я не справлюсь. Мне нужна команда! Хоть об этом и не сказано в квесте, но команда бравых кемповцов надёжна как алмазная броня и лишней не будет.\n\nТак. Главный вопрос. Кого позвать с собой в это приключение?")
            .setLabel("Глухой лес, утро")
            .setButton_1("Выбрать товарищей.") {}
            .setButton_2("К чёрту план – %user_name% страшен в своей импровизации") {setQuestItem(item_8)}

    val item_8 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_4, "Никакой план мне не нужен. Не зря в кемпе меня зовут %user_name%! все знают, что я справлюсь с любым квестом. Тем более вся слава карма достанется мне одному. Главное выбрать оружие побольше да помощнее, а остальное не так важно.")
            .setLabel("Глухой лес, утро")

    val item_9 = QuestItem(API_RESOURCES.EPIC_QUEST_NEW_YEAR_4, "Местный староста Зеон живёт в трёх часах ходьбы от сюда, посреди леса на высоком холме. Раньше я часто бывал у него, когда ходил на охоту. Новогодний квест отличный повод навестить старого знакомого!")
            .setLabel("Глухой лес, утро")
            .setButton_1("Подумать над планом") {setQuestItem(item_7)}
            .setButton_2("К чёрту план - %user_name% страшен в своей импровизации") {setQuestItem(item_8)}


    init {
        ControllerScreenAnimations.addAnimationWithClear(animation)
        setQuestItem(item_1)
    }



}