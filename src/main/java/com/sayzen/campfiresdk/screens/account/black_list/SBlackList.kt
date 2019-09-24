package com.sayzen.campfiresdk.screens.account.black_list

import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.settings.Settings

class SBlackList(
        val accountId:Long,
        val accountName: String
) : Screen(R.layout.screen_black_list) {

    private val vBlackListUsers: Settings = findViewById(R.id.vBlackListUsers)
    private val vBlackListFandoms: Settings = findViewById(R.id.vBlackListFandoms)

    init {
        vBlackListUsers.setOnClickListener { Navigator.to(SBlackListUsers(accountId, accountName)) }
        vBlackListFandoms.setOnClickListener { SBlackListFandoms.instance(accountId, accountName, Navigator.TO) }
    }


}
