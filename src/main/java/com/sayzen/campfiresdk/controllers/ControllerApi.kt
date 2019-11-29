package com.sayzen.campfiresdk.controllers

import android.graphics.Bitmap
import android.text.Html
import android.text.util.Linkify
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfo
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.requests.accounts.RAccountsLogout
import com.dzen.campfire.api.requests.accounts.RAccountsClearReports
import com.dzen.campfire.api.requests.accounts.RAccountsLoginSimple
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.requests.units.RUnitsAdminClearReports
import com.dzen.campfire.api.requests.units.RUnitsOnShare
import com.dzen.campfire.api.requests.units.RUnitsRemove
import com.dzen.campfire.api.requests.units.RUnitsReport
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.dzen.campfire.api_media.requests.RResourcesGetByTag
import com.sayzen.campfiresdk.R
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
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.image_loader.ImageLoaderTag
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.classes.items.ItemNullable
import com.sup.dev.java.libs.api_simple.ApiException
import com.sup.dev.java.libs.api_simple.client.Request
import com.sup.dev.java.libs.api_simple.client.TokenProvider
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception
import java.util.regex.Pattern


val api: API = API(
        ControllerCampfireSDK.projectKey,
        ControllerGoogleAuth.instanceTokenProvider(),
        if (ControllerCampfireSDK.IS_DEBUG) (if (ControllerCampfireSDK.IS_USE_SECOND_IP) ControllerCampfireSDK.SECOND_IP else "192.168.0.64") else API.IP,
        API.PORT_HTTPS,
        API.PORT_CERTIFICATE,
        { key, token -> ToolsStorage.put(key, token) },
        { ToolsStorage.getString(it, null) }
)

val apiMedia: APIMedia = APIMedia(
        ControllerCampfireSDK.projectKey,
        instanceTokenProvider(),
        APIMedia.IP,
        APIMedia.PORT_HTTPS,
        APIMedia.PORT_CERTIFICATE,
        { _, _ -> }, { "" }
)

