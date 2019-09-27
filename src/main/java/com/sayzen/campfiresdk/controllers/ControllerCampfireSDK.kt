package com.sayzen.campfiresdk.controllers

import android.view.Gravity
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.Fandom
import com.dzen.campfire.api.models.units.UnitForum
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.api.requests.accounts.RAccountsChangeName
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListContains
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.other.about.SAboutApp
import com.sayzen.campfiresdk.screens.other.about.SAboutCreators
import com.sayzen.campfiresdk.screens.other.rules.SRulesModerators
import com.sayzen.campfiresdk.screens.other.rules.SRulesUser
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.devsupandroidgoogle.ControllerGoogleToken
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetCheckBoxes
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

object ControllerCampfireSDK {

    var SECOND_IP = ""
    var IS_USE_SECOND_IP = false
    var IS_DEBUG = false

    var ON_TO_FANDOM_CLICKED: (fandomId: Long, languageId: Long, action: NavigationAction) -> Unit = { fandomId, languageId, _ -> openLink(ControllerApi.linkToFandom(fandomId, languageId)) }
    var ON_TO_ACCOUNT_CLICKED: (accountId: Long, action: NavigationAction) -> Unit = { accountId, _ -> openLink(ControllerApi.linkToUser(accountId)) }
    var ON_TO_MODERATION_CLICKED: (moderationId: Long, commentId: Long, action: NavigationAction) -> Unit = { moderationId, commentId, _ -> openLink(ControllerApi.linkToModerationComment(moderationId, commentId)) }
    var ON_TO_POST_CLICKED: (postId: Long, commentId: Long, action: NavigationAction) -> Unit = { postId, commentId, _ -> openLink(ControllerApi.linkToPostComment(postId, commentId)) }
    var ON_TO_DRAFTS_CLICKED: (action: NavigationAction) -> Unit = { }
    var ON_TO_DRAFT_CLICKED: (postId: Long, action: NavigationAction) -> Unit = { _, _ -> }
    var ON_TO_POST_TAGS_CLICKED: (postId: Long, isMyUnit: Boolean, action: NavigationAction) -> Unit = { _, _, _ -> }
    var ON_TO_FORUM_CLICKED: (forumId: Long, commentId: Long, action: NavigationAction) -> Unit = { forumId, commentId, _ -> openLink(ControllerApi.linkToForumComment(forumId, commentId)) }
    var ON_TO_ACHIEVEMENT_CLICKED: (accountId: Long, accountName: String, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) -> Unit = { _, _, _, _, _ -> }
    var ON_CHANGE_FORUM_CLICKED: (unit: UnitForum) -> Unit = { }
    var ON_SCREEN_CHAT_START: () -> Unit = { }

    var SEARCH_FANDOM: (callback: (Fandom) -> Unit) -> Unit = { }

    var executorLinks: ExecutorLinks? = null
    var projectKey = ""

    fun init(
            projectKey: String,
            logoColored: Int,
            logoWhite: Int,
            notificationExecutor: ControllerNotifications.ExecutorNotification,
            linksExecutor: ExecutorLinks,
            onLoginFailed: () -> Unit
    ) {
        this.projectKey = projectKey
        executorLinks = linksExecutor
        ControllerApi.init()
        ControllerChats.init()
        ControllerNotifications.init(logoColored, logoWhite, notificationExecutor)
        ControllerGoogleToken.init("276237287601-6e9aoah4uivbjh6lnn1l9hna6taljd9u.apps.googleusercontent.com", onLoginFailed)

        SAlert.GLOBAL_SHOW_WHOOPS = false
    }

    fun onToFandomClicked(fandomId: Long, languageId: Long, action: NavigationAction) {
        ON_TO_FANDOM_CLICKED.invoke(fandomId, languageId, action)
    }

    fun onToAccountClicked(accountId: Long, action: NavigationAction) {
        ON_TO_ACCOUNT_CLICKED.invoke(accountId, action)
    }

    fun onToModerationClicked(moderationId: Long, commentId: Long, action: NavigationAction) {
        ON_TO_MODERATION_CLICKED.invoke(moderationId, commentId, action)
    }

    fun onToPostClicked(postId: Long, commentId: Long, action: NavigationAction) {
        ON_TO_POST_CLICKED.invoke(postId, commentId, action)
    }

    fun onToDraftClicked(postId: Long, action: NavigationAction) {
        ON_TO_DRAFT_CLICKED.invoke(postId, action)
    }

    fun onToDraftsClicked(action: NavigationAction) {
        ON_TO_DRAFTS_CLICKED.invoke(action)
    }

    fun onToPostTagsClicked(postId: Long, isMyUnit: Boolean, action: NavigationAction) {
        ON_TO_POST_TAGS_CLICKED.invoke(postId, isMyUnit, action)
    }

    fun onToForumClicked(forumId: Long, commentId: Long, action: NavigationAction) {
        ON_TO_FORUM_CLICKED.invoke(forumId, commentId, action)
    }

    fun onToAchievementClicked(accountId: Long, accountName: String, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) {
        ON_TO_ACHIEVEMENT_CLICKED.invoke(accountId, accountName, achievementIndex, toPrev, action)
    }

    //
    //  Links
    //

