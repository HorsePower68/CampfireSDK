package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.CampfireLink
import com.dzen.campfire.api.models.units.post.PageCampfireObject
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireObjects
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.forums.view.SForumView
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardPageCampfireObject(
        unit: UnitPost?,
        page: PageCampfireObject
) : CardPage(R.layout.card_page_campfire_object, unit, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageCampfireObject

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatarTitle)

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

        vAvatar.setOnClickListener {
            when {
                link.isLinkToAccount() -> {
                    val id = link.getLongParamOrZero(0)
                    val name = if (link.link.length < 3) "" else link.link.removePrefix("@").replace("_", "")
                    if (id > 0) SAccount.instance(id, Navigator.TO)
                    else SAccount.instance(name, Navigator.TO)
                }
                link.isLinkToPost() -> SPost.instance(link.getLongParamOrZero(0), Navigator.TO)
                link.isLinkToChat() -> SChat.instance(API.CHAT_TYPE_FANDOM, link.getLongParamOrZero(0), link.getLongParamOrZero(1), false, Navigator.TO)
                link.isLinkToForum() -> SForumView.instance(link.getLongParamOrZero(0), Navigator.TO)
                link.isLinkToFandom() -> SFandom.instance(link.getLongParamOrZero(0), link.getLongParamOrZero(1), Navigator.TO)
                link.isLinkToStickersPack() -> SStickersView.instance(link.getLongParamOrZero(0), Navigator.TO)
            }
        }

    }

    override fun notifyItem() {
        ControllerCampfireObjects.load(CampfireLink((page as PageCampfireObject).link)) { title, subtitle, imageId -> }
    }



}
