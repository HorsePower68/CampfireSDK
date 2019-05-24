package com.sayzen.campfiresdk.controllers

import android.app.Activity
import com.dzen.campfire.api.models.Fandom
import com.dzen.campfire.api.models.units.UnitForum
import com.sup.dev.android.libs.screens.navigator.NavigationAction

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
    var ON_TO_TAGS_CLICKED: (fandomId: Long, languageId: Long, action: NavigationAction) -> Unit = { fandomId, languageId, action -> }
    var ON_CHANGE_FORUM_CLICKED: (unit: UnitForum) -> Unit = { unit -> }
    var ON_SCREEN_CHAT_START: () -> Unit = {  }

    var SEARCH_FANDOM: (callback: (Fandom) -> Unit) -> Unit = { }

    fun init(
            activityClass: Class<out Activity>,
            logoColored: Int,
            logoWhite: Int,
            notificationExecutor: ControllerNotifications.NotificationExecutor,
            onLoginFailed: () -> Unit
    ) {
        ControllerApi.init()
        ControllerChats.init()
        ControllerNotifications.init(activityClass, logoColored, logoWhite, notificationExecutor)
        ControllerToken.init(onLoginFailed)
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

    fun onToTagsClicked(fandomId: Long, languageId: Long, action: NavigationAction) {
        ON_TO_TAGS_CLICKED.invoke(fandomId, languageId, action)
    }

}