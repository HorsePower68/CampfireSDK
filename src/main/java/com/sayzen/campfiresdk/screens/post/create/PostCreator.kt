package com.sayzen.campfiresdk.screens.post.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PageImage
import com.dzen.campfire.api.models.units.post.PageText
import com.dzen.campfire.api.models.units.post.UnitPost
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sayzen.campfiresdk.models.events.units.EventPostChanged
import com.sayzen.campfiresdk.models.events.units.EventPostDraftCreated
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.screens.post.create.creators.CardMove
import com.sayzen.campfiresdk.screens.post.create.creators.WidgetAdd
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class PostCreator(
        val oldPages: Array<Page>,
        val vRecycler: RecyclerView,
        val vAdd: FloatingActionButton,
        val vFinish: FloatingActionButton,
        val onBackEmptyAndNewerAdd: () -> Unit,
        val requestPutPage: (Widget?, Array<Page>, (Array<Page>) -> Unit, () -> Unit) -> Unit,
        val requestRemovePage: (Array<Int>, () -> Unit) -> Unit,
        val requestChangePage: (Widget?, Page, Int, (Page) -> Unit) -> Unit,
        val requestMovePage: (Int, Int, () -> Unit) -> Unit
) {

    enum class ActionType {
        STOP, ADD
    }

    private val adapter: RecyclerCardAdapter
    private var actionType: ActionType? = null
    private val widgetAdd: WidgetAdd
    private var newerAdd = true

    val pages: Array<Page>
        get() {
            val cards = adapter.get(CardPage::class)
            val pages = arrayOfNulls<Page>(cards.size)
            for (i in cards.indices) pages[i] = cards[i].page
            return ToolsMapper.asNonNull(pages)
        }

    init {

        widgetAdd = WidgetAdd(
                { page, screen, widget, mapper, onFinish -> putPage(page, screen, widget, mapper, onFinish) },
                { page, card, screen, widget, onFinish -> changePage(page, card, screen, widget, onFinish) }
                , onBackEmptyAndNewerAdd)
        adapter = RecyclerCardAdapter()
        adapter.addItemsChangeListener { updateFinishEnabled() }
        adapter.add(CardSpace(72))

        vRecycler.layoutManager = LinearLayoutManager(vRecycler.context)
        vRecycler.adapter = adapter
        vRecycler.scrollToPosition(adapter.size() - 1)

        for (p in oldPages) addPage(CardPage.instance(null, p))

        vAdd.setOnClickListener { onFabClicked() }

        setActionType(ActionType.ADD)

        updateFinishEnabled()
    }


    //
    //  View
    //

    private fun updateFinishEnabled() {
        ToolsView.setFabEnabledR(vFinish, adapter.size() > 1, R.color.green_700)
    }

    fun setActionType(actionType: ActionType) {
        this.actionType = actionType
        if (actionType == ActionType.STOP)
            vAdd.setImageResource(R.drawable.ic_clear_white_24dp)
        else
            vAdd.setImageResource(R.drawable.ic_add_white_24dp)
    }

    //
    //  Actions
    //

    private fun addPage(c: CardPage) {
        adapter.add(adapter.size() - 1, c.setEditMod(true, { c1: CardPage -> this.startMove(c1) }, { c2: CardPage -> widgetAdd.changePage(c2) }, { c3: CardPage -> this.removePage(c3) }))
        ControllerPost.openAllSpoilers(adapter)
    }

    private fun startMove(c: CardPage) {
        stopMove()
        setActionType(ActionType.STOP)
        val startPosition = adapter.indexOf(c)
        for (i in adapter.size() - 1 downTo 0) {
            if (i == startPosition || i == startPosition + 1) continue
            adapter.add(i, CardMove { movePage(c, i) })
        }
    }

    private fun stopMove() {
        ControllerPost.openAllSpoilers(adapter)
        setActionType(ActionType.ADD)
        var i = 0
        while (i < adapter.size()) {
            if (adapter[i] is CardMove)
                adapter.remove(i--)
            i++
        }
    }

    fun hideMenu(){
        widgetAdd.hide()
    }

    private fun onFabClicked() {
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        if (actionType == ActionType.STOP)
            stopMove()
        else
            widgetAdd.asSheetShow()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K : Page, N : CardPage> onPageAdd(screen: Screen?, pages: Array<Page>, mapper: (K) -> N): CardPage {
        if (screen != null) Navigator.remove(screen)
        val card = mapper.invoke(pages[0] as K)
        addPage(card)
        ToolsThreads.main(200) { vRecycler.scrollToPosition((vRecycler.adapter as RecyclerCardAdapter).indexOf(card) + 1) }
        return card
    }

    private fun onChangePage(card: CardPage, screen: Screen?, page: Page) {
        if (screen != null) Navigator.remove(screen)
        card.page = page
        card.update()
    }


    //
    //  Requests
    //

    private fun putPage(page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) {
        newerAdd = false
        screen?.isEnabled = false

        requestPutPage.invoke(widget, arrayOf(page), { pages ->
            val card = onPageAdd(screen, pages, mapper)
            onFinish.invoke(card)
        }, {
            screen?.isEnabled = true
        })
    }

    private fun removePage(c: CardPage) {
        requestRemovePage.invoke(arrayOf(adapter.indexOf(c))) {
            adapter.remove(c)
            ControllerPost.openAllSpoilers(adapter)
        }
    }

    private fun changePage(page: Page, card: CardPage, screen: Screen?, widget: Widget?, onFinish: (Page) -> Unit) {
        requestChangePage.invoke(widget, page, adapter.indexOf(card)) { page ->
            onFinish.invoke(page)
            onChangePage(card, screen, page)
        }
    }

    private fun movePage(c: CardPage, index: Int) {
        val currentIndex = adapter.get(CardPage::class).indexOf(c)
        val targetIndex = if (currentIndex > index) index else index - 1
        requestMovePage(currentIndex, targetIndex) {
            stopMove()
            adapter.remove(c)
            adapter.add(targetIndex, c)
            ControllerPost.openAllSpoilers(adapter)
        }
    }

    //
    //  Share
    //

    fun addText(text: String, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val page = PageText()
        page.text = text
        page.size = PageText.SIZE_0
        putPage(page, null, null, { CardPageText(null, it as PageText) }, {
            onAdd.invoke()
        })
    }

    fun addImage(image: Uri, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            ToolsBitmap.getFromUri(image, {
                if (it == null) {
                    w.hide()
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@getFromUri
                }
                page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(it, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
                ToolsThreads.main {
                    putPage(page, null, w, { CardPageImage(null, it as PageImage) }, {
                        onAdd.invoke()
                    })
                }
            }, {
                w.hide()
                ToolsToast.show(R.string.error_cant_load_image)
            })
        }
    }

    fun addImage(image: Bitmap, onAdd: () -> Unit) {
        widgetAdd.wasClicked = true
        widgetAdd.hide()
        if (adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(image, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
            ToolsThreads.main {
                putPage(page, null, w, { CardPageImage(null, it as PageImage) }, {
                    onAdd.invoke()
                })
            }
        }
    }

    //
    //  Getters
    //

    fun isNewerAdd() = newerAdd


}