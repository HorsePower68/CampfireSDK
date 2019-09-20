package com.sayzen.campfiresdk.screens.post.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.*
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.post.*
import com.sayzen.campfiresdk.models.ScreenShare
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.units.EventPostChanged
import com.sayzen.campfiresdk.models.events.units.EventPostDraftCreated
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SPostCreate constructor(
        val fandomId: Long,
        val languageId: Long,
        val fandomName: String,
        val fandomImageId: Long,
        changePost: UnitPost?,
        private val tags: Array<Long>,
        showMenu: Boolean
) : Screen(R.layout.screen_post_create), ScreenShare {

    companion object {

        fun instance(unitId: Long, action: NavigationAction, onOpen: (SPostCreate) -> Unit = {}) {
            ApiRequestsSupporter.executeInterstitial(action, RPostGetDraft(unitId)) { r ->
                val screen = SPostCreate(r.unit.fandomId, r.unit.languageId, r.unit.fandomName, r.unit.fandomImageId, r.unit, Array(r.tags.size) { r.tags[it].id }, true)
                onOpen.invoke(screen)
                screen
            }
        }

        fun instance(fandomId: Long, languageId: Long, fandomName: String, fandomImageId: Long, presetTags: Array<Long>, action: NavigationAction) {
            Navigator.action(action, SPostCreate(fandomId, languageId, fandomName, fandomImageId, null, presetTags, true))
        }

        fun instance(fandomId: Long, languageId: Long, presetTags: Array<Long>, onOpen: (SPostCreate) -> Unit = {}, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsGet(fandomId, languageId, ControllerApi.getLanguageId())) { r ->
                val screen = SPostCreate(fandomId, languageId, r.fandom.name, r.fandom.imageId, null, presetTags, true)
                onOpen.invoke(screen)
                screen
            }
        }

    }

    constructor(fandomId: Long, languageId: Long, fandomName: String, fandomImageId: Long) : this(fandomId, languageId, fandomName, fandomImageId, null, emptyArray(), true)

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vAdd: FloatingActionButton = findViewById(R.id.vAdd)
    private val vFinish: FloatingActionButton = findViewById(R.id.vFinish)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val xPostCreator = PostCreator(changePost?.pages?: emptyArray(), vRecycler, vAdd, vFinish, { backIfEmptyAndNewerAdd() }, requestPutPage(), requestRemovePage(), requestChangePage(), requestMovePage())
    private val xFandom = XFandom(fandomId, languageId, fandomName, fandomImageId) { updateTitle() }

    private var unitId = 0L
    private var unitTag3 = 0L

    init {
        isSingleInstanceInBackStack = true

        this.unitId = changePost?.id ?: 0
        this.unitTag3 = changePost?.tag_3 ?: 0

        vFinish.setOnClickListener {
            if (changePost == null || changePost.isDraft) SCreationTags.instance(unitId, unitTag3, true, fandomId, languageId, tags, Navigator.TO)
            else Navigator.back()
        }
        vFinish.setOnLongClickListener {
            SCreationTags.create(unitId, tags, false, 0) { SPost.instance(unitId, 0, NavigationAction.replace()) }
            true
        }
        if (changePost != null && !changePost.isDraft) vFinish.setImageResource(ToolsResources.getDrawableAttrId(R.attr.ic_done_24dp))

        if (changePost != null && changePost.status == API.STATUS_PUBLIC && !WidgetAlert.check("ALERT_CHANGE_POSTS"))
          ToolsThreads.main(true) {   WidgetAlert()
                  .setTopTitleText(R.string.app_attention)
                  .setCancelable(false)
                  .setTitleImageBackgroundRes(R.color.blue_700)
                  .setText(R.string.post_change_alert)
                  .setChecker("ALERT_CHANGE_POSTS")
                  .setOnEnter(R.string.app_got_it)
                  .asSheetShow()
          }

        if (changePost == null && showMenu) ToolsThreads.main(true) {  vAdd.performClick() }

        updateTitle()
    }

    private fun updateTitle() {
        xFandom.setView(vAvatarTitle)
    }

    fun backIfEmptyAndNewerAdd() {
        if (xPostCreator.pages.isEmpty() && xPostCreator.isNewerAdd()) Navigator.back()
    }

    fun setUnitId(unitId: Long) {
        this.unitId = unitId
        EventBus.post(EventPostDraftCreated(unitId))
    }

    //
    //  Requests
    //

    private fun requestPutPage(): (Widget?, Array<Page>, (Array<Page>) -> Unit, () -> Unit) -> Unit = { widget, pages, onCreate, onFinish ->
        ApiRequestsSupporter.executeEnabled(widget, RPostPutPage(unitId, pages, fandomId, languageId, "", "")) { r ->
            if (this.unitId == 0L) setUnitId(r.unitId)
            onCreate.invoke(r.pages)
            EventBus.post(EventPostChanged(unitId, pages))
        }.onFinish {
            onFinish.invoke()
        }
    }

    private fun requestRemovePage(): (Array<Int>, () -> Unit) -> Unit = { pages, onFinish->
        ApiRequestsSupporter.executeEnabledConfirm(R.string.post_page_remove_confirm, R.string.app_remove, RPostRemovePage(unitId, pages)) {
            onFinish.invoke()
            EventBus.post(EventPostChanged(unitId, xPostCreator.pages))
            if (xPostCreator.pages.isEmpty()) {
                EventBus.post(EventUnitRemove(unitId))
                unitId = 0
            }
        }
    }

    private fun requestChangePage(): (Widget?, Page, Int, (Page) -> Unit) -> Unit = {widget, page, index, onFinish->
        ApiRequestsSupporter.executeEnabled(widget, RPostChangePage(unitId, page, index)) { r ->
            onFinish.invoke(r.page!!)
            EventBus.post(EventPostChanged(unitId, xPostCreator.pages))
        }
    }

    private fun requestMovePage(): (Int,Int,() -> Unit) -> Unit = { currentIndex, targetIndex, onFinish->
        ApiRequestsSupporter.executeProgressDialog(RPostMovePage(unitId, currentIndex, targetIndex)) { _ ->
            onFinish.invoke()
            EventBus.post(EventPostChanged(unitId, xPostCreator.pages))
        }
    }

    //
    //  Share
    //

    override fun addText(text: String, postAfterAdd: Boolean) {
        xPostCreator.addText(text) {
            if (postAfterAdd) SCreationTags.create(unitId, tags, false, 0) { SPost.instance(unitId, 0, NavigationAction.replace()) }
        }
    }

    override fun addImage(image: Uri, postAfterAdd: Boolean) {
        xPostCreator.addImage(image) {
            if (postAfterAdd) SCreationTags.create(unitId, tags, false, 0) { SPost.instance(unitId, 0, NavigationAction.replace()) }
        }
    }

    override fun addImage(image: Bitmap, postAfterAdd: Boolean) {
        xPostCreator.addImage(image) {
            if (postAfterAdd) SCreationTags.create(unitId, tags, false, 0) { SPost.instance(unitId, 0, NavigationAction.replace()) }
        }
    }

}