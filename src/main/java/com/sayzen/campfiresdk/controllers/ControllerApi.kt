package com.sayzen.campfiresdk.controllers

import android.graphics.Bitmap
import android.text.Html
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.ApiInfo
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfo
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.requests.accounts.RAccountsLogout
import com.dzen.campfire.api.requests.accounts.RAccountsClearReports
import com.dzen.campfire.api.requests.accounts.RAccountsLoginSimple
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.requests.publications.RPublicationsAdminClearReports
import com.dzen.campfire.api.requests.publications.RPublicationsOnShare
import com.dzen.campfire.api.requests.publications.RPublicationsRemove
import com.dzen.campfire.api.requests.publications.RPublicationsReport
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.dzen.campfire.api_media.requests.RResourcesGetByTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountCurrentChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountReportsCleared
import com.sayzen.campfiresdk.models.events.project.EventApiVersionChanged
import com.sayzen.campfiresdk.models.events.project.EventSalientTimeChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sup.dev.java.libs.text_format.TextFormatter
import com.sayzen.devsupandroidgoogle.ControllerGoogleAuth
import com.sup.dev.android.app.SupAndroid
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.image_loader.ImageLoaderTag
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.classes.items.ItemNullable
import com.dzen.campfire.api.tools.client.Request
import com.dzen.campfire.api.tools.client.TokenProvider
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

val api: API = API(
        ControllerCampfireSDK.projectKey,
        instanceTokenProvider(),
        if (ControllerCampfireSDK.IS_DEBUG) (if (ControllerCampfireSDK.IS_USE_SECOND_IP) ControllerCampfireSDK.SECOND_IP else "192.168.0.64") else API.IP,
        API.PORT_HTTPS,
        API.PORT_CERTIFICATE,
        { key, token -> ToolsStorage.put(key, token) },
        { ToolsStorage.getString(it, null) }
)

val apiMedia: APIMedia = APIMedia(
        ControllerCampfireSDK.projectKey,
        instanceTokenProviderMedia(),
        APIMedia.IP,
        APIMedia.PORT_HTTPS,
        APIMedia.PORT_CERTIFICATE,
        { _, _ -> }, { "" }
)

fun instanceTokenProvider(): TokenProvider {
    return object : TokenProvider {

        override fun getToken(callbackSource: (String?) -> Unit) {
            ControllerGoogleAuth.getToken {
                ControllerGoogleAuth.tokenPostExecutor.invoke(it) { token ->
                    callbackSource.invoke(token)
                }
            }
        }

        override fun clearToken() {
            ControllerGoogleAuth.clearToken()
        }

        override fun onLoginFailed() {
            ControllerGoogleAuth.onLoginFailed()
        }
    }
}


fun instanceTokenProviderMedia(): TokenProvider {
    return object : TokenProvider {

        override fun getToken(callbackSource: (String?) -> Unit) {
            callbackSource.invoke("")
        }

        override fun clearToken() {
            ControllerApi.setCurrentAccount(Account())
        }

        override fun onLoginFailed() {
            ControllerApi.setCurrentAccount(Account())
        }
    }
}

object ControllerApi {

    var account = Account()
    var hasSubscribes = false
    var protoadmins = emptyArray<Long>()
    private var serverTimeDelta = 0L
    private var fandomsKarmaCounts: Array<Item3<Long, Long, Long>?>? = null
    private var version = ""
    private var supported = ""
    private var apiInfo = ApiInfo()

    internal fun init() {
        ApiRequestsSupporter.init(api)

        ImageLoaderId.loader = { imageId ->
            val item = ItemNullable<ByteArray>(null)
            if (imageId > 0)
                RResourcesGet(imageId)
                        .onComplete { r -> item.a = r.bytes }
                        .sendNow(apiMedia)
            item.a
        }
        ImageLoaderTag.loader = { imageTag ->
            val item = ItemNullable<ByteArray>(null)
            if (imageTag.isNotEmpty())
                RResourcesGetByTag(imageTag)
                        .onComplete { r -> item.a = r.bytes }
                        .onError { err(it) }
                        .sendNow(apiMedia)
            item.a
        }
    }

