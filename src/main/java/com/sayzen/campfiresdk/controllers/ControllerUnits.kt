package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.moderations.*
import com.dzen.campfire.api.models.units.moderations.fandom.*
import com.dzen.campfire.api.models.units.moderations.chat.ModerationChatChange
import com.dzen.campfire.api.models.units.moderations.chat.ModerationChatCreate
import com.dzen.campfire.api.models.units.moderations.chat.ModerationChatRemove
import com.dzen.campfire.api.models.units.moderations.posts.*
import com.dzen.campfire.api.models.units.moderations.rubrics.ModerationRubricChangeName
import com.dzen.campfire.api.models.units.moderations.rubrics.ModerationRubricChangeOwner
import com.dzen.campfire.api.models.units.moderations.rubrics.ModerationRubricCreate
import com.dzen.campfire.api.models.units.moderations.rubrics.ModerationRubricRemove
import com.dzen.campfire.api.models.units.moderations.tags.*
import com.dzen.campfire.api.models.units.moderations.units.ModerationBlock
import com.dzen.campfire.api.models.units.moderations.units.ModerationForgive
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.dzen.campfire.api.requests.post.RPostToDrafts
import com.dzen.campfire.api.requests.tags.RTagsMove
import com.dzen.campfire.api.requests.tags.RTagsMoveCategory
import com.dzen.campfire.api.requests.tags.RTagsMoveTag
import com.dzen.campfire.api.requests.units.RUnitsAdminRestoreDeepBlock
import com.dzen.campfire.api.requests.units.RUnitsBookmarksChange
import com.dzen.campfire.api.requests.units.RUnitsCommentsWatchChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomTagMove
import com.sayzen.campfiresdk.models.events.units.*
import com.sayzen.campfiresdk.models.objects.TagParent
import com.sayzen.campfiresdk.models.widgets.WidgetCategoryCreate
import com.sayzen.campfiresdk.models.widgets.WidgetModerationBlock
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.tags.WidgetTagCreate
import com.sayzen.campfiresdk.screens.fandoms.tags.WidgetTagRemove
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
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

object ControllerUnits {

    fun getName(unitType: Long): String {
        return when (unitType) {
            API.UNIT_TYPE_COMMENT -> ToolsResources.s(R.string.app_sticker)
            API.UNIT_TYPE_CHAT_MESSAGE -> ToolsResources.s(R.string.app_message)
            API.UNIT_TYPE_TAG -> ToolsResources.s(R.string.app_tag)
            API.UNIT_TYPE_MODERATION -> ToolsResources.s(R.string.app_moderation)
            API.UNIT_TYPE_POST -> ToolsResources.s(R.string.app_post)
            API.UNIT_TYPE_REVIEW -> ToolsResources.s(R.string.app_review)
            API.UNIT_TYPE_STICKERS_PACK -> ToolsResources.s(R.string.app_stickers_pack)
            API.UNIT_TYPE_STICKER -> ToolsResources.s(R.string.app_sticker)
            else -> "[unknown]"
        }
    }

    fun toUnit(unitType: Long, unitId: Long, commentId: Long = 0) {

        if (unitType == API.UNIT_TYPE_POST) ControllerCampfireSDK.onToPostClicked(unitId, commentId, Navigator.TO)
        if (unitType == API.UNIT_TYPE_MODERATION) ControllerCampfireSDK.onToModerationClicked(unitId, commentId, Navigator.TO)
        if (unitType == API.UNIT_TYPE_STICKER) SStickersView.instanceBySticker(unitId, Navigator.TO)
        if (unitType == API.UNIT_TYPE_CHAT_MESSAGE) SChat.instance(unitId, true, Navigator.TO)
        if (unitType == API.UNIT_TYPE_STICKERS_PACK) {
            if (commentId == 0L) SStickersView.instance(unitId, Navigator.TO)
            else Navigator.to(SComments(unitId, commentId))
        }
    }

    fun block(unit: Unit, onBlock: () -> kotlin.Unit = {}) {
        WidgetModerationBlock.show(unit, onBlock)
    }

    fun report(unit: Unit) {
        ControllerApi.reportUnit(
                unit.id,
                R.string.post_report_confirm,
                R.string.post_error_gone
        )
    }

