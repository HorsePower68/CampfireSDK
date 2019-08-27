package com.sayzen.campfiresdk.controllers


import android.graphics.Bitmap
import android.text.Html
import android.text.util.Linkify
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.Language
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfo
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.requests.accounts.RAccountsClearReports
import com.dzen.campfire.api.requests.accounts.RAccountsLoginSimple
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.requests.units.RUnitsAdminClearReports
import com.dzen.campfire.api.requests.units.RUnitsOnShare
import com.dzen.campfire.api.requests.units.RUnitsRemove
import com.dzen.campfire.api.requests.units.RUnitsReport
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountReportsCleared
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitReportsClear
import com.sayzen.campfiresdk.models.support.TextParser
import com.sayzen.devsupandroidgoogle.ControllerGoogleToken
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.classes.items.ItemNullable
import com.sup.dev.java.libs.api_simple.client.TokenProvider
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsThreads
import java.util.regex.Pattern

val api: API = API(
        ControllerGoogleToken.instanceTokenProvider(),
        if (ControllerCampfireSDK.IS_DEBUG) (if (ControllerCampfireSDK.IS_USE_SECOND_IP) ControllerCampfireSDK.SECOND_IP else "192.168.0.64") else API.IP,
        API.PORT_HTTPS,
        API.PORT_CERTIFICATE,
        { key, token -> ToolsStorage.put(key, token) },
        { ToolsStorage.getString(it, null) }
)

