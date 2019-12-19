package com.sayzen.campfiresdk.screens.activities.quests

import android.annotation.SuppressLint
import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

@SuppressLint("MissingPermission")
class SQuestNewYear : SQuest() {

    /*

        Что делать на слайде 5?
        Что делать на слайде 8?
        Выбрать товарищей?

     */

    var weather_thunder = false

    val animation = DrawAnimationSnow()
    var sleepCounter = 3
    var phisic = false
    var stic = false

    val item_0 = QuestItem("И так... где вы:\nЭто новая система Campfire - Квесты.\n").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Квесты?") { to(item_0_1) }

    val item_0_1 = QuestItem("В планах создавать различные квесты, возможно они будут связаны с какими-то событиями, возможно нет. \nВсё зависит от тога как их воспримут пользователи.").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Пока ничего не понимаю...") { to(item_0_2) }

    val item_0_2 = QuestItem("Квесты - некая история, разбавленная юмором и эпичностью, загадками, интерактивами и кучей всего другого что мы пока не придумали.").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Это как бесконечно лето?"){ to(item_0_3) }

    val item_0_3 = QuestItem("Да!\nИменно так!\nЯ просто стеснялся сказать.").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Смогу ли я создать свой квест?"){ to(item_0_4) }

    val item_0_4 = QuestItem("Пока не известно. \nВсе зависит от пользователей, если им понравится такой формат, то мы будем развивать эту идею и дальше.").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Ясно. Давайте посмотрим как это работает."){ to(item_0_5) }

