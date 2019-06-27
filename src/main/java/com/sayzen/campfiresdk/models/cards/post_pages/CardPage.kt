package com.sayzen.campfiresdk.models.cards.post_pages

import android.support.annotation.CallSuper
import android.view.View
import com.dzen.campfire.api.models.units.post.*
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.WidgetMenu

abstract class CardPage(
        val unit: UnitPost?,
        page: Page
) : Card(), NotifyItem {

    companion object {

        //
        //  Static
        //

        fun instance(unit: UnitPost?, page: Page): CardPage {
            if (page is PageText) return CardPageText(unit, page)
            if (page is PageImage) return CardPageImage(unit, page)
            if (page is PageImages) return CardPageImages(unit, page)
            if (page is PageLink) return CardPageLink(unit, page)
            if (page is PageQuote) return CardPageQuote(unit, page)
            if (page is PageSpoiler) return CardPageSpoiler(unit, page)
            if (page is PagePolling) return CardPagePolling(unit, page)
            if (page is PageVideo) return CardPageVideo(unit, page)
            if (page is PageTable) return CardPageTable(unit, page)
            if (page is PageDownload) return CardPageDownload(unit, page)

            throw RuntimeException("Unknown page $page")
        }
    }

    open var page = page
    var hided = false
    var editMode: Boolean = false
    var postIsDraft: Boolean = false
    var clickable = true
    var widgetMenu: WidgetMenu
    private var onMoveClicked: ((CardPage) -> Unit)? = null
    private var onChangeClicked: ((CardPage) -> Unit)? = null
    private var onRemoveClicked: ((CardPage) -> Unit)? = null

    init {
        widgetMenu = WidgetMenu()
                .add(getChangeMenuItemText()) { w, c -> if (onChangeClicked != null) onChangeClicked!!.invoke(this) }
                .add(R.string.app_remove) { w, c -> if (onRemoveClicked != null) onRemoveClicked!!.invoke(this) }
                .add(R.string.app_move) { w, c -> if (onMoveClicked != null) onMoveClicked!!.invoke(this) }
    }

    @CallSuper
    override fun bindView(view: View) {
        super.bindView(view)
        view.visibility = if (hided) View.GONE else View.VISIBLE
        val vMore = view.findViewById<ViewIcon>(R.id.vMore)

        vMore.visibility = if (editMode) View.VISIBLE else View.GONE
        if (editMode) vMore.setOnClickListener { widgetMenu.asSheetShow() }

    }

    fun setEditMod(editMode: Boolean,
                   onMoveClicked: (CardPage) -> Unit,
                   onChangeClicked: (CardPage) -> Unit,
                   onRemoveClicked: (CardPage) -> Unit): CardPage {
        this.editMode = editMode
        this.postIsDraft = this.postIsDraft || editMode
        this.onMoveClicked = onMoveClicked
        this.onChangeClicked = onChangeClicked
        this.onRemoveClicked = onRemoveClicked
        update()
        return this
    }

    fun setHidedX(hided: Boolean) {
        this.hided = hided
        update()
    }

    open fun getChangeMenuItemText() = R.string.app_change


}
