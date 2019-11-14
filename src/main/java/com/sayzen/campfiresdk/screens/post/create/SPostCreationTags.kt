package com.sayzen.campfiresdk.screens.post.create

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.requests.post.RPostPublication
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
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
import com.sup.dev.android.views.widgets.WidgetChooseDate
import com.sup.dev.android.views.widgets.WidgetChooseTime
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import java.util.*

class SPostCreationTags private constructor(
        private val unitId: Long,
        private val fandomId: Long,
        private val language: Long,
        private val closed: Boolean,
        private val unitTag3: Long,
        private val isMyUnit: Boolean,
        private val presetTags: Array<Long>,
        tags: Array<PublicationTag>
) : Screen(R.layout.screen_post_create_tags) {

    companion object {

        fun instance(unitId: Long, isMyUnit: Boolean, action: NavigationAction) {
            ApiRequestsSupporter.executeProgressDialog(RPostGet(unitId)) { r ->
                instance(r.unit.id, r.unit.closed, r.unit.tag_3, isMyUnit, r.unit.fandomId, r.unit.languageId, ControllerUnits.tagsAsLongArray(r.tags), action)
            }
        }

        fun instance(unitId: Long, closed: Boolean, unitTag3: Long, isMyUnit: Boolean, fandomId: Long, languageId: Long, presetTags: Array<Long>, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RTagsGetAll(fandomId, languageId)) { r -> SPostCreationTags(unitId, fandomId, languageId, closed, unitTag3, isMyUnit, presetTags, r.tags) }
        }

        fun create(unitId: Long, tags: Array<Long>, notifyFollowers: Boolean, pendingTime: Long, closed: Boolean, rubricId: Long, onCreate: () -> Unit) {
            SGoogleRules.acceptRulesDialog {
                ApiRequestsSupporter.executeProgressDialog(RPostPublication(unitId, tags, "", notifyFollowers, pendingTime, closed, rubricId)) { _ ->
                    EventBus.post(EventPostStatusChange(unitId, API.STATUS_PUBLIC))
                    onCreate.invoke()
                    ControllerStoryQuest.incrQuest(API.QUEST_STORY_POST)
                }
            }
        }

    }


    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vNotifyFollowers: CheckBox = findViewById(R.id.vNotifyFollowers)
    private val vPending: CheckBox = findViewById(R.id.vPending)
    private val vClose: CheckBox = findViewById(R.id.vClose)
    private val vRubric: TextView = findViewById(R.id.vRubric)
    private val vLine: View = findViewById(R.id.vLine)
    private val vMessageContainer: View = findViewById(R.id.vMessageContainer)
    private val vContainer: ViewGroup = findViewById(R.id.vTagsContainer)
    private val vMenuContainer: ViewGroup = findViewById(R.id.vMenuContainer)

    private val chips = ArrayList<ViewChip>()
    private var pendingDate = 0L
    private var rubricId = 0L

    init {
        isNavigationShadowAvailable = false
        SActivityTypeBottomNavigation.setShadow(vLine)

        vNotifyFollowers.isEnabled = unitTag3 == 0L
        vNotifyFollowers.isChecked = false
        vPending.isChecked = false
        vClose.isChecked = closed
        vMenuContainer.visibility = if (vNotifyFollowers.isEnabled && isMyUnit) View.VISIBLE else View.GONE

        isSingleInstanceInBackStack = true

        if (tags.isNotEmpty()) vMessageContainer.visibility = View.GONE
        else vMessageContainer.visibility = View.VISIBLE

        vFab.setOnClickListener { sendPublication() }

        vPending.setOnClickListener { onPendingClicked() }
        vRubric.setOnClickListener { onRubricClicked() }

        setTags(tags)
    }

    private fun onPendingClicked() {
        if (!vPending.isChecked/*После нажатия положение меняется*/) setPendingDate(0)
        else {
            WidgetChooseDate()
                    .setOnEnter(R.string.app_choose) { _, date ->
                        WidgetChooseTime()
                                .setOnEnter(R.string.app_choose) { _, h, m ->
                                    setPendingDate(ToolsDate.getStartOfDay(date) + (h * 60L * 60 * 1000) + (m * 60L * 1000))
                                }
                                .asSheetShow()

                    }
                    .asSheetShow()
        }
    }

    private fun onRubricClicked() {
        if (rubricId > 0) {
            vRubric.text = ToolsResources.s(R.string.post_create_rubric)
            rubricId = 0
        } else {
            Navigator.to(SRubricsList(fandomId, language, ControllerApi.account.id) {
                vRubric.text = ToolsResources.s(R.string.app_rubric) + ": " + it.name
                rubricId = it.id
            })
        }
    }

    private fun setPendingDate(date: Long) {
        var dateV = date
        if (dateV != 0L && dateV < System.currentTimeMillis()) {
            ToolsToast.show(R.string.post_create_pending_error)
            dateV = 0L
        }

        pendingDate = dateV
        if (dateV > 0) {
            vPending.setText(ToolsResources.s(R.string.post_create_pending) + " (${ToolsDate.dateToString(dateV)})")
            vPending.isChecked = true
        } else {
            vPending.setText(R.string.post_create_pending)
            vPending.isChecked = false
        }

    }

    private fun sendPublication() {

        val selectedTags = ArrayList<PublicationTag>()
        for (v in chips) if (v.isChecked) selectedTags.add(v.tag as PublicationTag)


        val tags = Array(selectedTags.size) { selectedTags[it].id }

        if (isMyUnit) {
            create(unitId, tags, vNotifyFollowers.isChecked, pendingDate, vClose.isChecked, rubricId) {
                Navigator.removeAll(SPostCreate::class)
                if (pendingDate > 0) Navigator.replace(SPending())
                else SPost.instance(unitId, 0, NavigationAction.replace())
            }
        } else {
            WidgetField()
                    .setHint(R.string.moderation_widget_comment)
                    .setOnCancel(R.string.app_cancel)
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(R.string.app_change) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(w, RPostPublication(unitId, tags, comment, false, 0, false, 0)) {
                            Navigator.removeAll(SPostCreate::class)
                            EventBus.post(EventPostStatusChange(unitId, API.STATUS_PUBLIC))
                            SPost.instance(unitId, 0, NavigationAction.replace())
                        }
                    }.asSheetShow()
        }

    }


    fun setTags(tagsOriginal: Array<PublicationTag>) {

        vContainer.removeAllViews()

        val tags = ControllerUnits.parseTags(tagsOriginal)

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
