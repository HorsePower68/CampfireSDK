package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.CampfireLink
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.accounts.RAccountsGet
import com.dzen.campfire.api.requests.chat.RChatGet
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.classes.items.Item3

object ControllerCampfireObjects {

    private val cash:HashMap<String, Item3<String, String, Long>> = HashMap()
    private val inProgress:HashMap<String, ArrayList<(String, String, Long) -> Unit>> = HashMap()

    fun load(link: CampfireLink, onComplete: (String, String, Long) -> Unit) {

        if(cash.containsKey(link.linkRaw)){
            val get = cash.get(link.linkRaw)!!
            onComplete.invoke(get.a1, get.a2, get.a3)
            return
        }

        if(inProgress.containsKey(link.linkRaw)){
            val get = inProgress.get(link.linkRaw)
            get!!.add(onComplete)
            return
        }

        val list = ArrayList<(String, String, Long) -> Unit>()
        inProgress[link.linkRaw] = list
        list.add(onComplete)

        when {
            link.isLinkToAccount() -> loadAccount(link)
            link.isLinkToPost() -> loadPost(link)
            link.isLinkToChat() -> loadChat(link)
            link.isLinkToFandom() -> loadFandom(link)
            link.isLinkToStickersPack() -> loadStickersPack(link)
            else -> onError(link)
        }
    }

    private fun loadAccount(link: CampfireLink) {

        val id = link.getLongParamOrZero(0)
        val name = if(link.link.startsWith("@")) {
            if (link.link.length < 3) "" else link.link.removePrefix("@").replace("_", "")
        }else{
            link.params[0]
        }

        RAccountsGet(id, name)
                .onComplete { onComplete(link, it.account.name, ToolsResources.s(R.string.app_user), it.account.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadPost(link: CampfireLink) {
        val id = link.getLongParamOrZero(0)

        RPostGet(id)
                .onComplete { onComplete(link, it.unit.fandomName, ToolsResources.s(R.string.app_post), it.unit.fandomImageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadChat(link: CampfireLink) {
        val targetId = link.getLongParamOrZero(0)
        val targetSubId = link.getLongParamOrZero(1)

        RChatGet(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, targetId, targetSubId), 0)
                .onComplete { onComplete(link, it.chatName, ToolsResources.s(R.string.app_chat), it.chatImageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadFandom(link: CampfireLink) {
        val fandomId = link.getLongParamOrZero(0)
        val languageId = link.getLongParamOrZero(1)

        RFandomsGet(fandomId, languageId, ControllerApi.getLanguageId())
                .onComplete { onComplete(link, it.fandom.name, ToolsResources.s(R.string.app_fandom), it.fandom.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadStickersPack(link: CampfireLink) {
        val id = link.getLongParamOrZero(0)

        RStickersPacksGetInfo(id, 0)
                .onComplete { onComplete(link, it.stickersPack.name, ToolsResources.s(R.string.app_stickers), it.stickersPack.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun onComplete(link: CampfireLink, title:String, subtitle:String, image:Long){
        cash.put(link.linkRaw, Item3(title, subtitle, image))
        val callbacks = inProgress.get(link.linkRaw)
        if(callbacks != null){
            for(i in callbacks) i.invoke(title, subtitle, image)
            inProgress.remove(link.linkRaw)
        }
    }

    private fun onError(link: CampfireLink) {
        val callbacks = inProgress.get(link.linkRaw)
        if(callbacks != null){
            for(i in callbacks) i.invoke(ToolsResources.s(R.string.post_page_campfire_object_error), ToolsResources.s(R.string.app_error), 0)
            inProgress.remove(link.linkRaw)
        }
    }

}