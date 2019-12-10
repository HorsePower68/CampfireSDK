package com.sayzen.campfiresdk.controllers

import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.accounts.RAccountsBioSetSex
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.api.requests.accounts.RAccountsChangeName
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListContains
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedSex
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.other.about.SAboutApp
import com.sayzen.campfiresdk.screens.other.about.SAboutCreators
import com.sayzen.campfiresdk.screens.other.rules.SRulesModerators
import com.sayzen.campfiresdk.screens.other.rules.SRulesUser
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.CardAd
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.reviews.SReviews
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.create.SPostCreationTags
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleView
import com.sayzen.campfiresdk.screens.wiki.SWikiList
import com.sayzen.devsupandroidgoogle.ControllerFirebaseAnalytics
import com.sayzen.devsupandroidgoogle.ControllerGoogleAuth
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.*
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

object ControllerCampfireSDK {

    var ROOT_FANDOM_ID = 0L
    var ROOT_PROJECT_KEY: String = ""
    var ROOT_PROJECT_SUB_KEY: String = ""

    var SECOND_IP = ""
    var IS_USE_SECOND_IP = false
    var IS_DEBUG = false

    var ENABLE_CLOSE_FANDOM_ALERT = false

    var ON_TO_DRAFTS_CLICKED: (action: NavigationAction) -> Unit = { }
    var ON_SCREEN_CHAT_START: () -> Unit = { }

    var SEARCH_FANDOM: (callback: (Fandom) -> Unit) -> Unit = { }

    var projectKey = ""
    var onLoginFailed: () -> Unit = {}

    fun init(
            projectKey: String,
            logoColored: Int,
            logoWhite: Int,
            onLoginFailed: () -> Unit
    ) {
        this.projectKey = projectKey
        this.onLoginFailed = onLoginFailed
        ControllerApi.init()
        ControllerActivities.init()
        ControllerChats.init()
        ControllerNotifications.init(logoColored, logoWhite)
        ControllerFirebaseAnalytics.init()
        ControllerAlive.init()
        ControllerGoogleAuth.init("276237287601-6e9aoah4uivbjh6lnn1l9hna6taljd9u.apps.googleusercontent.com", onLoginFailed)

        SAlert.GLOBAL_SHOW_WHOOPS = false
    }

    fun onToFandomClicked(fandomId: Long, languageId: Long, action: NavigationAction) {
        SFandom.instance(fandomId, languageId, action)
    }

    fun onToAccountClicked(accountId: Long, action: NavigationAction) {
        SAccount.instance(accountId, action)
    }

    fun onToModerationClicked(moderationId: Long, commentId: Long, action: NavigationAction) {
        SModerationView.instance(moderationId, commentId, action)
    }

    fun onToPostClicked(postId: Long, commentId: Long, action: NavigationAction) {
        SPost.instance(postId, commentId, action)
    }

    fun onToDraftClicked(postId: Long, action: NavigationAction) {
        SPostCreate.instance(postId, action)
    }

    fun onToDraftsClicked(action: NavigationAction) {
        ON_TO_DRAFTS_CLICKED.invoke(action)
    }

    fun onToPostTagsClicked(postId: Long, isMyPublication: Boolean, action: NavigationAction) {
        SPostCreationTags.instance(postId, isMyPublication, action)
    }


    fun onToAchievementClicked(accountId: Long, accountName: String, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) {
        SAchievements.instance(accountId, accountName, achievementIndex, toPrev, action)
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
                    changeLoginNow(name, true) {}
                }
                .asSheetShow()
    }

    fun changeLoginNow(name: String, achievementNotificationEnabled: Boolean, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeProgressDialog(RAccountsChangeName(name, achievementNotificationEnabled)) { r ->
            ControllerApi.account.name = name
            EventBus.post(EventAccountChanged(ControllerApi.account.id, ControllerApi.account.name))
            onComplete.invoke()
        }.onApiError(RAccountsChangeName.E_LOGIN_NOT_ENABLED) {
            ToolsToast.show(R.string.error_login_taken)
        }.onApiError(RAccountsChangeName.E_LOGIN_IS_NOT_DEFAULT) {
            ToolsToast.show(R.string.error_login_cant_change)
            onComplete.invoke()
        }.onApiError(API.ERROR_ACCOUNT_IS_BANED) {
            ToolsToast.show(R.string.error_login_cant_change)
            onComplete.invoke()
        }
    }

    fun setSex(sex: Long, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeProgressDialog(RAccountsBioSetSex(sex)) { r ->
            EventBus.post(EventAccountBioChangedSex(ControllerApi.account.id, sex))
            onComplete.invoke()
        }
    }

    fun switchToBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RFandomsBlackListContains(fandomId)) { r ->
            if (r.contains) removeFromBlackListFandom(fandomId)
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

    fun logoutWithAlert() {
        WidgetAlert()
                .setText(R.string.settings_exit_confirm)
                .setOnEnter(R.string.app_exit) { logoutNow() }
                .setOnCancel(R.string.app_cancel)
                .asSheetShow()
    }

    fun logoutNow() {
        val d = ToolsView.showProgressDialog()
        ControllerApi.logout {
            d.hide()
            onLoginFailed.invoke()
        }
    }


    fun putAd(vRecycler: RecyclerView, adapterSub: RecyclerCardAdapter, retryCount: Int = 10) {
        val p = 10
        if (adapterSub.get(CardAd::class).isNotEmpty()) return
        if (adapterSub.get(CardPost::class).size < p) {
            info("XAd", "Ad native is not ready [1]")
            ToolsThreads.main(2000) { if (retryCount > 0) putAd(vRecycler, adapterSub, retryCount - 1) }
            return
        }
        val card = getCardAd()
        if (card == null) {
            info("XAd", "Ad native is not ready [2]")
            ToolsThreads.main(2000) { if (retryCount > 0) putAd(vRecycler, adapterSub, retryCount - 1) }
            return
        }

        info("XAd", "Ad inserted to feed")
        val extraCards = adapterSub.size() - adapterSub.get(CardPost::class).size
        var position = (vRecycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + p + extraCards
        if (position > adapterSub.size()) position = adapterSub.size()
        adapterSub.add(position, card)
    }

    private var cardAd: CardAd? = null

    fun getCardAd(): CardAd? {
        if (cardAd != null) return cardAd
        val ad = ControllerAppodeal.getNative()
        if (ad != null) cardAd = CardAd(ad)
        return cardAd
    }


}