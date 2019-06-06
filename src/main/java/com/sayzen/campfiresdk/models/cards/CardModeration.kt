package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.moderations.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable

class CardModeration(override val unit: UnitModeration) : CardUnit(unit) {

    private val xKarma: XKarma = XKarma(unit) { update() }
    private val xAccount: XAccount = XAccount(unit, unit.dateCreate) { update() }
    private val xFandom: XFandom = XFandom(unit, unit.dateCreate) { update() }

    private var clickDisabled: Boolean = false

    override fun getLayout() = R.layout.card_moderation

    override fun bindView(view: View) {
        super.bindView(view)
        val vText:ViewTextLinkable = view.findViewById(R.id.vText)
        val viewKarma:ViewKarma = view.findViewById(R.id.vKarma)
        val vComments:TextView = view.findViewById(R.id.vComments)
        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vContainerInfo:View = view.findViewById(R.id.vInfoContainer)
        val vStatus:ViewTextLinkable = view.findViewById(R.id.vStatus)

        if(unit.moderation is ModerationBlock){
            vStatus.visibility = View.VISIBLE
            if(unit.tag_2 == 0L) {
                vStatus.setText(R.string.moderation_checked_empty)
                vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
            }
            if(unit.tag_2 == 1L) {
                vStatus.text = ToolsResources.s(R.string.moderation_checked_yes, ControllerApi.linkToUser(unit.tag_s_1))
                vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
            }
            if(unit.tag_2 == 2L) {
                vStatus.text = ToolsResources.s(R.string.moderation_checked_no, ControllerApi.linkToUser(unit.tag_s_1))
                vStatus.setTextColor(ToolsResources.getColor(R.color.red_700))
            }
            ControllerApi.makeLinkable(vStatus)
        }else{
            vStatus.visibility = View.GONE
        }

        vComments.text = unit.subUnitsCount.toString() + ""
        if (!showFandom)
            xAccount.setView(vAvatar)
        else
            xFandom.setView(vAvatar)
        xKarma.setView(viewKarma)

        vContainerInfo.visibility = if (unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE
        vAvatar.visibility = if (unit.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        ControllerUnits.setModerationText(vText, unit)

        if (clickDisabled) view.setOnClickListener(null)
        else view.setOnClickListener { v -> ControllerCampfireSDK.onToModerationClicked(unit.id, 0, Navigator.TO) }
    }


    override fun notifyItem() {

    }

    fun setClickDisabled(clickDisabled: Boolean): CardModeration {
        this.clickDisabled = clickDisabled
        update()
        return this
    }


}

