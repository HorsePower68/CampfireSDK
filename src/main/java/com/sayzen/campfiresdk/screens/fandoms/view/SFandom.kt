package com.sayzen.campfiresdk.screens.fandoms.view

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.*
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.fandom.*
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.publications.EventPostPinedFandom
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.screens.fandoms.CardAd
import com.sayzen.campfiresdk.screens.fandoms.CardQuest
import com.sayzen.campfiresdk.screens.fandoms.CardUpdate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.cards.CardSpoiler
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.*
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SFandom private constructor(
        val r: RFandomsGet.Response
) : Screen(R.layout.screen_fandom), PostList {

    companion object {

        var BLACK_LIST_ENABLED = false
        var SUBSCRIPE_ENABLED = false
        var INCLUDE_SPECIAL_CARDS = false

        fun instance(fandomId: Long, action: NavigationAction) {
            instance(fandomId, ControllerApi.getLanguageId(), action)
        }

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            var languageIdV = languageId
            if (languageIdV < 1L) languageIdV = ControllerApi.getLanguageId()
            ApiRequestsSupporter.executeInterstitial(action, RFandomsGet(fandomId, languageIdV, ControllerApi.getLanguageId())) { r -> SFandom(r) }
        }
    }

    private val eventBus = EventBus
            .subscribe(EventPostStatusChange::class) { onEventPostStatusChange(it) }
            .subscribe(EventFandomRemove::class) { Navigator.remove(this) }
            .subscribe(EventFandomCategoryChanged::class) { onEventFandomCategoryChanged(it) }
            .subscribe(EventFandomClose::class) { onEventFandomClose(it) }
            .subscribe(EventPostPinedFandom::class) { if (it.fandomId == xFandom.fandomId && it.languageId == xFandom.languageId) setPinnedPost(it.post) }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vImageTitle: ImageView = findViewById(R.id.vImageTitle)
    private val vAvatar: ImageView = findViewById(R.id.vAvatar)
    private val vFab: View = findViewById(R.id.vFab)
    private val vMore: ViewIcon = findViewById(R.id.vMore)

    private val adapter: RecyclerCardAdapterLoading<CardPublication, Publication>
    private val xFandom = XFandom(r.fandom) { update() }
    private val spoiler = CardSpoiler()
    private val cardFilters: CardFilters
    private val cardTitle = CardTitle(xFandom, r.fandom.category, r.subscriptionType, r.notifyImportant)
    private var cardPinnedPost: CardPost? = null

    private var cardUpdate: CardUpdate? = null
    private var cardQuest: CardQuest? = null

    init {
        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))

        adapter = RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler, false, false, true) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsGetAll()
                            .setOffset(cards.size)
                            .setPublicationTypes(ControllerSettings.getFandomFilters())
                            .setOrder(RUnitsGetAll.ORDER_NEW)
                            .setFandomId(xFandom.fandomId)
                            .setLanguageId(xFandom.languageId)
                            .setImportant(if (ControllerSettings.fandomFilterOnlyImportant) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_NONE)
                            .setIncludeZeroLanguages(true)
                            .setIncludeMultilingual(true)
                            .setIncludeModerationsBlocks(ControllerSettings.fandomFilterModerationsBlocks)
                            .setIncludeModerationsOther(ControllerSettings.fandomFilterModerations)
                            .onComplete { rr ->
                                onLoad.invoke(rr.publications)
                                afterPackLoaded()
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
                .setRetryMessage(R.string.error_network, R.string.app_retry)
                .setEmptyMessage(R.string.fandom_posts_empty)
                .setNotifyCount(5)


        cardFilters = CardFilters {
            if (cardPinnedPost != null) setPinnedPost(cardPinnedPost!!.xPublication.publication as PublicationPost)
            reload()
        }

        spoiler.setTitleExpanded(R.string.fandom_hide_details)
        spoiler.setTitle(R.string.fandom_show_details)
        spoiler.setTitleGravity(Gravity.CENTER)
        spoiler.setUseExpandedTitleArrow(true)
        spoiler.setUseExpandedArrow(false)
        spoiler.add(CardKarmaCof(r.fandom))
        spoiler.add(CardReview(xFandom, r.reviews))
        spoiler.add(CardDescription(xFandom, r.description, r.names))
        spoiler.add(CardGallery(xFandom, r.gallery))
        spoiler.add(CardLinks(xFandom, r.links))

        adapter.add(CardSpace(56))
        adapter.add(cardTitle)
        adapter.add(CardButtons(xFandom, r.chatsCount, r.tagsCount, r.subscribersCountLanguage, r.subscribersCountTotal, r.modersCount, r.subscriptionType != API.PUBLICATION_IMPORTANT_NONE, r.wikiCount, r.rubricsCount))
        adapter.add(spoiler)
        adapter.add(cardFilters)

        reload()

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        vFab.setOnClickListener { Navigator.to(SPostCreate(xFandom.fandomId, xFandom.languageId, xFandom.name, xFandom.imageId)) }

        if (ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_FANDOM_IMAGE)) vImageTitle.setOnClickListener { changeTitleImage() }
        if (ControllerApi.can(API.LVL_ADMIN_FANDOM_AVATAR)) vAvatar.setOnClickListener { changeImage() }

        vMore.setOnClickListener {
            WidgetMenu()
                    .add(R.string.app_copy_link) { _, _ ->
                        ToolsAndroid.setToClipboard(xFandom.linkTo())
                        ToolsToast.show(R.string.app_copied)
                    }
                    .add(R.string.app_copy_link_with_language) { _, _ ->
                        ToolsAndroid.setToClipboard(xFandom.linkToWithLanguage())
                        ToolsToast.show(R.string.app_copied)
                    }
                    .add(R.string.settings_black_list) { _, _ -> ControllerCampfireSDK.switchToBlackListFandom(xFandom.fandomId) }.condition(BLACK_LIST_ENABLED)
                    .add(R.string.profile_change_avatar) { _, _ -> changeImage() }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_AVATAR)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                    .add(R.string.fandoms_menu_change_category) { _, _ -> changeCategory() }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_CATEGORY)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                    .add(R.string.fandoms_menu_rename) { _, _ -> rename() }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_NAME)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                    .add(if (r.fandom.closed) R.string.app_open else R.string.app_close) { _, _ -> close() }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_CLOSE)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                    .add(R.string.app_remove) { _, _ -> remove() }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_REMOVE)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                    .asSheetShow()
        }

        if (r.fandom.closed) ToolsThreads.main(true) { ControllerClosedFandoms.showAlertIfNeed(this, xFandom.fandomId, false) }

        setPinnedPost(r.pinnedPost)

        if (INCLUDE_SPECIAL_CARDS) {
            cardUpdate = CardUpdate()
            cardQuest = CardQuest()
        }

        update()
    }

    private fun reload() {
        if (cardUpdate != null) adapter.remove(cardUpdate!!)
        if (cardQuest != null) adapter.remove(cardQuest!!)
        adapter.remove(CardAd::class)
        adapter.reloadBottom()
        ControllerCampfireSDK.putAd(vRecycler, adapter, adapter.indexOf(cardFilters) + 1)
    }

    private fun afterPackLoaded() {
        if (cardPinnedPost != null && ControllerSettings.getFandomFilters().contains(API.PUBLICATION_TYPE_POST))
            for (c in adapter.get(CardPost::class))
                if (c.xPublication.publication.id == cardPinnedPost!!.xPublication.publication.id && !(c.xPublication.publication as PublicationPost).isPined)
                    adapter.remove(c)


        if (cardQuest != null && !adapter.contains(cardQuest!!)) adapter.add(adapter.indexOf(cardFilters) + 1, cardQuest!!)
        if (cardUpdate != null && !adapter.contains(cardUpdate!!)) adapter.add(adapter.indexOf(cardFilters) + 1, cardUpdate!!)
        cardQuest?.show()
    }

    private fun setPinnedPost(post: PublicationPost?) {
        if (cardPinnedPost != null) adapter.remove(cardPinnedPost!!)
        if (post == null) {
            cardPinnedPost = null
        } else {
            for (c in adapter.get(CardPost::class)) if (c.xPublication.publication.id == post.id) adapter.remove(c)
            post.isPined = true
            cardPinnedPost = CardPost(vRecycler, post)
            if (ControllerSettings.getFandomFilters().contains(API.PUBLICATION_TYPE_POST)) {
                adapter.add(adapter.indexOf(cardFilters) + 1, cardPinnedPost!!)
            }
        }
    }

    private fun updateCategory() {
        adapter.remove(CardParams::class)
        if (CampfireConstants.getParamTitle(r.fandom.category, 1) != null) spoiler.add(CardParams(xFandom, r.params1, r.fandom.category, 1))
        if (CampfireConstants.getParamTitle(r.fandom.category, 2) != null) spoiler.add(CardParams(xFandom, r.params2, r.fandom.category, 2))
        if (CampfireConstants.getParamTitle(r.fandom.category, 3) != null) spoiler.add(CardParams(xFandom, r.params3, r.fandom.category, 3))
        if (CampfireConstants.getParamTitle(r.fandom.category, 4) != null) spoiler.add(CardParams(xFandom, r.params4, r.fandom.category, 4))
    }

    override fun contains(card: CardPost) = adapter.contains(card)

    private fun update() {
        xFandom.setView(vTitle, vAvatar, vImageTitle)
        cardTitle.update()
    }

    //
    //  Moderation
    //

    private fun changeImage() {
        WidgetChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE) { _, b, _, _, _, _ ->
                        WidgetField()
                                .setHint(R.string.moderation_widget_comment)
                                .setOnCancel(R.string.app_cancel)
                                .setMin(API.MODERATION_COMMENT_MIN_L)
                                .setMax(API.MODERATION_COMMENT_MAX_L)
                                .setOnEnter(R.string.app_change) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE), API.FANDOM_IMG_WEIGHT)
                                        ToolsThreads.main {
                                            ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsAdminChangeImage(xFandom.fandomId, image, comment)) { _ ->
                                                ImageLoaderId(xFandom.imageId).clear()
                                                EventBus.post(EventFandomChanged(xFandom.fandomId, xFandom.name))
                                                ToolsToast.show(R.string.app_done)
                                            }
                                        }
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    private fun changeTitleImage() {
        WidgetChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@thread
                        }

                        ToolsThreads.main {


                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSizeW = if (isGif) API.FANDOM_TITLE_IMG_GIF_W else API.FANDOM_TITLE_IMG_W
                            val cropSizeH = if (isGif) API.FANDOM_TITLE_IMG_GIF_H else API.FANDOM_TITLE_IMG_H

                            Navigator.to(SCrop(bitmap, cropSizeW, cropSizeH) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.FANDOM_TITLE_IMG_GIF_WEIGHT) {
                                                d.hide()
                                                ToolsToast.show(R.string.error_too_long_file)
                                            } else {
                                                ControllerApi.toBytes(b2, API.FANDOM_TITLE_IMG_WEIGHT, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, true) {
                                                    if (it == null) d.hide()
                                                    else changeTitleImageNow(d, it, bytesSized)
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.FANDOM_TITLE_IMG_WEIGHT, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H, true) {
                                        if (it == null) d.hide()
                                        else changeTitleImageNow(d, it, null)
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()
    }

    private fun changeTitleImageNow(dialog: Widget, image: ByteArray, imageGif: ByteArray?) {
        dialog.hide()
        ToolsThreads.main {
            WidgetField().setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_change) { ww, comment ->
                        ApiRequestsSupporter.executeEnabled(ww, RFandomsModerationChangeImageTitle(xFandom.fandomId, xFandom.languageId, image, imageGif, comment)) { r ->
                            ImageLoaderId(xFandom.imageTitleId).clear()
                            EventBus.post(EventFandomChanged(xFandom.fandomId, "", -1, r.imageId, r.imageGifId))
                            ToolsToast.show(R.string.app_done)
                        }
                    }
                    .asSheetShow()
        }
    }

    private fun changeCategory() {
        val wMenu = WidgetMenu()
        for (c in CampfireConstants.CATEGORIES) {
            if (c.index != r.fandom.category) {
                wMenu.add(c.name).onClick { _, _ ->
                    WidgetField().setHint(R.string.moderation_widget_comment)
                            .setOnCancel(R.string.app_cancel)
                            .setMin(API.MODERATION_COMMENT_MIN_L)
                            .setMax(API.MODERATION_COMMENT_MAX_L)
                            .addChecker(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                            .setOnEnter(R.string.app_change) { w, comment ->
                                ApiRequestsSupporter.executeEnabled(w, RFandomsAdminChangeCategory(xFandom.fandomId, c.index, comment)) {
                                    EventBus.post(EventFandomCategoryChanged(xFandom.fandomId, c.index))
                                    ToolsToast.show(R.string.app_done)
                                }
                            }
                            .asSheetShow()
                }
            }
        }
        wMenu.asSheetShow()
    }

    private fun rename() {
        WidgetFieldTwo()
                .setTitle(R.string.fandoms_menu_rename)
                .setOnCancel(R.string.app_cancel)
                .setText_1(xFandom.name)
                .setHint_1(R.string.app_name_s)
                .setLinesCount_1(1)
                .addChecker_1(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                .setMin_1(1)
                .setMax_1(API.FANDOM_NAME_MAX)
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_2(R.string.comments_hint)
                .addChecker_2(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(R.string.app_rename) { _, name, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(R.string.fandoms_menu_rename_confirm, R.string.fandoms_menu_rename, RFandomsAdminChangeName(xFandom.fandomId, name, comment)) {
                        EventBus.post(EventFandomChanged(xFandom.fandomId, name))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    private fun close() {
        val closed = r.fandom.closed
        WidgetField()
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .addChecker(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(if (closed) R.string.app_open else R.string.app_close) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(
                            if (closed) R.string.fandom_open_confirm else R.string.fandom_close_confirm,
                            if (closed) R.string.app_open else R.string.app_close,
                            RFandomsAdminClose(xFandom.fandomId, !closed, comment)) {
                        EventBus.post(EventFandomClose(xFandom.fandomId, !closed))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    private fun remove() {
        WidgetField()
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .addChecker(R.string.error_use_english) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(R.string.app_remove) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(R.string.fandom_remove_confirm, R.string.app_remove, RFandomsAdminRemove(xFandom.fandomId, comment)) {
                        EventBus.post(EventFandomRemove(xFandom.fandomId))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    //
    //  EventBus
    //

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        if (e.status == API.STATUS_PUBLIC) reload()
    }

    private fun onEventFandomCategoryChanged(e: EventFandomCategoryChanged) {
        if (e.fandomId == e.fandomId) {
            r.fandom.category = e.newCategory
            r.params1 = emptyArray()
            r.params2 = emptyArray()
            r.params3 = emptyArray()
            r.params4 = emptyArray()
            updateCategory()
        }
    }

    private fun onEventFandomClose(e: EventFandomClose) {
        if (e.fandomId == e.fandomId) {
            r.fandom.closed = e.closed
        }
    }

}

