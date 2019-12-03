package com.sayzen.campfiresdk.screens.post.create

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.requests.post.RPostPublication
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.screens.other.rules.SGoogleRules
import com.sayzen.campfiresdk.screens.post.pending.SPending
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.activity.SActivityTypeBottomNavigation
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads
import java.util.*

class SPostCreationTags private constructor(
        private val publicationId: Long,
        private val fandomId: Long,
        private val language: Long,
        private val closed: Boolean,
        private val publicationTag3: Long,
        private val isMyPublication: Boolean,
        private val presetTags: Array<Long>,
        tags: Array<PublicationTag>
) : Screen(R.layout.screen_post_create_tags) {

    companion object {

        fun instance(publicationId: Long, isMyPublication: Boolean, action: NavigationAction) {
            ApiRequestsSupporter.executeProgressDialog(RPostGet(publicationId)) { r ->
                instance(r.publication.id, r.publication.closed, r.publication.tag_3, isMyPublication, r.publication.fandomId, r.publication.languageId, ControllerPublications.tagsAsLongArray(r.tags), action)
            }
        }

        fun instance(publicationId: Long, closed: Boolean, publicationTag3: Long, isMyPublication: Boolean, fandomId: Long, languageId: Long, presetTags: Array<Long>, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RTagsGetAll(fandomId, languageId)) { r -> SPostCreationTags(publicationId, fandomId, languageId, closed, publicationTag3, isMyPublication, presetTags, r.tags) }
        }

        fun create(publicationId: Long, tags: Array<Long>, notifyFollowers: Boolean, pendingTime: Long, closed: Boolean, rubricId: Long, userActivityId: Long, userActivityIdNextUserId: Long, onCreate: () -> Unit) {
            SGoogleRules.acceptRulesDialog {
                ApiRequestsSupporter.executeProgressDialog(RPostPublication(publicationId, tags, "", notifyFollowers, pendingTime, closed, rubricId, userActivityId, userActivityIdNextUserId)) { _ ->
                    EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
                    onCreate.invoke()
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_POST)
                    ControllerActivities.reloadActivities()
                }
            }
        }

    }


    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vLine: View = findViewById(R.id.vLine)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vContainer: ViewGroup = findViewById(R.id.vTagsContainer)
    private val vMenuContainer: ViewGroup = findViewById(R.id.vMenuContainer)
    private val vParams: View = findViewById(R.id.vParams)

    private val widgetTagsAdditional = WidgetTagsAdditional(fandomId, language, closed, publicationTag3)
    private val chips = ArrayList<ViewChip>()

    init {
        isNavigationShadowAvailable = false
        SActivityTypeBottomNavigation.setShadow(vLine)

        vMenuContainer.visibility = if (isMyPublication && publicationTag3 == 0L) View.VISIBLE else View.GONE

        isSingleInstanceInBackStack = true

        if (tags.isNotEmpty()) vMessageContainer.visibility = View.GONE
        else vMessageContainer.visibility = View.VISIBLE

        vFab.setOnClickListener { sendPublication() }
        vParams.setOnClickListener { widgetTagsAdditional.asSheetShow() }


        setTags(tags)
    }

    override fun onResume() {
        super.onResume()
        if(widgetTagsAdditional.needReShow)
            ToolsThreads.main(100) { widgetTagsAdditional.asSheetShow() }
    }


    private fun sendPublication() {

        val selectedTags = ArrayList<PublicationTag>()
        for (v in chips) if (v.isChecked) selectedTags.add(v.tag as PublicationTag)


        val tags = Array(selectedTags.size) { selectedTags[it].id }

        if (isMyPublication) {
            create(publicationId, tags, widgetTagsAdditional.isNotifyFollowers(), widgetTagsAdditional.getPendingDate(), widgetTagsAdditional.isClosed(), widgetTagsAdditional.getRubricId(), widgetTagsAdditional.getUserActivityId(), widgetTagsAdditional.getUserActivityIdNextUserId()) {
                Navigator.removeAll(SPostCreate::class)
                if (widgetTagsAdditional.getPendingDate() > 0) Navigator.replace(SPending())
                else SPost.instance(publicationId, 0, NavigationAction.replace())
            }
        } else {
            WidgetField()
                    .setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_change) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(w, RPostPublication(publicationId, tags, comment, false, 0, false, 0, 0, 0)) {
                            Navigator.removeAll(SPostCreate::class)
                            EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
                            SPost.instance(publicationId, 0, NavigationAction.replace())
                        }
                    }.asSheetShow()
        }

    }


    fun setTags(tagsOriginal: Array<PublicationTag>) {

        vContainer.removeAllViews()

        val tags = ControllerPublications.parseTags(tagsOriginal)

        for (tag in tags) {

            val vText: TextView = ToolsView.inflate(context, R.layout.z_text_subhead)
            vText.setPadding(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt())
            vText.text = tag.tag.name

            vContainer.addView(vText)


            val vFlow = LayoutFlow(context)

            for (t in tag.tags) {
                val v = ViewChip.instanceChooseOutline(context, t.name, t)
                for (tt in presetTags) if (t.id == tt) v.isChecked = true
                vFlow.addView(v)
                chips.add(v)
                if (t.imageId != 0L) {
                    v.setIcon(R.color.focus)
                    ToolsImagesLoader.load(t.imageId).into { bytes -> v.setIcon(ToolsBitmap.resize(ToolsBitmap.decode(bytes)!!, 32, 32)) }
                } else {
                    v.setIcon(null)
                }
            }

            vContainer.addView(vFlow, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            vFlow.setHorizontalSpacing(ToolsView.dpToPx(8).toInt())
            vFlow.setVerticalSpacing(ToolsView.dpToPx(8).toInt())

            (vFlow.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
            (vFlow.layoutParams as MarginLayoutParams).bottomMargin = ToolsView.dpToPx(8).toInt()

        }
    }


}
