package com.sayzen.campfiresdk.screens.post.view

import android.view.View
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.*
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewSpace
import com.sup.dev.android.views.views.layouts.LayoutFlow

class CardInfo(
        private val xUnit: XUnit,
        private val tags: Array<UnitTag>
) : Card(R.layout.screen_post_card_info) {

    init {
        xUnit.xFandom.showLanguage = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vFlow: LayoutFlow = view.findViewById(R.id.vFlow)
        val vMiddleDivider: View = view.findViewById(R.id.vMiddleDivider)

        val tags = ControllerUnits.parseTags(this.tags)

        if (tags.isEmpty()) {
            vMiddleDivider.visibility = View.GONE
            vFlow.visibility = View.GONE
        } else {
            vMiddleDivider.visibility = View.VISIBLE
            vFlow.visibility = View.VISIBLE
        }

        vFlow.removeAllViews()
        for (tagParent in tags) {
            addTag(tagParent.tag, vFlow)
            for (tag in tagParent.tags) addTag(tag, vFlow)
        }

        updateFandom()
        updateAccount()
        updateKarma()
        updateComments()
        updateReports()
    }

    fun updateFandom() {
        if (getView() == null) return
        xUnit.xFandom.setView(getView()!!.findViewById<ViewAvatar>(R.id.vFandom))
    }

    fun updateAccount() {
        if (getView() == null) return
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        xUnit.xAccount.setView(vAvatar)
        val unit = xUnit.unit as UnitPost
        if (unit.rubricId > 0) {
            vAvatar.vSubtitle.text = vAvatar.getSubTitle() + "  " + unit.rubricName
            ToolsView.addLink(vAvatar.vSubtitle, unit.rubricName) {
                SRubricPosts.instance(unit.rubricId, Navigator.TO)
            }
        }
    }

    fun updateKarma() {
        if (getView() == null) return
        xUnit.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    fun updateComments() {
        if (getView() == null) return
        xUnit.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    fun updateReports() {
        if (getView() == null) return
        xUnit.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun addTag(t: UnitTag, vFlow: LayoutFlow) {
        val vChip = if (t.parentUnitId == 0L) ViewChip.instance(vFlow.context) else ViewChip.instanceOutline(vFlow.context)
        vChip.text = t.name
        vChip.setOnClickListener { SPostsSearch.instance(t, Navigator.TO) }
        ControllerUnits.createTagMenu(vChip, t)
        if (vFlow.childCount != 0 && t.parentUnitId == 0L) vFlow.addView(ViewSpace(vFlow.context, ToolsView.dpToPx(1).toInt(), 0))
        vFlow.addView(vChip)
        if (t.imageId != 0L) ToolsImagesLoader.load(t.imageId).into { bytes -> vChip.setIcon(ToolsBitmap.decode(bytes)) }
    }


}