    fun getLanguageId(): Long {
        val code = ControllerSettings.appLanguage
        var englishId = 1L
        for (i in API.LANGUAGES) {
            if (i.code == code) return i.id
            if (i.code == "en") englishId = i.id
        }
        return englishId
    }

    fun setApiInfo(apiInfo: ApiInfo) {
        this.apiInfo = apiInfo
    }

    fun getApiInfo() = apiInfo

    fun isOldVersion() = version.isNotEmpty() && version != API.VERSION

    fun isUnsupportedVersion(): Boolean {
        try {
            val versionSupportedS = if (supported.contains('b')) supported.substring(0, supported.length - 1) else supported
            val versionS = if (version.contains('b')) version.substring(0, version.length - 1) else version
            val versionSupported = versionSupportedS.toDouble()
            val version = versionS.toDouble()
            return version < versionSupported
        } catch (e: Exception) {
            err(e)
            return true
        }
    }

    fun setVersion(version: String, supported: String) {
        this.version = version
        this.supported = supported
        EventBus.post(EventApiVersionChanged())
    }

    fun getLanguage() = getLanguage(getLanguageCode())

    fun getLanguageCode() = getLanguage(ToolsAndroid.getLanguageCode().toLowerCase()).code

    fun getLanguage(code: String) = API.getLanguage(code)

    fun getLanguage(languageId: Long) = API.getLanguage(languageId)

    fun getIconForLanguage(languageId: Long): Int {
        return when (languageId) {
            API.LANGUAGE_EN -> R.drawable.icon_flag_en
            API.LANGUAGE_RU -> R.drawable.icon_flag_ru
            API.LANGUAGE_PT -> R.drawable.icon_flag_pt
            API.LANGUAGE_UK -> R.drawable.icon_flag_uk
            API.LANGUAGE_DE -> R.drawable.icon_flag_de
            API.LANGUAGE_IT -> R.drawable.icon_flag_it
            API.LANGUAGE_PL -> R.drawable.icon_flag_pl
            API.LANGUAGE_FR -> R.drawable.icon_flag_fr
            else -> R.drawable.icon_flag_world
        }
    }

    @Suppress("DEPRECATION")
    fun makeTextHtml(vText: TextView) {
        val text = vText.text.toString().replace("<", "&#60;")
        val s_1 = TextFormatter(text).parse()
        val s_2 = s_1.replace("\n", "<br />")
        vText.text = Html.fromHtml(s_2)
    }

    fun toBytes(bitmap: Bitmap?, size: Int, w: Int = 0, h: Int = 0, weakSizesMode: Boolean = false, callback: (ByteArray?) -> Unit) {
        if (ToolsAndroid.isMainThread()) {
            ToolsThreads.thread { callback.invoke(toBytesNow(bitmap, size, w, h, weakSizesMode)) }
        } else {
            callback.invoke(toBytesNow(bitmap, size, w, h, weakSizesMode))
        }
    }

    fun toBytesNow(bitmap: Bitmap?, size: Int, w: Int = 0, h: Int = 0, weakSizesMode: Boolean = false): ByteArray? {
        if (bitmap == null) {
            ToolsToast.show(R.string.error_cant_load_image)
            return null
        }
        val bt = if (w > 0 && h > 0) {
            if (weakSizesMode) ToolsBitmap.keepMaxSizes(bitmap, w, h)
            else ToolsBitmap.resize(bitmap, w, h)
        } else {
            bitmap
        }
        val bytes = ToolsBitmap.toBytes(bt, size)
        if (bytes == null) ToolsToast.show(R.string.error_cant_load_image)
        return bytes
    }

