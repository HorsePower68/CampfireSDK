package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.wiki.WikiItem
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem

class CardWikiItem(
        val wikiItem:WikiItem
) : Card(R.layout.wiki_card_item),NotifyItem{

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage:ImageView = view.findViewById(R.id.vImage)
        val vName:TextView = view.findViewById(R.id.vName)

        ToolsImagesLoader.load(wikiItem.imageId).into(vImage)
        vName.text = wikiItem.name
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(wikiItem.imageId).intoCash()
    }

}