package com.sayzen.campfiresdk.views

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.stickers.RStickersGetAllByAccount
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPacksSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.Dialog
import com.sup.dev.android.views.splash.Sheet
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.WidgetRecycler

open class WidgetStickers : WidgetRecycler(R.layout.widget_stickers) {

    private val myAdapter = RecyclerCardAdapter()
    private val vEmptyContainer: View = findViewById(R.id.vEmptyContainer)
    private val vButton: Button = findViewById(R.id.vButton)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vProgress: View = findViewById(R.id.vProgress)

    private var onSelected: (PublicationSticker) -> Unit = { }
    private var spanCount = 4

    init {
        vEmptyContainer.visibility = View.GONE


        spanCount = if (ToolsAndroid.isScreenPortrait()) 4 else 8
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)

        setAdapter<WidgetRecycler>(myAdapter)


    }

    private fun load(){
        myAdapter.clear()
        vProgress.visibility = View.VISIBLE
        vEmptyContainer.visibility = View.GONE
        RStickersGetAllByAccount(ControllerApi.account.id)
                .onComplete { r->
                    for(i in r.stickers) {
                        val card = CardSticker(i)
                        card.onClick = {
                            onSelected.invoke(it)
                            onSelected = {}
                            hide()
                        }
                        myAdapter.add(card)
                    }
                    vProgress.visibility = View.GONE
                    if(r.stickers.isEmpty()){
                        vEmptyContainer.visibility = View.VISIBLE
                        vMessage.setText(R.string.stickers_empty)
                        vButton.setText(R.string.app_search)
                        vButton.setOnClickListener { Navigator.to(SStickersPacksSearch()) }
                    }
                }
                .onError {
                    vProgress.visibility = View.GONE
                    vEmptyContainer.visibility = View.VISIBLE
                    vMessage.setText(R.string.error_network)
                    vButton.setText(R.string.app_retry)
                    vButton.setOnClickListener { load() }
                }
                .send(api)

    }

    override fun onShow() {
        super.onShow()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is Dialog)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is Sheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()

        load()
    }

    //
    //  Setters
    //

    fun onSelected(onSelected:(PublicationSticker)->Unit): WidgetStickers{
        this.onSelected = onSelected
        return this
    }

}
