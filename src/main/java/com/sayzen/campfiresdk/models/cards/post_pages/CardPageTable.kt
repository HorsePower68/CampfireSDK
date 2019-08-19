package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.units.post.PageTable
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.views.table.ViewTable

class CardPageTable(
        unit: UnitPost?,
        page: PageTable
) : CardPage(R.layout.card_page_table, unit, page) {

    override fun bindView(view: View) {
        super.bindView(view)

        val page = page as PageTable

        val vTable:ViewTable = view.findViewById(R.id.vTable)

        vTable.clear()
        vTable.setColumnsCount(page.columnsCount, true)
        vTable.createRows(page.rowsCount, true)
        for(c in page.cells) {
            if(c.type == PageTable.CELL_TYPE_TEXT) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentText(c.text)
            if(c.type == PageTable.CELL_TYPE_IMAGE) vTable.getCell(c.rowIndex, c.columnIndex)?.setContentImageId(c.imageId)
        }

    }

    override fun notifyItem() {}
}