    fun isCurrentAccount(accountId: Long): Boolean {
        return account.id == accountId
    }

    fun setCurrentAccount(account: Account, hasSubscribes: Boolean = false, protoadmins: Array<Long> = emptyArray()) {
        this.account = account
        this.protoadmins = protoadmins
        ToolsStorage.put("account json", account.json(true, Json()))

        setHasFandomSubscribes(hasSubscribes)

        val jProtoadmins = JsonArray()
        for (i in protoadmins) jProtoadmins.put(i)
        ToolsStorage.put("protoadmins", jProtoadmins)

        ControllerPolling.clear()
        EventBus.post(EventAccountCurrentChanged())
    }

    fun setHasFandomSubscribes(hasSubscribes: Boolean) {
        this.hasSubscribes = hasSubscribes
        ToolsStorage.put("hasSubscribes", hasSubscribes)
    }

    fun getLastAccount(): Account {
        val json = ToolsStorage.getJson("account json") ?: Json()
        val account = Account()
        account.json(false, json)
        return account
    }

    fun getLastHasSubscribes(): Boolean {
        return ToolsStorage.getBoolean("hasSubscribes", false)
    }

    fun getLastProtadmins(): Array<Long> {
        return ToolsStorage.getJsonArray("protoadmins", JsonArray()).getLongs()
    }

    fun enableAutoRegistration() {
        ControllerGoogleAuth.tokenPostExecutor = { token, callback ->
            if (token == null) {
                callback.invoke(token)
            } else {
                loginWithRegistration(token) {
                    callback.invoke(token)
                }
            }
        }
    }


    fun loginWithRegistration(onFinish: () -> Unit) {
        loginWithRegistration(null, onFinish)
    }

    fun loginWithRegistration(loginToken: String?, onFinish: () -> Unit) {
        if (account.id != 0L) {
            onFinish.invoke()
            return
        }
        val dialog = ToolsView.showProgressDialog()
        login(loginToken) {
            if (account.id == 0L) {
                val r = RAccountsRegistration(getLanguage(getLanguageCode()).id, null)
                        .onFinish {
                            dialog.hide()
                            login(loginToken) {
                                onFinish.invoke()
                            }
                        }
                r.loginToken = loginToken
                r.send(api)
            } else {
                dialog.hide()
                onFinish.invoke()
            }
        }
    }

    fun loginIfTokenExist() {
        val token = api.getAccessToken()
        if (token != null && token.isNotEmpty()) {
            login(null) {

            }
        }
    }

    private fun login(loginToken: String?, onFinish: () -> Unit) {
        val r = RAccountsLoginSimple(ControllerNotifications.token)
                .onComplete {
                    account = it.account ?: Account()
                    setServerTime(it.serverTime)
                }
                .onFinish { onFinish.invoke() }
        r.loginToken = loginToken
        r.send(api)
    }

    fun currentTime() = System.currentTimeMillis() + serverTimeDelta

    fun setServerTime(serverTime: Long) {
        serverTimeDelta = serverTime - System.currentTimeMillis()
    }

    fun setFandomsKarma(fandomsIds: Array<Long>, languagesIds: Array<Long>, karmaCounts: Array<Long>) {
        fandomsKarmaCounts = arrayOfNulls(karmaCounts.size)
        for (i in fandomsKarmaCounts!!.indices) (fandomsKarmaCounts as Array)[i] =
                Item3(fandomsIds[i], languagesIds[i], karmaCounts[i])
    }

    fun logout(onComplete: () -> Unit) {
        RAccountsLogout()
                .onFinish {
                    ControllerNotifications.hideAll()
                    ControllerGoogleAuth.logout {
                        api.clearTokens()
                        ControllerChats.clearMessagesCount()
                        ControllerSettings.clear()
                        ControllerNotifications.setNewNotifications(emptyArray())
                        setCurrentAccount(Account())
                        this.fandomsKarmaCounts = null
                        serverTimeDelta = 0
                        onComplete.invoke()
                    }
                }
                .send(api)
    }

