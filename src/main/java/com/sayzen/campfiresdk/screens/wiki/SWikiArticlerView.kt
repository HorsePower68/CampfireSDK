package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.PagesContainer
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiGetPages
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiArticlerView(
        val item: WikiTitle,
        var languageId: Long
) : Screen(R.layout.screen_wiki_article), PagesContainer {


    private val eventBus = EventBus.subscribe(EventWikiRemove::class) { if (it.item.itemId == item.itemId) Navigator.remove(this) }

    private val vImageTitle: ImageView = findViewById(R.id.vImageTitle)
    private val vToolbarTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vMore: View = findViewById(R.id.vMore)
    private val vAvatarTouch: View = findViewById(R.id.vAvatarTouch)
    private val vAvatar: ImageView = findViewById(R.id.vAvatar)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vProgressLine: View = findViewById(R.id.vProgressLine)
    private val vAction: Button = findViewById(R.id.vAction)
    private val vEdit: View = findViewById(R.id.vEdit)

    private val adapter = RecyclerCardAdapter()
    private var pages = WikiPages()
    private var isLoading = false
    private var error = false

    init {
        ToolsImagesLoader.loadGif(item.imageId, 0, 0, 0, vAvatar)
        ToolsImagesLoader.loadGif(item.imageBigId, 0, 0, 0, vImageTitle)
        vToolbarTitle.text = item.getName(ControllerApi.getLanguageCode())

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter
        vRecycler.setOnClickListener { load() }

        vEdit.setOnClickListener { Navigator.to(SWikiArticleEdit(item, pages, languageId)) }

        load()
    }

    private fun load() {
        isLoading = true
        error = false
        adapter.clear()
        updateMessage()
        RWikiGetPages(item.itemId, languageId)
                .onComplete {
                    isLoading = false
                    if (it.wikiPages.pages.isEmpty() && languageId != 1L) {
                        languageId = 1L
                        load()
                    } else {
                        pages = it.wikiPages
                        for (i in pages.pages) adapter.add(CardPage.instance(this, i))
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
        vEdit.visibility = if (ControllerApi.can(item.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)) View.VISIBLE else View.GONE
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
                if (pages.pages.isEmpty()) {
                    vMessage.setText(R.string.wiki_article_empty)
                } else {
                    vMessage.visibility = View.GONE
                }
            }
        }


    }

    override fun getPagesArray() = pages.pages
}