val apiMedia: APIMedia = APIMedia(
        instanceTokenProvider(),
        APIMedia.IP,
        APIMedia.PORT_HTTPS,
        APIMedia.PORT_CERTIFICATE,
        { key, token -> }, { "" }
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
    private var serverTimeDelta = 0L
    private var fandomsKarmaCounts: Array<Item3<Long, Long, Long>?>? = null

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

    fun getLanguage() = getLanguage(getLanguageCode())

    fun getLanguageCode() = getLanguage(ToolsAndroid.getLanguageCode()).code

    fun getLanguage(code: String): Language {
        for (i in API.LANGUAGES) if (i.code == code.toLowerCase()) return i
        return API.LANGUAGES[0]
    }

    fun getLanguage(languageId: Long): Language {
        for (l in API.LANGUAGES) if (l.id == languageId) return l
        return API.LANGUAGES[0]
    }


    fun makeTextHtml(vText: TextView) {
        val text = vText.text.toString().replace("<", "&#60;")
        vText.text = Html.fromHtml(TextParser(text).parse().replace("\n", "<br />"))
    }

    fun toBytes(
            bitmap: Bitmap?,
            size: Int,
            w: Int = 0,
            h: Int = 0,
            weakSizesMode: Boolean = false,
            callback: (ByteArray?) -> Unit
    ) {
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

    fun setCurrentAccount(account: Account) {
        this.account = account
        ToolsStorage.put("account json", account.json(true, Json()))
        ControllerPolling.clear()
    }

    fun getLastAccount():Account{
        val json = ToolsStorage.getJson("account json")?:Json()
        val account = Account()
        account.json(false, json)
        return account
    }

    fun enableAutoRegistration() {
        ControllerGoogleToken.tokenPostExecutor = { token, callback ->
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
        login(loginToken) {
            if (account.id == 0L) {
                val r = RAccountsRegistration(getLanguage(getLanguageCode()).id, null)
                        .onFinish {
                            login(loginToken) {
                                onFinish.invoke()
                            }
                        }
                r.loginToken = loginToken
                r.send(api)
            } else {
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
        ControllerNotifications.hideAll()
        ControllerGoogleToken.logout {
            api.clearTokens()
            ControllerChats.clearMessagesCount()
            ControllerNotifications.setNewNotifications(emptyArray())
            setCurrentAccount(Account())
            this.fandomsKarmaCounts = null
            serverTimeDelta = 0
            onComplete.invoke()
        }
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

    fun isModerator(accountId: Long, lvl: Long) =
            !isProtoadmin(accountId, lvl) && !isAdmin(accountId, lvl) && lvl >= API.LVL_MODERATOR_BLOCK.lvl

    fun isAdmin(accountId: Long, lvl: Long) = !isProtoadmin(accountId, lvl) && lvl >= API.LVL_ADMIN_MODER.lvl

    fun isProtoadmin(accountId: Long, lvl: Long) = accountId == 1L || lvl >= API.LVL_PROTOADMIN.lvl

    fun isBot(accountName: String) = accountName.startsWith("Bot#")

    fun isModerator(account: Account) = isModerator(account.id, account.lvl)

    fun isAdmin(account: Account) = isAdmin(account.id, account.lvl)

    fun isProtoadmin(account: Account) = isProtoadmin(account.id, account.lvl)

    fun isModerator() = isModerator(account)

    fun isAdmin() = isAdmin(account)

    fun isProtoadmin() = isProtoadmin(account)

    fun can(adminInfo: LvlInfoUser): Boolean {
        if (account.id == 1L) return true
        return account.lvl >= adminInfo.lvl && account.karma30 >= adminInfo.karmaCount
    }

    fun can(adminInfo: LvlInfoAdmin): Boolean {
        if (account.id == 1L) return true
        return account.lvl >= adminInfo.lvl && account.karma30 >= adminInfo.karmaCount
    }

    fun can(fandomId: Long, languageId: Long, moderateInfo: LvlInfo): Boolean {
        if (account.id == 1L) return true
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
    fun linkToForum(forumId: Long) = API.LINK_FORUM + forumId
    fun linkToSticker(id: Long) = API.LINK_STICKER + id
    fun linkToStickersPack(id: Long) = API.LINK_STICKERS_PACK + id
    fun linkToPostComment(parentUnitId: Long, commentId: Long) = API.LINK_POST + parentUnitId + "_" + commentId
    fun linkToModerationComment(parentUnitId: Long, commentId: Long) = API.LINK_MODERATION + parentUnitId + "_" + commentId
    fun linkToForumComment(parentUnitId: Long, commentId: Long) = API.LINK_FORUM + parentUnitId + "_" + commentId
    fun linkToStickersComment(parentUnitId: Long, commentId: Long) = API.LINK_STICKERS_PACK + parentUnitId + "_" + commentId
    fun linkToChat(fandomId: Long) = API.LINK_CHAT + fandomId
    fun linkToChat(fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId
    fun linkToChatMessage(messageId: Long, fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId + "_" + messageId
    fun linkToEvent(eventId: Long) = API.LINK_EVENT + eventId
    fun linkToTag(tagId: Long) = API.LINK_TAG + tagId
    fun linkToComment(comment: UnitComment) = linkToComment(comment.id, comment.parentUnitType, comment.parentUnitId)
    fun linkToComment(commentId: Long, unitType: Long, unitId: Long): String {
        return when (unitType) {
            API.UNIT_TYPE_POST -> linkToPostComment(unitId, commentId)
            API.UNIT_TYPE_MODERATION -> linkToModerationComment(unitId, commentId)
            API.UNIT_TYPE_FORUM -> linkToForumComment(unitId, commentId)
            API.UNIT_TYPE_STICKERS_PACK -> linkToStickersComment(unitId, commentId)
            else -> ""
        }
    }

    fun makeLinkable(vText: ViewTextLinkable, onReplace: () -> Unit = {}) {
        replaceLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        replaceLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        replaceLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        replaceLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        replaceLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        replaceLinkable(vText, API.LINK_SHORT_FORUM_ID, API.LINK_FORUM)
        replaceLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        replaceLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        replaceLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        replaceLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        replaceLinkable(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        replaceLinkable(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        replaceLinkable(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        replaceLinkable(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_FIREWIRKS, API.LINK_BOX_WITH_FIREWORKS)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME)

        onReplace.invoke()
        makeTextHtml(vText)

        makeLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        makeLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        makeLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        makeLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        makeLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        makeLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        makeLinkable(vText, API.LINK_SHORT_FORUM_ID, API.LINK_FORUM)
        makeLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        makeLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        makeLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        makeLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        makeLinkableInner(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        makeLinkableInner(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_FIREWIRKS, API.LINK_BOX_WITH_FIREWORKS)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
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
        Linkify.addLinks(vText, Pattern.compile("$short$spec"), link, null, { match, url ->
            link + url.substring(short.length)
        })
    }


    //
    //  Share
    //

    fun sharePost(unitId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { w, text ->
                    ToolsIntent.shareText(text + "\n\r" + linkToPost(unitId))
                    ToolsThreads.main(10000) { RUnitsOnShare(unitId).send(api) }
                }
                .asSheetShow()
    }

    fun shareReview(unitId: Long) {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { w, text ->
                    ToolsIntent.shareText(text + "\n\r" + linkToReview(unitId))
                }
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun reportUnit(unitId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_report, RUnitsReport(unitId)) { r ->
            ToolsToast.show(R.string.app_reported)
            EventBus.post(EventUnitReportsAdd(unitId))
        }
                .onApiError(RUnitsReport.E_ALREADY_EXIST) { ToolsToast.show(R.string.app_report_already_exist) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun removeUnit(unitId: Long, stringRes: Int, stringResGone: Int, onRemove: () -> kotlin.Unit = {}) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes, R.string.app_remove, RUnitsRemove(unitId)) { r ->
            EventBus.post(EventUnitRemove(unitId))
            ToolsToast.show(R.string.app_removed)
            onRemove.invoke()
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun clearReportsUnit(unitId: Long, unitType: Long) {
        when(unitType){
            API.UNIT_TYPE_CHAT_MESSAGE -> clearReportsUnit(unitId,  R.string.chat_clear_reports_confirm, R.string.chat_error_gone)
            API.UNIT_TYPE_POST -> clearReportsUnit(unitId,  R.string.post_clear_reports_confirm, R.string.post_error_gone)
            API.UNIT_TYPE_COMMENT -> clearReportsUnit(unitId,  R.string.comment_clear_reports_confirm, R.string.comment_error_gone)
            API.UNIT_TYPE_REVIEW -> clearReportsUnit(unitId,  R.string.review_clear_reports_confirm, R.string.review_error_gone)
        }
    }

    private fun clearReportsUnit(unitId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(
                stringRes,
                R.string.app_clear,
                RUnitsAdminClearReports(unitId)
        ) { r ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventUnitReportsClear(unitId))
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun clearReportsUnitNow(unitId: Long) {
        ApiRequestsSupporter.execute(RUnitsAdminClearReports(unitId)) { r ->
            EventBus.post(EventUnitReportsClear(unitId))
        }
    }

    fun clearUserReports(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.app_clear_reports_confirm, R.string.app_clear, RAccountsClearReports(accountId)) { r ->
            EventBus.post(EventAccountReportsCleared(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun clearUserReportsNow(accountId: Long) {
        ApiRequestsSupporter.execute(RAccountsClearReports(accountId)) { r ->
            EventBus.post(EventAccountReportsCleared(accountId))
        }
    }

}