package com.sayzen.campfiresdk.screens.account.profile

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.requests.accounts.*
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.PostList
import com.sup.dev.android.tools.ToolsGif
import com.sayzen.campfiresdk.models.widgets.WidgetAdminBlock
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.account.*
import com.sayzen.campfiresdk.models.events.units.EventPostPinedProfile
import com.sayzen.campfiresdk.screens.administation.SAdministrationDeepBlocked
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace
import com.sup.dev.android.views.widgets.*
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SAccount private constructor(
        val r: RAccountsGetProfile.Response
) : Screen(R.layout.screen_account), PostList {

    companion object {

        fun instance(name: String, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RAccountsGetProfile(0, name)) { r ->
                SAccount(r)
            }
        }

        fun instance(accountId: Long, action: NavigationAction) {
            if (accountId == 0L) {
                val screen = SAlert(
                        ToolsResources.s(R.string.app_anonymous),
                        ToolsResources.s(R.string.profile_anonymous_text),
                        ToolsResources.s(R.string.app_back), null)
                screen.onAction = {Navigator.remove(screen)}
                screen.isNavigationAllowed = false
                screen.isNavigationAnimation = false
                Navigator.to(screen)
            } else ApiRequestsSupporter.executeInterstitial(action, RAccountsGetProfile(accountId, "")) { r ->
                SAccount(r)
            }
        }
    }

    private val eventBus = EventBus
            .subscribe(EventAccountAddToBlackList::class) { if (it.accountId == xAccount.accountId) r.inBlackList = true }
            .subscribe(EventAccountRemoveFromBlackList::class) { if (it.accountId == xAccount.accountId) r.inBlackList = false }
            .subscribe(EventPostPinedProfile::class) { if (it.accountId == xAccount.accountId) setPinnedPost(it.post) }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMore: View = findViewById(R.id.vMore)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vAvatar: ImageView = findViewById(R.id.vAvatar)
    val xAccount = XAccount(r.account, 0, r.titleImageId, r.titleImageGifId) { update() }
    private val adapter: RecyclerCardAdapterLoading<CardUnit, Unit>
    private val cardInfo: CardInfo
    private val cardBio: CardBio
    private val cardFilters: CardFilters
    private var cardPinnedPost: CardPost? = null

    init {
        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))

        vMore.setOnClickListener { showDialog() }
        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.addItemDecoration(DecoratorVerticalSpace(8))

        vAvatar.setOnClickListener {
            if (ControllerApi.isCurrentAccount(r.account.id)) onChangeAvatarClicked()
            else Navigator.to(SImageView(xAccount.imageId))
        }
        vAvatar.setOnLongClickListener {
            Navigator.to(SImageView(xAccount.imageId))
            true
        }

        vImage.setOnClickListener {
            if (ControllerApi.isCurrentAccount(r.account.id)) onChangeTitleImageClicked()
            else if (xAccount.titleImageGifId > 0) Navigator.to(SImageView(xAccount.titleImageGifId))
            else Navigator.to(SImageView(xAccount.titleImageId))
        }
        vImage.setOnLongClickListener {
            if (xAccount.titleImageGifId > 0) Navigator.to(SImageView(xAccount.titleImageGifId))
            else Navigator.to(SImageView(xAccount.titleImageId))
            true
        }


        vTitle.text = r.account.name


        adapter = RecyclerCardAdapterLoading<CardUnit, Unit>(CardUnit::class) { unit -> CardUnit.instance(unit, vRecycler, true, isShowFullInfo = true) }
                .setBottomLoader { onLoad, cards ->
                    val r = RUnitsGetAll()
                            .setAccountId(r.account.id)
                            .setOffset(cards.size)
                            .setUnitTypes(ControllerSettings.getProfileFilters())
                            .onComplete { r ->
                                onLoad.invoke(r.units)
                                afterPackLoaded()
                            }
                            .onNetworkError { onLoad.invoke(null) }
                    r.tokenRequired = true
                    r.send(api)
                }
                .setRetryMessage(R.string.error_network, R.string.app_retry)
                .setNotifyCount(5)
                .setEmptyMessage(if (r.account.id == ControllerApi.account.id) R.string.profile_empty_my else R.string.profile_empty_other)

        vRecycler.adapter = adapter

        cardInfo = CardInfo(xAccount, r.account.karma30, r.account.dateCreate, r.account.sex,
                r.banDate, r.isFollow, r.followsCount, r.followersCount, r.moderateFandomsCount, r.status,
                r.ratesCount, r.bansCount, r.warnsCount, r.note, r.fandomsCount, r.blackFandomsCount, r.blackAccountCount, r.stickersCount)
        cardFilters = CardFilters {
            if (cardPinnedPost != null) setPinnedPost(cardPinnedPost!!.xUnit.unit as UnitPost)
            adapter.reloadBottom()
        }

        cardBio = CardBio(r.account.id, r.account.sex, r.age, r.description, r.links)

        adapter.add(CardSpace(56))
        adapter.add(cardInfo)
        adapter.add(cardBio)
        adapter.add(cardFilters)
        adapter.loadBottom()

        setPinnedPost(r.pinnedPost)

        update()
    }

    private fun afterPackLoaded() {
        if (cardPinnedPost != null && ControllerSettings.getProfileFilters().contains(API.UNIT_TYPE_POST))
            for (c in adapter.get(CardPost::class))
                if (c.xUnit.unit.id == cardPinnedPost!!.xUnit.unit.id && !(c.xUnit.unit as UnitPost).isPined)
                    adapter.remove(c)
    }

    private fun setPinnedPost(post: UnitPost?) {
        if (cardPinnedPost != null) adapter.remove(cardPinnedPost!!)
        if (post == null) {
            cardPinnedPost = null
        } else {
            for (c in adapter.get(CardPost::class)) if (c.xUnit.unit.id == post.id) adapter.remove(c)
            post.isPined = true
            cardPinnedPost = CardPost(vRecycler, post)
            cardPinnedPost?.showFandom = true
            if (ControllerSettings.getProfileFilters().contains(API.UNIT_TYPE_POST)) {
                adapter.add(adapter.indexOf(cardFilters) + 1, cardPinnedPost!!)
            }
        }
    }

    override fun contains(card: CardPost) = adapter.contains(card)

    private fun update() {
        xAccount.setView(vTitle, vAvatar, vImage)
        cardInfo.update()
    }

    private fun showDialog() {
        val w = WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToUser(xAccount.name))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.account.id != xAccount.accountId)
                .add(R.string.app_report) { _, _ -> onReportClicked() }
                .add(R.string.app_note) { _, _ -> WidgetNote(this) }
                .add(if (r.inBlackList) R.string.profile_black_list_remove else R.string.profile_black_list_add) { _, _ -> if (r.inBlackList) ControllerCampfireSDK.removeFromBlackListUser(xAccount.accountId) else ControllerCampfireSDK.addToBlackListUser(xAccount.accountId) }
                .add(R.string.profile_remove_avatar) { _, _ -> onAdminRemoveAvatarClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_IMAGE))
                .add(R.string.profile_remove_name) { _, _ -> onAdminRemoveNameClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_NAME))
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearUserReports(xAccount.accountId) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_BAN))
                .add(R.string.app_punish) { _, _ -> onAdminPunishClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_BAN))
                .add(R.string.admin_change_name) { _, _ -> adminChangeName() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_CHANGE_NAME))
                .add(R.string.profile_remove_title_image) { _, _ -> onAdminRemoveTitleImageClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_IMAGE))
                .add(R.string.profile_remove_status) { _, _ -> removeStatus() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_STATUS))
                .reverseGroupCondition()
                .add(R.string.profile_change_avatar) { _, _ -> onChangeAvatarClicked() }
                .add(R.string.profile_change_name) { _, _ -> ControllerCampfireSDK.changeLogin() }.condition(ControllerApi.account.name.contains("#"))
                .add(R.string.profile_change_title_image) { _, _ -> onChangeTitleImageClicked() }.condition(ControllerApi.can(API.LVL_CAN_CHANGE_PROFILE_IMAGE))
                .clearGroupCondition()
                .add("Заблокированные публикации") { _, _ -> Navigator.to(SAdministrationDeepBlocked(xAccount.accountId)) }.condition(ControllerApi.can(API.LVL_PROTOADMIN)).backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add("Протоадминская авторизация") { _, _ -> protoadminAutorization() }.condition(ControllerApi.can(API.LVL_PROTOADMIN)).backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add("Поменять местами с другим аккаунтом") { _, _ -> protoadminTranslateAccount() }.condition(ControllerApi.can(API.LVL_PROTOADMIN)).backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add("Пересчитать достижения") { _, _ -> protoadminAchievementsRecount() }.condition(ControllerApi.can(API.LVL_PROTOADMIN)).backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add("Пересчитать карму") { _, _ -> protoadminKarmaRecount() }.condition(ControllerApi.can(API.LVL_PROTOADMIN)).backgroundRes(R.color.orange_700).textColorRes(R.color.white)

        w.asSheetShow()
    }

    fun onReportClicked() {
        WidgetAlert()
                .setText(R.string.profile_report_confirm)
                .setAutoHideOnEnter(false)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_report
                ) { dialog ->
                    RAccountsReport(xAccount.accountId)
                            .onComplete {
                                ToolsToast.show(R.string.profile_report_reported)
                                dialog.hide()
                            }
                            .onApiError(RAccountsReport.E_EXIST) {
                                ToolsToast.show(R.string.profile_report_already_exist)
                                dialog.hide()
                            }
                            .onNetworkError {
                                ToolsToast.show(R.string.error_network)
                                dialog.setEnabled(true)
                            }
                            .send(api)
                }
                .asSheetShow()
    }

    private fun onChangeAvatarClicked() {
        WidgetChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ControllerApi.can(API.LVL_CAN_CHANGE_AVATAR_GIF) && ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.ACCOUNT_IMG_SIDE_GIF else API.ACCOUNT_IMG_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.ACCOUNT_IMG_SIDE_GIF, API.ACCOUNT_IMG_SIDE_GIF, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.ACCOUNT_IMG_WEIGHT_GIF) {
                                                d.hide()
                                                ToolsToast.show(R.string.error_too_long_file)
                                            } else {
                                                changeAvatarNow(d, bytesSized)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.ACCOUNT_IMG_WEIGHT, API.ACCOUNT_IMG_SIDE, API.ACCOUNT_IMG_SIDE, true) {
                                        if (it == null) d.hide()
                                        else changeAvatarNow(d, it)
                                    }
                                }
                            })

                        }


                    }


                }
                .asSheetShow()
    }

    private fun changeAvatarNow(dialog: WidgetProgressTransparent, bytes: ByteArray) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RAccountsChangeAvatar(bytes)) { _ ->
            ToolsImagesLoader.clear(xAccount.imageId)
            EventBus.post(EventAccountChanged(xAccount.accountId, xAccount.name, xAccount.imageId))
        }
    }

    private fun onChangeTitleImageClicked() {
        WidgetChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(R.string.error_cant_load_image)
                            return@thread
                        }

                        ToolsThreads.main {


                            val isGif = ControllerApi.can(API.LVL_CAN_CHANGE_AVATAR_GIF) && ToolsBytes.isGif(bytes)
                            val cropSizeW = if (isGif) API.ACCOUNT_TITLE_IMG_GIF_W else API.ACCOUNT_TITLE_IMG_W
                            val cropSizeH = if (isGif) API.ACCOUNT_TITLE_IMG_GIF_H else API.ACCOUNT_TITLE_IMG_H

                            Navigator.to(SCrop(bitmap, cropSizeW, cropSizeH) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.ACCOUNT_TITLE_IMG_GIF_WEIGHT) {
                                                d.hide()
                                                ToolsToast.show(R.string.error_too_long_file)
                                            } else {
                                                ControllerApi.toBytes(b2, API.ACCOUNT_TITLE_IMG_WEIGHT, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, true) {
                                                    if (it == null) d.hide()
                                                    else changeTitleImageNow(d, it, bytesSized)
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.ACCOUNT_TITLE_IMG_WEIGHT, API.ACCOUNT_TITLE_IMG_W, API.ACCOUNT_TITLE_IMG_H, true) {
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

    private fun changeTitleImageNow(d: WidgetProgressTransparent, bytes: ByteArray, bytesGif: ByteArray?) {
        ApiRequestsSupporter.executeProgressDialog(d, RAccountsChangeTitleImage(bytes, bytesGif)) { r ->
            ToolsImagesLoader.clear(xAccount.titleImageId)
            ToolsImagesLoader.clear(xAccount.titleImageGifId)
            EventBus.post(EventAccountChanged(xAccount.accountId, xAccount.name, xAccount.imageId, r.imageId, r.imageGifId))
        }
    }

    fun onAdminRemoveAvatarClicked() {
        WidgetField()
                .setHint(R.string.profile_remove_avatar)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveAvatar(xAccount.accountId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        ToolsImagesLoader.clear(xAccount.imageId)
                        EventBus.post(EventAccountChanged(xAccount.accountId, xAccount.name, xAccount.imageId))
                    }
                }
                .asSheetShow()
    }

    fun onAdminRemoveNameClicked() {
        WidgetField()
                .setHint(R.string.profile_remove_name)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveName(xAccount.accountId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountChanged(xAccount.accountId, "User_" + xAccount.accountId, xAccount.imageId, 0, 0))
                    }
                }
                .asSheetShow()

    }

    fun adminChangeName() {
        WidgetFieldTwo()
                .setTitle(R.string.profile_change_name)
                .setOnCancel(R.string.app_cancel)
                .setText_1(xAccount.name)
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_1(R.string.app_name_s)
                .setLinesCount_1(1)
                .addChecker_1(R.string.profile_change_name_error) { s -> ToolsText.checkStringChars(s, API.ACCOUNT_LOGIN_CHARS) }
                .setMin_1(API.ACCOUNT_NAME_L_MIN)
                .setMax_1(API.ACCOUNT_NAME_L_MAX)
                .setHint_2(R.string.comments_hint)
                .setOnEnter(R.string.app_change) { dialog, name, comment ->
                    ApiRequestsSupporter.executeEnabled(dialog, RAccountsAdminChangeName(xAccount.accountId, name, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountChanged(xAccount.accountId, name))
                    }.onApiError(RAccountsAdminChangeName.E_LOGIN_NOT_ENABLED) {
                        ToolsToast.show(R.string.error_login_taken)
                    }
                }
                .asSheetShow()
    }

    fun onAdminPunishClicked() {
        WidgetAdminBlock.show(xAccount.accountId, xAccount.name)
    }

    fun onAdminRemoveTitleImageClicked() {
        WidgetField()
                .setTitle(R.string.profile_remove_title_image)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveTitleImage(xAccount.accountId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountChanged(xAccount.accountId, xAccount.name, xAccount.imageId, 0, 0))
                    }
                }
                .asSheetShow()


    }

    private fun removeStatus() {
        WidgetField()
                .setTitle(R.string.profile_remove_status)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsAdminStatusRemove(xAccount.accountId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventAccountStatusChanged(xAccount.accountId, ""))
                    }
                }
                .asSheetShow()
    }

    private fun protoadminAutorization() {
        ApiRequestsSupporter.executeEnabledConfirm("Авторизировться именем протоадмина?", "Авторизироваться", RAccountsProtoadminAutorization(xAccount.accountId)) {
            ToolsToast.show(R.string.app_done)
            ControllerCampfireSDK.logoutNow()
        }
    }

    private fun protoadminTranslateAccount() {
        Navigator.to(SAccountSearch {
            ApiRequestsSupporter.executeEnabledConfirm("Перенести аккаунт на ${it.name}?", "Перенести", RAccountsProtoadminReplaceGoogleId(xAccount.accountId, it.id)) {
                ToolsToast.show(R.string.app_done)
            }
        })

    }

    private fun protoadminAchievementsRecount() {
        ApiRequestsSupporter.executeEnabledConfirm("Пересчитать достижения", "Пересчитать", RAccountsAchievementsRecount(xAccount.accountId)) {
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun protoadminKarmaRecount() {
        ApiRequestsSupporter.executeEnabledConfirm("Пересчитать карму", "Пересчитать", RAccountsKarmaRecount(xAccount.accountId)) {
            ToolsToast.show(R.string.app_done)
        }
    }

}
