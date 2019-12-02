package com.sayzen.campfiresdk.screens.account.karma

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.KarmaInFandom
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardKarma(
        val karma: KarmaInFandom
) : CardAvatar(R.layout.screen_account_karma_card), NotifyItem {

    private val xFandom = XFandom(karma.fandomId, karma.fandomLanguageId, karma.fandomName, karma.fandomImageId){update()}

    init {
        setOnClick {
            SFandom.instance(karma.fandomId, karma.fandomLanguageId, Navigator.TO)
        }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: TextView = view.findViewById(R.id.vRate)

        vRate.text = (karma.karmaCount / 100).toString()
        vRate.setTextColor(ToolsResources.getColor(if (karma.karmaCount > 0L) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        xFandom.setView(vAvatar)
        ControllerLinks.makeLinkable(vAvatar.vTitle)
        ToolsImagesLoader.load(karma.fandomImageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(karma.fandomImageId).intoCash()
    }
}