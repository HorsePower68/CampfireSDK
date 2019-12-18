package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.moderations.*
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesChange
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesCreate
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesRemove
import com.dzen.campfire.api.models.publications.moderations.fandom.*
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatChange
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatCreate
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatRemove
import com.dzen.campfire.api.models.publications.moderations.posts.*
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricChangeName
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricChangeOwner
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricCreate
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricRemove
import com.dzen.campfire.api.models.publications.moderations.tags.*
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationForgive
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostToDrafts
import com.dzen.campfire.api.requests.tags.RTagsMove
import com.dzen.campfire.api.requests.tags.RTagsMoveCategory
import com.dzen.campfire.api.requests.tags.RTagsMoveTag
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRestoreDeepBlock
import com.dzen.campfire.api.requests.bookmarks.RBookmarksChange
import com.dzen.campfire.api.requests.comments.RCommentsWatchChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomTagMove
import com.sayzen.campfiresdk.models.events.publications.*
import com.sayzen.campfiresdk.models.objects.TagParent
import com.sayzen.campfiresdk.models.widgets.WidgetCategoryCreate
import com.sayzen.campfiresdk.models.widgets.WidgetModerationBlock
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.tags.WidgetTagCreate
import com.sayzen.campfiresdk.screens.fandoms.tags.WidgetTagRemove
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import java.util.*

object ControllerPublications {

    fun getMaskForPost(mask:String, type:Long): String {
        if (mask.isNotEmpty()) return " \"${mask}\""
        return getPostPageName(type)
    }


    fun getMaskForComment(mask:String, type:Long): String {
        if (mask.isNotEmpty()) return " \"${mask}\""
        return getCommentName(type)
    }

    fun getCommentName(type:Long):String{
        if (type == PublicationComment.TYPE_IMAGE) return " \"${ToolsResources.s(R.string.app_image)}\""
        if (type == PublicationComment.TYPE_GIF) return " \"${ToolsResources.s(R.string.app_gif)}\""
        if (type == PublicationComment.TYPE_IMAGES) return " \"${ToolsResources.s(R.string.app_images)}\""
        if (type == PublicationComment.TYPE_STICKER) return " \"${ToolsResources.s(R.string.app_sticker)}\""
        return ""
    }

    fun getPostPageName(type:Long):String{
        if (type == API.PAGE_TYPE_IMAGE) return " \"${ToolsResources.s(R.string.post_page_image)}\""
        if (type == API.PAGE_TYPE_IMAGES) return " \"${ToolsResources.s(R.string.post_page_images)}\""
        if (type == API.PAGE_TYPE_LINK) return " \"${ToolsResources.s(R.string.post_page_link)}\""
        if (type == API.PAGE_TYPE_QUOTE) return " \"${ToolsResources.s(R.string.post_page_quote)}\""
        if (type == API.PAGE_TYPE_SPOILER) return " \"${ToolsResources.s(R.string.post_page_spoiler)}\""
        if (type == API.PAGE_TYPE_POLLING) return " \"${ToolsResources.s(R.string.post_page_polling)}\""
        if (type == API.PAGE_TYPE_VIDEO) return " \"${ToolsResources.s(R.string.post_page_video)}\""
        if (type == API.PAGE_TYPE_TABLE) return " \"${ToolsResources.s(R.string.post_page_table)}\""
        if (type == API.PAGE_TYPE_CAMPFIRE_OBJECT) return " \"${ToolsResources.s(R.string.post_page_campfire_object)}\""
        return ""
    }