    fun showBlockedScreen(ex: ApiException, action: NavigationAction, text: Int) {
        val moderationId = if (ex.params.isNotEmpty() && ToolsMapper.isLongCastable(ex.params[0])) ex.params[0].toLong() else 0L
        SAlert.showMessage(text, if (moderationId > 0) R.string.app_details else R.string.app_back, SupAndroid.IMG_ERROR_GONE, action) { screen ->
            if (moderationId > 0) {
                Navigator.remove(screen)
                SModerationView.instance(moderationId, action)
            } else {
                Navigator.remove(screen)
            }
        }
    }

    fun showBlockedDialog(ex: ApiException, text: Int) {
        val moderationId = if (ex.params.isNotEmpty() && ToolsMapper.isLongCastable(ex.params[0])) ex.params[0].toLong() else 0L
        val w = WidgetAlert()
        w.setText(text)
        w.setOnCancel(R.string.app_ok)
        if (moderationId > 0) {
            w.setOnEnter(R.string.app_details) {
                SModerationView.instance(moderationId, Navigator.TO)
            }
        }
        w.asSheetShow()
    }

    //
    //  Account
    //

    fun getKarmaCount(fandomId: Long, languageId: Long): Long {
        if (fandomsKarmaCounts == null) return 0
        for (i in fandomsKarmaCounts!!)
            if (i!!.a1 == fandomId && i.a2 == languageId) {
                return i.a3
            }
        return 0
    }

    fun isModerator(lvl: Long) = lvl >= API.LVL_MODERATOR_BLOCK.lvl

    fun isModerator(lvl: Long, karma30: Long) = isModerator(lvl) && karma30 >= API.LVL_MODERATOR_BLOCK.karmaCount

    fun isAdmin(lvl: Long) = lvl >= API.LVL_ADMIN_MODER.lvl

    fun isAdmin(lvl: Long, karma30: Long) = isAdmin(lvl) && karma30 >= API.LVL_ADMIN_MODER.karmaCount

    fun isProtoadmin(accountId: Long, lvl: Long) = protoadmins.contains(accountId) || lvl >= API.LVL_PROTOADMIN.lvl

    fun isBot(accountName: String) = accountName.startsWith("Bot#")

    fun isModerator(account: Account) = isModerator(account.lvl)

    fun isAdmin(account: Account) = isAdmin(account.lvl)

    fun isProtoadmin(account: Account) = isProtoadmin(account.id, account.lvl)

    fun isModerator() = isModerator(account)

    fun isAdmin() = isAdmin(account)

    fun isProtoadmin() = isProtoadmin(account)

    fun can(adminInfo: LvlInfoUser): Boolean {
        if (protoadmins.contains(account.id)) return true
        return account.lvl >= adminInfo.lvl && account.karma30 >= adminInfo.karmaCount
    }

    fun can(adminInfo: LvlInfoAdmin): Boolean {
        if (protoadmins.contains(account.id)) return true
        return account.lvl >= adminInfo.lvl && account.karma30 >= adminInfo.karmaCount
    }

    fun can(fandomId: Long, languageId: Long, moderateInfo: LvlInfo): Boolean {
        if (protoadmins.contains(account.id)) return true
        if (can(API.LVL_ADMIN_MODER)) return true
        return account.lvl >= moderateInfo.lvl && getKarmaCount(fandomId, languageId) >= moderateInfo.karmaCount
    }

    //
    //  Share
    //

    fun sharePost(publicationId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { _, text ->
                    ToolsIntent.shareText(text + "\n\r" + ControllerLinks.linkToPost(publicationId))
                    ToolsThreads.main(10000) { RPublicationsOnShare(publicationId).send(api) }
                }
                .asSheetShow()
    }

