package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.annotation.StringRes
import com.sayzen.campfiresdk.R

import com.sayzen.campfiresdk.app.CampfreConstants
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardSpoiler
import com.sup.dev.java.tools.ToolsText

class CardSpoilerAchi : CardSpoiler() {

    private var finCount = 0
    private var karmaCount = 0.0

    fun addAchi(card: CardAchievement): CardSpoilerAchi {
        if (card.lvl == card.achievement.maxLvl) finCount++
        karmaCount += (CampfreConstants.getAchievement(card.achievement)).info.getForce() * card.lvl
        return super.add(card) as CardSpoilerAchi
    }

    override fun setTitle(@StringRes title: Int): CardSpoilerAchi {
        return super.setTitle(title) as CardSpoilerAchi
    }

    fun updateAchi() {
        if (karmaCount > 0)
            setRightText(finCount.toString() + " / " + cards.size + " (${ToolsText.numToStringRoundAndTrim(karmaCount, 2)})")
        else
            setRightText(finCount.toString() + " / " + cards.size)
        setRightTextColor(if (finCount == cards.size) ToolsResources.getColor(R.color.green_700) else 0)
        setExpanded(finCount != cards.size)
    }

}
