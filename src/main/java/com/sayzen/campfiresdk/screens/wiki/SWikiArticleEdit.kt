package com.sayzen.campfiresdk.screens.wiki

import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.requests.wiki.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.wiki.EventWikiPagesChanged
import com.sayzen.campfiresdk.screens.post.create.PostCreator
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SWikiArticleEdit(
        val wikiItemId:Long,
        val languageId:Long,
        val pages:Array<Page>
) : Screen(R.layout.screen_wiki_article_edit) {

    companion object{

        fun instance(wikiItemId:Long, languageId: Long, action:NavigationAction){
            ApiRequestsSupporter.executeInterstitial(action, RWikiGetPages(wikiItemId, languageId)) { r ->
                SWikiArticleEdit(wikiItemId, languageId, r.wikiPages?.pages?: emptyArray())
            }
        }

    }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vAdd: FloatingActionButton = findViewById(R.id.vAdd)
    private val vFinish: FloatingActionButton = findViewById(R.id.vFinish)
    private val xPostCreator = PostCreator(pages, vRecycler, vAdd, vFinish, { backIfEmptyAndNewerAdd() }, requestPutPage(), requestRemovePage(), requestChangePage(), requestMovePage())

    init {
        vFinish.setOnClickListener { Navigator.back() }

        if (!WidgetAlert.check("SWikiArticleEdit_ALERT"))
            ToolsThreads.main(true) {
                WidgetAlert()
                        .setTopTitleText(R.string.app_attention)
                        .setCancelable(false)
                        .setTitleImageBackgroundRes(R.color.blue_700)
                        .setText(R.string.wiki_edit_alert)
                        .setChecker("SWikiArticleEdit_ALERT")
                        .setOnEnter(R.string.app_got_it)
                        .asSheetShow()
            }
    }

    fun backIfEmptyAndNewerAdd() {
        if (xPostCreator.pages.isEmpty() && xPostCreator.isNewerAdd()) Navigator.back()
    }


    //
    //  Requests
    //

    private fun requestPutPage(): (Widget?, Array<Page>, (Array<Page>) -> Unit, () -> Unit) -> Unit = { widget, pages, onCreate, onFinish ->
        ApiRequestsSupporter.executeEnabled(widget, RWikiPagePut(wikiItemId, pages, languageId)) { r ->
            onCreate.invoke(r.pages)
            EventBus.post(EventWikiPagesChanged(wikiItemId, languageId, xPostCreator.pages))
        }.onFinish {
            onFinish.invoke()
        }
    }

    private fun requestRemovePage(): (Array<Int>, () -> Unit) -> Unit = { pages, onFinish->
        ApiRequestsSupporter.executeEnabledConfirm(R.string.post_page_remove_confirm, R.string.app_remove, RWikiPageRemove(wikiItemId, languageId, pages)) {
            onFinish.invoke()
            EventBus.post(EventWikiPagesChanged(wikiItemId, languageId, xPostCreator.pages))
        }
    }

    private fun requestChangePage(): (Widget?, Page, Int, (Page) -> Unit) -> Unit = {widget, page, index, onFinish->
        ApiRequestsSupporter.executeEnabled(widget, RWikiPageChange(wikiItemId, languageId, page, index)) { r ->
            onFinish.invoke(r.page!!)
            EventBus.post(EventWikiPagesChanged(wikiItemId, languageId, xPostCreator.pages))
        }
    }

    private fun requestMovePage(): (Int,Int,() -> Unit) -> Unit = { currentIndex, targetIndex, onFinish->
        ApiRequestsSupporter.executeProgressDialog(RWikiPageMove(wikiItemId, languageId, currentIndex, targetIndex)) { _ ->
            onFinish.invoke()
            EventBus.post(EventWikiPagesChanged(wikiItemId, languageId, xPostCreator.pages))
        }
    }


}
