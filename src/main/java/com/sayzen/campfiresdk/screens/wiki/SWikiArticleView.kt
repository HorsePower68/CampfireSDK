package com.sayzen.campfiresdk.screens.wiki

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.PagesContainer
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiGetPages
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerWiki
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.wiki.EventWikiPagesChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiArticleView(
        val wikiTitle: WikiTitle,
        var languageId: Long
        ) : Screen(R.layout.screen_wiki_article), PagesContainer {

    companion object{

        fun instance(wikiItemId:Long, action: NavigationAction){
            ApiRequestsSupporter.executeInterstitial(action, RWikiItemGet(wikiItemId)) { r ->
                SWikiArticleView(r.wikiTitle, ControllerApi.getLanguageId())
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventWikiPagesChanged::class) { this.onEventWikiPagesChanged(it) }
            .subscribe(EventWikiRemove::class) { if (it.itemId == wikiTitle.itemId) Navigator.remove(this) }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vImageTitle: ImageView = findViewById(R.id.vImageTitle)
    private val vToolbarTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vMore: View = findViewById(R.id.vMore)
    private val vAvatarTouch: View = findViewById(R.id.vAvatarTouch)
    private val vAvatar: ImageView = findViewById(R.id.vAvatar)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vProgressLine: View = findViewById(R.id.vProgressLine)
    private val vAction: Button = findViewById(R.id.vAction)
    private val vLanguage: Button = findViewById(R.id.vLanguage)

    private val adapter = RecyclerCardAdapter()
    private var pages:Array<Page> = emptyArray()
    private var isLoading = false
    private var error = false
    private var wasSwitchedToEnglish = false

    init {
        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))
        ToolsImagesLoader.loadGif(wikiTitle.imageId, 0, 0, 0, vAvatar)
        ToolsImagesLoader.loadGif(wikiTitle.imageBigId, 0, 0, 0, vImageTitle)
        vToolbarTitle.text = wikiTitle.getName(ControllerApi.getLanguageCode())

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        vRecycler.setOnClickListener { load() }

        vMore.setOnClickListener { ControllerWiki.showMenu(wikiTitle, languageId) }

        vLanguage.setOnClickListener {
            ControllerCampfireSDK.createLanguageMenu(languageId) { languageId ->
                this.languageId = languageId
                load()
            }.asSheetShow()
        }

        load()
    }

    private fun load() {
        isLoading = true
        error = false
        adapter.clear()
        updateMessage()
        vLanguage.text = ControllerApi.getLanguage(languageId).name
        RWikiGetPages(wikiTitle.itemId, languageId)
                .onComplete {
                    isLoading = false
                    if ((it.wikiPages == null || it.wikiPages!!.pages.isEmpty()) && languageId != 1L && !wasSwitchedToEnglish) {
                        languageId = 1L
                        wasSwitchedToEnglish = true
                        load()
                    } else {
                        pages = it.wikiPages?.pages?: emptyArray()
                        for (i in pages) adapter.add(CardPage.instance(this, i))
                        updateMessage()
                    }
                }
                .onApiError {
                    isLoading = false
                    if (it.code == API.ERROR_GONE) {
                        if (languageId != 1L) {
                            languageId = 1L
                            load()
                        } else {
                            updateMessage()
                        }
                    } else {
                        isLoading = false
                        error = true
                        updateMessage()
                    }
                }
                .onNetworkError {
                    isLoading = false
                    error = true
                    updateMessage()
                }
                .send(api)
    }

    private fun updateMessage() {
        vMessage.visibility = View.VISIBLE
        vProgressLine.visibility = View.GONE
        vAction.visibility = View.GONE
        if (isLoading) {
            vProgressLine.visibility = View.VISIBLE
            vMessage.setText(R.string.wiki_article_loading)
        } else {
            if (error) {
                vAction.visibility = View.VISIBLE
                vMessage.setText(R.string.error_network)
            } else {
                if (pages.isEmpty()) {
                    vMessage.setText(R.string.wiki_article_empty)
                } else {
                    vMessage.visibility = View.GONE
                }
            }
        }


    }

    override fun getPagesArray() = pages
    override fun getSourceType() = API.PAGES_SOURCE_TYPE_WIKI
    override fun getSourcId() = wikiTitle.id
    override fun getSourceIdSub() = languageId


    //
    //  EventBus
    //

    private fun onEventWikiPagesChanged(e: EventWikiPagesChanged) {
        if (e.itemId == wikiTitle.itemId) {
            if(e.languageId != languageId){
                languageId = e.languageId
                load()
            }else {
                adapter.remove(CardPage::class)
                pages = e.pages
                for (i in pages) adapter.add(CardPage.instance(this, i))
                updateMessage()
            }
        }
    }

}
