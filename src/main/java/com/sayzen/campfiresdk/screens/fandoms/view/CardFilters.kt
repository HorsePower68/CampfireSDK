package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.WidgetCheckBoxes

class CardFilters(
        private val onChange: () -> Unit
) : Card(R.layout.screen_account_card_filters) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vFilters: ViewIcon = view.findViewById(R.id.vFilters)

        vFilters.setOnClickListener {

            val fandomFilterModerationsPostsOld = ControllerSettings.fandomFilterModerationsPosts
            val fandomFilterOnlyImportantOld = ControllerSettings.fandomFilterOnlyImportant
            val fandomFilterAdministrationsOld = ControllerSettings.fandomFilterAdministrations
            val fandomFilterModerationsOld = ControllerSettings.fandomFilterModerations
            val fandomFilterModerationsBlocksOld = ControllerSettings.fandomFilterModerationsBlocks

            WidgetCheckBoxes()
                    .add(R.string.filter_posts).checked(ControllerSettings.fandomFilterModerationsPosts).onChange { _, _, b -> ControllerSettings.fandomFilterModerationsPosts = b }
                    .add(R.string.filter_only_important).checked(ControllerSettings.fandomFilterOnlyImportant).onChange { _, _, b -> ControllerSettings.fandomFilterOnlyImportant = b }
                    .add(R.string.filter_events).checked(ControllerSettings.fandomFilterAdministrations).onChange { _, _, b -> ControllerSettings.fandomFilterAdministrations = b }
                    .add(R.string.filter_moderations).checked(ControllerSettings.fandomFilterModerations).onChange { _, _, b -> ControllerSettings.fandomFilterModerations = b }
                    .add(R.string.filter_moderations_block).checked(ControllerSettings.fandomFilterModerationsBlocks).onChange { _, _, b -> ControllerSettings.fandomFilterModerationsBlocks = b }
                    .setOnHide {
                        if (fandomFilterModerationsPostsOld != ControllerSettings.fandomFilterModerationsPosts
                                || fandomFilterOnlyImportantOld != ControllerSettings.fandomFilterOnlyImportant
                                || fandomFilterAdministrationsOld != ControllerSettings.fandomFilterAdministrations
                                || fandomFilterModerationsOld != ControllerSettings.fandomFilterModerations
                                || fandomFilterModerationsBlocksOld != ControllerSettings.fandomFilterModerationsBlocks
                        ) {
                            onChange.invoke()
                        }
                    }
                    .asSheetShow()
        }
    }
}
