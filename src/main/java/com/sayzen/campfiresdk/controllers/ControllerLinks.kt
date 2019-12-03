package com.sayzen.campfiresdk.controllers

import android.text.util.Linkify
import android.view.Gravity
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.reviews.SReviews
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.other.about.SAboutApp
import com.sayzen.campfiresdk.screens.other.about.SAboutCreators
import com.sayzen.campfiresdk.screens.other.rules.SRulesModerators
import com.sayzen.campfiresdk.screens.other.rules.SRulesUser
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleView
import com.sayzen.campfiresdk.screens.wiki.SWikiList
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsThreads
import java.util.regex.Pattern

object ControllerLinks {

    fun parseLink(link: String): Boolean {
        try {

            var t:String
            if(link.startsWith(API.DOMEN)){
                t = link.substring(API.DOMEN.length)
            } else{
                t = link.substring("http://@".length)
                t = t.replace("_", "-")
            }

            val s1 = t.split("-")
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            when (linkV) {
                API.LINK_TAG_ABOUT -> Navigator.to(SAboutApp())
                API.LINK_TAG_RULES_USER -> Navigator.to(SRulesUser())
                API.LINK_TAG_RULES_MODER -> Navigator.to(SRulesModerators())
                API.LINK_TAG_CREATORS -> Navigator.to(SAboutCreators())
                API.LINK_TAG_BOX_WITH_FIREWIRKS -> {
                    ControllerScreenAnimations.fireworks()
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_FIREWORKS.index).send(api) }
                }
                API.LINK_TAG_BOX_WITH_SUMMER -> ControllerScreenAnimations.summer()
                API.LINK_TAG_BOX_WITH_AUTUMN -> ControllerScreenAnimations.autumn()
                API.LINK_TAG_BOX_WITH_WINTER -> ControllerScreenAnimations.winter()
                API.LINK_TAG_STICKER -> SStickersView.instanceBySticker(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_STICKERS_PACK -> {
                    if (params.size == 1) SStickersView.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) Navigator.to(SComments(params[0].toLong(), params[1].toLong()))
                }
                API.LINK_TAG_POST -> {
                    if (params.size == 1) SPost.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SPost.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_TAG_REVIEW -> {
                    SReviews.instance(params[0].toLong(), Navigator.TO)
                }
                API.LINK_TAG_FANDOM -> {
                    if (params.size == 1) SFandom.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SFandom.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_TAG_PROFILE_ID -> SAccount.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_PROFILE_NAME -> SAccount.instance(params[0], Navigator.TO)
                API.LINK_TAG_TAG -> SPostsSearch.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_MODERATION -> {
                    if (params.size == 1) SModerationView.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SModerationView.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_TAG_CHAT -> {
                    if (params.size == 1) SChat.instance(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), params[1].toLong(), true, Navigator.TO)
                }
                API.LINK_TAG_CONF -> {
                    if (params.size == 1) SChat.instance(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), params[1].toLong(), true, Navigator.TO)
                }
                API.LINK_TAG_WIKI_FANDOM -> SWikiList.instanceFandomId(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_WIKI_SECTION -> SWikiList.instanceItemId(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_WIKI_ARTICLE -> SWikiArticleView.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_FANDOM_CHAT -> SChat.instance(API.CHAT_TYPE_FANDOM_SUB, params[0].toLong(), 0, false, Navigator.TO)
                API.LINK_TAG_ACTIVITY -> SRelayRaceInfo.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_RUBRIC -> SRubricPosts.instance(params[0].toLong(), Navigator.TO)
                else -> {
                    info("ControllerExecutorLinks link was't found [$link]")
                    return false
                }

            }
            return true

        } catch (e: Throwable) {
            err(e)
            return false
        }
    }

    fun openLink(link: String) {
        if (parseLink(link)) return
        WidgetAlert()
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_open) { ToolsIntent.openLink(link) }
                .setText(R.string.message_link)
                .setTextGravity(Gravity.CENTER)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .asSheetShow()
    }

