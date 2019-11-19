package com.sayzen.campfiresdk.screens.activities.administration.block

import android.view.View
import android.widget.Button
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationBlocked
import com.dzen.campfire.api.requests.units.RUnitsAdminRemove
import com.dzen.campfire.api.requests.units.RUnitsAdminRestore
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlockedRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus

class CardUnitBlock(
        val publication: PublicationBlocked
) : Card(R.layout.screen_administration_block_card){

    override fun bindView(view: View) {
        super.bindView(view)

        val vCancel:Button = view.findViewById(R.id.vCancel)
        val vAccept:Button = view.findViewById(R.id.vAccept)
        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vFandom:ViewAvatar = view.findViewById(R.id.vFandom)

        val xAccount = XAccount(publication.moderatorId, publication.moderatorName, publication.moderatorImageId, publication.moderatorLvl, publication.moderatorLastOnlineTime){
            update()
        }
        val xFandom = XFandom(publication.publication){update()}

        xAccount.setView(vAvatar)
        xFandom.setView(vFandom)
        vAvatar.setSubtitle(publication.comment)
        vFandom.setChipText(null)

        vAccept.isEnabled = !ControllerApi.isCurrentAccount(publication.moderatorId) || ControllerApi.protoadmins.contains(publication.moderatorId)
        vCancel.isEnabled = !ControllerApi.isCurrentAccount(publication.moderatorId) || ControllerApi.protoadmins.contains(publication.moderatorId)

        vAccept.setOnClickListener {
            EventBus.post(EventPublicationBlockedRemove(publication.moderationId, publication.publication.id))
            ToolsToast.show(R.string.app_done)
            ApiRequestsSupporter.execute(RUnitsAdminRemove(publication.moderationId)){
            }
        }

        vCancel.setOnClickListener {
            WidgetField()
                    .setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_reject) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(w, RUnitsAdminRestore(publication.moderationId, comment)) {
                            ToolsToast.show(R.string.app_done)
                            EventBus.post(EventPublicationBlockedRemove(publication.moderationId, publication.publication.id))
                        }
                    }
                    .asSheetShow()
        }

    }

}