    fun shareReview(publicationId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { _, text ->
                    ToolsIntent.shareText(text + "\n\r" + ControllerLinks.linkToReview(publicationId))
                }
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun reportPublication(publicationId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_report, RPublicationsReport(publicationId)) {
            ToolsToast.show(R.string.app_reported)
            EventBus.post(EventPublicationReportsAdd(publicationId))
        }
                .onApiError(RPublicationsReport.E_ALREADY_EXIST) { ToolsToast.show(R.string.app_report_already_exist) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun removePublication(publicationId: Long, stringRes: Int, stringResGone: Int, onRemove: () -> Unit = {}) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_remove, RPublicationsRemove(publicationId)) {
            EventBus.post(EventPublicationRemove(publicationId))
            ToolsToast.show(R.string.app_removed)
            onRemove.invoke()
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun clearReportsPublication(publicationId: Long, publicationType: Long) {
        when (publicationType) {
            API.PUBLICATION_TYPE_CHAT_MESSAGE -> clearReportsPublication(publicationId, R.string.chat_clear_reports_confirm, R.string.chat_error_gone)
            API.PUBLICATION_TYPE_POST -> clearReportsPublication(publicationId, R.string.post_clear_reports_confirm, R.string.post_error_gone)
            API.PUBLICATION_TYPE_COMMENT -> clearReportsPublication(publicationId, R.string.comment_clear_reports_confirm, R.string.comment_error_gone)
            API.PUBLICATION_TYPE_REVIEW -> clearReportsPublication(publicationId, R.string.review_clear_reports_confirm, R.string.review_error_gone)
            API.PUBLICATION_TYPE_STICKERS_PACK -> clearReportsPublication(publicationId, R.string.stickers_packs_clear_reports_confirm, R.string.stickers_packs_error_gone)
        }
    }

    private fun clearReportsPublication(publicationId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(
                stringRes,
                R.string.app_clear,
                RPublicationsAdminClearReports(publicationId)
        ) {
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventPublicationReportsClear(publicationId))
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun clearReportsPublicationNow(publicationId: Long) {
        ApiRequestsSupporter.execute(RPublicationsAdminClearReports(publicationId)) {
            EventBus.post(EventPublicationReportsClear(publicationId))
        }
    }

    fun clearUserReports(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.app_clear_reports_confirm, R.string.app_clear, RAccountsClearReports(accountId)) {
            EventBus.post(EventAccountReportsCleared(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun clearUserReportsNow(accountId: Long) {
        ApiRequestsSupporter.execute(RAccountsClearReports(accountId)) {
            EventBus.post(EventAccountReportsCleared(accountId))
        }
    }

    fun <K : Request.Response> moderation(title: Int, action: Int, instanceRequest: (String) -> Request<K>, onComplete: (K) -> Unit) {
        WidgetField()
                .setTitle(title)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(action) { w, comment ->
                    ApiRequestsSupporter.executeProgressDialog(instanceRequest(comment)) { r ->
                        onComplete.invoke(r)
                    }
                }
                .asSheetShow()


    }

    fun showSalientDialog() {
        WidgetMenu()
                .add(R.string.settings_notifications_none) { _, _ -> setSalientTime(0) }
                .add(R.string.time_hour) { _, _ -> setSalientTime(1000L * 60 * 60) }
                .add(R.string.time_2_hour) { _, _ -> setSalientTime(1000L * 60 * 60 * 2) }
                .add(R.string.time_8_hour) { _, _ -> setSalientTime(1000L * 60 * 60 * 8) }
                .add(R.string.time_day) { _, _ -> setSalientTime(1000L * 60 * 60 * 24) }
                .asSheetShow()
    }

    private fun setSalientTime(time: Long) {
        ToolsToast.show(R.string.app_done)
        ControllerSettings.salientTime = System.currentTimeMillis() + time
        EventBus.post(EventSalientTimeChanged())
    }


}