    fun linkToAccount(name: String) = API.LINK_PROFILE_NAME + name
    fun linkToAccount(id: Long) = API.LINK_PROFILE_ID + id
    fun linkToFandom(fandomId: Long) = API.LINK_FANDOM + fandomId
    fun linkToFandom(fandomId: Long, languageId: Long) = API.LINK_FANDOM + fandomId + "_" + languageId
    fun linkToPost(postId: Long) = API.LINK_POST + postId
    fun linkToReview(reviewId: Long) = API.LINK_REVIEW + reviewId
    fun linkToModeration(moderationId: Long) = API.LINK_MODERATION + moderationId
    fun linkToWikiFandomId(fandomId: Long) = API.LINK_WIKI_FANDOM + fandomId
    fun linkToWikiItemId(itemId: Long) = API.LINK_WIKI_SECTION + itemId
    fun linkToWikiArticle(itemId: Long) = API.LINK_WIKI_ARTICLE + itemId
    fun linkToFandomChat(chatId: Long) = API.LINK_FANDOM_CHAT + chatId
    fun linkToActivity(activityId: Long) = API.LINK_ACTIVITY + activityId
    fun linkToRubric(rubricId: Long) = API.LINK_RUBRIC + rubricId
    fun linkToSticker(id: Long) = API.LINK_STICKER + id
    fun linkToStickersPack(id: Long) = API.LINK_STICKERS_PACK + id
    fun linkToPostComment(parentPublicationId: Long, commentId: Long) = API.LINK_POST + parentPublicationId + "_" + commentId
    fun linkToModerationComment(parentPublicationId: Long, commentId: Long) = API.LINK_MODERATION + parentPublicationId + "_" + commentId
    fun linkToStickersComment(parentPublicationId: Long, commentId: Long) = API.LINK_STICKERS_PACK + parentPublicationId + "_" + commentId
    fun linkToChat(fandomId: Long) = API.LINK_CHAT + fandomId
    fun linkToChat(fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId
    fun linkToChatMessage(messageId: Long, fandomId: Long, languageId: Long) = API.LINK_CHAT + fandomId + "_" + languageId + "_" + messageId
    fun linkToConf(chatId: Long) = API.LINK_CONF + chatId
    fun linkToConfMessage(messageId: Long, chatId: Long) = API.LINK_CONF + chatId + "_" + messageId
    fun linkToEvent(eventId: Long) = API.LINK_EVENT + eventId
    fun linkToTag(tagId: Long) = API.LINK_TAG + tagId
    fun linkToComment(comment: PublicationComment) = linkToComment(comment.id, comment.parentPublicationType, comment.parentPublicationId)
    fun linkToComment(commentId: Long, publicationType: Long, publicationId: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_POST -> linkToPostComment(publicationId, commentId)
            API.PUBLICATION_TYPE_MODERATION -> linkToModerationComment(publicationId, commentId)
            API.PUBLICATION_TYPE_STICKERS_PACK -> linkToStickersComment(publicationId, commentId)
            else -> ""
        }
    }

