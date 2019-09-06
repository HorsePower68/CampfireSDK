package com.sayzen.campfiresdk.screens.fandoms.view


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.FandomLink
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationLinkAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationLinkRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.fandom.EventFandomInfoChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

class CardLinks(
        private val xFandom: XFandom,
        private var links: Array<FandomLink>
) : Card(R.layout.screen_fandom_card_links) {

    private val eventBus = EventBus
            .subscribe(EventFandomInfoChanged::class) { this.onEventFandomInfoChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vLinksContainer: ViewGroup = view.findViewById(R.id.vLinksContainer)
        val vEmptyText: TextView = view.findViewById(R.id.vEmptyText)
        val vAdd: ViewIcon = view.findViewById(R.id.vAdd)

        vLinksContainer.visibility = if (links.isEmpty()) View.GONE else View.VISIBLE

        vAdd.visibility = if (ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_LINKS)) View.VISIBLE else View.INVISIBLE
        vAdd.setOnClickListener { addLink() }

        vEmptyText.visibility = if (links.isEmpty()) View.VISIBLE else View.GONE

        vLinksContainer.removeAllViews()
        for (link in links) {
            val vLink: View = ToolsView.inflate(R.layout.screen_fandom_card_info_link)
            val vLinkTitle: TextView = vLink.findViewById(R.id.vLinkTitle)
            val vLinkSubtitle: TextView = vLink.findViewById(R.id.vLinkUrl)
            val vLinkImage: ImageView = vLink.findViewById(R.id.vLinkImage)
            vLinkTitle.text = link.title
            vLinkSubtitle.text = link.url

            val w = WidgetMenu()
                    .add(R.string.app_copy_link) { _, _ ->
                        ToolsAndroid.setToClipboard(link.url)
                        ToolsToast.show(R.string.app_copied)
                    }
                    .add(R.string.app_remove) { _, _ -> removeLink(link) }.condition(ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_LINKS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)

            vLink.setOnLongClickListener {
                w.asSheetShow()
                true
            }
            vLink.setOnClickListener { ControllerCampfireSDK.openLink(link.url) }

            when (link.imageIndex) {
                1L -> vLinkImage.setImageResource(ToolsResources.getDrawableAttrId(R.attr.icon_youtube))
                2L -> vLinkImage.setImageResource(ToolsResources.getDrawableAttrId(R.attr.icon_discord))
                3L -> vLinkImage.setImageResource(ToolsResources.getDrawableAttrId(R.attr.icon_wiki))
                4L -> vLinkImage.setImageResource(ToolsResources.getDrawableAttrId(R.attr.icon_twitter))
                else -> vLinkImage.setImageResource(ToolsResources.getDrawableAttrId(R.attr.ic_insert_link_24dp))
            }

            vLinksContainer.addView(vLink)
        }
    }

    private fun removeLink(link: FandomLink) {
        WidgetField()
                .setTitle(R.string.app_remove_link)
                .setHint(R.string.moderation_widget_comment)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_remove) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(R.string.fandom_link_remove_confirm, R.string.app_remove, RFandomsModerationLinkRemove(link.index, comment)) {
                        val list = ArrayList<FandomLink>()
                        for (l in links) if (l.index != link.index) list.add(l)
                        EventBus.post(EventFandomInfoChanged(xFandom.fandomId, xFandom.languageId, emptyArray(), list.toTypedArray()))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    private fun addLink() {
        if (links.size >= API.FANDOM_GALLERY_MAX) {
            ToolsToast.show(R.string.error_too_many_items)
            return
        }
        WidgetLink { url, title, comment, imageIndex ->
            ApiRequestsSupporter.executeProgressDialog(RFandomsModerationLinkAdd(xFandom.fandomId, xFandom.languageId, title, url, imageIndex, comment)) { r ->
                val array = Array(links.size + 1) {
                    if (links.size == it) {
                        val link = FandomLink()
                        link.index = r.linkIndex
                        link.imageIndex = imageIndex
                        link.url = url
                        link.title = title
                        link
                    } else links[it]
                }
                EventBus.post(EventFandomInfoChanged(xFandom.fandomId, xFandom.languageId, emptyArray(), array))
                ToolsToast.show(R.string.app_done)
            }
        }.asSheetShow()
    }


    //
    //  EventBus
    //

    private fun onEventFandomInfoChanged(e: EventFandomInfoChanged) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId) {
            if (e.links.isNotEmpty()) this.links = e.links
            update()
        }
    }


}