fun instanceTokenProvider(): TokenProvider {
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
        if(moderationId > 0) {
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

    fun isAdmin(lvl: Long) = lvl >= API.LVL_ADMIN_MODER.lvl

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
    //  Links
    //

    fun linkToUser(name: String) = API.LINK_PROFILE_NAME + name
    fun linkToUser(id: Long) = API.LINK_PROFILE_ID + id
    fun linkToFandom(fandomId: Long) = API.LINK_FANDOM + fandomId
    fun linkToFandom(fandomId: Long, languageId: Long) = API.LINK_FANDOM + fandomId + "_" + languageId
    fun linkToPost(postId: Long) = API.LINK_POST + postId
    fun linkToReview(reviewId: Long) = API.LINK_REVIEW + reviewId
    fun linkToModeration(moderationId: Long) = API.LINK_MODERATION + moderationId
    fun linkToWikiFandomId(fandomId: Long) = API.LINK_WIKI_FANDOM + fandomId
    fun linkToWikiItemId(itemId: Long) = API.LINK_WIKI_SECTION + itemId
    fun linkToWikiArticle(itemId: Long) = API.LINK_WIKI_ARTICLE + itemId
    fun linkToFandomChat(chatId: Long) = API.LINK_FANDOM_CHAT + chatId
    fun linkToActivity(activityId: Long) = API.LINK_ACTIVITY + activityId
    fun linkToRubric(rubricId: Long) = API.LINK_RUBRIC + rubricId
    fun linkToSticker(id: Long) = API.LINK_STICKER + id
    fun linkToStickersPack(id: Long) = API.LINK_STICKERS_PACK + id
    fun linkToPostComment(parentPublicationId: Long, commentId: Long) = API.LINK_POST + parentPublicationId + "_" + commentId
    fun linkToModerationComment(parentPublicationId: Long, commentId: Long) = API.LINK_MODERATION + parentPublicationId + "_" + commentId
    fun linkToStickersComment(parentPublicationId: Long, commentId: Long) = API.LINK_STICKERS_PACK + parentPublicationId + "_" + commentId
    fun linkToChat(fandomId: Long) = API.LINK_CHAT + fandomId
    fun linkToChat(fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId
    fun linkToChatMessage(messageId: Long, fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId + "_" + messageId
    fun linkToConf(chatId: Long) = API.LINK_CONF + chatId
    fun linkToConfMessage(messageId: Long, chatId: Long) = API.LINK_CONF + chatId + "_" + messageId
    fun linkToEvent(eventId: Long) = API.LINK_EVENT + eventId
    fun linkToTag(tagId: Long) = API.LINK_TAG + tagId
    fun linkToComment(comment: PublicationComment) = linkToComment(comment.id, comment.parentPublicationType, comment.parentPublicationId)
    fun linkToComment(commentId: Long, publicationType: Long, publicationId: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_POST -> linkToPostComment(publicationId, commentId)
            API.PUBLICATION_TYPE_MODERATION -> linkToModerationComment(publicationId, commentId)
            API.PUBLICATION_TYPE_STICKERS_PACK -> linkToStickersComment(publicationId, commentId)
            else -> ""
        }
    }

    fun makeLinkable(vText: ViewTextLinkable, onReplace: () -> Unit = {}) {

        replaceLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        replaceLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        replaceLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        replaceLinkable(vText, API.LINK_SHORT_CONF_ID, API.LINK_CONF)
        replaceLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        replaceLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        replaceLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        replaceLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        replaceLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        replaceLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        replaceLinkable(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        replaceLinkable(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        replaceLinkable(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        replaceLinkable(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_FIREWORKS, API.LINK_BOX_WITH_FIREWORKS)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_AUTUMN, API.LINK_BOX_WITH_AUTUMN)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_WINTER, API.LINK_BOX_WITH_WINTER)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_FANDOM, API.LINK_WIKI_FANDOM)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_SECTION, API.LINK_WIKI_SECTION)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_ARTICLE, API.LINK_WIKI_ARTICLE)
        replaceLinkable(vText, API.LINK_SHORT_FANDOM_CHAT, API.LINK_FANDOM_CHAT)
        replaceLinkable(vText, API.LINK_SHORT_ACTIVITY, API.LINK_ACTIVITY)
        replaceLinkable(vText, API.LINK_SHORT_RUBRIC, API.LINK_RUBRIC)

        onReplace.invoke()
        makeTextHtml(vText)

        makeLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        makeLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        makeLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        makeLinkable(vText, API.LINK_SHORT_CONF_ID, API.LINK_CONF)
        makeLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        makeLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        makeLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        makeLinkable(vText, API.LINK_SHORT_WIKI_FANDOM, API.LINK_WIKI_FANDOM)
        makeLinkable(vText, API.LINK_SHORT_WIKI_SECTION, API.LINK_WIKI_SECTION)
        makeLinkable(vText, API.LINK_SHORT_WIKI_ARTICLE, API.LINK_WIKI_ARTICLE)
        makeLinkable(vText, API.LINK_SHORT_FANDOM_CHAT, API.LINK_FANDOM_CHAT)
        makeLinkable(vText, API.LINK_SHORT_ACTIVITY, API.LINK_ACTIVITY)
        makeLinkable(vText, API.LINK_SHORT_RUBRIC, API.LINK_RUBRIC)
        makeLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        makeLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        makeLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        makeLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        makeLinkableInner(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        makeLinkableInner(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_FIREWORKS, API.LINK_BOX_WITH_FIREWORKS)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_AUTUMN, API.LINK_BOX_WITH_AUTUMN)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_WINTER, API.LINK_BOX_WITH_WINTER)
        makeLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME, "([A-Za-z0-9#]+)")

        ToolsView.makeLinksClickable(vText)
    }

    private fun replaceLinkable(vText: TextView, short: String, link: String) {
        vText.text = vText.text.toString().replace(link, short)
    }

    private fun makeLinkableInner(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "([A-Za-z0-9_-]+)")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String, spec: String) {
        Linkify.addLinks(vText, Pattern.compile("$short$spec"), link, null, { _, url ->
            link + url.substring(short.length)
        })
    }


    //
    //  Share
    //

    fun sharePost(publicationId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { _, text ->
                    ToolsIntent.shareText(text + "\n\r" + linkToPost(publicationId))
                    ToolsThreads.main(10000) { RUnitsOnShare(publicationId).send(api) }
                }
                .asSheetShow()
    }

    fun shareReview(publicationId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { _, text ->
                    ToolsIntent.shareText(text + "\n\r" + linkToReview(publicationId))
                }
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun reportPublication(publicationId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_report, RUnitsReport(publicationId)) {
            ToolsToast.show(R.string.app_reported)
            EventBus.post(EventPublicationReportsAdd(publicationId))
        }
                .onApiError(RUnitsReport.E_ALREADY_EXIST) { ToolsToast.show(R.string.app_report_already_exist) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun removePublication(publicationId: Long, stringRes: Int, stringResGone: Int, onRemove: () -> Unit = {}) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_remove, RUnitsRemove(publicationId)) {
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
                RUnitsAdminClearReports(publicationId)
        ) {
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventPublicationReportsClear(publicationId))
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun clearReportsPublicationNow(publicationId: Long) {
        ApiRequestsSupporter.execute(RUnitsAdminClearReports(publicationId)) {
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