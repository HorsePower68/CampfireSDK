package com.sayzen.campfiresdk.models.cards

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.UnitForum
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.events.fandom.EventForumChanged
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardForum(override val unit: UnitForum) : CardUnit(unit) {

    private val eventBus = EventBus
            .subscribe(EventForumChanged::class) { this.onEventForumChanged(it) }

    private val xFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xKarma = XKarma(unit) { update() }

    override fun getLayout() = R.layout.card_forum

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vReports: TextView = view.findViewById(R.id.vReports)
        val vKarma: ViewKarma = view.findViewById(R.id.vKarma)
        val vTouch: View = view.findViewById(R.id.vTouch)
        val vComments:TextView = view.findViewById(R.id.vComments)
        val vMenu:View = view.findViewById(R.id.vMenu)

        vMenu.setOnClickListener {  ControllerUnits.showForumPopup(it, unit) }
        vComments.text = unit.subUnitsCount.toString() + ""
        vReports.text = unit.reportsCount.toString() + ""
        vReports.visibility = if (unit.reportsCount > 0 && ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK)) View.VISIBLE else View.GONE

        if (showFandom) xFandom.setView(vAvatar)
        else ToolsImagesLoader.load(unit.imageId).into(vAvatar.vAvatar.vImageView)



        vAvatar.setTitle(unit.name)
        if (unit.text.length > 500)
            vAvatar.setSubtitle("${unit.text.subSequence(0, 500)}...")
        else
            vAvatar.setSubtitle(unit.text)

        xKarma.setView(vKarma)

        vAvatar.setOnClickListener { vTouch.performClick() }
        vTouch.setOnClickListener { ControllerCampfireSDK.onToForumClicked(unit.id, 0, Navigator.TO) }

    }

    override fun notifyItem() {

    }

    //
    //  EventBus
    //

    private fun onEventForumChanged(e: EventForumChanged) {
        if (e.unitId == unit.id) {
            unit.name = e.name
            unit.text = e.text
            unit.imageId = e.unitId
            update()
        }
    }

}
