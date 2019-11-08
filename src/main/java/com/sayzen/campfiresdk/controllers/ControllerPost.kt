package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminMakeModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationImportant
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationToDrafts
import com.dzen.campfire.api.requests.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageSpoiler
import com.sayzen.campfiresdk.models.events.publications.*
import com.sayzen.campfiresdk.screens.post.history.SUnitHistory
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import java.util.*

object ControllerPost {

    var ON_PRE_SHOW_MENU: (Publication, WidgetMenu) -> kotlin.Unit = { _, _ -> }


    var ENABLED_BOOKMARK = false
    var ENABLED_WATCH = false
    var ENABLED_SHARE = false
    var ENABLED_COPY_LINK = false
    var ENABLED_NOTIFY_FOLLOWERS = false
    var ENABLED_CHANGE = false
    var ENABLED_CHANGE_TAGS = false
    var ENABLED_REMOVE = true
    var ENABLED_TO_DRAFTS = false
    var ENABLED_CHANGE_FANDOM = false
    var ENABLED_REPORT = true
    var ENABLED_CLEAR_REPORTS = false
    var ENABLED_BLOCK = false
    var ENABLED_MODER_TO_DRAFT = false
    var ENABLED_MODER_CHANGE_TAGS = false
    var ENABLED_INPORTANT = false
    var ENABLED_MAKE_MODER = false
    var ENABLED_MODER_CHANGE_FANDOM = false
    var ENABLED_PIN_PROFILE = false
    var ENABLED_PIN_FANDOM = false
    var ENABLED_MAKE_MULTILINGUAL = false
    var ENABLED_HISTORY = false
    var ENABLED_CLOSE = false

