package com.sayzen.campfiresdk.views

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.splash.Dialog
import com.sup.dev.android.views.splash.Sheet
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.WidgetRecycler

open class WidgetReactions : WidgetRecycler(R.layout.widget_reactions) {

    private val myAdapter = RecyclerCardAdapter()

    private var onSelected: (Long) -> Unit = { }
    private var spanCount = 4
    private var hided = false

    init {
        spanCount = if (ToolsAndroid.isScreenPortrait()) 6 else 12
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)

        setAdapter<WidgetRecycler>(myAdapter)
    }

    private fun load() {
        myAdapter.clear()

        for(i in API.REACTIONS.indices){
            myAdapter.add(CardReaction(i))
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

        if (viewWrapper is Dialog)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is Sheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()

        load()
    }

    //
    //  Setters
    //

    fun onSelected(onSelected: (Long) -> Unit): WidgetReactions {
        this.onSelected = onSelected
        return this
    }

    //
    //  Card
    //

    inner class CardReaction(val reaction: Int) : Card(R.layout.card_reaction) {

        override fun bindView(view: View) {
            super.bindView(view)

            val vText: TextView = view.findViewById(R.id.vText)

            if (reaction > -1 && reaction < API.REACTIONS.size) vText.text = "${API.REACTIONS[reaction]}"
            else vText.text = "${API.REACTIONS[0]}"

            vText.setOnClickListener {
                onSelected.invoke(reaction.toLong())
                hide()
            }
        }

    }

}
