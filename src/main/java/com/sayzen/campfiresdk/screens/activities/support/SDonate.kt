package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.notifications.project.NotificationDonate
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesCreateDraft
import com.dzen.campfire.api.requests.project.RProjectDonatesGetAll
import com.dzen.campfire.api.requests.project.RProjectSupportAdd
import com.dzen.campfire.api.requests.project.RProjectSupportGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerAppodeal
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.activities.EventVideoAdView
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.http_api.HttpRequest
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class SDonate private constructor(
        var r: RProjectSupportGetInfo.Response
) : Screen(R.layout.screen_donate) {

    companion object {

        fun instance(action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RProjectSupportGetInfo()) { r ->
                SDonate(r)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventVideoAdView::class) { reload() }
            .subscribe(EventNotification::class) { if (it.notification is NotificationDonate) reload() }

    private val vSwipe: SwipeRefreshLayout = findViewById(R.id.vSwipe)
    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vShadow: View = findViewById(R.id.vShadow)
    private val vButton: Button = findViewById(R.id.vButton)
    private val vSum: EditText = findViewById(R.id.vSum)
    private val vComment: EditText = findViewById(R.id.vComment)
    private val vIcon_yandex: ViewIcon = findViewById(R.id.vIcon_yandex)
    private val vIcon_card: ViewIcon = findViewById(R.id.vIcon_card)
    private val vIcon_phone: ViewIcon = findViewById(R.id.vIcon_phone)
    private val vFab: View = findViewById(R.id.vFab)
    private val vDonateContainer: View = findViewById(R.id.vDonateContainer)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vMobileAlert: ViewTextLinkable = findViewById(R.id.vMobileAlert)
    private val adapter = RecyclerCardAdapterLoading<CardDonate, Donate>(CardDonate::class){CardDonate(it)}

    init {
        isNavigationShadowAvailable = false

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        ControllerAppodeal.cashVideoReward()

        vSwipe.setOnRefreshListener {
            reload()
        }

        vFab.visibility = if (ControllerApi.account.id == 1L) View.VISIBLE else View.GONE
        vFab.setOnClickListener { addDonate() }


        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_DONATE.asWeb())
            ToolsToast.show(R.string.app_copied)
        }

        vButton.setOnClickListener {
            donate()
        }
        vSum.addTextChangedListener(TextWatcherChanged {
            updateEnabled()
        })
        vComment.addTextChangedListener(TextWatcherChanged {
            updateEnabled()
        })
        vSum.setText("10")
        vIcon_yandex.setOnClickListener { setSelected(vIcon_yandex) }
        vIcon_card.setOnClickListener { setSelected(vIcon_card) }
        vIcon_phone.setOnClickListener { setSelected(vIcon_phone) }

        SActivityTypeBottomNavigation.setShadow(vShadow)
        setSelected(vIcon_card)
        updateEnabled()

        ControllerLinks.makeLinkable(vMobileAlert)
        adapter.add(CardSupportTotal(r.totalCount, 1000))
        adapter.setBottomLoader{ onloaded, cards->
            RProjectDonatesGetAll(cards.size.toLong())
                    .onComplete { onloaded.invoke(it.donates) }
                    .onError { onloaded.invoke(null) }
                    .send(api)
        }
        adapter.loadBottom()

        vMessageContainer.visibility = View.GONE

        ImageLoader.load(API_RESOURCES.ICON_YANDEX_DENGI).into(vIcon_yandex)
        ImageLoader.load(API_RESOURCES.ICON_BANK_CARD).into(vIcon_card)
        ImageLoader.load(API_RESOURCES.ICON_PHONE).into(vIcon_phone)
    }

    private fun setSelected(v: ViewIcon) {
        vIcon_yandex.isIconSelected = v == vIcon_yandex
        vIcon_card.isIconSelected = v == vIcon_card
        vIcon_phone.isIconSelected = v == vIcon_phone

        vMobileAlert.visibility = if(vIcon_phone.isIconSelected) View.VISIBLE else View.GONE
    }

    private fun updateEnabled() {
        vButton.isEnabled = ToolsMapper.isIntCastable(vSum.text.toString()) && vComment.text.length <= API.DONATE_COMMENT_MAX_L
    }

    private fun addDonate() {
        Navigator.to(SAccountSearch(true, true) {
            val accountId = it.id
            WidgetField()
                    .setHint("Сумма")
                    .setOnCancel(R.string.cancel)
                    .setInputType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED)
                    .setOnEnter(R.string.app_add) { w, sum ->
                        ApiRequestsSupporter.executeEnabled(w, RProjectSupportAdd(accountId, (sum.toDouble()*100).toLong())) {
                            ToolsToast.show(R.string.app_done)
                            w.hide()
                            reload()
                        }
                    }
                    .asSheetShow()
        })
    }

    private fun donate() {
        ApiRequestsSupporter.executeProgressDialog(RProjectDonatesCreateDraft(vComment.text.toString())){ r->
            donateNow(r.donateId)
        }
    }

    private fun donateNow(donateId:Long) {

        val sum = vSum.text.toString().toInt()
        val type = if (vIcon_yandex.isIconSelected) "PC" else if (vIcon_card.isIconSelected) "AC" else "MC"

        val url = HttpRequest()
                .setUrl("https://money.yandex.ru/quickpay/confirm.xml")
                .param("receiver", "410011747883287")
                .param("quickpay-form", "donate")
                .param("paymentType", type)
                .param("sum", "$sum")
                .param("label", "${ControllerApi.account.id}-${donateId}-${sum}")
                .param("comment", ToolsResources.s(R.string.activities_support_comment_user, ControllerApi.account.name))
                .param("targets", ToolsResources.s(R.string.activities_support_comment))
                .param("formcomment", ToolsResources.s(R.string.activities_support_comment))
                .param("short-dest", ToolsResources.s(R.string.activities_support_comment))
                .makeUrl()

        ToolsIntent.startWeb(url) {
            ToolsToast.show(R.string.error_app_not_found)
        }
        ToolsThreads.main(2000) {
            vDonateContainer.visibility = View.GONE
            vMessageContainer.visibility = View.VISIBLE
        }
    }

    private fun reload() {
        RProjectSupportGetInfo()
                .onComplete {
                    r = it
                    adapter.reloadBottom()
                }.onFinish {
                    vSwipe.isRefreshing = false
                }
                .send(api)
    }

}