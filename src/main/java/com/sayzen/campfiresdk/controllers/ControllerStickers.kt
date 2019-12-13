package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.*
import com.dzen.campfire.api.requests.publications.RPublicationsRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.stickers.EventStickerCollectionChanged
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCollectionChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPackCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerStickers {

    //
    //  Stickers
    //

    fun showStickerPackPopup(publication: PublicationStickersPack) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerLinks.linkToStickersPack(publication.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.publication_menu_comments_watch) { _, _ -> ControllerPublications.changeWatchComments(publication.id) }.condition(publication.isPublic)
                .add(R.string.app_change) { _, _ -> Navigator.to(SStickersPackCreate(publication)) }.condition(publication.creatorId == ControllerApi.account.id)
                .add(R.string.app_remove) { _, _ -> removeStickersPack(publication.id) }.condition(publication.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportPublication(publication.id, R.string.stickers_packs_report_confirm, R.string.stickers_packs_error_gone) }.condition(publication.creatorId != ControllerApi.account.id)
                .add(R.string.app_collection) { _, _ -> switchStickerPackCollection(publication) }.condition(publication.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerPublications.clearReports(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.reportsCount > 0 && publication.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerPublications.block(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.creatorId != ControllerApi.account.id)
                .asSheetShow()
    }

    fun switchStickerPackCollection(publication: PublicationStickersPack) {
        ApiRequestsSupporter.executeProgressDialog(RStickersPackCollectionCheck(publication.id)){ r->
            if(r.inCollection) {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_remove, R.string.app_remove, RStickersPackCollectionRemove(publication.id)) {
                    EventBus.post(EventStickersPackCollectionChanged(publication, false))
                    ToolsToast.show(R.string.stickers_message_remove_from_collection_pack)
                }
            }
            else {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_add, R.string.app_add, RStickersPackCollectionAdd(publication.id)) {
                    EventBus.post(EventStickersPackCollectionChanged(publication, true))
                    ToolsToast.show(R.string.stickers_message_add_to_collection_pack)
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_STICKERS)
                }.onApiError(RStickersPackCollectionAdd.E_TOO_MANY) {
                    ToolsToast.show(R.string.stickers_message_too_many_paskc)
                }
            }
        }
    }

    fun removeStickersPack(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_packs_remove_confirm, R.string.app_remove, RPublicationsRemove(publicationId)) {
            EventBus.post(EventPublicationRemove(publicationId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun showStickerPopup(view: View, x: Int, y: Int, publication: PublicationSticker) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerLinks.linkToSticker(publication.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.app_remove) { _, _ -> removeSticker(publication.id) }.condition(publication.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportPublication(publication.id, R.string.stickers_report_confirm, R.string.sticker_error_gone) }
                .add(R.string.app_favorite) { _, _ -> switchStickerCollection(publication) }.condition(publication.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerPublications.clearReports(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.reportsCount > 0 && publication.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerPublications.block(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.creatorId != ControllerApi.account.id)
                .asPopupShow(view, x, y)
    }

    fun switchStickerCollection(publication: PublicationSticker) {
        ApiRequestsSupporter.executeProgressDialog(RStickerCollectionCheck(publication.id)){ r->
            if(r.inCollection) {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_remove_favorites, R.string.app_remove, RStickerCollectionRemove(publication.id)) {
                    EventBus.post(EventStickerCollectionChanged(publication, false))
                    ToolsToast.show(R.string.stickers_message_remove_from_collection)
                }
            }
            else {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_add_favorites, R.string.app_add, RStickerCollectionAdd(publication.id)) {
                    EventBus.post(EventStickerCollectionChanged(publication, true))
                    ToolsToast.show(R.string.stickers_message_add_to_collection)
                }
            }
        }
    }

    fun removeSticker(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_remove_confirm, R.string.app_remove, RPublicationsRemove(publicationId)) {
            EventBus.post(EventPublicationRemove(publicationId))
            ToolsToast.show(R.string.app_done)
        }
    }


}