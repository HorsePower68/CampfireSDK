package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.debug.log

class SUserActivitiesList constructor(
        private val onSelected: ((UserActivity) -> Unit)? = null
) : SLoadingRecycler<CardUserActivity, UserActivity>(R.layout.screen_activities_user_activities) {

    private var subscribedLoaded = false
    private var lockOnEmpty = false

    init {
        setTitle(R.string.app_relay_races)
        setTextEmpty(R.string.fandoms_empty)
        setTextProgress(R.string.fandoms_loading)
        setBackgroundImage(R.drawable.bg_7)

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        (vFab as View).visibility = if (ControllerApi.account.lvl >= API.LVL_MODERATOR_RELAY_RACE.lvl) View.VISIBLE else View.GONE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            Navigator.to(SRelayRaceCreate())
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUserActivity, UserActivity> {
        val adapterX = RecyclerCardAdapterLoading<CardUserActivity, UserActivity>(CardUserActivity::class) { CardUserActivity(it) }
                .setBottomLoader { onLoad, cards -> load(onLoad, cards) }

        return adapterX
    }

    override fun reload() {
        adapter!!.remove(CardDividerTitle::class)
        lockOnEmpty = true
        subscribedLoaded = false
        super.reload()
    }

    private fun load(onLoad: (Array<UserActivity>?) -> Unit, cards: ArrayList<CardUserActivity>) {
        lockOnEmpty = false
        if (!subscribedLoaded) {

            if (cards.size > 0) {
                subscribedLoaded = true
                onLoad.invoke(emptyArray())
                adapter!!.loadBottom()
                return
            }

            val e_1 = UserActivity()
            e_1.name = "Как я нашел приложение"
            e_1.description = "Проявилось на Prestigio 3 раза. Превью появляется после полного перезапуска приложения"
            e_1.imageId = 1
            e_1.fandomId = 10
            e_1.languageId = 2
            e_1.backgroundId = 564
            e_1.fandomImageId = 2
            e_1.fandomName = "Campfire"

            onLoad.invoke(arrayOf(e_1))

            adapter!!.add(CardDividerTitle(R.string.fandoms_global))
        } else {

            log(" >> [${cards.size}]")
            if (cards.size > 2) {
                onLoad.invoke(emptyArray())
                return
            }

            val e_1 = UserActivity()
            e_1.name = "Сколько у меня постов"
            e_1.description = "Ссылка Политика конфиденциальности не реагирует на нажатия\n" +
                    "\n" +
                    "Проверка осуществляется на эмуляторе"
            e_1.imageId = 1
            e_1.fandomId = 10
            e_1.languageId = 2
            e_1.backgroundId = 564
            e_1.fandomImageId = 2
            e_1.fandomName = "Campfire"

            val e_2 = UserActivity()
            e_2.name = "Куда жать?"
            e_2.description = "Добавить WiFi домофон DK103 28.154.\n" +
                    "Зайти в поиск, найти это устройство\n" +
                    "\n" +
                    "Результат: у устройство сообщение \"Параметры авторизации отличаются от заданных по умолчанию\", нет скриншота. Предполагается, что скриншот должен быть"
            e_2.imageId = 1
            e_2.fandomId = 10
            e_2.languageId = 2
            e_2.backgroundId = 564
            e_2.fandomImageId = 2
            e_2.fandomName = "Campfire"

            onLoad.invoke(arrayOf(e_1, e_2))

        }
    }


}
