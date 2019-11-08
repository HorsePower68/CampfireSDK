package com.sayzen.campfiresdk.screens.post.create.creators

import com.dzen.campfire.api.models.publications.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.*
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.sheets.Sheet
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetMenu

class WidgetAdd(
        private val requestPutPage:(Page, Screen?, Widget?, (Page) -> CardPage, ((CardPage)->Unit))->Unit,
        private val requestChangePage: (Page, CardPage, Screen?, Widget?, (Page)->Unit) -> Unit,
        private val onBackEmptyAndNewerAdd: () -> Unit
) : WidgetMenu() {

    var wasClicked = false
    var wasShowed = false

    init {
        add(R.string.post_page_text) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePageText(requestPutPage, requestChangePage, null, null))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_text_fields_24dp))
        add(R.string.post_page_image) { _, _ ->
            wasClicked = true
            WidgetPageImage(requestPutPage, requestChangePage).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_landscape_24dp))
        add(R.string.post_page_video) { _, _ ->
            wasClicked = true
            WidgetPageVideo(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_play_arrow_24dp))
        add(R.string.post_page_quote) { _, _ ->
            wasClicked = true
            WidgetPageQuote(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_format_quote_24dp))
        add(R.string.post_page_link) { _, _ ->
            wasClicked = true
            WidgetPageLink(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_insert_link_24dp))
        add(R.string.post_page_spoiler) { _, _ ->
            wasClicked = true
            WidgetPageSpoiler(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_code_24dp))
        add(R.string.post_page_polling) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePagePolling(requestPutPage, requestChangePage, null, null))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_check_box_24dp))
        add(R.string.post_page_table) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePageTable(requestPutPage, requestChangePage, null, null))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_border_all_24dp))
        add(R.string.post_page_campfire_object) { _, _ ->
            wasClicked = true
            WidgetPageCampfireObject(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_whatshot_24dp))
    }

    fun changePage(c: CardPage) {
        if (c is CardPageText) Navigator.to(SCreatePageText(requestPutPage, requestChangePage, c, c.page as PageText))
        if (c is CardPageImage) WidgetPageImage.change(c.page, requestPutPage, requestChangePage, c)
        if (c is CardPageVideo) WidgetPageVideo(requestPutPage, requestChangePage, c, c.page as PageVideo).asSheetShow()
        if (c is CardPageQuote) WidgetPageQuote(requestPutPage, requestChangePage, c, c.page as PageQuote).asSheetShow()
        if (c is CardPageLink) WidgetPageLink(requestPutPage, requestChangePage, c, c.page as PageLink).asSheetShow()
        if (c is CardPageSpoiler) WidgetPageSpoiler(requestPutPage, requestChangePage, c, c.page as PageSpoiler).asSheetShow()
        if (c is CardPagePolling) Navigator.to(SCreatePagePolling(requestPutPage, requestChangePage, c, c.page as PagePolling))
        if (c is CardPageImages) WidgetPageImages(requestChangePage, c, c.page as PageImages).asSheetShow()
        if (c is CardPageTable) Navigator.to(SCreatePageTable(requestPutPage, requestChangePage, c, c.page as PageTable))
        if (c is CardPageCampfireObject) WidgetPageCampfireObject(requestPutPage, requestChangePage, c, c.page as PageCampfireObject).asSheetShow()
    }

    override fun asSheetShow(): Sheet {
        wasShowed = true
        return super.asSheetShow()
    }

    override fun onHide() {
        super.onHide()
        if (wasShowed && !wasClicked) onBackEmptyAndNewerAdd.invoke()    //  Для того чтобы выйти из создания поста на клавишу Back
    }

    companion object {

        fun showConfirmCancelDialog(widget: Widget) {
            widget.setEnabled(false)
            WidgetAlert()
                    .setText(R.string.post_create_cancel_alert)
                    .setOnEnter(R.string.app_yes) { _ -> widget.hide() }
                    .setOnCancel(R.string.app_no)
                    .setOnHide { widget.setEnabled(true) }
                    .asSheetShow()
        }

        fun showConfirmCancelDialog(screen: Screen, onYes: (() -> Unit)? = null) {
            WidgetAlert()
                    .setText(R.string.post_create_cancel_alert)
                    .setOnEnter(R.string.app_yes) {
                        if (onYes == null) Navigator.remove(screen)
                        else onYes.invoke()
                    }
                    .setOnCancel(R.string.app_no)
                    .asSheetShow()
        }
    }
}
