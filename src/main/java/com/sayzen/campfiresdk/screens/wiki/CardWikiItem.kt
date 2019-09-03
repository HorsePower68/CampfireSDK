package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiItem
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.widgets.WidgetMenu

class CardWikiItem(
        val wikiItem:WikiItem
) : Card(R.layout.wiki_card_item),NotifyItem{

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage:ImageView = view.findViewById(R.id.vImage)
        val vName:TextView = view.findViewById(R.id.vName)
        val vSectionIcon:ImageView = view.findViewById(R.id.vSectionIcon)

        ToolsImagesLoader.load(wikiItem.imageId).into(vImage)
        vName.text = wikiItem.getName(ControllerApi.getLanguageCode())
        vSectionIcon.visibility = if(wikiItem.type == API.WIKI_TYPE_SECION) View.VISIBLE else View.GONE

        view.setOnClickListener {
            if(wikiItem.type == API.WIKI_TYPE_SECION) Navigator.to(SWikiList(wikiItem.fandomId, wikiItem.itemId))
        }

        view.setOnLongClickListener {
            WidgetMenu()
                    .add(R.string.app_change){w,i-> Navigator.to(SWikiItemCreate(wikiItem.fandomId, wikiItem.parentItemId, wikiItem))}
                    .add(R.string.app_remove){w,i-> removeWikiItem()}.condition(ControllerApi.can(wikiItem.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                    .asSheetShow()
            true
        }
    }

    private fun removeWikiItem(){

    }

    override fun notifyItem() {
        ToolsImagesLoader.load(wikiItem.imageId).intoCash()
    }

}