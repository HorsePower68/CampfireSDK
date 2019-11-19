package com.sayzen.campfiresdk.app

import androidx.annotation.DrawableRes
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.QuestInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.objects.*
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

object CampfireConstants {

    val RATE_TIME = 2000L

    val CHECK_RULES_ACCEPTED = "CHECK_RULES_ACCEPTED"

    private val RULES_USER_TITLES = ToolsResources.getStringIndexedArrayId("rules_users_%s_title")
    private val RULES_USER_BODIES = ToolsResources.getStringIndexedArrayId("rules_users_")
    val RULES_USER = Array(RULES_USER_TITLES.size) { Rule(RULES_USER_TITLES[it], RULES_USER_BODIES[it]) }
    val RULES_MODER = ToolsResources.getStringIndexedArrayId("rules_moderators_")

    val TEXT_ICONS = arrayOf(0/*Stub wikiTitle*/,
            R.attr.ic_arrow_back_24dp, R.attr.ic_menu_24dp, R.attr.ic_keyboard_arrow_right_24dp, R.attr.ic_keyboard_arrow_left_24dp, R.attr.ic_file_download_24dp, R.attr.ic_share_24dp, R.attr.ic_keyboard_arrow_up_24dp, R.attr.ic_keyboard_arrow_down_24dp, R.attr.ic_content_copy_24dp, R.attr.ic_folder_24dp,
            R.attr.ic_insert_drive_file_24dp, R.attr.ic_mic_24dp, R.attr.ic_clear_24dp, R.attr.ic_lock_24dp, R.attr.ic_access_time_24dp, R.attr.ic_account_balance_24dp, R.attr.ic_account_box_24dp,
            R.attr.ic_account_circle_24dp, R.attr.ic_add_24dp, R.attr.ic_alarm_24dp, R.attr.ic_all_inclusive_24dp, R.attr.ic_attach_file_24dp, R.attr.ic_bookmark_24dp, R.attr.ic_brush_24dp, R.attr.ic_burst_mode_24dp, R.attr.ic_cached_24dp, R.attr.ic_check_box_24dp,
            R.attr.ic_clear_24dp, R.attr.ic_code_24dp, R.attr.ic_done_all_24dp, R.attr.ic_done_24dp, R.attr.ic_email_24dp, R.attr.ic_exit_to_app_24dp, R.attr.ic_favorite_24dp, R.attr.ic_format_quote_24dp, R.attr.ic_gavel_24dp, R.attr.ic_group_24dp, R.attr.ic_help_24dp, R.attr.ic_info_outline_24dp, R.attr.ic_insert_link_24dp, R.attr.ic_insert_link_36dp,
            R.attr.ic_insert_photo_24dp, R.attr.ic_keyboard_arrow_down_24dp, R.attr.ic_keyboard_arrow_up_24dp, R.attr.ic_landscape_24dp, R.attr.ic_language_24dp, R.attr.ic_mode_comment_24dp, R.attr.ic_mode_edit_24dp, R.attr.ic_more_vert_24dp, R.attr.ic_notifications_24dp, R.attr.ic_person_24dp, R.attr.ic_play_arrow_24dp, R.attr.ic_reply_24dp, R.attr.ic_rowing_24dp, R.attr.ic_search_24dp, R.attr.ic_security_24dp, R.attr.ic_send_24dp,
            R.attr.ic_settings_24dp, R.attr.ic_share_24dp, R.attr.ic_star_border_24dp, R.attr.ic_star_24dp, R.attr.ic_text_fields_24dp, R.attr.ic_thumbs_up_down_24dp, R.attr.ic_translate_24dp, R.attr.ic_trending_flat_24dp, R.attr.ic_trending_up_24dp,
            R.attr.ic_tune_24dp, R.attr.ic_widgets_24dp, R.attr.ic_book_24dp, R.attr.ic_pets_24dp, R.attr.ic_directions_bike_24dp, R.attr.ic_border_all_24dp, R.attr.ic_border_left_24dp, R.attr.ic_border_top_24dp, R.attr.ic_border_right_24dp, R.attr.ic_border_bottom_24dp, R.attr.ic_not_interested_24dp, R.attr.ic_wb_incandescent_24dp, R.attr.ic_whatshot_24dp
    )

