package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewProgressLine

class CardSupportTotal(
        val count : Long,
        val need: Long
) : Card(R.layout.screen_support_card_total){


    override fun bindView(view: View) {
        super.bindView(view)

        val vText:TextView = view.findViewById(R.id.vText)
        val vCounter:TextView = view.findViewById(R.id.vCounter)
        val vLine: ViewProgressLine = view.findViewById(R.id.vLine)

        vLine.setProgress(count, need)
        vCounter.setText("${count} / ${need}")
    }

}