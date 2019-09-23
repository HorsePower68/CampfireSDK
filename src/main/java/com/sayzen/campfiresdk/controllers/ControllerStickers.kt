package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.dzen.campfire.api.requests.stickers.RStickerCollectionChange
import com.dzen.campfire.api.requests.stickers.RStickersPackCollectionChange
import com.dzen.campfire.api.requests.units.RUnitsRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.stickers.EventStickerCollectionChanged
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCollectionChanged
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.screens.stickers.SStickersPackCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections

object ControllerStickers {

    //
    //  Stickers
    //

    fun showStickerPackPopup(unit: UnitStickersPack) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToStickersPack(unit.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.unit_menu_comments_watch) { _, _ -> ControllerUnits.changeWatchComments(unit.id) }.condition(unit.isPublic)
                .add(R.string.app_change) { _, _ -> Navigator.to(SStickersPackCreate(unit)) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_remove) { _, _ -> removeStickersPack(unit.id) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.stickers_packs_report_confirm, R.string.stickers_packs_error_gone) }.condition(unit.creatorId != ControllerApi.account.id)
                .add(if (ControllerSettings.accountSettings.stickersPacks.contains(unit.id)) R.string.sticker_remove else R.string.sticker_add) { _, _ -> addStickerPackToCollection(unit) }.condition(unit.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.reportsCount > 0 && unit.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.creatorId != ControllerApi.account.id)
                .asSheetShow()
    }

    fun addStickerPackToCollection(unit: UnitStickersPack) {
        val inCollection = !ControllerSettings.accountSettings.stickersPacks.contains(unit.id)

        ApiRequestsSupporter.executeProgressDialog(RStickersPackCollectionChange(unit.id, inCollection)) { _->

            if (inCollection)
                ControllerSettings.accountSettings.stickersPacks = ToolsCollections.add(unit.id, ControllerSettings.accountSettings.stickersPacks)
            else
                ControllerSettings.accountSettings.stickersPacks = ToolsCollections.removeItem(unit.id, ControllerSettings.accountSettings.stickersPacks)

            EventBus.post(EventStickersPackCollectionChanged(unit))
        }
    }

    fun removeStickersPack(unitId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_packs_remove_confirm, R.string.app_remove, RUnitsRemove(unitId)) {
            EventBus.post(EventUnitRemove(unitId))
            ControllerSettings.accountSettings.stickersPacks = ToolsCollections.removeItem(unitId, ControllerSettings.accountSettings.stickersPacks)
            ToolsToast.show(R.string.app_done)
        }
    }

    fun showStickerPopup(view: View, x: Int, y: Int, unit: UnitSticker) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToSticker(unit.id)); ToolsToast.show(R.string.app_copied) }
                .add(R.string.app_remove) { _, _ -> removeSticker(unit.id) }.condition(unit.creatorId == ControllerApi.account.id)
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.stickers_report_confirm, R.string.sticker_error_gone) }
                .add(if (ControllerSettings.accountSettings.stickers.contains(unit.id)) R.string.sticker_remove else R.string.sticker_add) { _, _ -> addStickerToCollection(unit) }.condition(unit.status == API.STATUS_PUBLIC)
                .add(R.string.app_clear_reports) { _, _ -> ControllerUnits.clearReports(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.reportsCount > 0 && unit.creatorId != ControllerApi.account.id)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && unit.creatorId != ControllerApi.account.id)
                .asPopupShow(view, x, y)
    }


    fun addStickerToCollection(unit: UnitSticker) {
        val inCollection = !ControllerSettings.accountSettings.stickers.contains(unit.id)

        ApiRequestsSupporter.executeProgressDialog(RStickerCollectionChange(unit.id, inCollection)) { _->

            if (inCollection)
                ControllerSettings.accountSettings.stickers = ToolsCollections.add(unit.id, ControllerSettings.accountSettings.stickers)
            else
                ControllerSettings.accountSettings.stickers = ToolsCollections.removeItem(unit.id, ControllerSettings.accountSettings.stickers)

            EventBus.post(EventStickerCollectionChanged(unit))
        }
    }


    fun removeSticker(unitId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_remove_confirm, R.string.app_remove, RUnitsRemove(unitId)) {
            EventBus.post(EventUnitRemove(unitId))
            ToolsToast.show(R.string.app_done)
        }
    }


}