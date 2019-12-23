package com.sayzen.campfiresdk.screens.activities.quests

import android.annotation.SuppressLint
import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sup.dev.android.tools.ToolsVibration
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

@SuppressLint("MissingPermission")
class SQuestNewYear : SQuest() {

    fun createQuest(){

        //  TODO Активность из-за кторой накладывается проклятие хейтера (плюнуть на идола?)
        //  TODO Активность из-за кторой блокируется телефон (дернуть за веревку?)
        //  TODO Шторм
        //  TODO Отправка уведолмения другу
        //  TODO Призыв одного из 3х пользователей Campfire ???

        //  TODO Часть с осознаним (сцена 4) выглядит очень странно

        //
        //  Вступление
        //

        globalLabel = "Вступление"
        globalImage = API_RESOURCES.EPIC_QUEST_NEW_YEAR_1

        addQuest(
                QuestItem("0")
                        .text("Квесты - новая система Campfire.\nМы считаем что они могут стать очень важной частью приложения.")
                        .addButton("Квесты?") { to("0_1") },
                QuestItem("0_1")
                        .text("Квесты - некая история, разбавленная юмором и эпичностью, загадками, интерактивами и кучей всего другого что мы пока не придумали.")
                        .addButton("Это как бесконечно лето?") { to("0_2") },
                QuestItem("0_2")
                        .text("Да! Именно так!\nЯ просто стеснялся сказать.")
                        .addButton("Кажется я начинаю понимать") { to("0_3") },
                QuestItem("0_3")
                        .text("В планах создавать различные квесты, возможно они будут связаны с какими-то событиями, возможно нет. \nВсё зависит от тога как их воспримут пользователи.")
                        .addButton("Смогу ли я создать свой квест?") { to("0_4") },
                QuestItem("0_4")
                        .text("Пока не известно. \nВсе зависит от пользователей, если им понравится такой формат, то мы будем развивать эту идею и дальше.")
                        .addButton("Ясно. Давайте посмотрим как это работает.") { to("0_5") },
                QuestItem("0_5")
                        .text("Чтож, это наш первый квест, он достаточно короткий, но, надеемся что интересный, будем рады отзывам и замечаниям. :)").setLabel("Вступление")
                        .addButton("Да начинайте уже!") { makeDarknes { to("1") } }
        )

        //
        //  Акт 1. Сцена 1:
        //

        globalLabel = "Глухой лес, утро"
        globalImage = API_RESOURCES.EPIC_QUEST_NEW_YEAR_2
        var longSleep = false

        var sleepCounter = 3

        addQuest(
                QuestItem("1")
                        .text("Рассвет. Идет мелкий снег. \nРядом слышно потрескивание тлеющих углей \nВы спите у почти потухшего костра...")
                        .addButtonParams("Поспать ещё") { longSleep=true;makeDarknes{to("1")} }.visible{!longSleep}.finish()
                        .addButton("Проснуться") { sleepCounter--;if (sleepCounter < 0) {makeThunderBig();to("2")} else makeThunder() }
        )

        //
        //  Акт 1. Сцена 2:
        //

        var phisic = false 
        var stick = false

        addQuest(
                QuestItem("2")
                        .text("Вы медленно просыпаетесь. Ваше тело очень болит из-за ночи проведенной в тяжелых доспехах.")
                        .addButton("Размяться") { to("2_1") }
                        .addButton("Осмотреться возле костра") { stick = true;to("2_2") },
                QuestItem("2_1")
                        .text("Вы сняли доспехи и немного размялись, умыли лицо снегом, почистили одежду. Вы чувствуете себя намного лучше.")
                        .addTextJump(effect_good("Подъем сил"))
                        .setOnStart { phisic = true }
                        .addButton("Так... Что-же дальше?") { phisic = true;to("3") },
                QuestItem("2_2")
                        .text("Вокруг вас лес. \nЛагерь разбит рядом с одиноким камнем, больше похожем на кусок скалы. \nВы замечаете множество следов идущих в разные стороны от костра.")
                        .addButton("Поискать что нибудь полезное") { to("2_3") },
                QuestItem("2_3")
                        .text("Вы осматриваете сугробы вокруг лагеря и находите длинную палку, как опытный игрок РПГ вы понимаете что она вам еще понадобиться и забираете её.")
                        .addTextJump(item("Подозрительно длинная палка"))
                        .addButton("Так... Что-же дальше?") { to("3") }
        )

        //
        //  Акт 1. Сцена 3:
        //

        var item_3_1_showed = false
        var item_3_2_showed = false
        var item_3_3_showed = false

        addQuest(
                QuestItem("3")
                        .text("Вы задумались... \nВы пытаетесь вспомнить что-же вы здесь делаете.")
                        .addButton("Осмотреть лагерь") { to("3_1") }
                        .addButton("Осмотреть свою сумку") { to("3_2") }
                        .addButton("Найти свой меч") { to("3_3") },
                QuestItem("3_1")
                        .setOnStart { item_3_1_showed = true }
                        .text("Следы, вещи, даже чьето оружие\nЗдесь точно был кто-то еще.\n")
                        .addButtonParams("Осмотреть свою сумку") { to("3_2") }.visible{!item_3_2_showed}.finish()
                        .addButtonParams("Найти свой меч") { to("3_3") }.visible{!item_3_3_showed}.finish()
                        .addButtonParams("Продолжить") { to("3_4") }.visible{item_3_1_showed && item_3_2_showed && item_3_3_showed}.finish(),
                QuestItem("3_2")
                        .setOnStart { item_3_2_showed = true }
                        .text("Вы нахоидите в снегу свою сумку.\n")
                        .addButton("Открыть") { to("3_2_1") },
                QuestItem("3_2_1")
                        .text("Вы открываете сумку.\nВ ней ничего нет, только небольшой лист бумаги на дне.")
                        .addButton("Взять его") { to("3_2_2") },
                QuestItem("3_2_2")
                        .text("Вы достаете лист со дна сумки.")
                        .addTextJump(item("Приглашение") )
                        .addButtonParams("Осмотреть лагерь") { to("3_1")}.visible{!item_3_1_showed}.finish()
                        .addButtonParams("Найти свой меч") { to("3_3") }.visible{!item_3_3_showed}.finish()
                        .addButtonParams("Продолжить") { to("3_4") }.visible{item_3_1_showed && item_3_2_showed && item_3_3_showed}.finish(),
                QuestItem("3_3")
                        .setOnStart { item_3_3_showed = true }
                        .text("Вы замечаете пустые ножны на ваших доспехах.\nКажется у вас бы меч, но где он сейчас?")
                        .addButton("Осмотреть место сна"){ to("3_3_1")},
                QuestItem("3_3_1")
                        .text("Вы тщательно проверяете место где проснулись, но похоже здесь ничего нет.")
                        .addButtonParams("Осмотреть свою сумку") { to("3_2") }.visible{!item_3_2_showed}.finish()
                        .addButtonParams("Осмотреть лагерь") { to("3_1")}.visible{!item_3_1_showed}.finish()
                        .addButtonParams("Продолжить") { to("3_4") }.visible{item_3_1_showed && item_3_2_showed && item_3_3_showed}.finish(),
                QuestItem("3_4")
                        .text("Похоже вы полностью все осмотерли.\nНужно прочитать найденную записку и наконец понять что-же здесь происходит.")
                        .addButton("Прочитать квест") { to("4") }
        )

        //
        //  Акт 1. Сцена 4:
        //

        var item_5_1_showed = false
        var item_5_2_showed = false

        var cold = false    //  TODO Должно повлиять в дальйнешем, активность с обморожением

        addQuest(
                QuestItem("4")
                        .text("Вы разворачиваете смятый лист бумаги найденный в сумке.")
                        .addButton("Читать"){to("4_1")},
                QuestItem("4_1")
                        .text("''Приглашаю всех рытцарей Campfire на праздник в честь начала нвого года. Первые прибывшие получат специальные подарки. Староста.''")
                        .addButton("..."){to("4_2")},
                QuestItem("4_2")
                        .text("Вы начинаете понимать что произошло.\nВы как и все направлялись в сторону дома старосты что-бы получить свой подарок и во время очередного привала на ночь вас просто не стали будить, беспокоясь что подарков не хватит на всех.")
                        .addButton("В путь!"){ to("5") },
                QuestItem("5")
                        .text("Вы отправились в сторону дома старосты.\nПосле нескольких часов пути, дорогу вам преградили огромные ворота из камня и тяжелых бревен.")
                        .addButton("Толкнуть дверь"){ to("5_1") }
                        .addButton("Попытаться обойти"){ to("5_2") },
                QuestItem("5_1")
                        .setOnStart { item_5_1_showed = true }
                        .text("Вы толкаете дверь, но она неподвижна, только немного снега осыпалось с крыши над стеной.")
                        .setOnStart { makeThunder() }
                        .addButtonParams("Попытаться обойти"){ to("5_2") }.visible{!item_5_2_showed}.finish()
                        .addButton("Изучить ворота"){ to("5_3") },
                QuestItem("5_2")
                        .setOnStart { item_5_2_showed = true }
                        .text("Вы сходте с тропы и пытаетесь обойти ворота. Вы чувствуете как с каждым шагом все глубже уходите в снег.")
                        .addButton("Продолжить идти"){ to("5_2_3") }
                        .addButton("Вернуться назад"){ to("5_2_2") },
                QuestItem("5_2_2")
                        .text("Почти полностью уйдя под снег, вы решаете что лучше вернуться назад, иначе мы можете навсегда остаться под этими сугробами.")
                        .addButtonParams("Толкнуть дверь"){ to("5_1") }.visible{!item_5_1_showed}.finish()
                        .addButton("Изучить ворота"){ to("5_3") },
                QuestItem("5_2_3")
                        .text("Вы настолько глубоо проваливаетесь в снег, что он начинает заполнять ваши доспехи, вам становится тяжело дышать. Вы понимаете что это ваши последнии мгновения.")
                        .addButton("Вернуться назад"){ to("5_2_4") },
                QuestItem("5_2_4")
                        .text("Наглотавшись снега вы с огромным трудом выбираетесь обратно на тропу.")
                        .addTextJump(effect_bad("Обморожение"))
                        .setOnStart { cold = true }
                        .addButtonParams("Толкнуть дверь"){ to("5_1") }.visible{!item_5_1_showed}.finish()
                        .addButton("Изучить ворота"){ to("5_3") },
                QuestItem("5_3")
                        .text("Похоже это одни из тех ворот, которые можно открыть только с помощью спрятанного механизма.")
                        .addButton("Поискать кнопку"){ to("5_3_1") },
                QuestItem("5_3_1")
                        .text("Несколько минут поискав вокруг ворот, вы замечаете странный рычаг, который выглядывает из верхней части ворот.")
                        .addButton("Нажать на рычаг"){ to("5_3_2") },
                QuestItem("5_3_2")
                        .text("Вы хотите использовать этот рычаг, но он слишком высоко. Вот если бы у вас была какая-нибудь палка или вы были бы в прекрасной физической форме чтобы допрыгнуть до него...")
                        .addButtonParams("Приминить палку"){ to("5_3_3") }.enabled{stick}.finish()
                        .addButtonParams("Попытаться допрыгнуть"){ to("5_3_3") }.enabled{phisic}.finish(),
                QuestItem("5_3_3")
                        .text("Кажется у вас получилось, ворота с грохотом открываются.")
                        .setOnStart { makeThunderBig() }
                        .addButton("Продолжить путешествие"){ to("6") }
        )

        //
        //  Акт 2. Сцена 1:
        //

        var item_6_1_1_showed = false

        addQuest(
                QuestItem("6")
                        .text("Пройдя через ворота вы обнаружили небольшой алтарь с идолом протобожества.")
                        .addButton("Потребовать от идола о помощи"){to("6_1") }
                        .addButton("Умолять идола о помощи"){},
                QuestItem("6_1")
                        .text("Идол никак на вас не реагирует. Похоже вы ему не интересны.")
                        .addButton("Ударить идола"){to("6_1_1")}
                        .addButton("Столкнть идола с алтаря"){to("6_1_1")},
                QuestItem("6_1_1")
                        .setOnStart { item_6_1_1_showed = true }
                        .text("Как толкьо вы прикоснулись к идолу, раздался оглушительный грохот. Вы сразу отдернули руку.\nНебо затянуло тучами, начилась гроза, вы пожалели о своих действиях")
                        .setOnStart { makeThunderBig(); startThunder() }
                        .addButton("Умолять идола о прощении"){to("6_1_2")},
                QuestItem("6_1_2")
                        .text("Псле вашей мольбы о прощении граза начала утихать. В ваше голове раздался голос \"Готов ли ты принять наказание?\"")
                        .addButton("Согласиться"){to("6_1_3")},
                QuestItem("6_1_3")
                        .setOnStart { stopThunder() }
                        .text("Гроза стихла. Вы почуствовали слабость. Похоже вам не стоило проявлять агресию к изначальным богам.")
                        .addTextJump(effect_bad("Проклятие хейтера"))
                        .addButton("Раскаяться"){to("6_2")},
                QuestItem("6_2")
                        .text("Вы умоляете идола помочь вам в вашем путешествии. Кажется он не реагирует")
                        .addButtonParams("Ударить идола"){to("6_1_1")}.visible{!item_6_1_1_showed}.finish()
                        .addButton("Умолять сильнее"){to("6_3")},
                QuestItem("6_3")
                        .text("Идол пошатнулся. Похоже он готов помочь вам. В вашей голове раздается голос \"Проси, что тебе нужно?\".\nВы вспоминаете, что потеряли свой меч и решаете просить у идола оружие.")
                        .addButton("Попросить оружие"){to("6_4")},
                QuestItem("6_4")
                        .text("Вы чувствуете на себе взгляд, который буквально пронзает вас насквозь. Вы понимаете что идол смотрит на вашу карму, чтобы решить каго оружия вы достойны.")
                        .addButtonParams("Увесистая палка (50)"){to("6_4_1")}.enabled { ControllerApi.account.karma30 >= 50 }.finish()
                        .addButtonParams("Двуручный меч (180)"){to("6_4_2")}.enabled { ControllerApi.account.karma30 >= 180 }.finish()
                        .addButtonParams("Посох тайн (355)"){to("6_4_4")}.enabled { ControllerApi.account.karma30 >= 355 }.finish(),
                QuestItem("6_4_1")
                        .text("Идол не увидел в вас виликих подвигов и дал вам самое простое уружие.")
                        .addTextJump(item("Увесистая палка"))
                        .addButton("Принять дар"){to("6_5")},
                QuestItem("6_4_2")
                        .text("Похоже идол не сильно впечатлился вашими подвигами и дал вам стальной меч. Вы довольны. Нет ничего надёжнее стали!")
                        .addTextJump(item("Двуручный меч"))
                        .addButton("Принять дар"){to("6_5")},
                QuestItem("6_4_4")
                        .text("Вы очень впечатлили идола. Он дал вам редчайшее оружие. С таким оружием вам не страшна любая схватка.")
                        .addTextJump(item("Посох тайн"))
                        .addButton("Но я не знаю ни одного заклинания!"){to("6_4_4_1")}
                        .addButton("Принять дар"){to("6_5")},
                QuestItem("6_4_4_1")
                        .text("Идол никак не отреагировал на ваше замечание. Вы решаете что посох достаточно тяжелый и его можно спользовать как дубину.")
                        .addButton("Принять дар"){to("6_5")},
                QuestItem("6_5")
                        .text("Тперь у вас есть оружие, вы полны решимости продолжать своё путешествие к дому старосты.")
                        .addButton("Продолжить"){to("7")}
        )


        //
        //  Акт 2. Сцена 2:
        //

        addQuest(
                QuestItem("7")
                        .text("Мороз. Вы продолжаете свое путешествее. Местные тропы уже вам знокомы, вы понмиаете что до дома старосты осталось совсем немного.")
                        .addButton("Продолжать идти"){to("7_1") },
                QuestItem("7_1")
                        .text("Вас окутали воспоминания. Вы вспомнили старосту, очень уважаемого в местных лесх человека. С легкой дружью вспомнили его огромного пса, сидящего на гиганской цепи перед домом...")
                        .addButton("Продолжать идти"){to("7_2") },
                QuestItem("7_2")
                        .text("«Цыц, бешеный!» – грозно кричала на него Диана, местная лясничая, которую часто можно было встретить дома у старосты.")
                        .addButton("Продолжать идти"){to("7_3") },
                QuestItem("7_3")
                        .text("Обычно вокруг дома старосты всягда было шумно. Но не в этот раз. Сейчас, даже совсем не далеко от его дома, стояла подная тишина. Казалось что даже ветер стал намного тише.")
                        .addButton("Направитсья к дому"){to("7_4") },
                QuestItem("7_4")
                        .text("Наконец вы на месте. Дом старосты выглядит очень мрачно. Калитка распахнута, двор пуст, тишина... странная и недобрая")
                        .addButton("Осмотреться"){to("7_4_1") }
                        .addButton("Обнажить оружие"){to("7_4_2") }
                        .addButton("Попробовать обойти дом"){to("7_4_3") },
                QuestItem("7_4_1")
                        .text("Ощущение беды всё нарастало. Дверь в дом была раскрыта настежь, и дверца курятника была сорвана и валялась в стороне... Одно из окон в доме разбито, будка собаки перевернута, по всему двору разбросаны рыжие перья, а истоптанный снег забрызган красными пятнами")
                        .addButton("Обнажить оружие"){to("7_4_2") }
                        .addButton("Попробовать обойти дом"){to("7_4_3") },
                QuestItem("7_4_2")
                        .text("Вы приготовили свое оружие и собрались с духом.")
                        .addButton("Войти в дом"){to("7_4_2_1") },
                QuestItem("7_4_3")
                        .text("Вы бесшумно проникаете во двор. Убедившись, что вокруг никого нет вы подходите к дому.")
                        .addButton("Войти в дом"){to("7_4_3_1") },
                QuestItem("7_4_2_1")
                        .text("Вы резко врываетесь в дом. Внутри распахнуты все двери, в разбитое окно тянет морозом, но воздух внутри всё ещё тёплый. Кругом на полу снег вперемешку с грязью.")
                        .addButton("Крикнуть \"Кто здесь?\""){to("7_5_1") }
                        .addButton("Пойти по грязному следу"){to("7_5_2") },
                QuestItem("7_4_3_1")
                        .text("Вы тихо заходите в дом. Внутри распахнуты все двери, в разбитое окно тянет морозом, но воздух внутри всё ещё тёплый. Кругом на полу снег вперемешку с грязью.")
                        .addButton("Крикнуть \"Кто здесь?\""){to("7_5_1") }
                        .addButton("Пойти по грязному следу"){to("7_5_2") },
                QuestItem("7_5_1")
                        .setOnStart { makeThunderBig() }
                        .text("Поднеся ложони ко рту, вы громку кричите \"Ктоооо здеееесь?\". В это же мгновение вы слыщене оглушительный скрежет в дальней комнате.")
                        .addButton("Схватится за оружие!"){to("8")},
                QuestItem("7_5_2")
                        .setOnStart { makeThunderBig() }
                        .text("Снег явно занесло не ветром, вы не можете определить чьи это следы, из-за того, что снег быстро превратился в грязь. Пройдя через комнату, вы отчётливо слышите скрежет когтей в холе…")
                        .addButton("Схватится за оружие!"){to("8")}
        )

        //
        //  Акт 2. Сцена 3:
        //

        addQuest(
                QuestItem("8")
                        .setOnStart { startThunder() }
                        .text("Огромный волк. В его глазах видно безумее, весь дом содрагается при каждом его шаге. Вы понимаете что еще мгновение и он порвет вас на части.")
                        .addButton("Защищаться"){}
                        .addButton("Нанести удар"){}
        )
    }

    var weather_thunder = false
    val animation = DrawAnimationSnow()

    init {
        createQuest()
        ControllerScreenAnimations.addAnimationWithClear(animation)
        to("0")
    }


    //
    //  Efects
    //

    fun effect_good(text: String) = "{grey [ получен эффект: }{green _${text}_}{grey  ]}"

    fun effect_bad(text: String) = "{grey [ получен эффект: }{red _${text}_}{grey  ]}"

    fun item(text: String) = "{grey [ получен предмет: }{orange _${text}_}{grey  ]}"

    fun startThunder() {
        weather_thunder = true
        stepThunder()
    }

    private fun stepThunder() {
        if (!weather_thunder) return
        ToolsThreads.thread(ToolsMath.randomLong(2000, 5000)) {
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
        ToolsVibration.vibrate(600)
    }

    fun stopThunder() {
        weather_thunder = false
    }

    fun makeDarknes(onFinish: () -> Unit = {}) {
        ToolsView.toAlpha(viewScreen, 3000) { ToolsView.fromAlpha(viewScreen, 4000); onFinish.invoke() }
    }

}