    fun makeLinkable(vText: ViewTextLinkable, onReplace: () -> Unit = {}) {

        replaceLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        replaceLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        replaceLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        replaceLinkable(vText, API.LINK_SHORT_CONF_ID, API.LINK_CONF)
        replaceLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        replaceLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        replaceLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        replaceLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        replaceLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        replaceLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        replaceLinkable(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        replaceLinkable(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        replaceLinkable(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        replaceLinkable(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_FIREWORKS, API.LINK_BOX_WITH_FIREWORKS)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_AUTUMN, API.LINK_BOX_WITH_AUTUMN)
        replaceLinkable(vText, API.LINK_SHORT_BOX_WITH_WINTER, API.LINK_BOX_WITH_WINTER)
        replaceLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_FANDOM, API.LINK_WIKI_FANDOM)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_SECTION, API.LINK_WIKI_SECTION)
        replaceLinkable(vText, API.LINK_SHORT_WIKI_ARTICLE, API.LINK_WIKI_ARTICLE)
        replaceLinkable(vText, API.LINK_SHORT_FANDOM_CHAT, API.LINK_FANDOM_CHAT)
        replaceLinkable(vText, API.LINK_SHORT_ACTIVITY, API.LINK_ACTIVITY)
        replaceLinkable(vText, API.LINK_SHORT_RUBRIC, API.LINK_RUBRIC)

        onReplace.invoke()
        ControllerApi.makeTextHtml(vText)

        makeLinkable(vText, API.LINK_SHORT_POST_ID, API.LINK_POST)
        makeLinkable(vText, API.LINK_SHORT_REVIEW_ID, API.LINK_REVIEW)
        makeLinkable(vText, API.LINK_SHORT_CHAT_ID, API.LINK_CHAT)
        makeLinkable(vText, API.LINK_SHORT_CONF_ID, API.LINK_CONF)
        makeLinkable(vText, API.LINK_SHORT_FANDOM_ID, API.LINK_FANDOM)
        makeLinkable(vText, API.LINK_SHORT_PROFILE_ID, API.LINK_PROFILE_ID)
        makeLinkable(vText, API.LINK_SHORT_MODERATION_ID, API.LINK_MODERATION)
        makeLinkable(vText, API.LINK_SHORT_WIKI_FANDOM, API.LINK_WIKI_FANDOM)
        makeLinkable(vText, API.LINK_SHORT_WIKI_SECTION, API.LINK_WIKI_SECTION)
        makeLinkable(vText, API.LINK_SHORT_WIKI_ARTICLE, API.LINK_WIKI_ARTICLE)
        makeLinkable(vText, API.LINK_SHORT_FANDOM_CHAT, API.LINK_FANDOM_CHAT)
        makeLinkable(vText, API.LINK_SHORT_ACTIVITY, API.LINK_ACTIVITY)
        makeLinkable(vText, API.LINK_SHORT_RUBRIC, API.LINK_RUBRIC)
        makeLinkable(vText, API.LINK_SHORT_STICKER, API.LINK_STICKER)
        makeLinkable(vText, API.LINK_SHORT_STICKERS_PACK, API.LINK_STICKERS_PACK)
        makeLinkable(vText, API.LINK_SHORT_EVENT, API.LINK_EVENT)
        makeLinkable(vText, API.LINK_SHORT_TAG, API.LINK_TAG)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_USER, API.LINK_RULES_USER)
        makeLinkableInner(vText, API.LINK_SHORT_RULES_MODER, API.LINK_RULES_MODER)
        makeLinkableInner(vText, API.LINK_SHORT_CREATORS, API.LINK_CREATORS)
        makeLinkableInner(vText, API.LINK_SHORT_ABOUT, API.LINK_ABOUT)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_FIREWORKS, API.LINK_BOX_WITH_FIREWORKS)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_SUMMER, API.LINK_BOX_WITH_SUMMER)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_AUTUMN, API.LINK_BOX_WITH_AUTUMN)
        makeLinkableInner(vText, API.LINK_SHORT_BOX_WITH_WINTER, API.LINK_BOX_WITH_WINTER)
        makeLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME, "([A-Za-z0-9#]+)")

        ToolsView.makeLinksClickable(vText)
    }

    private fun replaceLinkable(vText: TextView, short: String, link: String) {
        vText.text = vText.text.toString().replace(link, short)
    }

    private fun makeLinkableInner(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "([A-Za-z0-9_-]+)")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String, spec: String) {
        Linkify.addLinks(vText, Pattern.compile("$short$spec"), link, null, { _, url ->
            link + url.substring(short.length)
        })
    }



}