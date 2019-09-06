package com.sayzen.campfiresdk.screens.fandoms

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.sayzen.campfiresdk.screens.fandoms.tags.WidgetTagCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomTagMove
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.objects.TagParent
import com.sayzen.campfiresdk.models.widgets.WidgetCategoryCreate
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

class STags private constructor(
        private val fandomId: Long,
        private val languageId: Long,
        tagsOriginal: Array<UnitTag>
) : Screen(R.layout.screen_tags) {


    companion object {

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RTagsGetAll(fandomId, languageId)) { r ->
                STags(fandomId, languageId, r.tags)
            }
        }
    }

    private val eventBus = EventBus.subscribe(EventFandomTagMove::class){
        Navigator.back()
        instance(fandomId, languageId, Navigator.TO)
    }


    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vContainer: ViewGroup = findViewById(R.id.vTagsContainer)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val tags: Array<TagParent>

    init {

        vFab.setOnClickListener { onActionClicked() }
        (vFab as View).visibility = if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_TAGS)) View.VISIBLE else View.GONE

        tags = ControllerUnits.parseTags(tagsOriginal)

        if (tags.isNotEmpty()) vMessageContainer.visibility = View.GONE
        else vMessageContainer.visibility = View.VISIBLE

        for (tag in tags) {

            val vText: TextView = ToolsView.inflate(context, R.layout.z_text_subhead_touch)
            vText.text = tag.tag.name
            vText.setPadding(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt())
            vText.setOnClickListener { SPostsSearch.instance(tag.tag, Navigator.TO) }

            vContainer.addView(vText, vContainer.childCount - 1)
            ControllerUnits.createTagMenu(vText, tag.tag, tags)

            val vFlow = LayoutFlow(context)

            for (t in tag.tags) {
                val v = ViewChip.instanceOutline(context)
                v.text = t.name
                v.setOnClickListener { SPostsSearch.instance(t, Navigator.TO) }
                v.tag = t.id
                vFlow.addView(v)
                ControllerUnits.createTagMenu(v, t, tags)
                if (t.imageId != 0L) {
                    v.setIcon(R.color.focus)
                    ToolsImagesLoader.load(t.imageId).into { bytes -> v.setIcon(ToolsBitmap.resize(ToolsBitmap.decode(bytes)!!, 32, 32)) }
                }else{
                    v.setIcon(null)
                }
            }
            vContainer.addView(vFlow, vContainer.childCount - 1, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

            vFlow.setHorizontalSpacing(ToolsView.dpToPx(8).toInt())
            vFlow.setVerticalSpacing(ToolsView.dpToPx(8).toInt())

            (vFlow.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
            (vFlow.layoutParams as MarginLayoutParams).bottomMargin = ToolsView.dpToPx(8).toInt()

        }
    }

    private fun onActionClicked() {

        WidgetMenu()
                .add(R.string.app_tag) { _, _ -> createTag() }
                .add(R.string.app_category) { _, _ -> WidgetCategoryCreate(fandomId, languageId) }
                .asSheetShow()
    }

    private fun createTag() {

        if(tags.isEmpty()){
            ToolsToast.show(R.string.error_cant_create_tag_without_category)
            return
        }

        val menu = WidgetMenu()
        for (tag in tags)
            menu.add(tag.tag.name) { _, _ -> WidgetTagCreate(tag.tag.id, fandomId, languageId) }
        menu.asSheetShow()
    }

}
