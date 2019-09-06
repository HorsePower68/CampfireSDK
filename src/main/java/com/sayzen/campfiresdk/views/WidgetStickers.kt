package com.sayzen.campfiresdk.views

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.dzen.campfire.api.requests.stickers.RStickersGetAll
import com.dzen.campfire.api.requests.stickers.RStickersGetAllById
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.screens.stickers.SStickersPacksSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.dialogs.DialogSheetWidget
import com.sup.dev.android.views.dialogs.DialogWidget
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.widgets.WidgetRecycler
import com.sup.dev.java.tools.ToolsThreads

open class WidgetStickers : WidgetRecycler(R.layout.widget_stickers) {

    private val myAdapter: RecyclerCardAdapterLoading<CardSticker, UnitSticker>
    private val vEmptyContainer: View = findViewById(R.id.vEmptyContainer)
    private val vSearchStickers: View = findViewById(R.id.vSearchStickers)
    private val stickersPacks = ControllerSettings.accountSettings.stickersPacks
    private var stickersPackIndex = 0

    private var onSelected: (UnitSticker) -> Unit = { }
    private var spanCount = 4
    private var hided = false
    private var stickerLoaded = false

    init {
        vEmptyContainer.visibility = View.GONE

        vSearchStickers.setOnClickListener { Navigator.to(SStickersPacksSearch()) }

        spanCount = if (ToolsAndroid.isScreenPortrait()) 4 else 8
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)

        myAdapter = RecyclerCardAdapterLoading(CardSticker::class){
            val card = CardSticker(it)
            card.onClick = {
                onSelected.invoke(it)
                hide()
            }
            card
        }
        myAdapter.setShowLoadingCard(false)

        setAdapter<WidgetRecycler>(myAdapter)

        myAdapter.setBottomLoader{ onLoad, _->

            if(!stickerLoaded && ControllerSettings.accountSettings.stickers.isNotEmpty()){
                RStickersGetAllById(ControllerSettings.accountSettings.stickers)
                        .onComplete { r->
                            stickerLoaded = true
                            if (r.stickersIds != null) ControllerSettings.accountSettings.stickers = r.stickersIds!!
                            onLoad.invoke(r.stickers)
                            if(r.stickers.isEmpty()){
                                ToolsThreads.main(true) {
                                    myAdapter.loadBottom()    //  Блокируется из-за пустых паков стикеров
                                }
                            }
                        }
                        .onError {
                            onLoad.invoke(null)
                        }
                        .send(api)
                return@setBottomLoader
            }

            if(stickersPackIndex >= stickersPacks.size) {
                onLoad.invoke(emptyArray())
                if(myAdapter.isEmpty) vEmptyContainer.visibility = View.VISIBLE
                return@setBottomLoader
            }
            vEmptyContainer.visibility = View.GONE
            RStickersGetAll(stickersPacks[stickersPackIndex])
                    .onComplete { r->
                        stickersPackIndex++
                        if (r.stickersPacks != null) ControllerSettings.accountSettings.stickersPacks = r.stickersPacks!!
                        onLoad.invoke(r.stickers)
                        if(r.stickers.isEmpty()){
                            ToolsThreads.main(true) {
                                myAdapter.loadBottom()    //  Блокируется из-за пустых паков стикеров
                            }
                        }
                    }
                    .onError {
                        onLoad.invoke(null)
                    }
                    .send(api)
        }

    }

    override fun onHide() {
        super.onHide()
        hided = true
    }

    override fun onShow() {
        super.onShow()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is DialogWidget)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is DialogSheetWidget)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()

        myAdapter.loadBottom()
    }

    //
    //  Setters
    //

    fun onSelected(onSelected:(UnitSticker)->Unit): WidgetStickers{
        this.onSelected = onSelected
        return this
    }

}
