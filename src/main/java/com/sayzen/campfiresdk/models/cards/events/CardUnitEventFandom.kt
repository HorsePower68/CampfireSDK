package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.events_fandoms.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardUnitEventFandom(
        unit: UnitEventFandom
) : CardUnit(R.layout.card_event, unit) {

    private val xAccount: XAccount

    init {
        val e = unit.event!!
        xAccount = XAccount(e.ownerAccountId, e.ownerAccountName, e.ownerAccountImageId, 0, 0) { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val unit = xUnit.unit as UnitEventFandom

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
            is ApiEventFandomAccepted -> {
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, ControllerApi.getLanguageId(), Navigator.TO) }
                text = ToolsResources.sCap(R.string.unit_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, R.string.he_accept, R.string.she_accept), e.fandomName)
            }
            is ApiEventFandomChangeAvatar -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomChangeCategory -> {
                text = ToolsResources.sCap(R.string.unit_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed),
                        e.fandomName,
                        CampfireConstants.getCategory(e.oldCategory).name,
                        CampfireConstants.getCategory(e.newCategory).name)
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomChangeParams -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed), "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_new) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + ToolsResources.s(R.string.unit_event_fandom_genres_remove) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomClose -> {
                text = ToolsResources.sCap(
                        R.string.unit_event_fandom_close,
                        if (e.closed) ToolsResources.sex(e.ownerAccountSex, R.string.he_close, R.string.she_close) else ToolsResources.sex(e.ownerAccountSex, R.string.he_open, R.string.she_open),
                        e.fandomName + "(" + ControllerApi.linkToFandom(e.fandomId) + ")")

                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
            }
            is ApiEventFandomCofChanged -> {
                text = "" + ToolsResources.sCap(R.string.unit_event_fandom_karma_cof,
                        ToolsResources.sex(e.ownerAccountSex, R.string.he_changed, R.string.she_changed),
                        e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId)})",
                        ToolsText.numToStringRound(e.oldCof / 100.0, 2),
                        ToolsText.numToStringRound(e.newCof / 100.0, 2)
                )
            }
            is ApiEventFandomMakeModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_make, R.string.she_make), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.fandomLanguageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventFandomRemove -> {
                text = "" + ToolsResources.sCap(R.string.unit_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, R.string.he_remove, R.string.she_remove), e.fandomName)
            }
            is ApiEventFandomRemoveModerator -> {
                text = ToolsResources.sCap(R.string.unit_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, R.string.he_deprived, R.string.she_deprived), ControllerApi.linkToUser(e.targetAccountName), e.fandomName + " (${ControllerApi.linkToFandom(e.fandomId, e.fandomLanguageId)})")
                view.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventFandomRename -> {
                text = ToolsResources.sCap(R.string.unit_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, R.string.he_rename, R.string.she_rename), e.oldName, "" + e.fandomName + " (" + ControllerApi.linkToFandom(e.fandomId) + ")")
                view.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(e.fandomId, 0, Navigator.TO) }
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
