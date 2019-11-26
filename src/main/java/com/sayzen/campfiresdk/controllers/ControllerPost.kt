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
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
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

    var ON_PRE_SHOW_MENU: (Publication, WidgetMenu) -> Unit = { _, _ -> }


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

    fun showPostMenu(post: PublicationPost) {

        val w = WidgetMenu()
                .add(R.string.bookmark) { _, _ -> ControllerPublications.changeBookmark(post.id) }.condition(ENABLED_BOOKMARK && post.isPublic)
                .add(R.string.publication_menu_comments_watch) { _, _ -> ControllerPublications.changeWatchComments(post.id) }.condition(ENABLED_WATCH && post.isPublic)
                .add(R.string.app_share) { _, _ -> ControllerApi.sharePost(post.id) }.condition(ENABLED_SHARE && post.isPublic)
                .add(R.string.app_copy_link) { _, _ -> copyLink(post) }.condition(ENABLED_COPY_LINK && post.isPublic)
                .add(R.string.app_history) { _, _ -> Navigator.to(SPublicationHistory(post.id)) }.condition(ENABLED_HISTORY)
                .groupCondition(ControllerApi.isCurrentAccount(post.creatorId))
                .add(R.string.post_create_notify_followers) { _, _ -> notifyFollowers(post.id) }.condition(ENABLED_NOTIFY_FOLLOWERS && post.isPublic && post.tag_3 == 0L)
                .add(R.string.app_change) { _, _ -> ControllerCampfireSDK.onToDraftClicked(post.id, Navigator.TO) }.condition(ENABLED_CHANGE && post.isPublic)
                .add(R.string.post_menu_change_tags) { _, _ -> changeTags(post) }.condition(ENABLED_CHANGE_TAGS && post.isPublic && post.languageId != -1L)
                .add(R.string.app_remove) { _, _ -> remove(post) }.condition(ENABLED_REMOVE)
                .add(R.string.app_to_drafts) { _, _ -> toDrafts(post) }.condition(ENABLED_TO_DRAFTS && (post.isPublic || post.status == API.STATUS_PENDING))
                .add(R.string.publication_menu_change_fandom) { _, _ -> changeFandom(post.id) }.condition(ENABLED_CHANGE_FANDOM && post.languageId != -1L && (post.status == API.STATUS_PUBLIC || post.status == API.STATUS_DRAFT))
                .add(R.string.publication_menu_pin_in_profile) { _, _ -> pinInProfile(post) }.condition(ENABLED_PIN_PROFILE && ControllerApi.can(API.LVL_CAN_PIN_POST) && post.isPublic && !post.isPined)
                .add(R.string.publication_menu_unpin_in_profile) { _, _ -> unpinInProfile(post) }.condition(ENABLED_PIN_PROFILE && post.isPined)
                .add(R.string.publication_menu_multilingual) { _, _ -> multilingual(post) }.condition(ENABLED_MAKE_MULTILINGUAL && post.languageId != -1L && post.status == API.STATUS_PUBLIC)
                .add(R.string.publication_menu_multilingual_not) { _, _ -> multilingualNot(post) }.condition(ENABLED_MAKE_MULTILINGUAL && post.languageId == -1L && post.status == API.STATUS_PUBLIC)
                .add(R.string.app_publish) { _, _ -> publishPending(post) }.condition(post.status == API.STATUS_PENDING)
                .add(R.string.app_close) { _, _ -> close(post) }.condition(!post.closed)
                .add(R.string.app_open) { _, _ -> open(post) }.condition(post.closed)
                .groupCondition(!ControllerApi.isCurrentAccount(post.creatorId) && post.isPublic)
                .add(R.string.app_report) { _, _ -> ControllerPublications.report(post) }.condition(ENABLED_REPORT)
                .add(R.string.app_clear_reports) { _, _ -> ControllerPublications.clearReports(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLEAR_REPORTS && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_BLOCK) && post.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerPublications.block(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_BLOCK && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_BLOCK))
                .add(R.string.publication_menu_moderator_to_drafts) { _, _ -> moderatorToDrafts(post.id) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_TO_DRAFT && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_TO_DRAFTS))
                .add(R.string.publication_menu_multilingual_not) { _, _ -> moderatorMakeMultilingualNot(post) }.backgroundRes(R.color.blue_700).condition(ENABLED_MAKE_MULTILINGUAL && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_TO_DRAFTS) && post.languageId == -1L && post.status == API.STATUS_PUBLIC)
                .add(R.string.post_menu_change_tags) { _, _ -> changeTagsModer(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_TAGS && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_POST_TAGS) && post.languageId != -1L)
                .add(R.string.publication_menu_pin_in_fandom) { _, _ -> pinInFandom(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_PIN_POST) && post.isPublic && !post.isPined)
                .add(R.string.publication_menu_unpin_in_fandom) { _, _ -> unpinInFandom(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_PIN_POST) && post.isPined)
                .add(R.string.app_close) { _, _ -> closeAdmin(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_CLOSE_POST) && !post.closed)
                .add(R.string.app_open) { _, _ -> openAdmin(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_CLOSE_POST) && post.closed)
                .clearGroupCondition()
                .add(if (post.important == API.PUBLICATION_IMPORTANT_IMPORTANT) R.string.publication_menu_important_unmark else R.string.publication_menu_important_mark) { _, _ -> markAsImportant(post.id, !(post.important == API.PUBLICATION_IMPORTANT_IMPORTANT)) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_INPORTANT && ControllerApi.can(post.fandomId, post.languageId, API.LVL_MODERATOR_IMPORTANT) && post.isPublic && post.languageId != -1L)
                .groupCondition(!ControllerApi.isCurrentAccount(post.creatorId) && post.isPublic)
                .add(R.string.admin_make_moder) { _, _ -> makeModerator(post) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MAKE_MODER && ControllerApi.can(API.LVL_ADMIN_MAKE_MODERATOR) && post.languageId != -1L)
                .add(R.string.publication_menu_change_fandom) { _, _ -> changeFandomAdmin(post.id) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_FANDOM && ControllerApi.can(API.LVL_ADMIN_POST_CHANGE_FANDOM) && post.languageId != -1L)
                .clearGroupCondition()
                .add("Востановить") { _, _ -> ControllerPublications.restoreDeepBlock(post.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && post.status == API.STATUS_DEEP_BLOCKED)

        ON_PRE_SHOW_MENU.invoke(post, w)
        w.asSheetShow()
    }

    fun close(publications: PublicationPost){
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_close_confirm,
                R.string.app_close,
                RPostClose(publications.id)
        ) {
            EventBus.post(EventPostCloseChange(publications.id, true))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun open(publications: PublicationPost){
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_open_confirm,
                R.string.app_open,
                RPostCloseNo(publications.id)
        ) {
            EventBus.post(EventPostCloseChange(publications.id, false))
            ToolsToast.show(R.string.app_done)
        }
    }
    fun closeAdmin(publications: PublicationPost){
        WidgetField()
                .setTitle(R.string.post_close_confirm)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_close) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseModerator(publications.id, comment)) {
                        EventBus.post(EventPostCloseChange(publications.id, true))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun openAdmin(publications: PublicationPost){
        WidgetField()
                .setTitle(R.string.post_open_confirm)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_open) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseNoModerator(publications.id, comment)) {
                        EventBus.post(EventPostCloseChange(publications.id, false))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun publishPending(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_pending_publish,
                R.string.app_publish,
                RPostPendingPublish(publications.id)
        ) {
            EventBus.post(EventPostStatusChange(publications.id, API.STATUS_PUBLIC))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun multilingual(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.publication_menu_multilingual_confirm,
                R.string.app_continue,
                RPostMakeMultilingual(publications.id)
        ) {
            EventBus.post(EventPostMultilingualChange(publications.id, -1L, publications.languageId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun multilingualNot(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.publication_menu_multilingual_not,
                R.string.app_continue,
                RPostMakeMultilingualNot(publications.id)
        ) {
            EventBus.post(EventPostMultilingualChange(publications.id, publications.tag_5, -1L))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun pinInFandom(publications: PublicationPost) {
        WidgetField()
                .setTitle(R.string.publication_menu_pin_in_fandom)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_pin) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(publications.id, publications.fandomId, publications.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(publications.fandomId, publications.languageId, publications))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun unpinInFandom(publications: PublicationPost) {
        WidgetField()
                .setTitle(R.string.publication_menu_unpin_in_fandom)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_unpin) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(0, publications.fandomId, publications.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(publications.fandomId, publications.languageId, null))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    fun pinInProfile(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.publication_menu_pin_profile_confirm,
                R.string.app_pin,
                RPostPinAccount(publications.id)
        ) {
            EventBus.post(EventPostPinedProfile(publications.creatorId, publications))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun unpinInProfile(publication: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.publication_menu_unpin_profile_confirm,
                R.string.app_unpin,
                RPostPinAccount(0)
        ) {
            EventBus.post(EventPostPinedProfile(publication.creatorId, null))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun notifyFollowers(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(
                R.string.post_create_notify_followers,
                R.string.app_notify,
                RPostNotifyFollowers(publicationId)
        ) {
            EventBus.post(EventPostNotifyFollowers(publicationId))
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

    fun changeFandom(publicationId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            ApiRequestsSupporter.executeEnabledConfirm(
                    R.string.publication_menu_change_fandom_confirm,
                    R.string.app_change,
                    RPostChangeFandom(publicationId, fandom.id, fandom.languageId, "")
            ) {
                ToolsToast.show(R.string.app_done)
                EventBus.post(EventPublicationFandomChanged(publicationId, fandom.id, fandom.languageId, fandom.name, fandom.imageId))
            }
                    .onApiError(RPostChangeFandom.E_SAME_FANDOM) { ToolsToast.show(R.string.error_same_fandom) }
        }
    }

    fun changeFandomAdmin(publicationId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            WidgetField()
                    .setTitle(R.string.publication_menu_change_fandom_confirm)
                    .setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_change) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(
                                w,
                                RPostChangeFandom(publicationId, fandom.id, fandom.languageId, comment)
                        ) {
                            ToolsToast.show(R.string.app_done)
                            EventBus.post(
                                    EventPublicationFandomChanged(
                                            publicationId,
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

    fun markAsImportant(publicationId: Long, important: Boolean) {
        WidgetField()
                .setTitle(if (!important) R.string.publication_menu_important_unmark else R.string.publication_menu_important_mark)
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(if (!important) R.string.app_do_unmark else R.string.app_do_mark) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(
                            if (!important) R.string.publication_menu_important_unmark_confirm else R.string.publication_menu_important_mark_confirm,
                            if (!important) R.string.app_do_unmark else R.string.app_do_mark,
                            RFandomsModerationImportant(publicationId, important, comment)
                    ) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(
                                EventPublicationImportantChange(
                                        publicationId,
                                        if (important) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_DEFAULT
                                )
                        )
                    }
                }
                .asSheetShow()
    }

    fun moderatorToDrafts(publicationId: Long) {
        WidgetField()
                .setTitle(R.string.publication_menu_moderator_to_drafts)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_to_return) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationToDrafts(publicationId, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventPublicationRemove(publicationId))
                    }
                            .onApiError(RFandomsModerationToDrafts.E_ALREADY) {
                                ToolsToast.show(R.string.error_already_returned_to_drafts)
                                EventBus.post(EventPublicationRemove(publicationId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_BLOCKED) {
                                ToolsToast.show(R.string.error_already_blocked)
                                EventBus.post(EventPublicationRemove(publicationId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_LOW_KARMA_FORCE) { ToolsToast.show(R.string.moderation_low_karma) }
                }
                .asSheetShow()
    }

    fun moderatorMakeMultilingualNot(publication: Publication) {
        WidgetField()
                .setTitle(R.string.publication_menu_multilingual_not)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_make) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostMakeMultilingualModeratorNot(publication.id, comment)) {
                        ToolsToast.show(R.string.app_done)
                        EventBus.post(EventPostMultilingualChange(publication.id, publication.tag_5, -1L))
                    }
                            .onApiError(RPostMakeMultilingualModeratorNot.E_LOW_KARMA_FORCE) { ToolsToast.show(R.string.moderation_low_karma) }
                }
                .asSheetShow()
    }

    fun makeModerator(publication: Publication) {
        WidgetField()
                .setTitle(R.string.admin_make_moder)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_make) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminMakeModerator(publication.id, comment)) { r ->
                        ToolsToast.show(R.string.app_done)
                    }
                            .onApiError(RFandomsAdminMakeModerator.E_ALREADY) { ToolsToast.show(R.string.error_moderator_already) }
                            .onApiError(RFandomsAdminMakeModerator.E_TOO_MANY) { ToolsToast.show(R.string.error_moderator_too_many) }
                            .onApiError(RFandomsAdminMakeModerator.E_FANDOM_HAVE_MODERATORS) { ToolsToast.show(R.string.error_moderator_moderators_exist) }
                            .onApiError(RFandomsAdminMakeModerator.E_LOW_LVL) { ToolsToast.show(R.string.error_moderator_low_lvl) }
                }
                .asSheetShow()

    }

    private fun copyLink(publication: Publication) {
        ToolsAndroid.setToClipboard(ControllerApi.linkToPost(publication.id))
        ToolsToast.show(R.string.app_copied)
    }

    private fun changeTags(publication: Publication) {
        ControllerCampfireSDK.onToPostTagsClicked(
                publication.id,
                true,
                Navigator.TO
        )
    }

    private fun changeTagsModer(publication: Publication) {
        ControllerCampfireSDK.onToPostTagsClicked(
                publication.id,
                false,
                Navigator.TO
        )
    }

    private fun remove(publication: Publication) {
        ControllerApi.removePublication(
                publication.id,
                R.string.post_remove_confirm,
                R.string.post_error_gone
        )
    }

    private fun toDrafts(publication: Publication) {
        ControllerPublications.toDrafts(publication.id) {
            ControllerCampfireSDK.onToDraftsClicked(
                    Navigator.REPLACE
            )
        }
    }

}