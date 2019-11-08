package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.*
import com.dzen.campfire.api.requests.units.RUnitsRemove
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

    fun showStickerPackPopup(unit: PublicationStickersPack) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToStickersPack(unit.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.unit_menu_comments_watch) { _, _ -> ControllerUnits.changeWatchComments(unit.id) }.condition(unit.isPublic)
                .add(R.string.app_change) { _, _ -> Navigator.to(SStickersPackCreate(unit)) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_remove) { _, _ -> removeStickersPack(unit.id) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.stickers_packs_report_confirm, R.string.stickers_packs_error_gone) }.condition(unit.creatorId != ControllerApi.account.id)
                .add(R.string.app_collection) { _, _ -> switchStickerPackCollection(unit) }.condition(unit.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.reportsCount > 0 && unit.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.creatorId != ControllerApi.account.id)
                .asSheetShow()
    }

    fun switchStickerPackCollection(unit: PublicationStickersPack) {
        ApiRequestsSupporter.executeProgressDialog(RStickersPackCollectionCheck(unit.id)){ r->
            if(r.inCollection) {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_remove, R.string.app_remove, RStickersPackCollectionRemove(unit.id)) {
                    EventBus.post(EventStickersPackCollectionChanged(unit, false))
                    ToolsToast.show(R.string.stickers_message_remove_from_collection_pack)
                }
            }
            else {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_add, R.string.app_add, RStickersPackCollectionAdd(unit.id)) {
                    EventBus.post(EventStickersPackCollectionChanged(unit, true))
                    ToolsToast.show(R.string.stickers_message_add_to_collection_pack)
                }
            }
        }
    }

    fun removeStickersPack(unitId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_packs_remove_confirm, R.string.app_remove, RUnitsRemove(unitId)) {
            EventBus.post(EventPublicationRemove(unitId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun showStickerPopup(view: View, x: Int, y: Int, unit: PublicationSticker) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToSticker(unit.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.app_remove) { _, _ -> removeSticker(unit.id) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.stickers_report_confirm, R.string.sticker_error_gone) }
                .add(R.string.app_favorite) { _, _ -> switchStickerCollection(unit) }.condition(unit.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.reportsCount > 0 && unit.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.creatorId != ControllerApi.account.id)
                .asPopupShow(view, x, y)
    }

    fun switchStickerCollection(unit: PublicationSticker) {
        ApiRequestsSupporter.executeProgressDialog(RStickerCollectionCheck(unit.id)){ r->
            if(r.inCollection) {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_remove_favorites, R.string.app_remove, RStickerCollectionRemove(unit.id)) {
                    EventBus.post(EventStickerCollectionChanged(unit, false))
                    ToolsToast.show(R.string.stickers_message_remove_from_collection)
                }
            }
            else {
                ApiRequestsSupporter.executeEnabledConfirm(R.string.sticker_add_favorites, R.string.app_add, RStickerCollectionAdd(unit.id)) {
                    EventBus.post(EventStickerCollectionChanged(unit, true))
                    ToolsToast.show(R.string.stickers_message_add_to_collection)
                }
            }
        }
    }

    fun removeSticker(unitId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_remove_confirm, R.string.app_remove, RUnitsRemove(unitId)) {
            EventBus.post(EventPublicationRemove(unitId))
            ToolsToast.show(R.string.app_done)
        }
    }


}