    fun showPostMenu(unit: PublicationPost) {

        val w = WidgetMenu()
                .add(R.string.bookmark) { _, _ -> ControllerUnits.changeBookmark(unit.id) }.condition(ENABLED_BOOKMARK && unit.isPublic)
                .add(R.string.unit_menu_comments_watch) { _, _ -> ControllerUnits.changeWatchComments(unit.id) }.condition(ENABLED_WATCH && unit.isPublic)
                .add(R.string.app_share) { _, _ -> ControllerApi.sharePost(unit.id) }.condition(ENABLED_SHARE && unit.isPublic)
                .add(R.string.app_copy_link) { _, _ -> copyLink(unit) }.condition(ENABLED_COPY_LINK && unit.isPublic)
                .add(R.string.app_history) { _, _ -> Navigator.to(SUnitHistory(unit.id)) }.condition(ENABLED_HISTORY)
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.post_create_notify_followers) { _, _ -> notifyFollowers(unit.id) }.condition(ENABLED_NOTIFY_FOLLOWERS && unit.isPublic && unit.tag_3 == 0L)
                .add(R.string.app_change) { _, _ -> ControllerCampfireSDK.onToDraftClicked(unit.id, Navigator.TO) }.condition(ENABLED_CHANGE && unit.isPublic)
                .add(R.string.post_menu_change_tags) { _, _ -> changeTags(unit) }.condition(ENABLED_CHANGE_TAGS && unit.isPublic && unit.languageId != -1L)
                .add(R.string.app_remove) { _, _ -> remove(unit) }.condition(ENABLED_REMOVE)
                .add(R.string.app_to_drafts) { _, _ -> toDrafts(unit) }.condition(ENABLED_TO_DRAFTS && (unit.isPublic || unit.status == API.STATUS_PENDING))
                .add(R.string.unit_menu_change_fandom) { _, _ -> changeFandom(unit.id) }.condition(ENABLED_CHANGE_FANDOM && unit.languageId != -1L && (unit.status == API.STATUS_PUBLIC || unit.status == API.STATUS_DRAFT))
                .add(R.string.unit_menu_pin_in_profile) { _, _ -> pinInProfile(unit) }.condition(ENABLED_PIN_PROFILE && ControllerApi.can(API.LVL_CAN_PIN_POST) && unit.isPublic && !unit.isPined)
                .add(R.string.unit_menu_unpin_in_profile) { _, _ -> unpinInProfile(unit) }.condition(ENABLED_PIN_PROFILE && unit.isPined)
                .add(R.string.unit_menu_multilingual) { _, _ -> multilingual(unit) }.condition(ENABLED_MAKE_MULTILINGUAL && unit.languageId != -1L && unit.status == API.STATUS_PUBLIC)
                .add(R.string.unit_menu_multilingual_not) { _, _ -> multilingualNot(unit) }.condition(ENABLED_MAKE_MULTILINGUAL && unit.languageId == -1L && unit.status == API.STATUS_PUBLIC)
                .add(R.string.app_publish) { _, _ -> publishPending(unit) }.condition(unit.status == API.STATUS_PENDING)
                .add(R.string.app_close) { _, _ -> close(unit) }.condition(!unit.closed)
                .add(R.string.app_open) { _, _ -> open(unit) }.condition(unit.closed)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId) && unit.isPublic)
                .add(R.string.app_report) { _, _ -> ControllerUnits.report(unit) }.condition(ENABLED_REPORT)
                .add(R.string.app_clear_reports) { _, _ -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLEAR_REPORTS && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_BLOCK && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .add(R.string.unit_menu_moderator_to_drafts) { _, _ -> moderatorToDrafts(unit.id) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_TO_DRAFT && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_TO_DRAFTS))
                .add(R.string.unit_menu_multilingual_not) { _, _ -> moderatorMakeMultilingualNot(unit) }.backgroundRes(R.color.blue_700).condition(ENABLED_MAKE_MULTILINGUAL && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_TO_DRAFTS) && unit.languageId == -1L && unit.status == API.STATUS_PUBLIC)
                .add(R.string.post_menu_change_tags) { _, _ -> changeTagsModer(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_TAGS && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_POST_TAGS) && unit.languageId != -1L)
                .add(R.string.unit_menu_pin_in_fandom) { _, _ -> pinInFandom(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_PIN_POST) && unit.isPublic && !unit.isPined)
                .add(R.string.unit_menu_unpin_in_fandom) { _, _ -> unpinInFandom(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_PIN_POST) && unit.isPined)
                .add(R.string.app_close) { _, _ -> closeAdmin(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_CLOSE_POST) && !unit.closed)
                .add(R.string.app_open) { _, _ -> openAdmin(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_CLOSE_POST) && unit.closed)
                .clearGroupCondition()
                .add(if (unit.important == API.PUBLICATION_IMPORTANT_IMPORTANT) R.string.unit_menu_important_unmark else R.string.unit_menu_important_mark) { _, _ -> markAsImportant(unit.id, !(unit.important == API.PUBLICATION_IMPORTANT_IMPORTANT)) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_INPORTANT && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_IMPORTANT) && unit.isPublic && unit.languageId != -1L)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId) && unit.isPublic)
                .add(R.string.admin_make_moder) { _, _ -> makeModerator(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MAKE_MODER && ControllerApi.can(API.LVL_ADMIN_MAKE_MODERATOR) && unit.languageId != -1L)
                .add(R.string.unit_menu_change_fandom) { _, _ -> changeFandomAdmin(unit.id) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_FANDOM && ControllerApi.can(API.LVL_ADMIN_POST_CHANGE_FANDOM) && unit.languageId != -1L)
                .clearGroupCondition()
                .add("Востановить") { _, _ -> ControllerUnits.restoreDeepBlock(unit.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && unit.status == API.STATUS_DEEP_BLOCKED)

        ON_PRE_SHOW_MENU.invoke(unit, w)
        w.asSheetShow()
    }

    fun close(unit: PublicationPost){
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_close_confirm,
                R.string.app_close,
                RPostClose(unit.id)
        ) {
            EventBus.post(EventPostCloseChange(unit.id, true))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun open(unit: PublicationPost){
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_open_confirm,
                R.string.app_open,
                RPostCloseNo(unit.id)
        ) {
            EventBus.post(EventPostCloseChange(unit.id, false))
            ToolsToast.show(R.string.app_done)
        }
    }
    fun closeAdmin(unit: PublicationPost){
        WidgetField()
                .setTitle(R.string.post_close_confirm)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_close) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseModerator(unit.id, comment)) {
                        EventBus.post(EventPostCloseChange(unit.id, true))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun openAdmin(unit: PublicationPost){
        WidgetField()
                .setTitle(R.string.post_open_confirm)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_open) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseNoModerator(unit.id, comment)) {
                        EventBus.post(EventPostCloseChange(unit.id, false))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun publishPending(unit: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_pending_publish,
                R.string.app_publish,
                RPostPendingPublish(unit.id)
        ) {
            EventBus.post(EventPostStatusChange(unit.id, API.STATUS_PUBLIC))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun multilingual(unit: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.unit_menu_multilingual_confirm,
                R.string.app_continue,
                RPostMakeMultilingual(unit.id)
        ) {
            EventBus.post(EventPostMultilingualChange(unit.id, -1L, unit.languageId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun multilingualNot(unit: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.unit_menu_multilingual_not,
                R.string.app_continue,
                RPostMakeMultilingualNot(unit.id)
        ) {
            EventBus.post(EventPostMultilingualChange(unit.id, unit.tag_5, -1L))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun pinInFandom(unit: PublicationPost) {
        WidgetField()
                .setTitle(R.string.unit_menu_pin_in_fandom)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_pin) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(unit.id, unit.fandomId, unit.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(unit.fandomId, unit.languageId, unit))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun unpinInFandom(unit: PublicationPost) {
        WidgetField()
                .setTitle(R.string.unit_menu_unpin_in_fandom)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_unpin) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(0, unit.fandomId, unit.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(unit.fandomId, unit.languageId, null))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun pinInProfile(unit: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.unit_menu_pin_profile_confirm,
                R.string.app_pin,
                RPostPinAccount(unit.id)
        ) {
            EventBus.post(EventPostPinedProfile(unit.creatorId, unit))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun unpinInProfile(unit: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.unit_menu_unpin_profile_confirm,
                R.string.app_unpin,
                RPostPinAccount(0)
        ) {
            EventBus.post(EventPostPinedProfile(unit.creatorId, null))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun notifyFollowers(unitId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_create_notify_followers,
                R.string.app_notify,
                RPostNotifyFollowers(unitId)
        ) {
            EventBus.post(EventPostNotifyFollowers(unitId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun getPagesGroup(adapter: RecyclerCardAdapter): ArrayList<CardPage> {
        var b = false
        val list = ArrayList<CardPage>()
        for (i in 0 until adapter.size()) {
            if (adapter[i] !is CardPage) {
                if (b)
                    break
                else
                    continue
            }
            b = true
            list.add(adapter[i] as CardPage)
        }
        return list
    }

    fun openAllSpoilers(adapter: RecyclerCardAdapter) {
        val list = getPagesGroup(adapter)
        for (card in list)
            if (card is CardPageSpoiler) {
                (card.page as PageSpoiler).isOpen = true
                card.setHidedX(false)
            }
        updateSpoilers(list)
    }

    fun updateSpoilers(adapter: RecyclerCardAdapter) {
        updateSpoilers(getPagesGroup(adapter))
    }

    fun updateSpoilers(listMine: ArrayList<CardPage>) {
        val list = ArrayList<CardPage>()
        for (c in listMine) list.add(c)
        while (!list.isEmpty()) {
            val card = list.removeAt(0)
            if (card is CardPageSpoiler) parseSpoiler(
                    list,
                    (card.page as PageSpoiler).count,
                    !(card.page as PageSpoiler).isOpen
            )
        }
    }

    private fun parseSpoiler(list: ArrayList<CardPage>, maxCount: Int, hide: Boolean) {
        var parsedPages = 0
        while (!list.isEmpty()) {
            val card = list.removeAt(0)
            parsedPages++
            card.setHidedX(hide)
            if (card is CardPageSpoiler) parseSpoiler(
                    list,
                    (card.page as PageSpoiler).count,
                    !(card.page as PageSpoiler).isOpen || hide
            )
            if (parsedPages == maxCount) return
        }
    }

    fun changeFandom(unitId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            ApiRequestsSupporter.executeEnabledConfirm(
                    R.string.unit_menu_change_fandom_confirm,
                    R.string.app_change,
                    RPostChangeFandom(unitId, fandom.id, fandom.languageId, "")
            ) {
                ToolsToast.show(R.string.app_done)
                EventBus.post(EventPublicationFandomChanged(unitId, fandom.id, fandom.languageId, fandom.name, fandom.imageId))
            }
                    .onApiError(RPostChangeFandom.E_SAME_FANDOM) { ToolsToast.show(R.string.error_same_fandom) }
        }
    }

    fun changeFandomAdmin(unitId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            WidgetField()
                    .setTitle(R.string.unit_menu_change_fandom_confirm)
                    .setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_change) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(
                                w,
                                RPostChangeFandom(unitId, fandom.id, fandom.languageId, comment)
                        ) {
                            ToolsToast.show(R.string.app_done)
                            EventBus.post(
                                    EventPublicationFandomChanged(
                                            unitId,
                                            fandom.id,
                                            fandom.languageId,
                                            fandom.name,
                                            fandom.imageId
                                    )
                            )
                        }
                                .onApiError(RPostChangeFandom.E_SAME_FANDOM) { ToolsToast.show(R.string.error_same_fandom) }
                    }
                    .asSheetShow()
        }
    }

    fun markAsImportant(unitId: Long, important: Boolean) {
        WidgetField()
                .setTitle(if (!important) R.string.unit_menu_important_unmark else R.string.unit_menu_important_mark)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(if (!important) R.string.app_do_unmark else R.string.app_do_mark) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(
                            if (!important) R.string.unit_menu_important_unmark_confirm else R.string.unit_menu_important_mark_confirm,
                            if (!important) R.string.app_do_unmark else R.string.app_do_mark,
                            RFandomsModerationImportant(unitId, important, comment)
                    ) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(
                                EventPublicationImportantChange(
                                        unitId,
                                        if (important) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_DEFAULT
                                )
                        )
                    }
                }
                .asSheetShow()
    }

    fun moderatorToDrafts(unitId: Long) {
        WidgetField()
                .setTitle(R.string.unit_menu_moderator_to_drafts)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_to_return) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationToDrafts(unitId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventPublicationRemove(unitId))
                    }
                            .onApiError(RFandomsModerationToDrafts.E_ALREADY) {
                                ToolsToast.show(R.string.error_already_returned_to_drafts)
                                EventBus.post(EventPublicationRemove(unitId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_BLOCKED) {
                                ToolsToast.show(R.string.error_already_blocked)
                                EventBus.post(EventPublicationRemove(unitId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_LOW_KARMA_FORCE) { ToolsToast.show(R.string.moderation_low_karma) }
                }
                .asSheetShow()
    }

    fun moderatorMakeMultilingualNot(unit: Publication) {
        WidgetField()
                .setTitle(R.string.unit_menu_multilingual_not)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_make) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostMakeMultilingualModeratorNot(unit.id, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventPostMultilingualChange(unit.id, unit.tag_5, -1L))
                    }
                            .onApiError(RPostMakeMultilingualModeratorNot.E_LOW_KARMA_FORCE) { ToolsToast.show(R.string.moderation_low_karma) }
                }
                .asSheetShow()
    }

    fun makeModerator(unit: Publication) {
        WidgetField()
                .setTitle(R.string.admin_make_moder)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_make) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminMakeModerator(unit.id, comment)) { r ->
                        EventBus.post(EventPublicationKarmaAdd(unit.id, r.myKarmaCount))
                    }
                            .onApiError(RFandomsAdminMakeModerator.E_ALREADY) { ToolsToast.show(R.string.error_moderator_already) }
                            .onApiError(RFandomsAdminMakeModerator.E_TOO_MANY) { ToolsToast.show(R.string.error_moderator_too_many) }
                            .onApiError(RFandomsAdminMakeModerator.E_FANDOM_HAVE_MODERATORS) { ToolsToast.show(R.string.error_moderator_moderators_exist) }
                            .onApiError(RFandomsAdminMakeModerator.E_LOW_LVL) { ToolsToast.show(R.string.error_moderator_low_lvl) }
                }
                .asSheetShow()

    }

    private fun copyLink(unit: Publication) {
        ToolsAndroid.setToClipboard(ControllerApi.linkToPost(unit.id))
        ToolsToast.show(R.string.app_copied)
    }

    private fun changeTags(unit: Publication) {
        ControllerCampfireSDK.onToPostTagsClicked(
                unit.id,
                true,
                Navigator.TO
        )
    }

    private fun changeTagsModer(unit: Publication) {
        ControllerCampfireSDK.onToPostTagsClicked(
                unit.id,
                false,
                Navigator.TO
        )
    }

    private fun remove(unit: Publication) {
        ControllerApi.removeUnit(
                unit.id,
                R.string.post_remove_confirm,
                R.string.post_error_gone
        )
    }

    private fun toDrafts(unit: Publication) {
        ControllerUnits.toDrafts(unit.id) {
            ControllerCampfireSDK.onToDraftsClicked(
                    Navigator.REPLACE
            )
        }
    }

}