    val item_0_5 = QuestItem("Чтож, это наш первый квест, так что не судите строго, будем рады отзывам и замечаниям. :)").setLabel("Вступление").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_1)
            .addButton("Да начинайте уже!"){ makeDarknes{to(item_1)} }

    val item_1 = QuestItem("Рассвет. Идет мелкий снег. \nРядом слышно потрескивание тлеющих углей \nВы спите у почти потухшего костра...").setLabel("Глухой лес, утро").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2)
            .addButton("Поспать ещё") { makeDarknes() }
            .addButton("Проснуться") { sleepCounter--;if (sleepCounter < 0) makeThunderBig() else makeThunder() }

    //  TODO Должно повлиять в дальйнешем, активность с физческим состоянием
    //  TODO Должно повлиять в дальйнешем, активность с палкой
    val item_2 = QuestItem("Вы медленно просыпаетесь. Выше тело очень болит из-за ночи проведенной в тяжелых доспехах.").setLabel("Глухой лес, утро").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2)
            .addButton("Размяться") { to(item_2_1) }
            .addButton("Осмотреться возле костра") { stic = true;to(item_2_2) }

    val item_2_1 = QuestItem("Вы сняли доспехи и немного размялись, умыли лицо снегом, почистили одежду. Вы чувствуете себя намного лучше.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addTextJump(effect_good("Подъем сил"))
            .setOnStart { phisic = true }
            .addButton("Так... Что-же дальше?") { phisic = true;to(item_3) }

    val item_2_2 = QuestItem("Вокруг вас лес. \nЛагерь разбит рядом с одиноким камнем, больше похожем на кусок скалы. \nВы замечаете множество следов идущих в разные стороны от костра.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addButton("Поискать что нибудь полезное") { to(item_2_3) }

    val item_2_3 = QuestItem("Вы осматриваете сугробы вокруг лагеря и находите длинную палку, как опытный игрок РПГ вы понимаете что она вам еще понадобиться и забираете её.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addTextJump(item("Подозрительно длинная палка"))
            .addButton("Так... Что-же дальше?") { to(item_3) }

    val item_3 = QuestItem("Вы задумались... \nВы пытаетесь вспомнить что-же вы здесь делает \n... \nО нееееет! \nВас окутывает ужас... \nВ вашей голове только одна мысль:").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addButton("Похоже это банальный ход с амнезией главного героя!") { to(item_3_1) }

    val item_3_1 = QuestItem("К сожалению это именно он. \nВы стараетесь успокоиться, но вам все еще не пособие.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addButton("Они не могли придумать что-то поинтересней?") { to(item_3_2) }

    val item_3_2 = QuestItem("Постепенно к вам приходит осознание что это не так уж и плохо, и что лучше вам быть благодарным и за это, на другие праздники не было вообще ничего.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addButton("Нет, я все еще возмущен!") { to(item_3_3) }
            .addButton("Да, продолжим.") { to(item_4) }

    val item_3_3 = QuestItem("Нед вами сгущаются тучи. Все вокруг погружается во мрак. Вы слышите раскаты грома... \nВдруг над вами заноситься банхамер. Еще один раскат грома, молния... Вы ослеплены на мгновение...").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .setOnStart { startThunder() }
            .setOnFinish { stopThunder() }
            .addButton("Что происходит?") {  makeDarknes{to(item_3_4)} }

    //  TODO Выдать пользователю проклятье хейтера
    val item_3_4 = QuestItem("Тучи пропали. Но вы чувствуете слабость и опустошение.").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
            .addTextJump(effect_bad("Проклятье хейтера"))
            .setOnStart {;makeThunderBig() }
            .addButton("Смириться с таким сюжетом и продолжить.") { to(item_4) }

    val item_4 = QuestItem("...").setImage(API_RESOURCES.EPIC_QUEST_NEW_YEAR_2).setLabel("Глухой лес, утро")
    //  .setButton_1("Продолжим") { setQuestItem(item_2_1) }

    /*
    - Кажется огонь горел всю ночь и растопил весь снег вокруг. Но с боку на снегу видны следы от доспехов. Буд-то кто-то так же как и я спал у костра, но встал раньше и ушёл.
     Вы вспоминаете, что как раз ночью получили Новогодний квест, скорее всего все остальные кемповцы пошли выполнять и забыли вас разбудить.
    + Отказаться выполнять квест.
    + Прочитать квест.

    - Хм… Я %user_name%, у меня есть доспехи, меч и симпатичный шлем с тремя дырками. Боже мой, да я же Рыцарь! Кажется, этой ночью мы сидели у костра с другими рыцарями.
      Вы вспоминаете, что как раз ночью получили Новогодний квест, скорее всего все остальные кемповцы пошли выполнять его и забыли вас разбудить.
    + Отказаться выполнять квест
    + Прочитать квест

    - Квесты? Звучит как что-то страшное, опасное и совсем не весёлое. Лучше останусь здесь, у костра, вон тут как раз лес рядом, там грибы, ягоды, шишки разные. Квесты это точно не для меня, вдруг там драконы? Боже, драконы, я же зажарюсь в этой броне как картошечка в мундире. Ха. Смешно. Картошечка…

    - ...
    + Подумать над планом
    + К чёрту план - %user_name% страшен в своей импровизации
    + Чёрт. Да кто такой этот Староста?

    - Если остановиться и подумать. Хм. Путь до домика старосты не близкий. Кругом снег. Да ещё и эти боксы, явно не самый лёгкий груз, думаю один я не справлюсь. Мне нужна команда! Хоть об этом и не сказано в квесте, но команда бравых кемповцов надёжна как алмазная броня и лишней не будет.
      Так. Главный вопрос. Кого позвать с собой в это приключение?
    + Выбрать товарищей.
    + К чёрту план – %user_name% страшен в своей импровизации

    - Никакой план мне не нужен. Не зря в кемпе меня зовут %user_name%! все знают, что я справлюсь с любым квестом. Тем более вся слава карма достанется мне одному. Главное выбрать оружие побольше да помощнее, а остальное не так важно.

    - Местный староста Зеон живёт в трёх часах ходьбы от сюда, посреди леса на высоком холме. Раньше я часто бывал у него, когда ходил на охоту. Новогодний квест отличный повод навестить старого знакомого!
    + Подумать над планом
    + К чёрту план - %user_name% страшен в своей импровизации
     */


    init {
        ControllerScreenAnimations.addAnimationWithClear(animation)
        to(item_0)
    }

    //
    //  Efects
    //

    fun effect_good(text:String) = "{grey [ получен эффект: }{green _${text}_}{grey  ]}"

    fun effect_bad(text:String) = "{grey [ получен эффект: }{red _${text}_}{grey  ]}"

    fun item(text:String) = "{grey [ получен предмет: }{orange _${text}_}{grey  ]}"

    fun startThunder() {
        weather_thunder = true
        stepThunder()
    }

    private fun stepThunder() {
        if (!weather_thunder) return
        ToolsThreads.thread(ToolsMath.randomLong(1000, 200)) {
            makeThunder()
            stepThunder()
        }
    }

    fun makeThunder() {
        animation.inflate(2000)
        ToolsVibration.vibrate(200)
    }

    fun makeThunderBig() {
        animation.inflate(4000)
        to(item_2)
        ToolsVibration.vibrate(600)
    }

    fun stopThunder() {
        weather_thunder = false
    }

    fun makeDarknes(onFinish:()->Unit={}){
        ToolsView.toAlpha(viewScreen, 3000) { ToolsView.fromAlpha(viewScreen, 4000); onFinish.invoke() }
    }

}