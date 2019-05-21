package com.sayzen.campfiresdk.controllers


import android.graphics.Bitmap
import android.text.Html
import android.view.Gravity
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.Language
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfo
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.requests.accounts.RAccountsLogin
import com.dzen.campfire.api.requests.accounts.RAccountsLoginSimple
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.requests.units.RUnitsRemove
import com.dzen.campfire.api.requests.units.RUnitsReport
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.support.TextParser
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.devsupandroidgoogle.ControllerToken
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.classes.items.ItemNullable
import com.sup.dev.java.libs.api_simple.client.TokenProvider
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

val api: API = API(
    ControllerToken.instanceTokenProvider(),
    API.IP,
    API.PORT_HTTPS,
    API.PORT_CERTIFICATE,
    { key, token -> ToolsStorage.put(key, token) },
    {
        val token = ToolsStorage.getString(it, null)
        if (ControllerApi.account.id == 0L) ControllerApi.login(
            token
        ) { }
        token
    }
)

val apiMedia: APIMedia = APIMedia(
    instanceTokenProvider(),
    APIMedia.IP,
    APIMedia.PORT_HTTPS,
    APIMedia.PORT_CERTIFICATE,
    { key, token -> }, { "cats" }
)

fun instanceTokenProvider(): TokenProvider {
    return object : TokenProvider {

        override fun getToken(callbackSource: (String?) -> Unit) {
            callbackSource.invoke("cats")
        }

        override fun clearToken() {
        }

        override fun onLoginFailed() {
        }
    }
}

object ControllerApi {

    var account = Account()
    var loginInProgress = false
    private var serverTimeDelta = 0L
    private var fandomsKarmaCounts: Array<Item3<Long, Long, Long>?>? = null