    fun getName(publicationType: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_COMMENT -> ToolsResources.s(R.string.app_sticker)
            API.PUBLICATION_TYPE_CHAT_MESSAGE -> ToolsResources.s(R.string.app_message)
            API.PUBLICATION_TYPE_TAG -> ToolsResources.s(R.string.app_tag)
            API.PUBLICATION_TYPE_MODERATION -> ToolsResources.s(R.string.app_moderation)
            API.PUBLICATION_TYPE_POST -> ToolsResources.s(R.string.app_post)
            API.PUBLICATION_TYPE_REVIEW -> ToolsResources.s(R.string.app_review)
            API.PUBLICATION_TYPE_STICKERS_PACK -> ToolsResources.s(R.string.app_stickers_pack)
            API.PUBLICATION_TYPE_STICKER -> ToolsResources.s(R.string.app_sticker)
            else -> "[unknown]"
        }
    }

    fun toPublication(publicationType: Long, publicationId: Long, commentId: Long = 0) {

        if (publicationType == API.PUBLICATION_TYPE_POST) ControllerCampfireSDK.onToPostClicked(publicationId, commentId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_MODERATION) ControllerCampfireSDK.onToModerationClicked(publicationId, commentId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_STICKER) SStickersView.instanceBySticker(publicationId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) {
            SChat.instance(publicationId, true, Navigator.TO)
        }
        if (publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            if (commentId == 0L) SStickersView.instance(publicationId, Navigator.TO)
            else Navigator.to(SComments(publicationId, commentId))
        }
    }

    fun block(publication: Publication, onBlock: () -> Unit = {}) {
        WidgetModerationBlock.show(publication, onBlock)
    }

    fun report(publication: Publication) {
        ControllerApi.reportPublication(
                publication.id,
                R.string.post_report_confirm,
                R.string.post_error_gone
        )
    }

    fun clearReports(publication: Publication) {
        ControllerApi.clearReportsPublication(publication.id, publication.publicationType)
    }


    //
    //  Tag
    //

    fun createTagMenu(view: View, publication: PublicationTag, tags: Array<TagParent> = emptyArray()) {

        var parentTags = ArrayList<PublicationTag>()
        if (publication.parentPublicationId > 0) for (t in tags) if (t.tag.id == publication.parentPublicationId) parentTags = t.tags

        val w = WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerLinks.linkToTag(publication.id))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_TAGS))
                .add(R.string.app_change) { _, _ ->
                    if (publication.parentPublicationId == 0L) WidgetCategoryCreate(publication)
                    else WidgetTagCreate(publication)
                }
                .add(R.string.app_move) { _, _ ->
                    val menu = WidgetMenu()
                    for (tag in tags) {
                        if (publication.parentPublicationId != tag.tag.id) menu.add(tag.tag.name) { _, _ ->
                            WidgetField()
                                    .setHint(R.string.moderation_widget_comment)
                                    .setOnCancel(R.string.app_cancel)
                                    .setMin(API.MODERATION_COMMENT_MIN_L)
                                    .setMax(API.MODERATION_COMMENT_MAX_L)
                                    .setOnEnter(R.string.app_move) { w, comment ->
                                        ApiRequestsSupporter.executeEnabled(w, RTagsMove(publication.id, tag.tag.id, comment)) {
                                            EventBus.post(EventFandomTagMove(publication.fandomId, publication.languageId, publication.id, publication.parentPublicationId, tag.tag.id))
                                            ToolsToast.show(R.string.app_done)
                                        }
                                    }
                                    .asSheetShow()
                        }
                    }
                    menu.asSheetShow()
                }.condition(tags.size > 1 && publication.parentPublicationId > 0)
                .add(R.string.app_display_before) { _, _ ->
                    val menu = WidgetMenu()
                    for (t in parentTags) {
                        if (t.id != publication.id)
                            menu.add(t.name) { _, _ ->
                                WidgetField()
                                        .setHint(R.string.moderation_widget_comment)
                                        .setOnCancel(R.string.app_cancel)
                                        .setMin(API.MODERATION_COMMENT_MIN_L)
                                        .setMax(API.MODERATION_COMMENT_MAX_L)
                                        .setOnEnter(R.string.app_move) { w, comment ->
                                            ApiRequestsSupporter.executeEnabled(w, RTagsMoveTag(publication.id, t.id, comment)) {
                                                EventBus.post(EventFandomTagMove(publication.fandomId, publication.languageId, publication.id, publication.parentPublicationId, t.id))
                                                ToolsToast.show(R.string.app_done)
                                            }
                                        }
                                        .asSheetShow()
                            }
                    }
                    menu.asSheetShow()
                }.condition(parentTags.size > 1 && publication.parentPublicationId > 0)
                .add(R.string.app_display_above) { _, _ ->
                    val menu = WidgetMenu()
                    for (tag in tags) {
                        if (publication.id != tag.tag.id) menu.add(tag.tag.name) { _, _ ->
                            WidgetField()
                                    .setHint(R.string.moderation_widget_comment)
                                    .setOnCancel(R.string.app_cancel)
                                    .setMin(API.MODERATION_COMMENT_MIN_L)
                                    .setMax(API.MODERATION_COMMENT_MAX_L)
                                    .setOnEnter(R.string.app_move) { w, comment ->
                                        ApiRequestsSupporter.executeEnabled(w, RTagsMoveCategory(publication.id, tag.tag.id, comment)) {
                                            EventBus.post(EventFandomTagMove(publication.fandomId, publication.languageId, publication.id, publication.parentPublicationId, tag.tag.id))
                                            ToolsToast.show(R.string.app_done)
                                        }
                                    }
                                    .asSheetShow()
                        }
                    }
                    menu.asSheetShow()
                }.condition(tags.size > 1 && publication.parentPublicationId == 0L)
                .add(R.string.app_remove) { _, _ -> WidgetTagRemove(publication) }

        view.setOnLongClickListener {
            w.asSheetShow()
            return@setOnLongClickListener true
        }
    }

    fun parseTags(tagsOriginal: Array<PublicationTag>): Array<TagParent> {


        val tags = ArrayList<PublicationTag>()
        Collections.addAll(tags, *tagsOriginal)
        val map = HashMap<Long, TagParent>()

        var i = 0
        while (i < tags.size) {
            if (tags[i].parentPublicationId == 0L && !map.containsKey(tags[i].id)) map[tags[i].id] = TagParent(tags.removeAt(i--))
            i++
        }

        for (u in tags) {
            val tagParent = map[u.parentPublicationId] ?: continue
            tagParent.tags.add(u)
        }

        val resultTags = ArrayList(map.values).toTypedArray()

        resultTags.sortWith(Comparator { o1, o2 -> (o2.tag.tag_1 - o1.tag.tag_1).toInt() })
        for (n in resultTags) n.tags.sortWith(Comparator { o1, o2 -> (o2.tag_1 - o1.tag_1).toInt() })

        return resultTags
    }

    fun tagsAsLongArray(tags: Array<PublicationTag>) = Array(tags.size) { tags[it].id }


    //
    //  Moderation
    //


    fun showModerationPopup(publication: PublicationModeration) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerLinks.linkToModeration(publication.id))
                    ToolsToast.show(R.string.app_copied)
                }.condition(publication.isPublic)
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun changeWatchComments(publicationId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentsWatchChange(publicationId)) { r ->
            EventBus.post(EventPublicationCommentWatchChange(publicationId, r.follow))
            if (r.follow) ToolsToast.show(R.string.publication_menu_comments_watch_on)
            else ToolsToast.show(R.string.publication_menu_comments_watch_off)
        }
    }

    fun changeBookmark(publicationId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RBookmarksChange(publicationId)) { r ->
            ControllerStoryQuest.incrQuest(API.QUEST_STORY_BOOKMARKS)
            EventBus.post(EventPublicationBookmarkChange(publicationId, r.bookmark))
            if (r.bookmark) ToolsToast.show(R.string.bookmarks_added)
            else ToolsToast.show(R.string.bookmarks_removed)
        }
    }

    fun toDrafts(publicationId: Long, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.post_confirm_to_draft, R.string.post_confirm_to_draft_enter, RPostToDrafts(publicationId)) {
            EventBus.post(EventPostStatusChange(publicationId, API.STATUS_DRAFT))
            onComplete.invoke()
        }
    }

    fun restoreDeepBlock(publicationId: Long) {
        WidgetField()
                .setTitle("Востановить из глубокой блокировки?")
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_restore) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPublicationsAdminRestoreDeepBlock(publicationId, comment)) {
                        EventBus.post(EventPublicationDeepBlockRestore(publicationId))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    //
    //  Moderation
    //

    fun setModerationText(vText: ViewTextLinkable, publication: PublicationModeration) {
        val m = publication.moderation
        var text = ""
        when (m) {
            is ModerationBlock -> {
                if (m.publicationType == API.PUBLICATION_TYPE_REVIEW) {
                    text = ToolsResources.sCap(R.string.moderation_card_block_text_main_review, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), ControllerLinks.linkToAccount(m.accountName))
                } else {
                    val publicationType = if (m.publicationType == API.PUBLICATION_TYPE_POST) ToolsResources.sCap(R.string.moderation_publication_post) else if (m.publicationType == API.PUBLICATION_TYPE_COMMENT) ToolsResources.sCap(R.string.moderation_publication_comment) else if (m.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) ToolsResources.s(R.string.moderation_publication_message) else "null"
                    text = ToolsResources.sCap(R.string.moderation_card_block_text_main, ToolsResources.sex(publication.creatorSex, R.string.he_blocked, R.string.she_blocked), publicationType, ControllerLinks.linkToAccount(m.accountName))
                }
                if (m.accountBlockDate > 0) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_ban, ToolsDate.dateToString(m.accountBlockDate))
                if (m.lastPublicationsBlocked) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_block_last)
            }
            is ModerationTagCreate -> {
                text = ToolsResources.sCap(if (m.tagParentId == 0L) R.string.moderation_text_tag_create_category else R.string.moderation_text_tag_create_tag, ToolsResources.sex(publication.creatorSex, R.string.he_created, R.string.she_created), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationTagRemove -> {
                text = ToolsResources.sCap(if (m.tagParentId == 0L) R.string.moderation_text_tag_remove_category else R.string.moderation_text_tag_remove_tag, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationTagChange -> {
                val isTag = m.tagParentId != 0L
                val nameChanged = m.tagName != m.tagOldName
                val imageAdded = m.tagImageId != 0L && m.tagOldImageId == 0L
                val imageChanged = m.tagImageId != 0L && m.tagOldImageId != 0L

                if (isTag && nameChanged && !imageAdded && !imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (!isTag && nameChanged && !imageAdded && !imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_category, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_image, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.tagName)
                if (imageAdded) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_add_image, ToolsResources.sex(publication.creatorSex, R.string.he_add, R.string.she_add), m.tagName)
                if (nameChanged && imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_and_image, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (nameChanged && imageAdded) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_and_add_image, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)


                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationDescription -> {
                text = ToolsResources.sCap(R.string.moderation_text_description, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed))
                text += "\n${ToolsResources.sCap(R.string.app_text)}: ${m.description}"
            }
            is ModerationGalleryAdd -> {
                text = ToolsResources.sCap(R.string.moderation_text_gallery_add, ToolsResources.sex(publication.creatorSex, R.string.he_add, R.string.she_add))
            }
            is ModerationGalleryRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_gallery_remove, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationImportant -> {
                if (m.isImportant) text = ToolsResources.sCap(R.string.moderation_text_importance_mark, ToolsResources.sex(publication.creatorSex, R.string.he_mark, R.string.she_mark))
                else text = ToolsResources.sCap(R.string.moderation_text_importance_unmark, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove))
                text += "\n${ToolsResources.sCap(R.string.app_publication)}: ${ControllerLinks.linkToPost(m.importantPublicationId)}"
            }
            is ModerationTitleImage -> {
                text = ToolsResources.sCap(R.string.moderation_text_title_image, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed))
            }
            is ModerationLinkAdd -> {
                text = ToolsResources.sCap(R.string.moderation_text_link_add, ToolsResources.sex(publication.creatorSex, R.string.he_add, R.string.she_add))
                text += "\n${ToolsResources.sCap(R.string.app_naming)}: ${m.title}"
                text += "\n${ToolsResources.sCap(R.string.app_link)}: ${m.url}"
            }
            is ModerationLinkRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_link_remove, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationToDrafts -> {
                text = ToolsResources.sCap(R.string.moderation_text_to_drafts, ToolsResources.sex(publication.creatorSex, R.string.he_return, R.string.she_return), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationMultilingualNot -> {
                text = ToolsResources.sCap(R.string.moderation_text_multilingual_not, ToolsResources.sex(publication.creatorSex, R.string.he_make, R.string.she_make), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationBackgroundImage -> {
                if (m.imageId > 0)
                    text = ToolsResources.sCap(R.string.moderation_background_image, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed))
                else
                    text = ToolsResources.sCap(R.string.moderation_background_image, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationBackgroundImageSub -> {
                if (m.imageId > 0)
                    text = ToolsResources.sCap(R.string.moderation_background_image_sub, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.chatName)
                else
                    text = ToolsResources.sCap(R.string.moderation_background_image_sub, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), m.chatName)
            }
            is ModerationPostTags -> {
                text = ToolsResources.sCap(R.string.moderation_text_post_tags, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), ControllerLinks.linkToPost(m.publicationId))

                if (m.newTags.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.publication_event_fandom_genres_new) + " " + m.newTags[0]
                    for (i in 1 until m.newTags.size) text += ", " + m.newTags[i]
                }

                if (m.removedTags.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.publication_event_fandom_genres_remove) + " " + m.removedTags[0]
                    for (i in 1 until m.removedTags.size) text += ", " + m.removedTags[i]
                }
            }
            is ModerationNames -> {
                text = ToolsResources.sCap(R.string.moderation_text_names, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed))

                if (m.newNames.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.publication_event_fandom_genres_new) + " " + m.newNames[0]
                    for (i in 1 until m.newNames.size) text += ", " + m.newNames[i]
                }

                if (m.removedNames.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.publication_event_fandom_genres_remove) + " " + m.removedNames[0]
                    for (i in 1 until m.removedNames.size) text += ", " + m.removedNames[i]
                }
            }
            is ModerationForgive -> {
                text = ToolsResources.sCap(R.string.moderation_text_forgive, ToolsResources.sex(publication.creatorSex, R.string.he_forgive, R.string.she_forgive), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationActivitiesCreate -> {
                text = ToolsResources.sCap(R.string.moderation_text_activities_relay_race_create, ToolsResources.sex(publication.creatorSex, R.string.he_created, R.string.she_created), m.name)
            }
            is ModerationActivitiesChange -> {
                text = ToolsResources.sCap(R.string.moderation_text_activities_relay_race_change, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.oldName)
                if(m.oldName != m.newName) text += ToolsResources.s(R.string.moderation_text_activities_relay_race_change_name, m.newName)
                if(m.oldDescription != m.newDescription) text += ToolsResources.s(R.string.moderation_text_activities_relay_race_change_description, m.newDescription)
            }
            is ModerationActivitiesRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_activities_relay_race_remove, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), m.name)
            }
            is ModerationChatCreate -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_create, ToolsResources.sex(publication.creatorSex, R.string.he_created, R.string.she_created), m.name)
            }
            is ModerationChatChange -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_change, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.name)
            }
            is ModerationChatRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_remove, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), m.name)
            }
            is ModerationTagMove -> {
                if (m.tagParentId == 0L) text = ToolsResources.sCap(R.string.moderation_tag_move_category, ToolsResources.sex(publication.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOtherName)
                else text = ToolsResources.sCap(R.string.moderation_tag_move_tag, ToolsResources.sex(publication.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOtherName)
            }
            is ModerationTagMoveBetweenCategory -> {
                text = ToolsResources.sCap(R.string.moderation_tag_move_tag_between_category, ToolsResources.sex(publication.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOldName, m.tagNewName)
            }
            is ModerationPinPostInFandom -> {
                text = ToolsResources.sCap(R.string.moderation_pin_post_in_fandom, ToolsResources.sex(publication.creatorSex, R.string.he_pined, R.string.she_pined), ControllerLinks.linkToPost(m.postId))
            }
            is ModerationPostClose -> {
                text = ToolsResources.sCap(R.string.moderation_post_close, ToolsResources.sex(publication.creatorSex, R.string.he_close, R.string.she_close), ControllerLinks.linkToPost(m.postId))
            }
            is ModerationPostCloseNo -> {
                text = ToolsResources.sCap(R.string.moderation_post_close_no, ToolsResources.sex(publication.creatorSex, R.string.he_open, R.string.she_open), ControllerLinks.linkToPost(m.postId))
            }
            is ModerationRubricChangeName -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_change_name, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.rubricOldName, m.rubricNewName)
            }
            is ModerationRubricChangeOwner -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_change_owner, ToolsResources.sex(publication.creatorSex, R.string.he_changed, R.string.she_changed), m.rubricName, m.oldOwnerName, m.newOwnerName)
            }
            is ModerationRubricCreate -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_crete, ToolsResources.sex(publication.creatorSex, R.string.he_created, R.string.she_created), m.rubricName, ToolsResources.sex(publication.creatorSex, R.string.he_assign, R.string.she_assign), m.ownerName)
            }
            is ModerationRubricRemove -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_remove, ToolsResources.sex(publication.creatorSex, R.string.he_remove, R.string.she_remove), m.rubricName)
            }


        }

        if (publication.moderation != null)
            if (!ToolsText.empty(publication.moderation!!.comment)) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_comment, publication.moderation!!.comment)
        vText.text = text
        ControllerLinks.makeLinkable(vText)


        when (m) {
            is ModerationRubricChangeName -> {
                ToolsView.addLink(vText, m.rubricNewName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
            }
            is ModerationRubricChangeOwner -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.oldOwnerName) { SProfile.instance(m.oldOwnerId, Navigator.TO) }
                ToolsView.addLink(vText, m.newOwnerName) { SProfile.instance(m.newOwnerId, Navigator.TO) }
            }
            is ModerationRubricCreate -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.ownerName) { SProfile.instance(m.ownerId, Navigator.TO) }
            }
            is ModerationChatCreate -> {
                ToolsView.addLink(vText, m.name) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationChatChange -> {
                ToolsView.addLink(vText, m.name) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationBackgroundImageSub -> {
                ToolsView.addLink(vText, m.chatName) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationActivitiesCreate -> {
                ToolsView.addLink(vText, m.name) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
            }
            is ModerationActivitiesChange -> {
                ToolsView.addLink(vText, m.oldName) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
                ToolsView.addLink(vText, m.newName) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
            }
        }
    }

}
