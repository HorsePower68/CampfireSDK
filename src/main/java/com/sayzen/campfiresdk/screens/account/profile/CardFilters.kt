package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.WidgetCheckBoxes

class CardFilters(
        private val onChange: () -> Unit
) : Card( R.layout.screen_account_card_filters) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vFilters: ViewIcon = view.findViewById(R.id.vFilters)

        vFilters.setOnClickListener {

            val eventsOld = ControllerSettings.profileFilterEvents
            val postOld = ControllerSettings.profileFilterPosts
            val commentOld = ControllerSettings.profileFilterComments
            val chatMessagesOld = ControllerSettings.profileFilterChatMessages
            val moderationsOld = ControllerSettings.profileFilterModerations
            val reviewsOld = ControllerSettings.profileFilterReviews
            val forumsOld = ControllerSettings.profileFilterForums
            val stickersOld = ControllerSettings.profileFilterStickers

            WidgetCheckBoxes()
                    .add(R.string.filter_events).checked(ControllerSettings.profileFilterEvents).onChange {_, _, b -> ControllerSettings.profileFilterEvents = b }
                    .add(R.string.filter_posts).checked(ControllerSettings.profileFilterPosts).onChange {_, _, b -> ControllerSettings.profileFilterPosts = b }
                    .add(R.string.filter_comment).checked(ControllerSettings.profileFilterComments).onChange {_, _, b -> ControllerSettings.profileFilterComments = b }
                    .add(R.string.filter_chat_messages).checked(ControllerSettings.profileFilterChatMessages).onChange {_, _, b -> ControllerSettings.profileFilterChatMessages = b }
                    .add(R.string.filter_moderations).checked(ControllerSettings.profileFilterModerations).onChange {_, _, b -> ControllerSettings.profileFilterModerations = b }
                    .add(R.string.app_reviews).checked(ControllerSettings.profileFilterReviews).onChange {_, _, b -> ControllerSettings.profileFilterReviews = b }
                    .add(R.string.app_forums).checked(ControllerSettings.profileFilterForums).onChange {_, _, b -> ControllerSettings.profileFilterForums = b }
                    .add(R.string.app_stickers).checked(ControllerSettings.profileFilterStickers).onChange { _, _, b -> ControllerSettings.profileFilterStickers = b }
                    .setOnHide {
                        if (eventsOld != ControllerSettings.profileFilterEvents
                                || postOld != ControllerSettings.profileFilterPosts
                                || commentOld != ControllerSettings.profileFilterComments
                                || chatMessagesOld != ControllerSettings.profileFilterChatMessages
                                || moderationsOld != ControllerSettings.profileFilterModerations
                                || reviewsOld != ControllerSettings.profileFilterReviews
                                || forumsOld != ControllerSettings.profileFilterForums
                                || stickersOld != ControllerSettings.profileFilterStickers
                        ) {
                            if (!ControllerSettings.profileFilterEvents
                                    && !ControllerSettings.profileFilterPosts
                                    && !ControllerSettings.profileFilterComments
                                    && !ControllerSettings.profileFilterChatMessages
                                    && !ControllerSettings.profileFilterModerations
                                    && !ControllerSettings.profileFilterReviews
                                    && !ControllerSettings.profileFilterForums
                                    && !ControllerSettings.profileFilterStickers
                            ) {
                                ControllerSettings.profileFilterEvents = true
                                ControllerSettings.profileFilterPosts = true
                                ControllerSettings.profileFilterComments = true
                                ControllerSettings.profileFilterChatMessages = true
                                ControllerSettings.profileFilterModerations = true
                                ControllerSettings.profileFilterReviews = true
                                ControllerSettings.profileFilterForums = true
                                ControllerSettings.profileFilterStickers = true
                            }
                            onChange.invoke()
                        }
                    }
                    .asSheetShow()
        }
    }

}