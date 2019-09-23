package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiArticleChangeLanguage
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.wiki.EventWikiChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiPagesChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleEdit
import com.sayzen.campfiresdk.screens.wiki.SWikiItemCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerWiki {

    fun instanceMenu(wikiTitle: WikiTitle, languageId: Long): WidgetMenu {
        return WidgetMenu()
                .add(R.string.app_link) { _, _ -> copyLink(wikiTitle) }
                .add(R.string.app_change) { _, _ -> Navigator.to(SWikiItemCreate(wikiTitle.fandomId, wikiTitle.parentItemId, wikiTitle)) }.backgroundRes(R.color.blue_700).condition(wikiTitle.itemType == API.WIKI_TYPE_SECION && ControllerApi.can(wikiTitle.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                .add(R.string.app_remove) { _, _ -> removeWikiItem(wikiTitle.itemId) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(wikiTitle.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                .add(R.string.app_edit) { w, i -> toEditArticle(wikiTitle.itemId, languageId)}.backgroundRes(R.color.blue_700).condition(wikiTitle.itemType == API.WIKI_TYPE_ARTICLE && ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT))
                .add(R.string.wiki_change_language) { w, i -> changeLanguage(wikiTitle.itemId, languageId)}.backgroundRes(R.color.blue_700).condition(wikiTitle.itemType == API.WIKI_TYPE_ARTICLE && ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT))
    }

    fun showMenu(wikiTitle: WikiTitle, languageId: Long) {
        instanceMenu(wikiTitle, languageId).asSheetShow()
    }

    fun toEditArticle(itemId:Long, languageId: Long){
        if(languageId != ControllerApi.getLanguageId()){
            WidgetAlert()
                    .setText(R.string.wiki_article_edit_language_alert)
                    .setOnEnter(ControllerApi.getLanguage().name){
                        SWikiArticleEdit.instance(itemId, ControllerApi.getLanguageId(), Navigator.TO)
                    }
                    .setOnCancel(ControllerApi.getLanguage(languageId).name){
                        SWikiArticleEdit.instance(itemId, languageId, Navigator.TO)
                    }
                    .asSheetShow()
        }else {
            SWikiArticleEdit.instance(itemId, languageId, Navigator.TO)
        }
    }

    private fun changeLanguage(itemId:Long, fromLanguageId: Long){
        ControllerCampfireSDK.createLanguageMenu(0, arrayOf(fromLanguageId)) { languageId ->
            ApiRequestsSupporter.executeEnabledConfirm(R.string.wiki_change_language_alert, R.string.app_change, RWikiArticleChangeLanguage(itemId, fromLanguageId, languageId)) {
                EventBus.post(EventWikiPagesChanged(itemId, fromLanguageId, emptyArray()))
                EventBus.post(EventWikiPagesChanged(itemId, languageId, it.item.pages))
                ToolsToast.show(R.string.app_done)
            }
        }.asSheetShow()
    }

    private fun copyLink(wikiTitle: WikiTitle){
        if (wikiTitle.itemType == API.WIKI_TYPE_ARTICLE) ToolsAndroid.setToClipboard(ControllerApi.linkToWikiArticle(wikiTitle.itemId))
        else ToolsAndroid.setToClipboard(ControllerApi.linkToWikiItemId(wikiTitle.itemId))
        ToolsToast.show(R.string.app_copied)
    }

    private fun removeWikiItem(itemId:Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.wiki_item_remove_confirm, R.string.app_remove, RWikiRemove(itemId)) {
            EventBus.post(EventWikiRemove(itemId))
            ToolsToast.show(R.string.app_done)
        }
    }

}