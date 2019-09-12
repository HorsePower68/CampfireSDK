package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.events_moderators.ApiEventModerBan
import com.dzen.campfire.api.models.units.events_moderators.ApiEventModerWarn
import com.dzen.campfire.api.models.units.events_moderators.UnitEventModer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.tools.ToolsDate

class CardUnitEventModer(
        unit: UnitEventModer
) : CardUnit(R.layout.card_event, unit) {

    private val xAccount: XAccount

    init {
        val e = unit.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val unit = xUnit.unit as UnitEventModer

        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vDate.text = ToolsDate.dateToString(unit.dateCreate)
        vName.text = ""
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = unit.event!!
        var text = ""

        xUnit.xAccount.lvl = 0    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventModerBan -> {
                text = ToolsResources.sCap(R.string.unit_event_blocked_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(e.targetAccountName), "" + e.fandomName + " (" + ControllerApi.linkToFandom(unit.fandomId, unit.languageId) + ")", ToolsDate.dateToStringFull(e.blockDate))
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventModerWarn -> {
                text = ToolsResources.sCap(R.string.unit_event_unit_warn_app_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(e.targetAccountName), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId, e.fandomLanguageId) + ")")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }

        }

        if (e.comment.isNotEmpty()) text += "\n" + ToolsResources.s(R.string.app_comment) + ": " + e.comment

        vText.text = text
        ControllerApi.makeLinkable(vText)

        if (showFandom && unit.fandomId > 0) {
            xUnit.xFandom.setView(vAvatarTitle)
            vName.text = xUnit.xFandom.name
        } else {
            xAccount.setView(vAvatarTitle)
            vName.text = xAccount.name
        }
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        update()
    }

    override fun updateAccount() {
        update()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        update()
    }

    override fun notifyItem() {

    }

}
