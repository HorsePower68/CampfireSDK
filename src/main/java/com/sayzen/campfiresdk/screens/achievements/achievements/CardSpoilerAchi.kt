package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.annotation.StringRes
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R

import com.sayzen.campfiresdk.app.CampfreConstants
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.cards.CardSpoiler
import com.sup.dev.java.tools.ToolsText

class CardSpoilerAchi(
        val pageAchievements: PageAchievements,
        val packIndex: Int
) : CardSpoiler() {

    private var finCount = 0
    val cardLoading = CardLoading()
    var isFirstExpand = true
    var karmaCount = 0.0

    init {
        super.add(cardLoading)

        val pack = if (packIndex == 1) API.ACHI_PACK_1
        else if (packIndex == 2) API.ACHI_PACK_2
        else if (packIndex == 3) API.ACHI_PACK_3
        else if (packIndex == 4) API.ACHI_PACK_4
        else API.ACHI_PACK_5


        for (i in pack) karmaCount += (CampfreConstants.getAchievement(i.index)).info.getForce() * pageAchievements.achiLvl(i.index)

        if (karmaCount > 0)
            setRightText(finCount.toString() + " / " + pack.size + " (${ToolsText.numToStringRoundAndTrim(karmaCount, 2)})")
        else
            setRightText(finCount.toString() + " / " + pack.size)
        setRightTextColor(if (finCount == pack.size) ToolsResources.getColor(R.color.green_700) else 0)
    }

    override fun onExpandedClicked(expanded: Boolean) {
        if (!expanded) return
        if (!isFirstExpand) return
        isFirstExpand = false
        pageAchievements.loadPack(packIndex, this)
    }

    fun addAchi(card: CardAchievement): CardSpoilerAchi {
        if (card.lvl == card.achievement.maxLvl) finCount++
        return super.add(card) as CardSpoilerAchi
    }

    override fun setTitle(@StringRes title: Int): CardSpoilerAchi {
        return super.setTitle(title) as CardSpoilerAchi
    }

    fun onLoaded() {
        remove(cardLoading)

        if (packIndex == 1) {
            for (i in API.ACHI_PACK_1) addAchi(CardAchievement(pageAchievements, i))
        }
        if (packIndex == 2) {
            for (i in API.ACHI_PACK_2) addAchi(CardAchievement(pageAchievements, i))
        }
        if (packIndex == 3) {
            for (i in API.ACHI_PACK_3) addAchi(CardAchievement(pageAchievements, i))
        }
        if (packIndex == 4) {
            for (i in API.ACHI_PACK_4) addAchi(CardAchievement(pageAchievements, i))
        }
        if (packIndex == 5) {
            for (i in API.ACHI_PACK_5) addAchi(CardAchievement(pageAchievements, i))
        }

    }

}