    fun parseLink(link: String): Boolean {
        try {

            val t = link.substring(API.DOMEN.length)
            val s1 = t.split("-")
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            when (linkV) {
                API.LINK_TAG_ABOUT -> Navigator.to(SAboutApp())
                API.LINK_TAG_RULES_USER -> Navigator.to(SRulesUser())
                API.LINK_TAG_RULES_MODER -> Navigator.to(SRulesModerators())
                API.LINK_TAG_CREATORS -> Navigator.to(SAboutCreators())
                API.LINK_TAG_BOX_WITH_FIREWIRKS -> {
                    ControllerScreenAnimations.fireworks()
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_FIREWORKS.index).send(api) }
                }
                API.LINK_TAG_BOX_WITH_SUMMER -> ControllerScreenAnimations.summer()
                API.LINK_TAG_BOX_WITH_AUTUMN -> ControllerScreenAnimations.autumn()
                API.LINK_TAG_STICKER -> SStickersView.instanceBySticker(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_STICKERS_PACK -> {
                    if (params.size == 1) SStickersView.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) Navigator.to(SComments(params[0].toLong(), params[1].toLong()))
                }

                else -> return executorLinks?.parseLink(linkV, params) ?: false

            }
            return true

        } catch (e: Throwable) {
            err(e)
            return false
        }
    }

    fun openLink(link: String) {
        if (parseLink(link)) return
        WidgetAlert()
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_open) { ToolsIntent.openLink(link) }
                .setText(R.string.message_link)
                .setTextGravity(Gravity.CENTER)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .asSheetShow()
    }


    //
    //  Actions
    //

    fun changeLogin() {
        WidgetField()
                .setTitle(R.string.profile_change_name)
                .addChecker(R.string.profile_change_name_error) { s -> ToolsText.checkStringChars(s, API.ACCOUNT_LOGIN_CHARS) }
                .setMin(API.ACCOUNT_NAME_L_MIN)
                .setMax(API.ACCOUNT_NAME_L_MAX)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_change) { dialog, name ->
                    ApiRequestsSupporter.executeEnabled(dialog, RAccountsChangeName(name)) {
                        ControllerApi.account.name = name
                        EventBus.post(EventAccountChanged(ControllerApi.account.id, ControllerApi.account.name))
                        dialog.hide()
                    }.onApiError(API.ERROR_ACCOUNT_IS_BANED) {
                        ToolsToast.show(R.string.error_login_taken)
                    }
                }
                .asSheetShow()
    }


    fun switchToBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RFandomsBlackListContains(fandomId)){r->
            if(r.contains) removeFromBlackListFandom(fandomId)
            else addToBlackListFandom(fandomId)
        }
    }

    fun addToBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.fandoms_menu_black_list_add_confirm, R.string.app_add, RFandomsBlackListAdd(fandomId)) {
            EventBus.post(EventFandomBlackListChange(fandomId, true))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun removeFromBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.fandoms_menu_black_list_remove_confirm, R.string.app_remove, RFandomsBlackListRemove(fandomId)) {
            EventBus.post(EventFandomBlackListChange(fandomId, false))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun addToBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.profile_black_list_add_confirm, R.string.app_add, RAccountsBlackListAdd(accountId)) {
            EventBus.post(EventAccountAddToBlackList(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun removeFromBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.profile_black_list_remove_confirm, R.string.app_remove, RAccountsBlackListRemove(accountId)) {
            EventBus.post(EventAccountRemoveFromBlackList(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun shareCampfireApp() {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { _, text ->
                    ToolsIntent.shareText("$text\n\rhttps://play.google.com/store/apps/details?id=com.dzen.campfire")
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_APP_SHARE.index).send(api) }
                }
                .asSheetShow()
    }

    fun createLanguageMenu(selectedId: Long, exclude: Array<Long> = emptyArray(), onClick: (Long) -> Unit): WidgetMenu {
        val w = WidgetMenu()
        val code = ControllerApi.getLanguageCode()

        for (i in API.LANGUAGES)
            if (i.code == code || i.code == "en")
                if (!exclude.contains(i.id))
                    w.add(i.name) { _, _ -> onClick.invoke(i.id) }.backgroundRes(R.color.focus) { i.id == selectedId }
        w.group(" ")

        for (i in API.LANGUAGES)
            if (i.code != code && i.code != "en")
                if (!exclude.contains(i.id))
                    w.add(i.name) { _, _ -> onClick.invoke(i.id) }.backgroundRes(R.color.focus) { i.id == selectedId }

        return w
    }


    fun createLanguageCheckMenu(languages: ArrayList<Long>): WidgetCheckBoxes {
        val w = WidgetCheckBoxes()
        val code = ControllerApi.getLanguageCode()
        for (i in API.LANGUAGES) {
            if (i.code == code || i.code == "en")
                w.add(i.name).checked(languages.contains(i.id)).onChange { _, _, b ->
                    if (b) {
                        if (!languages.contains(i.id)) languages.add(i.id)
                    } else {
                        languages.remove(i.id)
                    }
                }
        }
        w.group(" ")
        for (i in API.LANGUAGES) {
            if (i.code != code && i.code != "en")
                w.add(i.name).checked(languages.contains(i.id)).onChange { _, _, b ->
                    if (b) {
                        if (!languages.contains(i.id)) languages.add(i.id)
                    } else {
                        languages.remove(i.id)
                    }
                }
        }
        return w
    }

    interface ExecutorLinks {

        fun parseLink(link: String, params: List<String>): Boolean

    }


}