    val ACHIEVEMENTS = arrayOf(
            Achievement(API.ACHI_APP_SHARE, R.string.achi_share, R.color.red_500, true, R.drawable.achi_3_2),
            Achievement(API.ACHI_CONTENT_SHARE, R.string.achi_content_share, R.color.pink_500, false, R.drawable.achi_3_3),
            Achievement(API.ACHI_ADD_RECRUITER, R.string.achi_add_recruiter, R.color.green_500, true, R.drawable.achi_1_2),
            Achievement(API.ACHI_ENTERS, R.string.achi_enters, R.color.deep_purple_500, false, R.drawable.achi_13),
            Achievement(API.ACHI_KARMA_COUNT, R.string.achi_karma_count, R.color.indigo_500, false, R.drawable.achi_20_2),
            Achievement(API.ACHI_REFERRALS_COUNT, R.string.achi_referals_count, R.color.blue_500, false, R.drawable.achi_1_3),
            Achievement(API.ACHI_RATES_COUNT, R.string.achi_rates_count, R.color.light_blue_500, false, R.drawable.achi_10),
            Achievement(API.ACHI_POSTS_COUNT, R.string.achi_posts_count, R.color.cyan_900, false, R.drawable.achi_2_2),
            Achievement(API.ACHI_POST_KARMA, R.string.achi_posts_karma_count, R.color.teal_500, false, R.drawable.achi_5_2),
            Achievement(API.ACHI_COMMENTS_KARMA, R.string.achi_comments_karma_count, R.color.light_green_900, false, R.drawable.achi_4_1),
            Achievement(API.ACHI_STICKERS_KARMA, R.string.achi_stickers_karma_count, R.color.lime_900, false, R.drawable.achi_4_2),
            Achievement(API.ACHI_COMMENTS_COUNT, R.string.achi_comments_count, R.color.orange_500, false, R.drawable.achi_4_4),
            Achievement(API.ACHI_LOGIN, R.string.achi_login, R.color.red_500, true, R.drawable.achi_17),
            Achievement(API.ACHI_CHAT, R.string.achi_chat, R.color.blue_500, false, R.drawable.achi_3_1),
            Achievement(API.ACHI_COMMENT, R.string.achi_comment, R.color.pink_500, false, R.drawable.achi_4_2),
            Achievement(API.ACHI_ANSWER, R.string.achi_answer, R.color.teal_500, false, R.drawable.achi_4_3),
            Achievement(API.ACHI_RATE, R.string.achi_rate, R.color.light_blue_500, false, R.drawable.achi_1_5),
            Achievement(API.ACHI_CHANGE_PUBLICATION, R.string.achi_change_publication, R.color.deep_purple_500, false, R.drawable.achi_2_3),
            Achievement(API.ACHI_CHANGE_COMMENT, R.string.achi_change_comment, R.color.indigo_500, false, R.drawable.achi_4_5),
            Achievement(API.ACHI_REVIEW, R.string.achi_review, R.color.indigo_500, false, R.drawable.achi_10),
            Achievement(API.ACHI_FIRST_POST, R.string.achi_first_post, R.color.cyan_900, false, R.drawable.achi_2_2),
            Achievement(API.ACHI_SUBSCRIBE, R.string.achi_first_follow, R.color.teal_500, false, R.drawable.achi_5_1),
            Achievement(API.ACHI_TAGS_SEARCH, R.string.achi_tags_search, R.color.pink_500, false, R.drawable.achi_9),
            Achievement(API.ACHI_LANGUAGE, R.string.achi_language, R.color.cyan_900, false, R.drawable.achi_24),
            Achievement(API.ACHI_TITLE_IMAGE, R.string.achi_title_image, R.color.blue_500, false, R.drawable.achi_8, arrayOf(ToolsText.numToStringRound(API.LVL_CAN_CHANGE_PROFILE_IMAGE.lvl / 100.0, 2))),
            Achievement(API.ACHI_CREATE_TAG, R.string.achi_create_tag, R.color.teal_500, false, R.drawable.achi_12),
            Achievement(API.ACHI_QUESTS, R.string.achi_quests, R.color.teal_500, false, R.drawable.achi_6),
            Achievement(API.ACHI_FANDOMS, R.string.achi_fandoms, R.color.orange_500, true, R.drawable.achi_19_1),
            Achievement(API.ACHI_RULES_USER, R.string.achi_rules_user, R.color.teal_500, true, R.drawable.achi_21),
            Achievement(API.ACHI_RULES_MODERATOR, R.string.achi_rules_moderator, R.color.cyan_900, true, R.drawable.achi_21),
            Achievement(API.ACHI_FOLLOWERS, R.string.achi_followers, R.color.red_500, false, R.drawable.achi_5_3),
            Achievement(API.ACHI_MODER_CHANGE_POST_TAGS, R.string.achi_moderators_tags, R.color.indigo_500, false, R.drawable.achi_5_3),
            Achievement(API.ACHI_FIREWORKS, R.string.achi_50, R.color.indigo_500, false, R.drawable.achi_23),
            Achievement(API.ACHI_MAKE_MODER, R.string.achi_51, R.color.orange_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_CREATE_CHAT, R.string.achi_52, R.color.pink_500, false, R.drawable.achi_13),
            Achievement(API.ACHI_REVIEW_MODER_ACTION, R.string.achi_53, R.color.cyan_900, false, R.drawable.achi_21),
            Achievement(API.ACHI_ACCEPT_FANDOM, R.string.achi_54, R.color.teal_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_MODERATOR_COUNT, R.string.achi_55, R.color.teal_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_MODERATOR_ACTION_KARMA, R.string.achi_56, R.color.light_blue_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_KARMA_30, R.string.achi_57, R.color.deep_purple_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_UP_RATES, R.string.achi_58, R.color.indigo_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_UP_RATES_OVER_DOWN, R.string.achi_59, R.color.cyan_900, false, R.drawable.achi_21),
            Achievement(API.ACHI_CHAT_SUBSCRIBE, R.string.achi_60, R.color.teal_500, false, R.drawable.achi_21),
            Achievement(API.ACHI_VIDEO_AD, R.string.achi_61, R.color.orange_500, true, R.drawable.achi_21)
    )

