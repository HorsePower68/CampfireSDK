package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.units.UnitForum
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.events.fandom.EventForumChanged
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardForum(
    unit: UnitForum
) : CardUnit(R.layout.card_forum, unit) {

    private val eventBus = EventBus
            .subscribe(EventForumChanged::class) { this.onEventForumChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitForum

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vTouch: View = view.findViewById(R.id.vTouch)
        val vComments:TextView = view.findViewById(R.id.vComments)
        val vMenu:View = view.findViewById(R.id.vMenu)

        vMenu.setOnClickListener {  ControllerUnits.showForumPopup(unit) }
        vComments.text = unit.subUnitsCount.toString() + ""

        vAvatar.setTitle(unit.name)
        if (unit.text.length > 500)
            vAvatar.setSubtitle("${unit.text.subSequence(0, 500)}...")
        else
            vAvatar.setSubtitle(unit.text)


        vAvatar.setOnClickListener { vTouch.performClick() }
        vTouch.setOnClickListener { ControllerCampfireSDK.onToForumClicked(unit.id, 0, Navigator.TO) }
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        if(getView() == null) return
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        if (showFandom) xUnit.xFandom.setView(vAvatar)
        else ToolsImagesLoader.load((xUnit.unit as UnitForum).imageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun updateAccount() {
        update()
    }

    override fun updateKarma() {
        if(getView() == null) return
        xUnit.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    override fun updateReports() {
        if(getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }


    override fun notifyItem() {

    }

    //
    //  EventBus
    //

    private fun onEventForumChanged(e: EventForumChanged) {
        val unit = xUnit.unit as UnitForum
        if (e.unitId == unit.id) {
            unit.name = e.name
            unit.text = e.text
            unit.imageId = e.unitId
            update()
        }
    }

}
