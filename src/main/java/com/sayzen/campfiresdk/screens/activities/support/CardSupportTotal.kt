package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.java.tools.ToolsText

class CardSupportTotal(
        val sum : Long,
        val need: Long
) : Card(R.layout.screen_support_card_total){


    override fun bindView(view: View) {
        super.bindView(view)

        val vText:TextView = view.findViewById(R.id.vText)
        val vCounter:TextView = view.findViewById(R.id.vCounter)
        val vLine: ViewProgressLine = view.findViewById(R.id.vLine)

        val countX = sum/100.0
        vLine.setProgress(countX.toLong(), need)
        vCounter.setText("${ToolsText.numToStringRoundAndTrim(countX, 2)} / ${need} \u20BD")
    }

}