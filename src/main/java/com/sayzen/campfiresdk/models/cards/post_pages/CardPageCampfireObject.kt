package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.CampfireLink
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageCampfireObject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireObjects
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardPageCampfireObject(
        pagesContainer: PagesContainer?,
        page: PageCampfireObject
) : CardPage(R.layout.card_page_campfire_object, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageCampfireObject

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatarTitleObject)
        val vTouch: View = view.findViewById(R.id.vTouch)

        vAvatar.tag = this
        vAvatar.vAvatar.vImageView.setImageResource(R.color.focus)
        vAvatar.setTitle(R.string.app_loading)
        vAvatar.setSubtitle("")
        val link = CampfireLink(page.link)
        ControllerCampfireObjects.load(link) { title, subtitle, imageId ->
            if (vAvatar.tag == this) {
                vAvatar.setTitle(title)
                vAvatar.setSubtitle(subtitle)
                if (imageId > 0) ToolsImagesLoader.load(imageId).into(vAvatar.vAvatar.vImageView)
                else vAvatar.vAvatar.vImageView.setImageResource(R.color.focus)
            }
        }

        vTouch.isClickable = true
        vTouch.isEnabled = true
        vTouch.isFocusable = true
        vTouch.setOnClickListener {
            when {
                link.isLinkToAccount() -> {
                    val id = link.getLongParamOrZero(0)
                    val name = if (link.link.length < 3) "" else link.link.removePrefix("@").replace("_", "")
                    if (id > 0) SAccount.instance(id, Navigator.TO)
                    else SAccount.instance(name, Navigator.TO)
                }
                link.isLinkToPost() -> SPost.instance(link.getLongParamOrZero(0), Navigator.TO)
                link.isLinkToChat() -> SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, link.getLongParamOrZero(0), link.getLongParamOrZero(1)), 0, false, Navigator.TO)
                link.isLinkToFandom() -> SFandom.instance(link.getLongParamOrZero(0), link.getLongParamOrZero(1), Navigator.TO)
                link.isLinkToStickersPack() -> SStickersView.instance(link.getLongParamOrZero(0), Navigator.TO)
            }
        }


    }

    override fun notifyItem() {
        ControllerCampfireObjects.load(CampfireLink((page as PageCampfireObject).link)) { _, _, _ -> }
    }


}
