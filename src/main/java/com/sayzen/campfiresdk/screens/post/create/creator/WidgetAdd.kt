package com.sayzen.campfiresdk.screens.post.create.creator

import com.dzen.campfire.api.models.units.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.*
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetMenu

class WidgetAdd(
        private val screen: SPostCreate
) : WidgetMenu() {

    var wasClicked: Boolean = false

    init {
        add(R.string.post_page_text) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePageText(screen, null, null))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_text_fields_24dp))
        add(R.string.post_page_image) { _, _ ->
            wasClicked = true
            WidgetPageImage(screen).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_landscape_24dp))
        add(R.string.post_page_video) { _, _ ->
            wasClicked = true
            WidgetPageVideo(screen, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_play_arrow_24dp))
        add(R.string.post_page_images) { _, _ ->
            wasClicked = true
            WidgetPageImages(screen, null, null, screen.adapter.size(CardPage::class)).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_burst_mode_24dp))
        add(R.string.post_page_quote) { _, _ ->
            wasClicked = true
            WidgetPageQuote(screen, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_format_quote_24dp))
        add(R.string.post_page_link) { _, _ ->
            wasClicked = true
            WidgetPageLink(screen, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_insert_link_24dp))
        add(R.string.post_page_spoiler) { _, _ ->
            wasClicked = true
            WidgetPageSpoiler(screen, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_code_24dp))
        add(R.string.post_page_polling) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePagePolling(screen, null, null))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_check_box_24dp))
        add(R.string.post_page_table) { _, _ ->
            wasClicked = true
            Navigator.to(SCreatePageTable(screen, null, null, screen.adapter.size(CardPage::class)))
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_border_all_24dp))
        add(R.string.post_page_campfire_object) { _, _ ->
            wasClicked = true
            WidgetPageCampfireObject(screen, null, null).asSheetShow()
        }.icon(ToolsResources.getDrawableAttrId(R.attr.ic_whatshot_24dp))
    }

    fun changePage(c: CardPage) {
        if (c is CardPageText) Navigator.to(SCreatePageText(screen, c, c.page as PageText))
        if (c is CardPageImage) WidgetPageImage.change(c.page, screen, c)
        if (c is CardPageVideo) WidgetPageVideo(screen, c, c.page as PageVideo).asSheetShow()
        if (c is CardPageQuote) WidgetPageQuote(screen, c, c.page as PageQuote).asSheetShow()
        if (c is CardPageLink) WidgetPageLink(screen, c, c.page as PageLink).asSheetShow()
        if (c is CardPageSpoiler) WidgetPageSpoiler(screen, c, c.page as PageSpoiler).asSheetShow()
        if (c is CardPagePolling) Navigator.to(SCreatePagePolling(screen, c, c.page as PagePolling))
        if (c is CardPageImages) WidgetPageImages(screen, c, c.page as PageImages, screen.adapter.indexOf(c)).asSheetShow()
        if (c is CardPageTable) Navigator.to(SCreatePageTable(screen, c,  c.page as PageTable, screen.adapter.indexOf(c)))
        if (c is CardPageCampfireObject) WidgetPageCampfireObject(screen, c, c.page as PageCampfireObject).asSheetShow()
    }

    override fun onHide() {
        super.onHide()
        if (!wasClicked) screen.backIfEmptyAndNewerAdd()
    }

    companion object {

        fun showConfirmCancelDialog(widget: Widget) {
            widget.setEnabled(false)
            WidgetAlert()
                    .setText(R.string.post_create_cancel_alert)
                    .setOnEnter(R.string.app_yes) { _ -> widget.hide() }
                    .setOnCancel(R.string.app_no)
                    .setOnHide {  widget.setEnabled(true) }
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