    fun clearReports(unit: Unit) {
        ControllerApi.clearReportsUnit(unit.id, unit.unitType)
    }


    //
    //  Tag
    //

    fun createTagMenu(view: View, unit: UnitTag, tags: Array<TagParent> = emptyArray()) {

        var parentTags = ArrayList<UnitTag>()
        if (unit.parentUnitId > 0) for (t in tags) if (t.tag.id == unit.parentUnitId) parentTags = t.tags

        val w = WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToTag(unit.id))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_TAGS))
                .add(R.string.app_change) { _, _ ->
                    if (unit.parentUnitId == 0L) WidgetCategoryCreate(unit)
                    else WidgetTagCreate(unit)
                }
                .add(R.string.app_move) { _, _ ->
                    val menu = WidgetMenu()
                    for (tag in tags) {
                        if (unit.parentUnitId != tag.tag.id) menu.add(tag.tag.name) { _, _ ->
                            WidgetField()
                                    .setHint(R.string.moderation_widget_comment)
                                    .setOnCancel(R.string.app_cancel)
                                    .setMin(API.MODERATION_COMMENT_MIN_L)
                                    .setMax(API.MODERATION_COMMENT_MAX_L)
                                    .setOnEnter(R.string.app_move) { w, comment ->
                                        ApiRequestsSupporter.executeEnabled(w, RTagsMove(unit.id, tag.tag.id, comment)) {
                                            EventBus.post(EventFandomTagMove(unit.fandomId, unit.languageId, unit.id, unit.parentUnitId, tag.tag.id))
                                            ToolsToast.show(R.string.app_done)
                                        }
                                    }
                                    .asSheetShow()
                        }
                    }
                    menu.asSheetShow()
                }.condition(tags.size > 1 && unit.parentUnitId > 0)
                .add(R.string.app_display_before) { _, _ ->
                    val menu = WidgetMenu()
                    for (t in parentTags) {
                        if (t.id != unit.id)
                            menu.add(t.name) { _, _ ->
                                WidgetField()
                                        .setHint(R.string.moderation_widget_comment)
                                        .setOnCancel(R.string.app_cancel)
                                        .setMin(API.MODERATION_COMMENT_MIN_L)
                                        .setMax(API.MODERATION_COMMENT_MAX_L)
                                        .setOnEnter(R.string.app_move) { w, comment ->
                                            ApiRequestsSupporter.executeEnabled(w, RTagsMoveTag(unit.id, t.id, comment)) {
                                                EventBus.post(EventFandomTagMove(unit.fandomId, unit.languageId, unit.id, unit.parentUnitId, t.id))
                                                ToolsToast.show(R.string.app_done)
                                            }
                                        }
                                        .asSheetShow()
                            }
                    }
                    menu.asSheetShow()
                }.condition(parentTags.size > 1 && unit.parentUnitId > 0)
                .add(R.string.app_display_above) { _, _ ->
                    val menu = WidgetMenu()
                    for (tag in tags) {
                        if (unit.id != tag.tag.id) menu.add(tag.tag.name) { _, _ ->
                            WidgetField()
                                    .setHint(R.string.moderation_widget_comment)
                                    .setOnCancel(R.string.app_cancel)
                                    .setMin(API.MODERATION_COMMENT_MIN_L)
                                    .setMax(API.MODERATION_COMMENT_MAX_L)
                                    .setOnEnter(R.string.app_move) { w, comment ->
                                        ApiRequestsSupporter.executeEnabled(w, RTagsMoveCategory(unit.id, tag.tag.id, comment)) {
                                            EventBus.post(EventFandomTagMove(unit.fandomId, unit.languageId, unit.id, unit.parentUnitId, tag.tag.id))
                                            ToolsToast.show(R.string.app_done)
                                        }
                                    }
                                    .asSheetShow()
                        }
                    }
                    menu.asSheetShow()
                }.condition(tags.size > 1 && unit.parentUnitId == 0L)
                .add(R.string.app_remove) { _, _ -> WidgetTagRemove(unit) }

        view.setOnLongClickListener {
            w.asSheetShow()
            return@setOnLongClickListener true
        }
    }

    fun parseTags(tagsOriginal: Array<UnitTag>): Array<TagParent> {


        val tags = ArrayList<UnitTag>()
        Collections.addAll(tags, *tagsOriginal)
        val map = HashMap<Long, TagParent>()

        var i = 0
        while (i < tags.size) {
            if (tags[i].parentUnitId == 0L && !map.containsKey(tags[i].id)) map[tags[i].id] = TagParent(tags.removeAt(i--))
            i++
        }

        for (u in tags) {
            val tagParent = map[u.parentUnitId] ?: continue
            tagParent.tags.add(u)
        }

        val resultTags = ArrayList(map.values).toTypedArray()

        resultTags.sortWith(Comparator { o1, o2 -> (o2.tag.tag_1 - o1.tag.tag_1).toInt() })
        for (n in resultTags) n.tags.sortWith(Comparator { o1, o2 -> (o2.tag_1 - o1.tag_1).toInt() })

        return resultTags
    }

    fun tagsAsLongArray(tags: Array<UnitTag>) = Array(tags.size) { tags[it].id }


    //
    //  Moderation
    //


    fun showModerationPopup(unit: UnitModeration) {
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToModeration(unit.id))
                    ToolsToast.show(R.string.app_copied)
                }.condition(unit.isPublic)
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun changeWatchComments(unitId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RUnitsCommentsWatchChange(unitId)) { r ->
            EventBus.post(EventUnitCommentWatchChange(unitId, r.follow))
            if (r.follow) ToolsToast.show(R.string.unit_menu_comments_watch_on)
            else ToolsToast.show(R.string.unit_menu_comments_watch_off)
        }
    }

    fun changeBookmark(unitId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RUnitsBookmarksChange(unitId)) { r ->
            EventBus.post(EventUnitBookmarkChange(unitId, r.bookmark))
            if (r.bookmark) ToolsToast.show(R.string.bookmarks_added)
            else ToolsToast.show(R.string.bookmarks_removed)
        }
    }

    fun toDrafts(unitId: Long, onComplete: () -> kotlin.Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.post_confirm_to_draft, R.string.post_confirm_to_draft_enter, RPostToDrafts(unitId)) {
            EventBus.post(EventPostStatusChange(unitId, API.STATUS_DRAFT))
            onComplete.invoke()
        }
    }

    fun restoreDeepBlock(unitId: Long) {
        WidgetField()
                .setTitle("Востановить из глубокой блокировки?")
                .setHint(R.string.comments_hint)
                .setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_restore) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RUnitsAdminRestoreDeepBlock(unitId, comment)) {
                        EventBus.post(EventUnitDeepBlockRestore(unitId))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    //
    //  Moderation
    //

    fun setModerationText(vText: ViewTextLinkable, unit: UnitModeration) {
        val m = unit.moderation
        var text = ""
        when (m) {
            is ModerationBlock -> {
                if (m.unitType == API.UNIT_TYPE_REVIEW) {
                    text = ToolsResources.sCap(R.string.moderation_card_block_text_main_review, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(m.accountName))
                } else {
                    val unitType = if (m.unitType == API.UNIT_TYPE_POST) ToolsResources.sCap(R.string.moderation_unit_post) else if (m.unitType == API.UNIT_TYPE_COMMENT) ToolsResources.sCap(R.string.moderation_unit_comment) else if (m.unitType == API.UNIT_TYPE_CHAT_MESSAGE) ToolsResources.s(R.string.moderation_unit_message) else "null"
                    text = ToolsResources.sCap(R.string.moderation_card_block_text_main, ToolsResources.sex(unit.creatorSex, R.string.he_blocked, R.string.she_blocked), unitType, ControllerApi.linkToUser(m.accountName))
                }
                if (m.accountBlockDate > 0) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_ban, ToolsDate.dateToString(m.accountBlockDate))
                if (m.lastUnitsBlocked) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_block_last)
            }
            is ModerationTagCreate -> {
                text = ToolsResources.sCap(if (m.tagParentId == 0L) R.string.moderation_text_tag_create_category else R.string.moderation_text_tag_create_tag, ToolsResources.sex(unit.creatorSex, R.string.he_created, R.string.she_created), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationTagRemove -> {
                text = ToolsResources.sCap(if (m.tagParentId == 0L) R.string.moderation_text_tag_remove_category else R.string.moderation_text_tag_remove_tag, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationTagChange -> {
                val isTag = m.tagParentId != 0L
                val nameChanged = m.tagName != m.tagOldName
                val imageAdded = m.tagImageId != 0L && m.tagOldImageId == 0L
                val imageChanged = m.tagImageId != 0L && m.tagOldImageId != 0L

                if (isTag && nameChanged && !imageAdded && !imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (!isTag && nameChanged && !imageAdded && !imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_category, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_image, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.tagName)
                if (imageAdded) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_add_image, ToolsResources.sex(unit.creatorSex, R.string.he_add, R.string.she_add), m.tagName)
                if (nameChanged && imageChanged) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_and_image, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)
                if (nameChanged && imageAdded) text = ToolsResources.sCap(R.string.moderation_text_tag_change_tag_and_add_image, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.tagOldName, m.tagName)


                if (m.tagParentId != 0L) text += "\n" + ToolsResources.sCap(R.string.app_category) + ": " + m.tagParentName
            }
            is ModerationDescription -> {
                text = ToolsResources.sCap(R.string.moderation_text_description, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed))
                text += "\n${ToolsResources.sCap(R.string.app_text)}: ${m.description}"
            }
            is ModerationGalleryAdd -> {
                text = ToolsResources.sCap(R.string.moderation_text_gallery_add, ToolsResources.sex(unit.creatorSex, R.string.he_add, R.string.she_add))
            }
            is ModerationGalleryRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_gallery_remove, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationImportant -> {
                if (m.isImportant) text = ToolsResources.sCap(R.string.moderation_text_importance_mark, ToolsResources.sex(unit.creatorSex, R.string.he_mark, R.string.she_mark))
                else text = ToolsResources.sCap(R.string.moderation_text_importance_unmark, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove))
                text += "\n${ToolsResources.sCap(R.string.app_publication)}: ${ControllerApi.linkToPost(m.importantUnitId)}"
            }
            is ModerationTitleImage -> {
                text = ToolsResources.sCap(R.string.moderation_text_title_image, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed))
            }
            is ModerationLinkAdd -> {
                text = ToolsResources.sCap(R.string.moderation_text_link_add, ToolsResources.sex(unit.creatorSex, R.string.he_add, R.string.she_add))
                text += "\n${ToolsResources.sCap(R.string.app_naming)}: ${m.title}"
                text += "\n${ToolsResources.sCap(R.string.app_link)}: ${m.url}"
            }
            is ModerationLinkRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_link_remove, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationToDrafts -> {
                text = ToolsResources.sCap(R.string.moderation_text_to_drafts, ToolsResources.sex(unit.creatorSex, R.string.he_return, R.string.she_return), ControllerApi.linkToUser(m.accountName))
            }
            is ModerationMultilingualNot -> {
                text = ToolsResources.sCap(R.string.moderation_text_multilingual_not, ToolsResources.sex(unit.creatorSex, R.string.he_make, R.string.she_make), ControllerApi.linkToUser(m.accountName))
            }
            is ModerationBackgroundImage -> {
                if (m.imageId > 0)
                    text = ToolsResources.sCap(R.string.moderation_background_image, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed))
                else
                    text = ToolsResources.sCap(R.string.moderation_background_image, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove))
            }
            is ModerationPostTags -> {
                text = ToolsResources.sCap(R.string.moderation_text_post_tags, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), ControllerApi.linkToPost(m.unitId))

                if (m.newTags.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.unit_event_fandom_genres_new) + " " + m.newTags[0]
                    for (i in 1 until m.newTags.size) text += ", " + m.newTags[i]
                }

                if (m.removedTags.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.unit_event_fandom_genres_remove) + " " + m.removedTags[0]
                    for (i in 1 until m.removedTags.size) text += ", " + m.removedTags[i]
                }
            }
            is ModerationNames -> {
                text = ToolsResources.sCap(R.string.moderation_text_names, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed))

                if (m.newNames.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.unit_event_fandom_genres_new) + " " + m.newNames[0]
                    for (i in 1 until m.newNames.size) text += ", " + m.newNames[i]
                }

                if (m.removedNames.isNotEmpty()) {
                    text += "\n" + ToolsResources.sCap(R.string.unit_event_fandom_genres_remove) + " " + m.removedNames[0]
                    for (i in 1 until m.removedNames.size) text += ", " + m.removedNames[i]
                }
            }
            is ModerationForgive -> {
                text = ToolsResources.sCap(R.string.moderation_text_forgive, ToolsResources.sex(unit.creatorSex, R.string.he_forgive, R.string.she_forgive), ControllerApi.linkToUser(m.accountName))
            }
            is ModerationChatCreate -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_create, ToolsResources.sex(unit.creatorSex, R.string.he_created, R.string.she_created), m.name)
            }
            is ModerationChatChange -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_change, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.name)
            }
            is ModerationChatRemove -> {
                text = ToolsResources.sCap(R.string.moderation_text_chat_remove, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove), m.name)
            }
            is ModerationTagMove -> {
                if (m.tagParentId == 0L) text = ToolsResources.sCap(R.string.moderation_tag_move_category, ToolsResources.sex(unit.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOtherName)
                else text = ToolsResources.sCap(R.string.moderation_tag_move_tag, ToolsResources.sex(unit.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOtherName)
            }
            is ModerationTagMoveBetweenCategory -> {
                text = ToolsResources.sCap(R.string.moderation_tag_move_tag_between_category, ToolsResources.sex(unit.creatorSex, R.string.he_move, R.string.she_move), m.tagName, m.tagOldName, m.tagNewName)
            }
            is ModerationPinPostInFandom -> {
                text = ToolsResources.sCap(R.string.moderation_pin_post_in_fandom, ToolsResources.sex(unit.creatorSex, R.string.he_pined, R.string.she_pined), ControllerApi.linkToPost(m.postId))
            }
            is ModerationPostClose -> {
                text = ToolsResources.sCap(R.string.moderation_post_close, ToolsResources.sex(unit.creatorSex, R.string.he_close, R.string.she_close), ControllerApi.linkToPost(m.postId))
            }
            is ModerationPostCloseNo -> {
                text = ToolsResources.sCap(R.string.moderation_post_close_no, ToolsResources.sex(unit.creatorSex, R.string.he_open, R.string.she_open), ControllerApi.linkToPost(m.postId))
            }
            is ModerationRubricChangeName -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_change_name, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.rubricOldName, m.rubricNewName)
            }
            is ModerationRubricChangeOwner -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_change_owner, ToolsResources.sex(unit.creatorSex, R.string.he_changed, R.string.she_changed), m.rubricName, m.oldOwnerName, m.newOwnerName)
            }
            is ModerationRubricCreate -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_crete, ToolsResources.sex(unit.creatorSex, R.string.he_created, R.string.she_created), m.rubricName, ToolsResources.sex(unit.creatorSex, R.string.he_assign, R.string.she_assign), m.ownerName)
            }
            is ModerationRubricRemove -> {
                text = ToolsResources.sCap(R.string.moderation_rubric_remove, ToolsResources.sex(unit.creatorSex, R.string.he_remove, R.string.she_remove), m.rubricName)
            }


        }

        if (unit.moderation != null)
            if (!ToolsText.empty(unit.moderation!!.comment)) text += "\n" + ToolsResources.sCap(R.string.moderation_card_block_text_comment, unit.moderation!!.comment)
        vText.text = text
        ControllerApi.makeLinkable(vText)


        when (m) {
            is ModerationRubricChangeName -> {
                ToolsView.addLink(vText, m.rubricNewName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
            }
            is ModerationRubricChangeOwner -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.oldOwnerName) { SAccount.instance(m.oldOwnerId, Navigator.TO) }
                ToolsView.addLink(vText, m.newOwnerName) { SAccount.instance(m.newOwnerId, Navigator.TO) }
            }
            is ModerationRubricCreate -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.ownerName) { SAccount.instance(m.ownerId, Navigator.TO) }
            }
        }
    }

}
