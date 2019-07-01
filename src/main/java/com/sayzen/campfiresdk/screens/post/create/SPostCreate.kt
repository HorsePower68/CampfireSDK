package com.sayzen.campfiresdk.screens.post.create

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.*
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.post.*
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageImage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sayzen.campfiresdk.models.events.units.EventPostChanged
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.models.ScreenShare
import com.sayzen.campfiresdk.screens.post.create.creator.CardMove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.screens.post.create.creator.WidgetAdd
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class SPostCreate constructor(
        val fandomId: Long,
        val languageId: Long,
        val fandomName: String,
        val fandomImageId: Long,
        private val changePost: UnitPost?,
        private val tags: Array<Long>,
        private val showMenu: Boolean
) : Screen(R.layout.screen_post_create), ScreenShare {

    companion object {

        fun instance(unitId: Long, action: NavigationAction, onOpen:(SPostCreate)->Unit={}) {
            ApiRequestsSupporter.executeInterstitial(action, RPostGetDraft(unitId)) { r ->
                val screen = SPostCreate(r.unit.fandomId, r.unit.languageId, r.unit.fandomName, r.unit.fandomImageId, r.unit, Array(r.tags.size) { r.tags[it].id }, true)
                onOpen.invoke(screen)
                screen
            }
        }

        fun instance(fandomId: Long, languageId: Long,  fandomName: String, fandomImageId: Long, presetTags: Array<Long>, action: NavigationAction) {
            Navigator.action(action, SPostCreate(fandomId, languageId, fandomName, fandomImageId, null, presetTags, true))
        }

        fun instance(fandomId: Long, languageId: Long, presetTags: Array<Long>, onOpen:(SPostCreate)->Unit={}, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsGet(fandomId, languageId, ControllerApi.getLanguageId())) { r ->
                val screen =  SPostCreate(fandomId, languageId, r.fandom.name, r.fandom.imageId, null, presetTags, true)
                onOpen.invoke(screen)
                screen
            }
        }

    }

    constructor(fandomId: Long, languageId: Long, fandomName: String, fandomImageId: Long) : this(fandomId, languageId, fandomName, fandomImageId, null, emptyArray(), true)

    private val vRecycler: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.vRecycler)
    private val vAdd: FloatingActionButton = findViewById(R.id.vAdd)
    private val vFinish: FloatingActionButton = findViewById(R.id.vFinish)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    val adapter: RecyclerCardAdapter
    private val widgetAdd: WidgetAdd

    private var actionType: ActionType? = null
    private var unitId = 0L
    private var unitTag3 = 0L
    private var newerAdd = true

    val xFandom = XFandom(fandomId, languageId, fandomName, fandomImageId){
        updateTitle()
    }

    val pages: Array<Page>
        get() {
            val cards = adapter.get(CardPage::class)
            val pages = arrayOfNulls<Page>(cards.size)
            for (i in cards.indices) pages[i] = cards[i].page
            return ToolsMapper.asNonNull(pages)
        }

    enum class ActionType {
        STOP, ADD
    }


    init {
        isSingleInstanceInBackStack = true
        this.unitId = changePost?.id ?: 0
        this.unitTag3 = changePost?.tag_3 ?: 0

        adapter = RecyclerCardAdapter()
        adapter.addItemsChangeListener { updateFinishEnabled() }
        adapter.add(CardSpace(72))
        widgetAdd = WidgetAdd(this)

        if (changePost != null) for (p in changePost.pages) addPage(CardPage.instance(null, p))

        vFinish.setOnClickListener { v ->
            if (changePost == null || changePost.isDraft) SCreationTags.instance(unitId, unitTag3, true, fandomId, languageId, tags, Navigator.TO)
            else Navigator.back()
        }
        vFinish.setOnLongClickListener { v ->
            SCreationTags.create(unitId, tags, false) { SPost.instance(unitId, 0, NavigationAction.replace()) }
            true
        }
        if (changePost != null && !changePost.isDraft) vFinish.setImageResource(ToolsResources.getDrawableAttrId(R.attr.ic_done_24dp))


        vAdd.setOnClickListener { v -> onFabClicked() }
        vRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(vRecycler.context)
        vRecycler.adapter = adapter
        vRecycler.scrollToPosition(adapter.size()-1)

        setActionType(ActionType.ADD)
        updateFinishEnabled()

        if (changePost != null && changePost.status == API.STATUS_PUBLIC && !WidgetAlert.check("ALERT_CHANGE_POSTS"))
            WidgetAlert()
                    .setTopTitleText(R.string.app_attention)
                    .setCancelable(false)
                    .setTitleImageBackgroundRes(R.color.blue_700)
                    .setText(R.string.post_change_alert)
                    .setChecker("ALERT_CHANGE_POSTS")
                    .setOnEnter(R.string.app_got_it)
                    .asSheetShow()

        if (changePost == null && showMenu) vAdd.performClick()

        updateTitle()
    }

    fun setUnitId(unitId:Long){
        this.unitId = unitId
    }

    private fun updateTitle(){
        xFandom.setView(vAvatarTitle)
    }

    private fun updateFinishEnabled() {
        ToolsView.setFabEnabledR(vFinish, adapter.size() > 1, R.color.green_700)
    }

    private fun onFabClicked() {
        if(adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT){
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        if (actionType == ActionType.STOP)
            stopMove()
        else
            widgetAdd.asSheetShow()
    }

    private fun addPage(c: CardPage) {
        adapter.add(adapter.size() - 1, c.setEditMod(true, { c: CardPage -> this.startMove(c) }, { c: CardPage -> widgetAdd.changePage(c) }, { c: CardPage -> this.removePage(c) }))
        ControllerPost.openAllSpoilers(adapter)
    }


    fun <K : Page, N : CardPage> putPage(page: K, screen: Screen?, widget: Widget?, mapper: (K) -> N, onFinish: (()->Unit)?, needSend: Boolean) {
        newerAdd = false
        if (needSend) {
            screen?.isEnabled = false
            ApiRequestsSupporter.executeEnabled(widget, RPostPutPage(unitId, arrayOf(page), fandomId, languageId, "", "")) { r ->
                onPageAdd(screen, r, mapper)
            }.onFinish {
                screen?.isEnabled = true
                onFinish?.invoke()
            }
        } else {
            if (screen != null) Navigator.remove(screen)
            val card = mapper.invoke(page)
            addPage(card)
            EventBus.post(EventPostChanged(unitId, pages))
            ToolsThreads.main(200) { vRecycler.scrollToPosition((vRecycler.adapter as RecyclerCardAdapter).indexOf(card) + 1) }
            onFinish?.invoke()
        }
    }

    private fun <K : Page, N : CardPage> onPageAdd(screen: Screen?, r: RPostPutPage.Response, mapper: (K) -> N) {
        if (screen != null) Navigator.remove(screen)
        if (unitId == 0L) unitId = r.unitId
        val card = mapper.invoke(r.pages[0] as K)
        addPage(card)
        EventBus.post(EventPostChanged(unitId, pages))
        ToolsThreads.main(200) { vRecycler.scrollToPosition((vRecycler.adapter as RecyclerCardAdapter).indexOf(card) + 1) }
    }

    fun changePage(page: Page, card: CardPage, screen: Screen?, widget: Widget?, needSend: Boolean = true) {
        if (needSend) {
            if (widget == null)
                ApiRequestsSupporter.executeProgressDialog(RPostChangePage(unitId, page, adapter.indexOf(card))) { r ->
                    onChangePage(card, screen, r)
                }
            else
                ApiRequestsSupporter.executeEnabled(widget, RPostChangePage(unitId, page, adapter.indexOf(card))) { r ->
                    onChangePage(card, screen, r)
                }
        } else {
            if (screen != null) Navigator.remove(screen)
            card.page = page
            card.update()
            EventBus.post(EventPostChanged(unitId, pages))
        }
    }

    private fun onChangePage(card: CardPage, screen: Screen?, r: RPostChangePage.Response) {
        if (screen != null) Navigator.remove(screen)
        card.page = r.page!!
        card.update()
        EventBus.post(EventPostChanged(unitId, pages))
    }

    private fun removePage(c: CardPage) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.post_page_remove_confirm, R.string.app_remove, RPostRemovePage(unitId, arrayOf(adapter.indexOf(c)))) { r ->
            adapter.remove(c)
            if (adapter.size(CardPage::class) == 0) {
                EventBus.post(EventUnitRemove(unitId))
                unitId = 0
            }
            ControllerPost.openAllSpoilers(adapter)
            EventBus.post(EventPostChanged(unitId, pages))
        }
    }

    private fun movePage(c: CardPage, index: Int) {
        val currentIndex = adapter.get(CardPage::class).indexOf(c)
        val targetIndex = if (currentIndex > index) index else index - 1
        ApiRequestsSupporter.executeProgressDialog(RPostMovePage(unitId, currentIndex, targetIndex)) { r ->
            stopMove()
            adapter.remove(c)
            adapter.add(targetIndex, c)
            ControllerPost.openAllSpoilers(adapter)
            EventBus.post(EventPostChanged(unitId, pages))
        }
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

    fun setActionType(actionType: ActionType) {
        this.actionType = actionType
        if (actionType == ActionType.STOP)
            vAdd.setImageResource(R.drawable.ic_clear_white_24dp)
        else
            vAdd.setImageResource(R.drawable.ic_add_white_24dp)
    }

    fun backIfEmptyAndNewerAdd() {
        if (adapter.isEmpty && newerAdd) Navigator.back()
    }

    fun getUnitId() = unitId

    //
    //  Share
    //

    override fun addText(text:String, postAfterAdd:Boolean){
        if(adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT){
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val page = PageText()
        page.text = text
        page.size = PageText.SIZE_0
        putPage(page, null, null, {CardPageText(null, it)},  {
            if(postAfterAdd) SCreationTags.create(unitId, tags, false) { SPost.instance(unitId, 0, NavigationAction.replace()) }
        }, true)
    }

    override fun addImage(image:Uri, postAfterAdd:Boolean){
        if(adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT){
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            ToolsBitmap.getFromUri(image, {
                if(it == null) {
                    w.hide()
                    ToolsToast.show(R.string.error_cant_load_image)
                    return@getFromUri
                }
                page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(it, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
                ToolsThreads.main { putPage(page, null, w, { CardPageImage(null, it) }, {
                    if(postAfterAdd) SCreationTags.create(unitId, tags, false) { SPost.instance(unitId, 0, NavigationAction.replace()) }
                }, true) }
            },{
                w.hide()
                ToolsToast.show(R.string.error_cant_load_image)
            })
        }
    }

    override fun addImage(image: Bitmap, postAfterAdd:Boolean){
        if(adapter.get(CardPage::class).size >= API.POST_MAX_PAGES_COUNT){
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        val w = ToolsView.showProgressDialog()
        ToolsThreads.thread {
            val page = PageImage()
            page.insertBytes = ToolsBitmap.toBytes(ToolsBitmap.keepMaxSides(image, API.PAGE_IMAGE_SIDE), API.PAGE_IMAGE_WEIGHT)
            ToolsThreads.main { putPage(page, null, w, { CardPageImage(null, it) }, {
                if(postAfterAdd) SCreationTags.create(unitId, tags, false) { SPost.instance(unitId, 0, NavigationAction.replace()) }
            }, true) }
        }
    }

}
