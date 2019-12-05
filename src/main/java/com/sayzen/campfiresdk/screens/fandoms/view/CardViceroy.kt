package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsViceroyGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardViceroy(
        val fandomId: Long,
        val languageId: Long
) : Card(R.layout.screen_fandom_card_viceroy) {

    var date = 0L
    var loading = false
    var startLoading = false
    var error = false
    var xAccount: XAccount? = null


    override fun bindView(view: View) {
        super.bindView(view)

        if (!startLoading) {
            startLoading = true
            load()
        }

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vRetry: TextView = view.findViewById(R.id.vRetry)
        val vLoading: TextView = view.findViewById(R.id.vLoading)

        vMenu.setOnClickListener { }
        vRetry.setOnClickListener {
            load()
            update()
        }

        vRetry.visibility = if (error) View.VISIBLE else View.GONE
        vLoading.visibility = if (loading) View.VISIBLE else View.GONE
        vMenu.visibility = if (ControllerApi.can(API.LVL_ADMIN_FANDOM_VICEROY) && !error && !loading) View.VISIBLE else View.GONE
        vAvatar.visibility = if (!error && !loading) View.VISIBLE else View.GONE

        if (xAccount != null) xAccount!!.setView(vAvatar)
    }

    private fun load(tryCount: Int = 5) {
        error = false
        loading = true
        if (tryCount <= 0) {
            error = true
            loading = false
            update()
            return
        }
        RFandomsViceroyGet(fandomId, languageId)
                .onComplete {
                    date = it.date
                    if (it.account != null) xAccount = XAccount(it.account!!) { update() }
                    loading = false
                    update()
                }
                .onError {
                    load(tryCount - 1)
                }
                .send(api)
    }


}