    val LVLS = arrayOf(
            AppLevel(API.LVL_APP_ACCESS, R.string.lvl_app_access),
            AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE, R.string.lvl_app_profile_image),
            AppLevel(API.LVL_CAN_CHANGE_STATUS, R.string.lvl_app_status),
            AppLevel(API.LVL_CAN_CHANGE_AVATAR_GIF, R.string.lvl_avatar_gif),
            AppLevel(API.LVL_CAN_PIN_POST, R.string.lvl_pin_post),
            AppLevel(API.LVL_CREATE_STICKERS, R.string.lvl_create_stickers),
            AppLevel(R.string.lvl_moderate_block, API.LVL_MODERATOR_BLOCK, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_to_drafts, API.LVL_MODERATOR_TO_DRAFTS, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_chats, API.LVL_MODERATOR_CHATS, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_post_tags, API.LVL_MODERATOR_POST_TAGS, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_image_title, API.LVL_MODERATOR_FANDOM_IMAGE, R.color.blue_500),
            AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE_GIF, R.string.lvl_app_profile_image_gif, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_description, API.LVL_MODERATOR_DESCRIPTION, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_names, API.LVL_MODERATOR_NAMES, R.color.blue_500),
            AppLevel(R.string.lvl_wiki_edit, API.LVL_MODERATOR_WIKI_EDIT, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_gallery, API.LVL_MODERATOR_GALLERY, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_links, API.LVL_MODERATOR_LINKS, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_tags, API.LVL_MODERATOR_TAGS, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_pin_post, API.LVL_MODERATOR_PIN_POST, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_important, API.LVL_MODERATOR_IMPORTANT, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_review_remove_text, API.LVL_MODERATOR_REVIEW_REMOVE_TEXT, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_close_post, API.LVL_MODERATOR_CLOSE_POST, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_relay_race, API.LVL_MODERATOR_RELAY_RACE, R.color.blue_500),
            AppLevel(R.string.lvl_moderate_rubrics, API.LVL_MODERATOR_RUBRIC, R.color.blue_500),
            AppLevel(R.string.lvl_admin_moder, API.LVL_ADMIN_MODER, R.color.red_500),
            AppLevel(R.string.lvl_admin_fandom_rename, API.LVL_ADMIN_FANDOM_NAME, R.color.red_500),
            AppLevel(R.string.lvl_admin_fandom_image, API.LVL_ADMIN_FANDOM_AVATAR, R.color.red_500),
            AppLevel(R.string.lvl_admin_fandom_genres, API.LVL_ADMIN_FANDOM_PARAMS, R.color.red_500),
            AppLevel(R.string.lvl_admin_fandom_category, API.LVL_ADMIN_FANDOM_CATEGORY, R.color.red_500),
            AppLevel(R.string.lvl_ads_ban, API.LVL_ADMIN_BAN, R.color.red_500),
            AppLevel(R.string.lvl_post_fandom_change, API.LVL_ADMIN_POST_CHANGE_FANDOM, R.color.red_500),
            AppLevel(R.string.lvl_fandoms, API.LVL_ADMIN_FANDOMS_ACCEPT, R.color.red_500),
            AppLevel(R.string.lvl_remove_status, API.LVL_ADMIN_USER_REMOVE_STATUS, R.color.red_500),
            AppLevel(R.string.lvl_remove_image, API.LVL_ADMIN_USER_REMOVE_IMAGE, R.color.red_500),
            AppLevel(R.string.lvl_remove_name, API.LVL_ADMIN_USER_REMOVE_NAME, R.color.red_500),
            AppLevel(R.string.lvl_remove_descrpition, API.LVL_ADMIN_USER_REMOVE_DESCRIPTION, R.color.red_500),
            AppLevel(R.string.lvl_remove_link, API.LVL_ADMIN_USER_REMOVE_LINK, R.color.red_500),
            AppLevel(R.string.lvl_change_user_name, API.LVL_ADMIN_USER_CHANGE_NAME, R.color.red_500),
            AppLevel(R.string.lvl_user_punishment_remove, API.LVL_ADMIN_USER_PUNISHMENTS_REMOVE, R.color.red_500),
            AppLevel(R.string.lvl_remove_moderator, API.LVL_ADMIN_REMOVE_MODERATOR, R.color.red_500),
            AppLevel(R.string.lvl_make_moderators, API.LVL_ADMIN_MAKE_MODERATOR, R.color.red_500),
            AppLevel(R.string.lvl_fandom_close, API.LVL_ADMIN_FANDOM_CLOSE, R.color.red_500),
            AppLevel(R.string.lvl_remove_fandoms, API.LVL_ADMIN_FANDOM_REMOVE, R.color.red_500),
            AppLevel(R.string.lvl_fandom_admin, API.LVL_ADMIN_FANDOM_ADMIN, R.color.red_500),
            AppLevel(R.string.lvl_fandom_set_cof, API.LVL_ADMIN_FANDOM_SET_COF, R.color.red_500)
    )

    val QUESTS = arrayOf(
            Quest(API.QUEST_RATES, R.string.quests_text_rates),
            Quest(API.QUEST_KARMA, R.string.quests_text_karma),
            Quest(API.QUEST_POSTS, R.string.quests_text_posts),
            Quest(API.QUEST_COMMENTS, R.string.quests_text_comments),
            Quest(API.QUEST_CHAT, R.string.quests_text_chat),
            Quest(API.QUEST_POST_KARMA, R.string.quests_text_posts_karma),
            Quest(API.QUEST_COMMENTS_KARMA, R.string.quests_text_comments_karma)
    )

    val QUESTS_STORY = arrayOf(
            QuestStory(API.QUEST_STORY_START, R.string.quests_story_1, R.string.quests_story_1_button, false),
            QuestStory(API.QUEST_STORY_KARMA, R.string.quests_story_2),
            QuestStory(API.QUEST_STORY_ACHI_SCREEN, R.string.quests_story_3),
            QuestStory(API.QUEST_STORY_CHAT, R.string.quests_story_4),
            QuestStory(API.QUEST_STORY_FANDOM, R.string.quests_story_5),
            QuestStory(API.QUEST_STORY_PROFILE, R.string.quests_story_6),
            QuestStory(API.QUEST_STORY_FILTERS, R.string.quests_story_7),
            QuestStory(API.QUEST_STORY_POST, R.string.quests_story_8),
            QuestStory(API.QUEST_STORY_FINISH, R.string.quests_story_9)
    )

    val CATEGORIES = arrayOf(
            FandomParam(API.CATEGORY_GAMES, R.string.category_games),
            FandomParam(API.CATEGORY_ANIME, R.string.category_anime),
            FandomParam(API.CATEGORY_MUSIC, R.string.category_music),
            FandomParam(API.CATEGORY_PROGRAMS, R.string.category_programs),
            FandomParam(API.CATEGORY_MOVIES, R.string.category_movies),
            FandomParam(API.CATEGORY_SITE, R.string.category_sites),
            FandomParam(API.CATEGORY_COMPANY, R.string.category_companies),
            FandomParam(API.CATEGORY_BOOKS, R.string.category_books),
            FandomParam(API.CATEGORY_ANIMALS, R.string.category_animals),
            FandomParam(API.CATEGORY_HOBBIES, R.string.category_hobbies),
            FandomParam(API.CATEGORY_PEOPLE, R.string.category_people),
            FandomParam(API.CATEGORY_EVENT, R.string.category_event),
            FandomParam(API.CATEGORY_PLANTS, R.string.category_plants),
            FandomParam(API.CATEGORY_PLACES, R.string.category_places),
            FandomParam(API.CATEGORY_OTHER, R.string.category_other)
    )

    private val GAMES_1_ARRAY = ToolsResources.getStringIndexedArrayId("games_genres_")
    val GAMES_1 = Array(GAMES_1_ARRAY.size) { FandomParam(it.toLong(), GAMES_1_ARRAY[it]) }


    private val GAMES_2_ARRAY = ToolsResources.getStringIndexedArrayId("games_platform_")
    val GAMES_2 = Array(GAMES_2_ARRAY.size) { FandomParam(it.toLong(), GAMES_2_ARRAY[it]) }
    private val GAMES_3_ARRAY = ToolsResources.getStringIndexedArrayId("games_control_")
    val GAMES_3 = Array(GAMES_3_ARRAY.size) { FandomParam(it.toLong(), GAMES_3_ARRAY[it]) }
    private val ANIME_1_ARRAY = ToolsResources.getStringIndexedArrayId("anime_genres_")
    val ANIME_1 = Array(ANIME_1_ARRAY.size) { FandomParam(it.toLong(), ANIME_1_ARRAY[it]) }
    private val ANIME_2_ARRAY = ToolsResources.getStringIndexedArrayId("anime_type_")
    val ANIME_2 = Array(ANIME_2_ARRAY.size) { FandomParam(it.toLong(), ANIME_2_ARRAY[it]) }
    private val MUSIC_1_ARRAY = ToolsResources.getStringIndexedArrayId("music_1_")
    val MUSIC_1 = Array(MUSIC_1_ARRAY.size) { FandomParam(it.toLong(), MUSIC_1_ARRAY[it]) }
    private val MUSIC_2_ARRAY = ToolsResources.getStringIndexedArrayId("music_2_")
    val MUSIC_2 = Array(MUSIC_2_ARRAY.size) { FandomParam(it.toLong(), MUSIC_2_ARRAY[it]) }
    private val MUSIC_3_ARRAY = ToolsResources.getStringIndexedArrayId("music_3_")
    val MUSIC_3 = Array(MUSIC_3_ARRAY.size) { FandomParam(it.toLong(), MUSIC_3_ARRAY[it]) }
    private val PROGRAMS_1_ARRAY = ToolsResources.getStringIndexedArrayId("programs_1_")
    val PROGRAMS_1 = Array(PROGRAMS_1_ARRAY.size) { FandomParam(it.toLong(), PROGRAMS_1_ARRAY[it]) }
    private val PROGRAMS_2_ARRAY = ToolsResources.getStringIndexedArrayId("programs_2_")
    val PROGRAMS_2 = Array(PROGRAMS_2_ARRAY.size) { FandomParam(it.toLong(), PROGRAMS_2_ARRAY[it]) }
    private val MOVIES_1_ARRAY = ToolsResources.getStringIndexedArrayId("movies_1_")
    val MOVIES_1 = Array(MOVIES_1_ARRAY.size) { FandomParam(it.toLong(), MOVIES_1_ARRAY[it]) }
    private val MOVIES_2_ARRAY = ToolsResources.getStringIndexedArrayId("movies_2_")
    val MOVIES_2 = Array(MOVIES_2_ARRAY.size) { FandomParam(it.toLong(), MOVIES_2_ARRAY[it]) }
    private val SITE_1_ARRAY = ToolsResources.getStringIndexedArrayId("site_1_")
    val SITE_1 = Array(SITE_1_ARRAY.size) { FandomParam(it.toLong(), SITE_1_ARRAY[it]) }
    private val COMPANY_1_ARRAY = ToolsResources.getStringIndexedArrayId("company_1_")
    val COMPANY_1 = Array(COMPANY_1_ARRAY.size) { FandomParam(it.toLong(), COMPANY_1_ARRAY[it]) }
    private val BOOKS_1_ARRAY = ToolsResources.getStringIndexedArrayId("books_1_")
    val BOOKS_1 = Array(BOOKS_1_ARRAY.size) { FandomParam(it.toLong(), BOOKS_1_ARRAY[it]) }
    private val ANIMALS_1_ARRAY = ToolsResources.getStringIndexedArrayId("animals_1_")
    val ANIMALS_1 = Array(ANIMALS_1_ARRAY.size) { FandomParam(it.toLong(), ANIMALS_1_ARRAY[it]) }
    private val HOBBIES_1_ARRAY = ToolsResources.getStringIndexedArrayId("hobbies_1_")
    val HOBBIES_1 = Array(HOBBIES_1_ARRAY.size) { FandomParam(it.toLong(), HOBBIES_1_ARRAY[it]) }
    private val PEOPLE_1_ARRAY = ToolsResources.getStringIndexedArrayId("people_1_")
    val PEOPLE_1 = Array(PEOPLE_1_ARRAY.size) { FandomParam(it.toLong(), PEOPLE_1_ARRAY[it]) }
    private val EVENT_1_ARRAY = ToolsResources.getStringIndexedArrayId("event_1_")
    val EVENT_1 = Array(EVENT_1_ARRAY.size) { FandomParam(it.toLong(), EVENT_1_ARRAY[it]) }
    private val PLANTS_1_ARRAY = ToolsResources.getStringIndexedArrayId("plants_1_")
    val PLANTS_1 = Array(PLANTS_1_ARRAY.size) { FandomParam(it.toLong(), PLANTS_1_ARRAY[it]) }
    private val PLACES_1_ARRAY = ToolsResources.getStringIndexedArrayId("places_1_")
    val PLACES_1 = Array(PLACES_1_ARRAY.size) { FandomParam(it.toLong(), PLACES_1_ARRAY[it]) }

    fun getAchievement(info: AchievementInfo): Achievement {
        return getAchievement(info.index)
    }

    fun getAchievement(index: Long): Achievement {
        for (a in ACHIEVEMENTS)
            if (a.info.index == index)
                return a
        return Achievement(API.ACHI_UNKNOWN, R.string.error_unknown, R.color.red_500, true, R.drawable.ic_clear_black_24dp)
    }

    fun getQuest(info: QuestInfo): Quest {
        return getQuest(info.index)
    }

    fun getQuest(index: Long): Quest {
        for (a in QUESTS)
            if (a.quest.index == index)
                return a
        return Quest(API.QUEST_UNKNOWN, R.string.error_unknown)
    }

    fun getStoryQuest(index: Long): QuestStory? {
        for (a in QUESTS_STORY)
            if (a.index == index)
                return a
        return null
    }

    fun getCategory(index: Long): FandomParam {
        for (a in CATEGORIES)
            if (a.index == index) return a
        return FandomParam(API.CATEGORY_UNKNOWN, R.string.error_unknown)
    }

    @DrawableRes
    fun getCategoryIcon(categoryId: Long): Int {
        return ToolsResources.getDrawableAttrId(
                when (categoryId) {
                    API.CATEGORY_GAMES -> R.attr.icon_games
                    API.CATEGORY_ANIME -> R.attr.icon_anime
                    API.CATEGORY_MUSIC -> R.attr.ic_music_note_18dp
                    API.CATEGORY_PROGRAMS -> R.attr.ic_memory_18dp
                    API.CATEGORY_MOVIES -> R.attr.ic_movie_creation_18dp
                    API.CATEGORY_SITE -> R.attr.ic_language_24dp
                    API.CATEGORY_COMPANY -> R.attr.ic_rowing_24dp
                    API.CATEGORY_OTHER -> R.attr.ic_widgets_24dp
                    API.CATEGORY_BOOKS -> R.attr.ic_book_24dp
                    API.CATEGORY_ANIMALS -> R.attr.ic_pets_24dp
                    API.CATEGORY_HOBBIES -> R.attr.ic_directions_bike_24dp
                    API.CATEGORY_PEOPLE -> R.attr.ic_person_24dp
                    API.CATEGORY_EVENT -> R.attr.ic_access_time_24dp
                    API.CATEGORY_PLANTS -> R.attr.ic_spa_24dp
                    API.CATEGORY_PLACES -> R.attr.ic_map_24dp
                    else -> R.attr.ic_clear_24dp
                }
        )
    }

    fun getParamTitle(categoryId: Long, paramsPosition: Int): String? {
        return when (categoryId) {
            API.CATEGORY_GAMES ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_genres)
                    2 -> ToolsResources.s(R.string.app_platforms)
                    3 -> ToolsResources.s(R.string.app_controllers)
                    else -> null
                }
            API.CATEGORY_ANIME ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_genres)
                    2 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_MUSIC ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_genres)
                    2 -> ToolsResources.s(R.string.app_instrument)
                    3 -> ToolsResources.s(R.string.app_composition)
                    else -> null
                }
            API.CATEGORY_PROGRAMS ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_purpose)
                    2 -> ToolsResources.s(R.string.app_platforms)
                    else -> null
                }
            API.CATEGORY_MOVIES ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_genres)
                    2 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_SITE ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_COMPANY ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_BOOKS ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_genres)
                    else -> null
                }
            API.CATEGORY_ANIMALS ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_HOBBIES ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_PEOPLE ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_EVENT ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_PLANTS ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_PLACES ->
                when (paramsPosition) {
                    1 -> ToolsResources.s(R.string.app_type)
                    else -> null
                }
            API.CATEGORY_OTHER -> null
            else -> ToolsResources.s(R.string.error_unknown)
        }
    }

    fun getParams(categoryId: Long, paramsPosition: Int): Array<FandomParam>? {
        return when (categoryId) {
            API.CATEGORY_GAMES ->
                when (paramsPosition) {
                    1 -> GAMES_1
                    2 -> GAMES_2
                    3 -> GAMES_3
                    else -> null
                }
            API.CATEGORY_ANIME ->
                when (paramsPosition) {
                    1 -> ANIME_1
                    2 -> ANIME_2
                    else -> null
                }
            API.CATEGORY_MUSIC ->
                when (paramsPosition) {
                    1 -> MUSIC_1
                    2 -> MUSIC_2
                    3 -> MUSIC_3
                    else -> null
                }
            API.CATEGORY_PROGRAMS ->
                when (paramsPosition) {
                    1 -> PROGRAMS_1
                    2 -> PROGRAMS_2
                    else -> null
                }
            API.CATEGORY_MOVIES ->
                when (paramsPosition) {
                    1 -> MOVIES_1
                    2 -> MOVIES_2
                    else -> null
                }
            API.CATEGORY_SITE ->
                when (paramsPosition) {
                    1 -> SITE_1
                    else -> null
                }
            API.CATEGORY_COMPANY ->
                when (paramsPosition) {
                    1 -> COMPANY_1
                    else -> null
                }
            API.CATEGORY_BOOKS ->
                when (paramsPosition) {
                    1 -> BOOKS_1
                    else -> null
                }
            API.CATEGORY_ANIMALS ->
                when (paramsPosition) {
                    1 -> ANIMALS_1
                    else -> null
                }
            API.CATEGORY_HOBBIES ->
                when (paramsPosition) {
                    1 -> HOBBIES_1
                    else -> null
                }
            API.CATEGORY_PEOPLE ->
                when (paramsPosition) {
                    1 -> PEOPLE_1
                    else -> null
                }
            API.CATEGORY_EVENT ->
                when (paramsPosition) {
                    1 -> EVENT_1
                    else -> null
                }
            API.CATEGORY_PLANTS ->
                when (paramsPosition) {
                    1 -> PLANTS_1
                    else -> null
                }
            API.CATEGORY_PLACES ->
                when (paramsPosition) {
                    1 -> PLACES_1
                    else -> null
                }
            API.CATEGORY_OTHER -> null
            else -> null
        }
    }

    fun getParam(categoryId: Long, paramsPosition: Int, index: Long): FandomParam {
        val params = getParams(categoryId, paramsPosition)!!
        for (i in params) if (i.index == index) return i
        return FandomParam(0, R.string.error_unknown)
    }

    fun getLvlImage(lvl: Long): Int {
        return when (lvl / 100) {
            1L -> R.drawable.bg_lvl_1
            2L -> R.drawable.bg_lvl_2
            3L -> R.drawable.bg_lvl_3
            4L -> R.drawable.bg_lvl_4
            5L -> R.drawable.bg_lvl_5
            6L -> R.drawable.bg_lvl_6
            7L -> R.drawable.bg_lvl_7
            8L -> R.drawable.bg_lvl_8
            9L -> R.drawable.bg_lvl_9
            else -> R.drawable.bg_lvl_10
        }
    }

    val FEED_TEXTS = ToolsResources.getStringIndexedArrayId("feed_loading_")
    val ACCOUNT_TEXT = ToolsResources.getStringIndexedArrayId("profile_subtitle_text_")
    val HELLO_TEXT = ToolsResources.getStringIndexedArrayId("campfire_hello_")

}