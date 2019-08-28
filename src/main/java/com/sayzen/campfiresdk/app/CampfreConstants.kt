package com.sayzen.campfiresdk.app

import androidx.annotation.DrawableRes
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.QuestInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.objects.*
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

object CampfreConstants {

    val RATE_TIME = 2000L

    val CHECK_RULES_ACCEPTED = "CHECK_RULES_ACCEPTED"

    val RULES_USER = arrayOf(
        Rule(R.string.rules_users_1_title, R.string.rules_users_1),
        Rule(R.string.rules_users_2_title, R.string.rules_users_2),
        Rule(R.string.rules_users_3_title, R.string.rules_users_3),
        Rule(R.string.rules_users_4_title, R.string.rules_users_4),
        Rule(R.string.rules_users_5_title, R.string.rules_users_5),
        Rule(R.string.rules_users_6_title, R.string.rules_users_6),
        Rule(R.string.rules_users_7_title, R.string.rules_users_7),
        Rule(R.string.rules_users_8_title, R.string.rules_users_8),
        Rule(R.string.rules_users_9_title, R.string.rules_users_9),
        Rule(R.string.rules_users_10_title, R.string.rules_users_10),
        Rule(R.string.rules_users_11_title, R.string.rules_users_11),
        Rule(R.string.rules_users_12_title, R.string.rules_users_12),
        Rule(R.string.rules_users_13_title, R.string.rules_users_13),
        Rule(R.string.rules_users_14_title, R.string.rules_users_14)
    )
    val RULES_MODER = arrayOf(
        R.string.rules_moderators_1,
        R.string.rules_moderators_2,
        R.string.rules_moderators_3,
        R.string.rules_moderators_4,
        R.string.rules_moderators_5,
        R.string.rules_moderators_6,
        R.string.rules_moderators_7,
        R.string.rules_moderators_8,
        R.string.rules_moderators_9
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
        Achievement(API.ACHI_TITLE_IMAGE, ToolsResources.s(R.string.achi_title_image, ToolsText.numToStringRound(API.LVL_CAN_CHANGE_PROFILE_IMAGE.lvl / 100.0, 2)), R.color.blue_500, false, R.drawable.achi_8),
        Achievement(API.ACHI_CREATE_TAG, R.string.achi_create_tag, R.color.teal_500, false, R.drawable.achi_12),
        Achievement(API.ACHI_QUESTS, R.string.achi_quests, R.color.teal_500, false, R.drawable.achi_6),
        Achievement(API.ACHI_FANDOMS, R.string.achi_fandoms, R.color.orange_500, true, R.drawable.achi_19_1),
        Achievement(API.ACHI_RULES_USER, R.string.achi_rules_user, R.color.teal_500, true, R.drawable.achi_21),
        Achievement(API.ACHI_RULES_MODERATOR, R.string.achi_rules_moderator, R.color.cyan_900, true, R.drawable.achi_21),
        Achievement(API.ACHI_FOLLOWERS, R.string.achi_followers, R.color.red_500, false, R.drawable.achi_5_3),
        Achievement(API.ACHI_MODER_CHANGE_POST_TAGS, R.string.achi_moderators_tags, R.color.indigo_500, false, R.drawable.achi_5_3),
        Achievement(API.ACHI_FIREWORKS, R.string.achi_50, R.color.indigo_500, false, R.drawable.achi_23),
        Achievement(API.ACHI_MAKE_MODER, R.string.achi_51, R.color.orange_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_CREATE_FORUM, R.string.achi_52, R.color.pink_500, false, R.drawable.achi_13),
        Achievement(API.ACHI_REVIEW_MODER_ACTION, R.string.achi_53, R.color.cyan_900, false, R.drawable.achi_21),
        Achievement(API.ACHI_ACCEPT_FANDOM, R.string.achi_54, R.color.teal_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_MODERATOR_COUNT, R.string.achi_55, R.color.teal_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_MODERATOR_ACTION_KARMA, R.string.achi_56, R.color.light_blue_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_KARMA_30, R.string.achi_57, R.color.deep_purple_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_UP_RATES, R.string.achi_58, R.color.indigo_500, false, R.drawable.achi_21),
        Achievement(API.ACHI_UP_RATES_OVER_DOWN, R.string.achi_59, R.color.cyan_900, false, R.drawable.achi_21),
        Achievement(API.ACHI_CHAT_SUBSCRIBE, R.string.achi_60, R.color.teal_500, false, R.drawable.achi_21)
    )

