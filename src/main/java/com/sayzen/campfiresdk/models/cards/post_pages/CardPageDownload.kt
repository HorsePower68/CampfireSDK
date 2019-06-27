package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.units.post.PageDownload
import com.dzen.campfire.api.models.units.post.PageImage
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.tools.ToolsText

class CardPageDownload(
        unit: UnitPost?,
        page: PageDownload
) : CardPage(unit, page) {

    override fun getLayout() = R.layout.card_page_download

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageDownload
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vDownload: Button = view.findViewById(R.id.vDownload)

        vTitle.text = page.title
        if (page.patch.isNotEmpty()) {
            vTitle.text = vTitle.text.toString() + if (vTitle.text.isNotEmpty()) "\n" else ""
            vTitle.text = vTitle.text.toString() + page.patch
        }
        if (page.autoUnzip) {
            vTitle.text = vTitle.text.toString() + if (vTitle.text.isNotEmpty()) "\n" else ""
            vTitle.text = vTitle.text.toString() + ToolsResources.s(R.string.post_page_download_unzip)
        }
        vTitle.visibility = if (vTitle.text.isEmpty()) View.GONE else View.VISIBLE

        vDownload.text = ToolsResources.s(R.string.app_download) + "(${ToolsText.numToBytesString(page.size)})"

        if (clickable) vDownload.setOnClickListener { download() }
        else vDownload.setOnClickListener(null)

    }

    protected fun download() {
        val page = this.page as PageDownload
        WidgetAlert()
                .setOnCancel(R.string.app_cancel)
                .setText(R.string.post_page_download_alert)
                .setTextGravity(Gravity.CENTER)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setOnEnter(R.string.app_download) {

                    ApiRequestsSupporter.executeProgressDialog(R.string.app_downloading, RResourcesGet(page.resourceId)) { r ->

                        ToolsToast.show(R.string.app_done)
                    }

                }
                .asSheetShow()


    }

    override fun notifyItem() {

    }

}
