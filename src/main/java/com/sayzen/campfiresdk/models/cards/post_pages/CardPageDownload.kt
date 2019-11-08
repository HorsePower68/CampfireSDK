package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.models.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageDownload
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.apiMedia
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class CardPageDownload(
        pagesContainer: PagesContainer?,
        page: PageDownload
) : CardPage(R.layout.card_page_download, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageDownload
        val vSubTitle: TextView = view.findViewById(R.id.vSubTitle)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vDownload: Button = view.findViewById(R.id.vDownload)


        vTitle.text = ToolsResources.s(R.string.app_file) + ": " + ToolsText.numToBytesString(page.size)

        vSubTitle.text = page.title
        if (page.patch.isNotEmpty()) {
            vSubTitle.text = vSubTitle.text.toString() + if (vSubTitle.text.isNotEmpty()) "\n" else ""
            vSubTitle.text = vSubTitle.text.toString() + page.patch
        }
        if (page.autoUnzip) {
            vSubTitle.text = vSubTitle.text.toString() + if (vSubTitle.text.isNotEmpty()) "\n" else ""
            vSubTitle.text = vSubTitle.text.toString() + ToolsResources.s(R.string.post_page_download_unzip)
        }
        vSubTitle.visibility = if (vSubTitle.text.isEmpty()) View.GONE else View.VISIBLE

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

                    val dProgress = ToolsView.showProgressDialog(R.string.app_downloading)
                    RResourcesGet(page.resourceId)
                            .onComplete { r ->
                                ToolsPermission.requestWritePermission {
                                    val dProgress2 = ToolsView.showProgressDialog(R.string.app_downloading)
                                    ToolsThreads.thread {
                                        ToolsFilesAndroid.unpackZip(page.patch, r.bytes)
                                        ToolsThreads.main {
                                            dProgress2.hide()
                                            dProgress.hide()    //  Почему-то не скрывается в onFinish
                                            ToolsToast.show(R.string.app_done)
                                        }
                                    }
                                }
                            }
                            .onFinish { dProgress.hide() }
                            .send(apiMedia)
                }
                .asSheetShow()


    }

    override fun notifyItem() {

    }

}
