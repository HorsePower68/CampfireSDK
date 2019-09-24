package com.sayzen.campfiresdk.screens.rates

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.Rate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate

class CardRateText(
        val rate: Rate
) : CardAvatar(R.layout.card_rate_text), NotifyItem {

    private val xAccount = XAccount(rate.accountId, rate.accountName, rate.accountImageId, rate.accountLvl, rate.accountKarma30, rate.accountLastOnlineTime){
        update()
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: TextView = view.findViewById(R.id.vRate)

        vRate.text = (rate.karmaCount / 100).toString()
        vRate.setTextColor(ToolsResources.getColor(if (rate.karmaCount > 0L) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        xAccount.setView(vAvatar)
        vAvatar.setSubtitle(ToolsDate.dateToString(rate.date))
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(rate.fandomImageId).intoCash()
    }
}
