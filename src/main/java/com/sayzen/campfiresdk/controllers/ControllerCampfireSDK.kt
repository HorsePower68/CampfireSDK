package com.sayzen.campfiresdk.controllers

import android.app.Activity
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.Fandom
import com.dzen.campfire.api.models.units.UnitForum
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.api.requests.accounts.RAccountsChangeName
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.forums.view.SForumView
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.reviews.SReviews
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.other.SAboutApp
import com.sayzen.campfiresdk.screens.other.SAboutCreators
import com.sayzen.campfiresdk.screens.other.SRulesModerators
import com.sayzen.campfiresdk.screens.other.SRulesUser
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sayzen.devsupandroidgoogle.ControllerGoogleToken
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
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

    var ON_TO_FANDOM_CLICKED: (fandomId: Long, languageId: Long, action: NavigationAction) -> Unit = { fandomId, languageId, action -> }
    var ON_TO_ACCOUNT_CLICKED: (accountId: Long, action: NavigationAction) -> Unit = { accountId, action -> }
    var ON_TO_MODERATION_CLICKED: (moderationId: Long, commentId: Long, action: NavigationAction) -> Unit = { moderationId, commentId, action -> }
    var ON_TO_POST_CLICKED: (postId: Long, commentId: Long, action: NavigationAction) -> Unit = { postId, commentId, action -> }
    var ON_TO_DRAFTS_CLICKED: (action: NavigationAction) -> Unit = { action -> }
    var ON_TO_DRAFT_CLICKED: (postId: Long, action: NavigationAction) -> Unit = { postId, action -> }
    var ON_TO_POST_TAGS_CLICKED: (postId: Long, isMyUnit: Boolean, action: NavigationAction) -> Unit = { postId, isMyUnit, action -> }
    var ON_TO_FORUM_CLICKED: (forumId: Long, commentId: Long, action: NavigationAction) -> Unit = { forumId, commentId, action -> }
    var ON_TO_ACHIEVEMENT_CLICKED: (accountId: Long, accountName: String, accountLvl: Long, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) -> Unit = { accountId, accountName, accountLvl, achievementIndex, toPrev, action -> }
    var ON_CHANGE_FORUM_CLICKED: (unit: UnitForum) -> Unit = { unit -> }
    var ON_SCREEN_CHAT_START: () -> Unit = { }

    var SEARCH_FANDOM: (callback: (Fandom) -> Unit) -> Unit = { }

    var executorLinks: ExecutorLinks? = null

    fun init(
            activityClass: Class<out Activity>,
            logoColored: Int,
            logoWhite: Int,
            notificationExecutor: ControllerNotifications.ExecutorNotification,
            linksExecutor: ExecutorLinks,
            onLoginFailed: () -> Unit
    ) {
        executorLinks = linksExecutor
        ControllerApi.init()
        ControllerChats.init()
        ControllerNotifications.init(activityClass, logoColored, logoWhite, notificationExecutor)
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

    fun onToAchievementClicked(accountId: Long, accountName: String, accountLvl: Long, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) {
        ON_TO_ACHIEVEMENT_CLICKED.invoke(accountId, accountName, accountLvl, achievementIndex, toPrev, action)
    }

    //
    //  Links
    //


    fun startCampForAccount(accountId: Long) {
        openLink(API.LINK_PROFILE_ID + accountId)
    }

    fun parseLink(link: String): Boolean {
        try {

            val t = link.substring(API.DOMEN.length)
            val s1 = t.split("-")
            val link = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            when (link) {
                API.LINK_TAG_ABOUT -> Navigator.to(SAboutApp())
                API.LINK_TAG_RULES_USER -> Navigator.to(SRulesUser())
                API.LINK_TAG_RULES_MODER -> Navigator.to(SRulesModerators())
                API.LINK_TAG_CREATORS -> Navigator.to(SAboutCreators())
                API.LINK_TAG_BOX_WITH_FIREWIRKS -> {
                    ControllerScreenAnimations.fireworks()
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_FIREWORKS.index).send(api) }
                }
                API.LINK_TAG_BOX_WITH_SUMMER -> ControllerScreenAnimations.summer()
                API.LINK_TAG_STICKER -> SStickersView.instanceBySticker(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_STICKERS_PACK -> {
                    if (params.size == 1) SStickersView.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) Navigator.to(SComments(params[0].toLong(), params[1].toLong()))
                }

                else -> return executorLinks?.parseLink(link, params) ?: false

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
                    ApiRequestsSupporter.executeEnabled(dialog, RAccountsChangeName(name)) { r ->
                        ControllerApi.account.name = name
                        EventBus.post(EventAccountChanged(ControllerApi.account.id, ControllerApi.account.name))
                        dialog.hide()
                    }.onApiError(API.ERROR_ACCOUNT_IS_BANED) {
                        ToolsToast.show(R.string.error_login_taken)
                    }
                }
                .asSheetShow()
    }


    fun addToBlackListFandom(fandomId: Long) {
        WidgetAlert()
                .setText(R.string.fandoms_menu_black_list_add_confirm)
                .setOnEnter(R.string.app_add) {
                    ControllerSettings.feedIgnoreFandoms = ToolsCollections.add(fandomId, ControllerSettings.feedIgnoreFandoms)
                    EventBus.post(EventFandomBlackListChange(fandomId, true))
                    ToolsToast.show(R.string.app_done)
                }
                .setOnCancel(R.string.app_cancel)
                .asSheetShow()
    }


    fun removeFromBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.profile_black_list_remove_confirm, R.string.app_remove, RAccountsBlackListRemove(accountId)) {
            EventBus.post(EventAccountRemoveFromBlackList(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun addToBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.profile_black_list_add_confirm, R.string.app_add, RAccountsBlackListAdd(accountId)) {
            EventBus.post(EventAccountAddToBlackList(accountId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun removeFromBlackListFandom(fandomId: Long) {
        WidgetAlert()
                .setText(R.string.fandoms_menu_black_list_remove_confirm)
                .setOnEnter(R.string.app_remove) {
                    ControllerSettings.feedIgnoreFandoms = ToolsCollections.removeItem(fandomId, ControllerSettings.feedIgnoreFandoms)
                    EventBus.post(EventFandomBlackListChange(fandomId, false))
                    ToolsToast.show(R.string.app_done)
                }
                .setOnCancel(R.string.app_cancel)
                .asSheetShow()
    }

    fun shareCampfireApp() {
        WidgetField()
                .setHint(R.string.app_message)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_share) { w, text ->
                    ToolsIntent.shareText(text + "\n\r" + "https://play.google.com/store/apps/details?id=com.dzen.campfire")
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_APP_SHARE.index).send(api) }
                }
                .asSheetShow()
    }

    fun createLanguageMenu(selectedId: Long, onClick: (Long) -> kotlin.Unit): WidgetMenu {
        val w = WidgetMenu()
        val code = ToolsAndroid.getLanguageCode().toLowerCase()

        for (i in API.LANGUAGES)
            if (i.code == code || i.code == "en")
                w.add(i.name) { wii, c ->
                    onClick.invoke(i.id)
                }.backgroundRes(R.color.focus) { i.id == selectedId }

        w.group(" ")

        for (i in API.LANGUAGES)
            if (i.code != code && i.code != "en")
                w.add(i.name) { wii, c ->
                    onClick.invoke(i.id)
                }.backgroundRes(R.color.focus) { i.id == selectedId }

        return w
    }


    fun createLanguageCheckMenu(languages: ArrayList<Long>): WidgetCheckBoxes {
        val w = WidgetCheckBoxes()
        val code = ToolsAndroid.getLanguageCode().toLowerCase()
        for (i in API.LANGUAGES) {
            if (i.code == code || i.code == "en")
                w.add(i.name).checked(languages.contains(i.id)).onChange { ww, item, b ->
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
                w.add(i.name).checked(languages.contains(i.id)).onChange { ww, item, b ->
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