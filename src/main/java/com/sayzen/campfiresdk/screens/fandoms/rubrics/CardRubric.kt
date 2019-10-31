package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.Rubric
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsText

class CardRubric(val rubric: Rubric) : Card(R.layout.card_rubric){

    val xAccount = XAccount(rubric.ownerId, rubric.ownerName, rubric.ownerImageId, rubric.ownerLevel,rubric.ownerKarma30, rubric.ownerLastOnlineTime){
        update()
    }
    var onClick:((Rubric)->Unit)? = null

    override fun bindView(view: View) {

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRate: TextView = view.findViewById(R.id.vRate)

        xAccount.setView(vAvatar)
        vAvatar.setTitle(rubric.name)
        vAvatar.setSubtitle(rubric.ownerName)
        vAvatar.vAvatar.isClickable = onClick == null
        vAvatar.isClickable = false

        vRate.text  = ToolsText.numToStringRound(rubric.karmaCof / 100.0, 2)

        view.setOnClickListener {
            if(onClick == null){

            }else {
                onClick?.invoke(rubric)
            }
        }
    }

}