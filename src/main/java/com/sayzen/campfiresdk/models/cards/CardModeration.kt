package com.sayzen.campfiresdk.models.cards

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.moderations.*
import com.dzen.campfire.api.models.units.moderations.units.ModerationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable

class CardModeration(
        unit: UnitModeration
) : CardUnit(R.layout.card_moderation, unit) {

    private var clickDisabled: Boolean = false

    override fun bindView(view: View) {
        super.bindView(view)

        val unit = xUnit.unit as UnitModeration

        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vContainerInfo: View = view.findViewById(R.id.vInfoContainer)
        val vStatus: ViewTextLinkable = view.findViewById(R.id.vStatus)
        val vStatusComment: ViewTextLinkable = view.findViewById(R.id.vStatusComment)

        vStatusComment.visibility = View.GONE

        if (unit.moderation is ModerationBlock) {
            vStatus.visibility = View.VISIBLE
            if (unit.tag_2 == 0L) {
                vStatus.setText(R.string.moderation_checked_empty)
                vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
            }
            if (unit.tag_2 == 1L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
                vStatus.text = ToolsResources.s(R.string.moderation_checked_yes, ControllerApi.linkToUser((unit.moderation!! as ModerationBlock).checkAdminName))
            }
            if (unit.tag_2 == 2L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.red_700))
                vStatus.text = ToolsResources.s(R.string.moderation_checked_no, ControllerApi.linkToUser((unit.moderation!! as ModerationBlock).checkAdminName))
                vStatusComment.visibility = View.VISIBLE
                vStatusComment.text = (unit.moderation!! as ModerationBlock).checkAdminComment
            }
            ControllerApi.makeLinkable(vStatus)
        } else {
            vStatus.visibility = View.GONE
        }

        vContainerInfo.visibility = if (unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        ControllerUnits.setModerationText(vText, unit)

        if (clickDisabled) view.setOnClickListener(null)
        else view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(unit.id, 0, Navigator.TO) }
    }

    override fun updateComments() {
        if (getView() == null) return
        xUnit.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateAccount() {
        if (getView() == null) return
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        if (!showFandom)
            xUnit.xAccount.setView(vAvatar)
        else
            xUnit.xFandom.setView(vAvatar)
        vAvatar.visibility = if (xUnit.unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE
    }

    override fun updateKarma() {
        if (getView() == null) return
        xUnit.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    override fun updateReports() {
        update()
    }

    override fun notifyItem() {

    }

    fun setClickDisabled(clickDisabled: Boolean): CardModeration {
        this.clickDisabled = clickDisabled
        update()
        return this
    }


}