    val LVLS = arrayOf(
        AppLevel(API.LVL_APP_ACCESS, R.string.lvl_app_access),
        AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE, R.string.lvl_app_profile_image),
        AppLevel(API.LVL_CAN_CHANGE_STATUS, R.string.lvl_app_status),
        AppLevel(API.LVL_CAN_ADS_LESS, R.string.lvl_ads_less),
        AppLevel(API.LVL_CAN_CHANGE_AVATAR_GIF, R.string.lvl_avatar_gif),
        AppLevel(API.LVL_CAN_PIN_POST, R.string.lvl_pin_post),
        AppLevel(R.string.lvl_moderate_block, API.LVL_MODERATOR_BLOCK, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_to_drafts, API.LVL_MODERATOR_TO_DRAFTS, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_forums, API.LVL_MODERATOR_FORUMS, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_post_tags, API.LVL_MODERATOR_POST_TAGS, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_image_title, API.LVL_MODERATOR_FANDOM_IMAGE, R.color.blue_500),
        AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE_GIF, R.string.lvl_app_profile_image_gif, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_description, API.LVL_MODERATOR_DESCRIPTION, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_names, API.LVL_MODERATOR_NAMES, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_gallery, API.LVL_MODERATOR_GALLERY, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_links, API.LVL_MODERATOR_LINKS, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_tags, API.LVL_MODERATOR_TAGS, R.color.blue_500),
        AppLevel(API.LVL_CAN_ADS_REMOVE, R.string.lvl_ads_remove, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_pin_post, API.LVL_MODERATOR_PIN_POST, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_important, API.LVL_MODERATOR_IMPORTANT, R.color.blue_500),
        AppLevel(R.string.lvl_moderate_review_remove_text, API.LVL_MODERATOR_REVIEW_REMOVE_TEXT, R.color.blue_500),
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
        AppLevel(R.string.lvl_community_admin, API.LVL_ADMIN_FANDOM_ADMIN, R.color.red_500)
    )

    val QUESTS = arrayOf(
        Quest(API.QUEST_RATES, R.string.quests_text_rates),
        Quest(API.QUEST_POSTS, R.string.quests_text_posts),
        Quest(API.QUEST_COMMENTS, R.string.quests_text_comments),
        Quest(API.QUEST_CHAT, R.string.quests_text_chat),
        Quest(API.QUEST_POST_KARMA, R.string.quests_text_posts_karma),
        Quest(API.QUEST_COMMENTS_KARMA, R.string.quests_text_comments_karma))

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
        FandomParam(API.CATEGORY_OTHER, R.string.category_other)
    )

    val GAMES_1 = arrayOf(FandomParam(1, R.string.games_genres_1),
        FandomParam(2, R.string.games_genres_2),
        FandomParam(3, R.string.games_genres_3),
        FandomParam(4, R.string.games_genres_4),
        FandomParam(5, R.string.games_genres_5),
        FandomParam(6, R.string.games_genres_6),
        FandomParam(7, R.string.games_genres_7),
        FandomParam(8, R.string.games_genres_8),
        FandomParam(9, R.string.games_genres_9),
        FandomParam(10, R.string.games_genres_10),
        FandomParam(11, R.string.games_genres_11),
        FandomParam(12, R.string.games_genres_12),
        FandomParam(13, R.string.games_genres_13),
        FandomParam(14, R.string.games_genres_14),
        FandomParam(15, R.string.games_genres_15),
        FandomParam(16, R.string.games_genres_16),
        FandomParam(17, R.string.games_genres_17),
        FandomParam(18, R.string.games_genres_18),
        FandomParam(19, R.string.games_genres_19),
        FandomParam(20, R.string.games_genres_20),
        FandomParam(21, R.string.games_genres_21),
        FandomParam(22, R.string.games_genres_22),
        FandomParam(23, R.string.games_genres_23),
        FandomParam(24, R.string.games_genres_24),
        FandomParam(25, R.string.games_genres_25),
        FandomParam(26, R.string.games_genres_26),
        FandomParam(27, R.string.games_genres_27),
        FandomParam(28, R.string.games_genres_28)
    )
    val GAMES_2 = arrayOf(FandomParam(1, R.string.games_platform_1),
        FandomParam(2, R.string.games_platform_2),
        FandomParam(3, R.string.games_platform_3),
        FandomParam(4, R.string.games_platform_4),
        FandomParam(5, R.string.games_platform_5),
        FandomParam(6, R.string.games_platform_6),
        FandomParam(7, R.string.games_platform_7),
        FandomParam(8, R.string.games_platform_8),
        FandomParam(9, R.string.games_platform_9),
        FandomParam(10, R.string.games_platform_10)
    )
    val GAMES_3 = arrayOf(FandomParam(1, R.string.games_control_1),
        FandomParam(2, R.string.games_control_2),
        FandomParam(3, R.string.games_control_3),
        FandomParam(4, R.string.games_control_4),
        FandomParam(5, R.string.games_control_5)
    )
    val ANIME_1 = arrayOf(FandomParam(1, R.string.anime_genres_1),
        FandomParam(2, R.string.anime_genres_2),
        FandomParam(3, R.string.anime_genres_3),
        FandomParam(4, R.string.anime_genres_4),
        FandomParam(5, R.string.anime_genres_5),
        FandomParam(6, R.string.anime_genres_6),
        FandomParam(7, R.string.anime_genres_7),
        FandomParam(8, R.string.anime_genres_8),
        FandomParam(9, R.string.anime_genres_9),
        FandomParam(10, R.string.anime_genres_10),
        FandomParam(11, R.string.anime_genres_11),
        FandomParam(12, R.string.anime_genres_12),
        FandomParam(13, R.string.anime_genres_13),
        FandomParam(14, R.string.anime_genres_14),
        FandomParam(15, R.string.anime_genres_15),
        FandomParam(16, R.string.anime_genres_16),
        FandomParam(17, R.string.anime_genres_17),
        FandomParam(18, R.string.anime_genres_18),
        FandomParam(19, R.string.anime_genres_19),
        FandomParam(20, R.string.anime_genres_20),
        FandomParam(21, R.string.anime_genres_21),
        FandomParam(22, R.string.anime_genres_22),
        FandomParam(23, R.string.anime_genres_23),
        FandomParam(24, R.string.anime_genres_24),
        FandomParam(25, R.string.anime_genres_25),
        FandomParam(26, R.string.anime_genres_26),
        FandomParam(27, R.string.anime_genres_27),
        FandomParam(28, R.string.anime_genres_28),
        FandomParam(29, R.string.anime_genres_29),
        FandomParam(30, R.string.anime_genres_30)
    )
    val ANIME_2 = arrayOf(FandomParam(1, R.string.anime_type_1),
        FandomParam(2, R.string.anime_type_2),
        FandomParam(3, R.string.anime_type_3)
    )
    val MUSIC_1 = arrayOf(FandomParam(1, R.string.music_1_1),
        FandomParam(2, R.string.music_1_2),
        FandomParam(3, R.string.music_1_3),
        FandomParam(4, R.string.music_1_4),
        FandomParam(5, R.string.music_1_5),
        FandomParam(6, R.string.music_1_6),
        FandomParam(7, R.string.music_1_7),
        FandomParam(8, R.string.music_1_8),
        FandomParam(9, R.string.music_1_9),
        FandomParam(10, R.string.music_1_10),
        FandomParam(11, R.string.music_1_11),
        FandomParam(12, R.string.music_1_12),
        FandomParam(13, R.string.music_1_13),
        FandomParam(14, R.string.music_1_14),
        FandomParam(15, R.string.music_1_15),
        FandomParam(16, R.string.music_1_16),
        FandomParam(17, R.string.music_1_17),
        FandomParam(18, R.string.music_1_18),
        FandomParam(19, R.string.music_1_19),
        FandomParam(20, R.string.music_1_20),
        FandomParam(21, R.string.music_1_21),
        FandomParam(22, R.string.music_1_22),
        FandomParam(23, R.string.music_1_23)
    )
    val MUSIC_2 = arrayOf(FandomParam(1, R.string.music_2_1),
        FandomParam(2, R.string.music_2_2),
        FandomParam(3, R.string.music_2_3),
        FandomParam(4, R.string.music_2_4),
        FandomParam(5, R.string.music_2_5),
        FandomParam(6, R.string.music_2_6),
        FandomParam(7, R.string.music_2_7),
        FandomParam(8, R.string.music_2_8),
        FandomParam(9, R.string.music_2_9),
        FandomParam(10, R.string.music_2_10),
        FandomParam(11, R.string.music_2_11),
        FandomParam(12, R.string.music_2_12),
        FandomParam(13, R.string.music_2_13),
        FandomParam(14, R.string.music_2_14),
        FandomParam(15, R.string.music_2_15),
        FandomParam(16, R.string.music_2_16),
        FandomParam(17, R.string.music_2_17)
    )
    val MUSIC_3 = arrayOf(FandomParam(1, R.string.music_3_1),
        FandomParam(2, R.string.music_3_2),
        FandomParam(3, R.string.music_3_3),
        FandomParam(4, R.string.music_3_4),
        FandomParam(5, R.string.music_3_5),
        FandomParam(6, R.string.music_3_6),
        FandomParam(7, R.string.music_3_7),
        FandomParam(8, R.string.music_3_8)
    )
    val PROGRAMS_1 = arrayOf(FandomParam(1, R.string.programs_1_1),
        FandomParam(2, R.string.programs_1_2),
        FandomParam(3, R.string.programs_1_3),
        FandomParam(4, R.string.programs_1_4),
        FandomParam(5, R.string.programs_1_5),
        FandomParam(6, R.string.programs_1_6),
        FandomParam(7, R.string.programs_1_7),
        FandomParam(8, R.string.programs_1_8),
        FandomParam(9, R.string.programs_1_9),
        FandomParam(10, R.string.programs_1_10),
        FandomParam(11, R.string.programs_1_11),
        FandomParam(12, R.string.programs_1_12),
        FandomParam(13, R.string.programs_1_13),
        FandomParam(14, R.string.programs_1_14),
        FandomParam(15, R.string.programs_1_15),
        FandomParam(16, R.string.programs_1_16),
        FandomParam(17, R.string.programs_1_17),
        FandomParam(18, R.string.programs_1_18),
        FandomParam(19, R.string.programs_1_19),
        FandomParam(20, R.string.programs_1_20),
        FandomParam(21, R.string.programs_1_21),
        FandomParam(22, R.string.programs_1_22),
        FandomParam(23, R.string.programs_1_23),
        FandomParam(24, R.string.programs_1_24),
        FandomParam(25, R.string.programs_1_25),
        FandomParam(26, R.string.programs_1_26),
        FandomParam(27, R.string.programs_1_27),
        FandomParam(28, R.string.programs_1_28),
        FandomParam(29, R.string.programs_1_29)
    )
    val PROGRAMS_2 = arrayOf(FandomParam(1, R.string.programs_2_1),
        FandomParam(2, R.string.programs_2_2),
        FandomParam(3, R.string.programs_2_3),
        FandomParam(4, R.string.programs_2_4),
        FandomParam(5, R.string.programs_2_5)
    )
    val MOVIES_1 = arrayOf(FandomParam(1, R.string.movies_1_1),
        FandomParam(2, R.string.movies_1_2),
        FandomParam(3, R.string.movies_1_3),
        FandomParam(4, R.string.movies_1_4),
        FandomParam(5, R.string.movies_1_5),
        FandomParam(6, R.string.movies_1_6),
        FandomParam(7, R.string.movies_1_7),
        FandomParam(8, R.string.movies_1_8),
        FandomParam(9, R.string.movies_1_9),
        FandomParam(10, R.string.movies_1_10),
        FandomParam(11, R.string.movies_1_11),
        FandomParam(12, R.string.movies_1_12),
        FandomParam(13, R.string.movies_1_13),
        FandomParam(14, R.string.movies_1_14),
        FandomParam(15, R.string.movies_1_15)
    )
    val MOVIES_2 = arrayOf(FandomParam(1, R.string.movies_2_1),
        FandomParam(2, R.string.movies_2_2),
        FandomParam(3, R.string.movies_2_3),
        FandomParam(4, R.string.movies_2_4),
        FandomParam(5, R.string.movies_2_5)
    )
    val SITE_1 = arrayOf(FandomParam(1, R.string.site_1_1),
        FandomParam(2, R.string.site_1_2),
        FandomParam(3, R.string.site_1_3),
        FandomParam(4, R.string.site_1_4),
        FandomParam(5, R.string.site_1_5),
        FandomParam(6, R.string.site_1_6),
        FandomParam(7, R.string.site_1_7),
        FandomParam(8, R.string.site_1_8),
        FandomParam(9, R.string.site_1_9),
        FandomParam(10, R.string.site_1_10),
        FandomParam(11, R.string.site_1_11),
        FandomParam(12, R.string.site_1_12),
        FandomParam(13, R.string.site_1_13),
        FandomParam(14, R.string.site_1_14)
    )
    val COMPANY_1 = arrayOf(FandomParam(1, R.string.company_1_1),
        FandomParam(2, R.string.company_1_2),
        FandomParam(3, R.string.company_1_3),
        FandomParam(4, R.string.company_1_4),
        FandomParam(5, R.string.company_1_5),
        FandomParam(6, R.string.company_1_6),
        FandomParam(7, R.string.company_1_7),
        FandomParam(8, R.string.company_1_8),
        FandomParam(9, R.string.company_1_9),
        FandomParam(10, R.string.company_1_10)
    )
    val BOOKS_1 = arrayOf(FandomParam(1, R.string.books_1_1),
        FandomParam(2, R.string.books_1_2),
        FandomParam(3, R.string.books_1_3),
        FandomParam(4, R.string.books_1_4),
        FandomParam(5, R.string.books_1_5),
        FandomParam(6, R.string.books_1_6),
        FandomParam(7, R.string.books_1_7),
        FandomParam(8, R.string.books_1_8),
        FandomParam(9, R.string.books_1_9),
        FandomParam(10, R.string.books_1_10),
        FandomParam(11, R.string.books_1_11)
    )
    val ANIMALS_1 = arrayOf(FandomParam(1, R.string.animals_1_1),
        FandomParam(2, R.string.animals_1_2),
        FandomParam(3, R.string.animals_1_3),
        FandomParam(4, R.string.animals_1_4),
        FandomParam(5, R.string.animals_1_5),
        FandomParam(6, R.string.animals_1_6),
        FandomParam(7, R.string.animals_1_7),
        FandomParam(8, R.string.animals_1_8)
    )
    val HOBBIES_1 = arrayOf(FandomParam(1, R.string.hobbies_1_1),
        FandomParam(2, R.string.hobbies_1_2),
        FandomParam(3, R.string.hobbies_1_3),
        FandomParam(4, R.string.hobbies_1_4)
    )
    val PEOPLE_1 = arrayOf(FandomParam(1, R.string.people_1_1),
        FandomParam(2, R.string.people_1_2),
        FandomParam(3, R.string.people_1_3),
        FandomParam(4, R.string.people_1_4),
        FandomParam(5, R.string.people_1_5),
        FandomParam(6, R.string.people_1_6),
        FandomParam(7, R.string.people_1_7),
        FandomParam(8, R.string.people_1_8),
        FandomParam(9, R.string.people_1_9)
    )


    fun getAchievement(info: AchievementInfo): Achievement {
        return getAchievement(info.index)
    }

    fun getAchievement(index: Long): Achievement {
        for (a in ACHIEVEMENTS)
            if (a.info.index == index)
                return a
        throw RuntimeException("Unknown achievement $index")
    }

    fun getQuest(info: QuestInfo): Quest {
        return getQuest(info.index)
    }

    fun getQuest(index: Long): Quest {
        for (a in QUESTS)
            if (a.quest.index == index)
                return a
        throw RuntimeException("Unknown quest $index")
    }

    fun getCategory(index: Long): FandomParam {
        for (a in CATEGORIES)
            if (a.index == index) return a
        throw RuntimeException("Unknown Category $index")
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
                else -> throw RuntimeException("Unknown category $categoryId")
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
            API.CATEGORY_OTHER -> null
            else -> throw RuntimeException("Unknown category $categoryId")
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
            API.CATEGORY_OTHER -> null
            else -> null
        }
    }

    fun getParam(categoryId: Long, paramsPosition: Int, index: Long): FandomParam {
        val params = getParams(categoryId, paramsPosition)!!
        for (i in params) if (i.index == index) return i
        throw RuntimeException("Unknown Genre $index in categoryId $categoryId paramsPosition $paramsPosition")
    }

    fun getLvlImage(lvl: Long):Int {
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

    val FEED_TEXTS = arrayOf(R.string.feed_loading,
            R.string.feed_loading_1, R.string.feed_loading_2, R.string.feed_loading_3, R.string.feed_loading_4, R.string.feed_loading_5,
            R.string.feed_loading_6, R.string.feed_loading_7, R.string.feed_loading_8, R.string.feed_loading_9, R.string.feed_loading_10,
            R.string.feed_loading_11, R.string.feed_loading_12, R.string.feed_loading_13, R.string.feed_loading_14, R.string.feed_loading_15,
            R.string.feed_loading_16, R.string.feed_loading_17, R.string.feed_loading_18, R.string.feed_loading_19, R.string.feed_loading_20, R.string.feed_loading_21,
            R.string.feed_loading_22, R.string.feed_loading_23, R.string.feed_loading_24, R.string.feed_loading_25, R.string.feed_loading_26, R.string.feed_loading_27, R.string.feed_loading_28, R.string.feed_loading_29, R.string.feed_loading_30,
            R.string.feed_loading_31, R.string.feed_loading_32
    )

    val ACCOUNT_TEXT = arrayOf(
            R.string.profile_subtitle_text_1, R.string.profile_subtitle_text_2, R.string.profile_subtitle_text_3, R.string.profile_subtitle_text_4, R.string.profile_subtitle_text_5, R.string.profile_subtitle_text_6, R.string.profile_subtitle_text_7, R.string.profile_subtitle_text_8, R.string.profile_subtitle_text_9,
            R.string.profile_subtitle_text_10
    )

}