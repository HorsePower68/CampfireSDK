package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleEdit
import com.sayzen.campfiresdk.screens.wiki.SWikiItemCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerWiki {

    fun instanceMenu(wikiTitle: WikiTitle, languageId: Long): WidgetMenu {
        return WidgetMenu()
                .add(R.string.app_change) { _, _ -> Navigator.to(SWikiItemCreate(wikiTitle.fandomId, wikiTitle.parentItemId, wikiTitle)) }
                .add(R.string.app_remove) { _, _ -> removeWikiItem(wikiTitle) }.condition(ControllerApi.can(wikiTitle.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                .add(R.string.app_edit) { w, i -> SWikiArticleEdit.instance(wikiTitle.itemId, languageId, Navigator.TO)}.backgroundRes(R.color.blue_700).condition(ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT))
    }

    fun showMenu(wikiTitle: WikiTitle, languageId: Long) {
        instanceMenu(wikiTitle, languageId).asSheetShow()
    }

    private fun removeWikiItem(wikiTitle: WikiTitle) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_remove_confirm, R.string.app_remove, RWikiRemove(wikiTitle.itemId)) {
            EventBus.post(EventWikiRemove(wikiTitle))
            ToolsToast.show(R.string.app_done)
        }
    }

}