package com.sayzen.campfiresdk.screens.account.search


import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsMapper

class SAccountSearch(
        private val onSelected: (Account)->Unit
) : SLoadingRecycler<CardAccount, Account>(R.layout.screen_account_search) {

    private val vField: SettingsField = findViewById(R.id.vField)
    private val vSearch: ViewIcon = findViewById(R.id.vSearch)

    private var first = true

    init {
        setTitle(R.string.app_search)
        setTextEmpty(R.string.app_nothing_found)
        setBackgroundImage(R.drawable.bg_4)


        ToolsView.onFieldEnterKey(vField.vField) { reload() }
        vSearch.setOnClickListener { reload() }

        if (first) {
            first = false
            ToolsView.showKeyboard(vField.vField)
        }

    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { ac ->
            CardAccount(ac).setOnClick {
                Navigator.back()
                onSelected.invoke(ac)
            }
        }
                .setBottomLoader { onLoad, cards ->
                    RAccountsGetAll()
                            .setUsername(vField.getText())
                            .setOffset(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(removeMyAccount(r.accounts)) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun removeMyAccount(accounts: Array<Account>): Array<Account> {
        for (a in accounts)
            if (a.id == ControllerApi.account.id) {
                val newAccounts = arrayOfNulls<Account>(accounts.size - 1)
                var x = 0
                for (i in accounts.indices) {
                    if (accounts[i].id == ControllerApi.account.id) {
                        x = 1
                        continue
                    }
                    newAccounts[i - x] = accounts[i]
                }
                return ToolsMapper.asNonNull(newAccounts)
            }
        return accounts
    }
}
