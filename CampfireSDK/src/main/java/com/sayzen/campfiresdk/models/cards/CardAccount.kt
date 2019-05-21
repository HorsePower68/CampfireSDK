package com.sayzen.campfiresdk.models.cards

import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle

open class CardAccount(
        account: Account
) : CardAvatar(), NotifyItem {

    val xAccount = XAccount(account, 0L, 0L, 0L){
        update()
    }

    init {
        setTitle(account.name)
        setOnClick { ControllerCampfireSDK.onToAccountClicked(account.id, Navigator.TO) }
        setDividerVisible(true)
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        xAccount.setView(vAvatar)
        vAvatar.setOnClickListener(null)
        vAvatar.vAvatar.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(xAccount.accountId, Navigator.TO) }
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(xAccount.imageId).intoCash()
    }

    override fun setOnClick(onClick: () -> Unit): CardAccount {
        return super.setOnClick(onClick) as CardAccount
    }

}
