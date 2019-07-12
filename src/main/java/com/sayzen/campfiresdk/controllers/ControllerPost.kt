package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.post.PageSpoiler
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminMakeModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationImportant
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationToDrafts
import com.dzen.campfire.api.requests.post.RPostChangeFandom
import com.dzen.campfire.api.requests.post.RPostNotifyFollowers
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageSpoiler
import com.sayzen.campfiresdk.models.events.units.*
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

    var ON_PRE_SHOW_MENU: (Unit, WidgetMenu) -> kotlin.Unit = { u, w -> }

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

    fun showPostPopup(view: View, unit: UnitPost) {

        val w = WidgetMenu()
                .add(R.string.bookmark) { w, card -> ControllerUnits.changeBookmark(unit.id) }.condition(ENABLED_BOOKMARK && unit.isPublic)
                .add(R.string.unit_menu_comments_watch) { w, card -> ControllerUnits.changeWatchComments(unit.id) }.condition(ENABLED_WATCH && unit.isPublic)
                .add(R.string.app_share) { w, card -> ControllerApi.sharePost(unit.id) }.condition(ENABLED_SHARE && unit.isPublic)
                .add(R.string.app_copy_link) { w, card -> copyLink(unit) }.condition(ENABLED_COPY_LINK && unit.isPublic)
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.post_create_notify_followers) { w, card -> notifyFollowers(unit.id) }.condition(ENABLED_NOTIFY_FOLLOWERS && unit.isPublic && unit.tag_3 == 0L)
                .add(R.string.app_change) { w, card -> ControllerCampfireSDK.onToDraftClicked(unit.id, Navigator.TO) }.condition(ENABLED_CHANGE && unit.isPublic)
                .add(R.string.post_menu_change_tags) { w, card -> changeTags(unit) }.condition(ENABLED_CHANGE_TAGS && unit.isPublic)
                .add(R.string.app_remove) { w, card -> remove(unit) }.condition(ENABLED_REMOVE)
                .add(R.string.app_to_drafts) { w, card -> toDrafts(unit) }.condition(ENABLED_TO_DRAFTS && unit.isPublic)
                .add(R.string.unit_menu_change_fandom) { w, card -> changeFandom(unit.id) }.condition(ENABLED_CHANGE_FANDOM)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId) && unit.isPublic)
                .add(R.string.app_report) { w, card -> ControllerUnits.report(unit) }.condition(ENABLED_REPORT)
                .add(R.string.app_clear_reports) { w, card -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.blue_700).condition(ENABLED_CLEAR_REPORTS && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { w, card -> ControllerUnits.block(unit) }.backgroundRes(R.color.blue_700).condition(ENABLED_BLOCK && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .add(R.string.unit_menu_moderator_to_drafts) { w, card -> moderatorToDrafts(unit.id) }.backgroundRes(R.color.blue_700).condition(ENABLED_MODER_TO_DRAFT && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_TO_DRAFTS))
                .add(R.string.post_menu_change_tags) { w, card -> changeTagsModer(unit) }.backgroundRes(R.color.blue_700).condition(ENABLED_MODER_CHANGE_TAGS && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_POST_TAGS))
                .clearGroupCondition()
                .add(if (unit.important == API.UNIT_IMPORTANT_IMPORTANT) R.string.unit_menu_important_unmark else R.string.unit_menu_important_mark) { w, card -> markAsImportant(unit.id, !(unit.important == API.UNIT_IMPORTANT_IMPORTANT)) }.backgroundRes(R.color.blue_700).condition(ENABLED_INPORTANT && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_IMPORTANT) && unit.isPublic)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId) && unit.isPublic)
                .add(R.string.admin_make_moder) { w, card -> makeModerator(unit) }.backgroundRes(R.color.red_700)
                .condition(ENABLED_MAKE_MODER && ControllerApi.can(API.LVL_ADMIN_MAKE_MODERATOR))
                .add(R.string.unit_menu_change_fandom) { w, card -> changeFandomAdmin(unit.id) }
                .backgroundRes(R.color.red_700)
                .condition(ENABLED_MODER_CHANGE_FANDOM && ControllerApi.can(API.LVL_ADMIN_POST_CHANGE_FANDOM))
                .clearGroupCondition()
        ON_PRE_SHOW_MENU.invoke(unit, w)
        w.asSheetShow()
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
                EventBus.post(EventUnitFandomChanged(unitId, fandom.id, fandom.languageId, fandom.name, fandom.imageId))
            }
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
                                    EventUnitFandomChanged(
                                            unitId,
                                            fandom.id,
                                            fandom.languageId,
                                            fandom.name,
                                            fandom.imageId
                                    )
                            )
                        }
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
                .setOnEnter(if (!important) R.string.app_do_unmark else R.string.app_do_mark) { w, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(
                            if (!important) R.string.unit_menu_important_unmark_confirm else R.string.unit_menu_important_mark_confirm,
                            if (!important) R.string.app_do_unmark else R.string.app_do_mark,
                            RFandomsModerationImportant(unitId, important, comment)
                    ) { r ->
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(
                                EventUnitImportantChange(
                                        unitId,
                                        if (important) API.UNIT_IMPORTANT_IMPORTANT else API.UNIT_IMPORTANT_DEFAULT
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
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationToDrafts(unitId, comment)) { r ->
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventUnitRemove(unitId))
                    }
                            .onApiError(RFandomsModerationToDrafts.E_ALREADY) {
                                ToolsToast.show(R.string.error_already_returned_to_drafts)
                                EventBus.post(EventUnitRemove(unitId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_BLOCKED) {
                                ToolsToast.show(R.string.error_already_blocked)
                                EventBus.post(EventUnitRemove(unitId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_LOW_KARMA_FORCE) { ToolsToast.show(R.string.moderation_low_karma) }
                }
                .asSheetShow()
    }

    fun makeModerator(unit: Unit) {
        WidgetField()
                .setTitle(R.string.admin_make_moder)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_make) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminMakeModerator(unit.id, comment)) { r ->
                        EventBus.post(EventUnitKarmaAdd(unit.id, r.myKarmaCount))
                    }
                            .onApiError(RFandomsAdminMakeModerator.E_ALREADY) { ToolsToast.show(R.string.error_moderator_already) }
                            .onApiError(RFandomsAdminMakeModerator.E_TOO_MANY) { ToolsToast.show(R.string.error_moderator_too_many) }
                            .onApiError(RFandomsAdminMakeModerator.E_FANDOM_HAVE_MODERATORS) { ToolsToast.show(R.string.error_moderator_moderators_exist) }
                            .onApiError(RFandomsAdminMakeModerator.E_LOW_LVL) { ToolsToast.show(R.string.error_moderator_low_lvl) }
                }
                .asSheetShow()

    }

    private fun copyLink(unit: Unit) {
        ToolsAndroid.setToClipboard(ControllerApi.linkToPost(unit.id))
        ToolsToast.show(R.string.app_copied)
    }

    private fun changeTags(unit: Unit) {
        ControllerCampfireSDK.onToPostTagsClicked(
                unit.id,
                true,
                Navigator.TO
        )
    }

    private fun changeTagsModer(unit: Unit) {
        ControllerCampfireSDK.onToPostTagsClicked(
                unit.id,
                false,
                Navigator.TO
        )
    }

    private fun remove(unit: Unit) {
        ControllerApi.removeUnit(
                unit.id,
                R.string.post_remove_confirm,
                R.string.post_error_gone
        )
    }

    private fun toDrafts(unit: Unit) {
        ControllerUnits.toDrafts(unit.id) {
            ControllerCampfireSDK.onToDraftsClicked(
                    Navigator.REPLACE
            )
        }
    }

}