    fun init() {
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

    fun getLanguage() =
        getLanguageByCode(getLanguageCode())

    fun getLanguageCode() = getLanguageByCode(ToolsAndroid.getLanguageCode()).code

    fun getLanguageByCode(code: String): Language {
        for (i in API.LANGUAGES) if (i.code == code.toLowerCase()) return i
        return API.LANGUAGES[0]
    }

    fun makeTextHtml(vText: TextView) {
        val text = vText.text.toString().replace("<", "&#60;")
        vText.text = Html.fromHtml(TextParser(text).parse().replace("\n", "<br />"))
    }

    fun toBytes(bitmap: Bitmap?, size: Int, w: Int = 0, h: Int = 0, weakSizesMode: Boolean = false, callback: (ByteArray?) -> Unit) {
        if (ToolsAndroid.isMainThread()) {
            ToolsThreads.thread {
                callback.invoke(
                    toBytesNow(
                        bitmap,
                        size,
                        w,
                        h,
                        weakSizesMode
                    )
                )
            }
        } else {
            callback.invoke(
                toBytesNow(
                    bitmap,
                    size,
                    w,
                    h,
                    weakSizesMode
                )
            )
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

    fun reportUnit(unitId: Long, stringRes: Int, stringResGone: Int) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes,
            R.string.app_report, RUnitsReport(unitId)) { r ->
            ToolsToast.show(R.string.app_reported)
        }
            .onApiError(RUnitsReport.E_ALREADY_EXIST) { ToolsToast.show(R.string.app_report_already_exist) }
            .onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun removeUnit(unitId: Long, stringRes: Int, stringResGone: Int, onRemove: () -> kotlin.Unit = {}) {
        ApiRequestsSupporter.executeEnabledConfirm(stringRes,
            R.string.app_remove, RUnitsRemove(unitId)) { r ->
            EventBus.post(EventUnitRemove(unitId))
            ToolsToast.show(R.string.app_removed)
            onRemove.invoke()
        }.onApiError(API.ERROR_GONE) { ToolsToast.show(stringResGone) }
    }

    fun makeLinkable(vText: ViewTextLinkable) {
        makeTextHtml(vText)
        ToolsView.makeLinksClickable(vText)
    }

    fun isCurrentAccount(accountId: Long): Boolean {
        return account.id == accountId
    }

    fun clear() {
        this.account = Account()
        this.fandomsKarmaCounts = null
        //  setServerTimeDelta(0)
    }

    fun login(token: String?, onFinish: () -> Unit) {
        if (loginInProgress || token == null) {
            onFinish.invoke()
            return
        }
        loginInProgress = true
        RAccountsLoginSimple(ControllerNotifications.token)
            .onComplete {
                account = it.account ?: Account()
                if (account.id == 0L) {
                    RAccountsRegistration(getLanguageByCode(getLanguageCode()).id, null)
                        .onComplete {
                            account.id = it.accountId
                        }
                        .onFinish {
                            loginInProgress = false
                            onFinish.invoke()
                        }
                        .send(api)
                } else {
                    onFinish.invoke()
                    loginInProgress = false
                }
            }
            .onError {
                loginInProgress = false
                onFinish.invoke()
            }
            .send(api)
    }

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

    fun startCampForAccount(accountId: Long) {
        openLink(API.LINK_PROFILE_ID + accountId)
    }

    fun currentTime() = System.currentTimeMillis() + serverTimeDelta

    fun setParams(r: RAccountsLogin.Response) {
        serverTimeDelta = r.serverTime - System.currentTimeMillis()
        fandomsKarmaCounts = arrayOfNulls(r.karmaCounts.size)
        for (i in fandomsKarmaCounts!!.indices) (fandomsKarmaCounts as Array)[i] = Item3(r.fandomsIds[i], r.languagesIds[i], r.karmaCounts[i])
    }

    fun getKarmaCount(fandomId: Long, languageId: Long): Long {
        if (fandomsKarmaCounts == null) return 0
        for (i in fandomsKarmaCounts!!)
            if (i!!.a1 == fandomId && i.a2 == languageId) {
                return i.a3
            }
        return 0
    }

    fun openLink(link: String) {
        WidgetAlert()
            .setOnCancel(R.string.app_cancel)
            .setOnEnter(R.string.app_open) { ToolsIntent.openLink(link) }
            .setText(R.string.message_link)
            .setTextGravity(Gravity.CENTER)
            .setTitleImage(R.drawable.ic_security_white_48dp)
            .setTitleImageBackgroundRes(R.color.blue_700)
            .asSheetShow()
    }


    fun isModerator(accountId: Long, lvl: Long) = !isProtoadmin(accountId, lvl) && !isAdmin(accountId, lvl) && lvl >= API.LVL_MODERATOR_BLOCK.lvl

    fun isAdmin(accountId: Long, lvl: Long) = !isProtoadmin(accountId, lvl) && lvl >= API.LVL_ADMIN_MODER.lvl

    fun isProtoadmin(accountId: Long, lvl: Long) = accountId == 1L || lvl >= API.LVL_PROTOADMIN.lvl

    fun isBot(accountName: String) = accountName.startsWith("Bot#")

    fun isModerator(account: Account) = isModerator(account.id, account.lvl)

    fun isAdmin(account: Account) = isAdmin(account.id, account.lvl)

    fun isProtoadmin(account: Account) = isProtoadmin(account.id, account.lvl)

    fun isModerator() = isModerator(account)

    fun isAdmin() = isAdmin(account)

    fun isProtoadmin() = isProtoadmin(account)

    //
    //  Links
    //

    fun linkToUser(name: String) = API.LINK_PROFILE_NAME + name
    fun linkToFandom(fandomId: Long) = API.LINK_FANDOM + fandomId
    fun linkToFandom(fandomId: Long, languageId: Long) = API.LINK_FANDOM + fandomId + "_" + languageId
    fun linkToPost(postId: Long) = API.LINK_POST + postId
    fun linkToReview(reviewId: Long) = API.LINK_REVIEW + reviewId
    fun linkToModeration(moderationId: Long) = API.LINK_MODERATION + moderationId
    fun linkToForum(forumId: Long) = API.LINK_FORUM + forumId
    fun linkToPostComment(parentUnitId: Long, commentId: Long) = API.LINK_POST + parentUnitId + "_" + commentId
    fun linkToModerationComment(parentUnitId: Long, commentId: Long) = API.LINK_MODERATION + parentUnitId + "_" + commentId
    fun linkToForumComment(parentUnitId: Long, commentId: Long) = API.LINK_FORUM + parentUnitId + "_" + commentId
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
            else -> ""
        }
    }

}