package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectSupportAdd
import com.dzen.campfire.api.requests.project.RProjectSupportGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.activities.EventVideoAdView
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus

class SDonate private constructor(
        var r: RProjectSupportGetInfo.Response
) : Screen(R.layout.screen_support) {

    companion object {

        fun instance(action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RProjectSupportGetInfo()) { r ->
                SDonate(r)
            }
        }

    }

    private val eventBus = EventBus.subscribe(EventVideoAdView::class){ reload() }

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vShadow: View = findViewById(R.id.vShadow)
    private val vButton: Button = findViewById(R.id.vButton)
    private val adapter = RecyclerCardAdapter()

    init {
        isNavigationShadowAvailable = false

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        reset()
        ControllerAppodeal.cashVideoReward()


        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_DONATE.asWeb())
            ToolsToast.show(R.string.app_copied)
        }

        if(ControllerApi.account.id == 1L){
            vButton.setOnClickListener {addDonate() }
            vButton.setText("Добавить")
        }else{
            vButton.setOnClickListener {
                ToolsIntent.startWeb("https://money.yandex.ru/to/410011747883287"){
                    ToolsToast.show(R.string.error_app_not_found)
                }
            }
        }

        SActivityTypeBottomNavigation.setShadow(vShadow)
    }

    private fun reset(){
        adapter.clear()
        adapter.add(CardSupportTotal(r.totalCount, 1000))
        for(i in r.accounts.indices) adapter.add(CardSupportUser(r.accounts[i], r.values[i]))
    }

    private fun addDonate(){
        Navigator.to(SAccountSearch(true, true) {
            val accountId = it.id
            WidgetField()
                    .setHint("Сумма")
                    .setOnCancel(R.string.cancel)
                    .setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL)
                    .setOnEnter("Добавить"){w,sum->
                        ApiRequestsSupporter.executeEnabled(w,RProjectSupportAdd(accountId, sum.toLong())){
                            ToolsToast.show(R.string.app_done)
                            w.hide()
                            reload()
                        }
                    }
                    .asSheetShow()
        })
    }

    private fun reload(){
        RProjectSupportGetInfo()
                .onComplete{
                    r = it
                    reset()
                }
                .send(api)
    }

}