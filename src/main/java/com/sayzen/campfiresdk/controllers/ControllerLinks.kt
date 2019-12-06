package com.sayzen.campfiresdk.controllers

import android.text.util.Linkify
import android.view.Gravity
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
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

            var t: String
            if (link.startsWith(API.DOMEN)) {
                t = link.substring(API.DOMEN.length)
            } else {
                t = link.substring("http://@".length)
                t = t.replace("_", "-")
            }

            val s1 = t.split("-")
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            when (linkV) {
                API.LINK_ABOUT.link -> Navigator.to(SAboutApp())
                API.LINK_RULES_USER.link -> Navigator.to(SRulesUser())
                API.LINK_RULES_MODER.link -> Navigator.to(SRulesModerators())
                API.LINK_CREATORS.link -> Navigator.to(SAboutCreators())
                API.LINK_BOX_WITH_FIREWORKS.link -> {
                    ControllerScreenAnimations.fireworks()
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_FIREWORKS.index).send(api) }
                }
                API.LINK_BOX_WITH_SUMMER.link -> ControllerScreenAnimations.summer()
                API.LINK_BOX_WITH_AUTUMN.link -> ControllerScreenAnimations.autumn()
                API.LINK_BOX_WITH_WINTER.link -> ControllerScreenAnimations.winter()
                API.LINK_BOX_WITH_BOMB.link -> ControllerScreenAnimations.bomb()
                API.LINK_BOX_WITH_CRASH.link -> ControllerScreenAnimations.crash()
                API.LINK_STICKER.link -> SStickersView.instanceBySticker(params[0].toLong(), Navigator.TO)
                API.LINK_STICKERS_PACK.link -> {
                    if (params.size == 1) SStickersView.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) Navigator.to(SComments(params[0].toLong(), params[1].toLong()))
                }
                API.LINK_POST.link -> {
                    if (params.size == 1) SPost.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SPost.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_REVIEW.link -> {
                    SReviews.instance(params[0].toLong(), Navigator.TO)
                }
                API.LINK_FANDOM.link -> {
                    if (params.size == 1) SFandom.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SFandom.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_PROFILE_ID.link -> SAccount.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_PROFILE_NAME -> SAccount.instance(params[0], Navigator.TO)
                API.LINK_TAG.link -> SPostsSearch.instance(params[0].toLong(), Navigator.TO)
                API.LINK_MODERATION.link -> {
                    if (params.size == 1) SModerationView.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SModerationView.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_CHAT.link -> {
                    if (params.size == 1) SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), 0), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), params[1].toLong()), 0, true, Navigator.TO)
                }
                API.LINK_CONF.link -> {
                    if (params.size == 1) SChat.instance(ChatTag(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), 0), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(ChatTag(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), params[1].toLong()), 0, true, Navigator.TO)
                }
                API.LINK_WIKI_FANDOM.link -> SWikiList.instanceFandomId(params[0].toLong(), Navigator.TO)
                API.LINK_WIKI_SECTION.link -> SWikiList.instanceItemId(params[0].toLong(), Navigator.TO)
                API.LINK_WIKI_ARTICLE.link -> SWikiArticleView.instance(params[0].toLong(), Navigator.TO)
                API.LINK_FANDOM_CHAT.link -> SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, params[0].toLong(), 0), 0, false, Navigator.TO)
                API.LINK_ACTIVITY.link -> SRelayRaceInfo.instance(params[0].toLong(), Navigator.TO)
                API.LINK_RUBRIC.link -> SRubricPosts.instance(params[0].toLong(), Navigator.TO)
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
    fun linkToAccount(id: Long) = API.LINK_PROFILE_ID.asWeb() + id
    fun linkToFandom(fandomId: Long) = API.LINK_FANDOM.asWeb() + fandomId
    fun linkToFandom(fandomId: Long, languageId: Long) = API.LINK_FANDOM.asWeb() + fandomId + "_" + languageId
    fun linkToPost(postId: Long) = API.LINK_POST.asWeb() + postId
    fun linkToReview(reviewId: Long) = API.LINK_REVIEW.asWeb() + reviewId
    fun linkToModeration(moderationId: Long) = API.LINK_MODERATION.asWeb() + moderationId
    fun linkToWikiFandomId(fandomId: Long) = API.LINK_WIKI_FANDOM.asWeb() + fandomId
    fun linkToWikiItemId(itemId: Long) = API.LINK_WIKI_SECTION.asWeb() + itemId
    fun linkToWikiArticle(itemId: Long) = API.LINK_WIKI_ARTICLE.asWeb() + itemId
    fun linkToFandomChat(chatId: Long) = API.LINK_FANDOM_CHAT.asWeb() + chatId
    fun linkToActivity(activityId: Long) = API.LINK_ACTIVITY.asWeb() + activityId
    fun linkToRubric(rubricId: Long) = API.LINK_RUBRIC.asWeb() + rubricId
    fun linkToSticker(id: Long) = API.LINK_STICKER.asWeb() + id
    fun linkToStickersPack(id: Long) = API.LINK_STICKERS_PACK.asWeb() + id
    fun linkToPostComment(parentPublicationId: Long, commentId: Long) = API.LINK_POST.asWeb() + parentPublicationId + "_" + commentId
    fun linkToModerationComment(parentPublicationId: Long, commentId: Long) = API.LINK_MODERATION.asWeb() + parentPublicationId + "_" + commentId
    fun linkToStickersComment(parentPublicationId: Long, commentId: Long) = API.LINK_STICKERS_PACK.asWeb() + parentPublicationId + "_" + commentId
    fun linkToChat(fandomId: Long) = API.LINK_CHAT.asWeb() + fandomId
    fun linkToChat(fandomId: Long, languageId: Long) = API.LINK_CHAT.asWeb() + fandomId + "_" + languageId
    fun linkToChatMessage(messageId: Long, fandomId: Long, languageId: Long) = API.LINK_CHAT.asWeb() + fandomId + "_" + languageId + "_" + messageId
    fun linkToConf(chatId: Long) = API.LINK_CONF.asWeb() + chatId
    fun linkToConfMessage(messageId: Long, chatId: Long) = API.LINK_CONF.asWeb() + chatId + "_" + messageId
    fun linkToEvent(eventId: Long) = API.LINK_EVENT.asWeb() + eventId
    fun linkToTag(tagId: Long) = API.LINK_TAG.asWeb() + tagId
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

        replaceLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME)

        for (i in API.LINKS_ARRAY) replaceLinkable(vText, i.asLink(), i.asWeb())

        onReplace.invoke()
        ControllerApi.makeTextHtml(vText)

        for (i in API.LINKS_ARRAY) if (i.isInnerLink) makeLinkableInner(vText, i.asLink(), i.asWeb()) else makeLinkable(vText, i.asLink(), i.asWeb())
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