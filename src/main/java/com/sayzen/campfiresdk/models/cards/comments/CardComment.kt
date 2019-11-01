package com.sayzen.campfiresdk.models.cards.comments

import android.annotation.SuppressLint
import android.text.Html
import android.util.LongSparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.units.NotificationMention
import com.dzen.campfire.api.requests.comments.RCommentReactionAdd
import com.dzen.campfire.api.requests.comments.RCommentReactionRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.units.EventCommentChange
import com.sayzen.campfiresdk.models.events.units.EventCommentRemove
import com.sayzen.campfiresdk.models.events.units.EventUnitDeepBlockRestore
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.post.history.SUnitHistory
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.views.WidgetReactions
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.views.*
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsHTML

abstract class CardComment protected constructor(
        layout: Int,
        unit: UnitComment,
        private val dividers: Boolean,
        protected val miniSize: Boolean,
        private val onClick: ((UnitComment) -> Boolean)? = null,
        private val onQuote: ((UnitComment) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null
) : CardUnit(layout, unit) {

    companion object {

        fun instance(unit: UnitComment, dividers: Boolean, miniSize: Boolean, onClick: ((UnitComment) -> Boolean)? = null, onQuote: ((UnitComment) -> Unit)? = null, onGoTo: ((Long) -> Unit)? = null): CardComment {
            when (unit.type) {
                UnitComment.TYPE_TEXT -> return CardCommentText(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                UnitComment.TYPE_IMAGE, UnitComment.TYPE_GIF -> return CardCommentImage(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                UnitComment.TYPE_IMAGES -> return CardCommentImages(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                UnitComment.TYPE_STICKER -> return CardCommentSticker(unit, dividers, miniSize, onClick, onQuote, onGoTo)
                else -> return CardCommentUnknown(unit, dividers, miniSize, onClick, onQuote, onGoTo)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventCommentChange::class) { e: EventCommentChange -> this.onCommentChange(e) }
            .subscribe(EventUnitDeepBlockRestore::class) { onEventUnitDeepBlockRestore(it) }

    var changeEnabled = true
    var quoteEnabled = true
    var copyEnabled = true

    init {
        flashViewId = R.id.vRootContainer
    }

    protected abstract fun bind(view: View)

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitComment

        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val vLabel: TextView? = view.findViewById(R.id.vLabel)
        val vLabelName: TextView? = view.findViewById(R.id.vLabelName)
        val vLabelDate: TextView? = view.findViewById(R.id.vLabelDate)
        val vDivider: View? = view.findViewById(R.id.vDivider)
        val vText: ViewTextLinkable? = view.findViewById(R.id.vCommentText)
        val vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vQuoteText: ViewTextLinkable? = view.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesSwipe? = view.findViewById(R.id.vQuoteImage)

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationComment::class, unit.id)
            ControllerNotifications.removeNotificationFromNew(NotificationCommentAnswer::class, unit.id)
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, unit.id)
        }

        if (vSwipe != null) {
            vSwipe.onClick = { _, _ -> if (onClick()) showMenu() }
            vSwipe.onLongClick = { _, _ -> showMenu() }
            vSwipe.swipeEnabled = quoteEnabled && onQuote != null
            if (onQuote != null) {
                vSwipe.onClick = { _, _ ->
                    if (ControllerApi.isCurrentAccount(unit.creatorId)) showMenu()
                    else onClick()
                }
                vSwipe.onSwipe = { onQuote.invoke(unit) }
            }
        }

        if (vQuoteContainer != null) {
            vQuoteContainer.visibility = if (unit.quoteText.isEmpty() && unit.quoteImages.isEmpty()) View.GONE else View.VISIBLE
            vQuoteContainer.setOnClickListener {
                if (onGoTo != null) onGoTo!!.invoke(unit.quoteId)
            }
        }

        if (vQuoteText != null) {
            vQuoteText.text = unit.quoteText
            ControllerApi.makeLinkable(vQuoteText)
        }

        if (vQuoteImage != null) {
            vQuoteImage.clear()
            vQuoteImage.visibility = View.VISIBLE
            if (unit.quoteStickerId != 0L) {
                vQuoteImage.add(unit.quoteStickerImageId, onClick = { SStickersView.instanceBySticker(unit.quoteStickerId, Navigator.TO) })
            } else if (unit.quoteImages.isNotEmpty()) {
                for (i in unit.quoteImages) vQuoteImage.add(i)
            } else {
                vQuoteImage.visibility = View.GONE
            }
        }

        if (vText != null) {
            vText.text = unit.text
            ControllerApi.makeLinkable(vText) {
                val myName = ControllerApi.account.name + ","
                if (unit.text.startsWith(myName)) vText.text = Html.fromHtml(ToolsHTML.font_color(myName, "#FF6D00") + unit.text.substring(myName.length))
            }
        }



        if (vLabelName != null) vLabelName.text = unit.creatorName
        if (vLabelDate != null) vLabelDate.text = "${ToolsDate.dateToString(unit.dateCreate)}${if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else ""}"
        if (vLabel != null) vLabel.text = unit.creatorName + "   " + ToolsDate.dateToString(unit.dateCreate) + (if (unit.changed) " " + ToolsResources.s(R.string.app_edited) else "")
        if (vDivider != null) vDivider.visibility = if (dividers) View.VISIBLE else View.GONE

        updateReactions()

        bind(view)
    }

    fun showMenu() {
        val unit = xUnit.unit as UnitComment
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToComment(unit))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_remove) { _, _ -> ControllerApi.removeUnit(unit.id, R.string.comment_remove_confirm, R.string.comment_error_gone) { EventBus.post(EventCommentRemove(unit.id, unit.parentUnitId)) } }
                .add(R.string.app_change) { _, _ -> WidgetComment(unit).asSheetShow() }.condition(changeEnabled)
                .clearGroupCondition()
                .add(R.string.app_copy) { _, _ ->
                    ToolsAndroid.setToClipboard(unit.text)
                    ToolsToast.show(R.string.app_copied)
                }.condition(copyEnabled)
                .add(R.string.app_quote) { _, _ -> onQuote?.invoke(unit) }.condition(quoteEnabled && onQuote != null)
                .add(R.string.app_history) { _, _ -> Navigator.to(SUnitHistory(unit.id)) }.condition(ControllerPost.ENABLED_HISTORY)
                .add(R.string.app_reaction) { _, _ -> reaction() }.condition(unit.type == UnitComment.TYPE_GIF || unit.type == UnitComment.TYPE_IMAGE || unit.type == UnitComment.TYPE_IMAGES || unit.type == UnitComment.TYPE_TEXT)
                .groupCondition(!ControllerApi.isCurrentAccount(unit.creatorId))
                .add(R.string.app_report) { _, _ -> ControllerApi.reportUnit(unit.id, R.string.comment_report_confirm, R.string.comment_error_gone) }
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearReportsUnit(unit.id, unit.unitType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK) && unit.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerUnits.block(unit) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(unit.fandomId, unit.languageId, API.LVL_MODERATOR_BLOCK))
                .clearGroupCondition()
                .add("Востановить") { _, _ -> ControllerUnits.restoreDeepBlock(unit.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && unit.status == API.STATUS_DEEP_BLOCKED)
                .asSheetShow()
    }

    private fun reaction() {
        WidgetReactions()
                .onSelected { sendReaction(it) }
                .asSheetShow()
    }

    private fun sendReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentReactionAdd(xUnit.unit.id, reactionIndex)) { _ ->
            xUnit.unit as UnitComment
            xUnit.unit.reactions = ToolsCollections.add(UnitComment.Reaction(ControllerApi.account.id, reactionIndex), xUnit.unit.reactions)
            updateReactions()
            ToolsToast.show(R.string.app_done)
        }
                .onApiError(API.ERROR_ALREADY) { ToolsToast.show(R.string.app_done) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(R.string.comment_error_gone) }
    }

    private fun removeReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentReactionRemove(xUnit.unit.id, reactionIndex)) { _ ->
            xUnit.unit as UnitComment
            xUnit.unit.reactions = ToolsCollections.removeIf(xUnit.unit.reactions) { it.accountId == ControllerApi.account.id && it.reactionIndex == reactionIndex }
            updateReactions()
            ToolsToast.show(R.string.app_done)
        }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(R.string.comment_error_gone) }
    }

    fun updateReactions() {
        if (getView() == null) return
        val vReactions: ViewGroup? = getView()!!.findViewById(R.id.vReactions)
        if (vReactions == null) return
        val dp = ToolsView.dpToPx(8)


        xUnit.unit as UnitComment
        val map = LongSparseArray<ViewChip>()
        for (i in xUnit.unit.reactions) {
            var v: ViewChip? = map.get(i.reactionIndex)
            if (v == null) {
                v = ToolsView.inflate(R.layout.z_chip)
                v.setOnClickListener { sendReaction(i.reactionIndex) }
                v.tag = 0
                v.setTextPaddings(0f, dp)
                map.put(i.reactionIndex, v)
            }

            v.tag = (v.tag as Int) + 1
            if (i.accountId == ControllerApi.account.id) {
                v.setChipBackgroundColorResource(R.color.blue_700)
                v.setOnClickListener { removeReaction(i.reactionIndex) }
            }

            if (i.reactionIndex > -1 && i.reactionIndex < API.REACTIONS.size) v.text = "${API.REACTIONS[i.reactionIndex.toInt()]}"
            else v.text = "${API.REACTIONS[0]}"
        }

        vReactions.removeAllViews()
        for (i in 0 until map.size()) {
            val v = map.valueAt(i)
            v.text = " ${v.text} ${v.tag}"
            vReactions.addView(v)
        }
    }

    override fun updateKarma() {
        if (getView() == null) return
        val vKarma: ViewKarma? = getView()!!.findViewById(R.id.vKarma)
        if (vKarma != null) xUnit.xKarma.setView(vKarma)
    }

    override fun updateAccount() {
        if (getView() == null) return
        if (showFandom && xUnit.xFandom.imageId == 0L) xUnit.xFandom.imageId = xUnit.xAccount.imageId
        val vAvatar: ViewAvatar = getView()!!.findViewById(R.id.vAvatar)
        if (!showFandom) xUnit.xAccount.setView(vAvatar)
        else xUnit.xFandom.setView(vAvatar)
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports: TextView? = getView()!!.findViewById(R.id.vReports)
        if (vReports != null) xUnit.xReports.setView(vReports)
    }

    private fun onClick(): Boolean {
        val unit = xUnit.unit as UnitComment
        if (onClick == null) {
            if (unit.parentUnitType == 0L) {
                ToolsToast.show(R.string.post_error_gone)
            } else {
                ControllerUnits.toUnit(unit.parentUnitType, unit.parentUnitId, unit.id)
            }
            return false
        } else {
            return !onClick.invoke(unit)
        }
    }

    override fun notifyItem() {
        val unit = xUnit.unit as UnitComment
        ToolsImagesLoader.load(unit.creatorImageId).intoCash()
    }

    //
    //  Methods
    //

    override fun equals(other: Any?): Boolean {
        val unit = xUnit.unit as UnitComment
        if (other is CardComment) return other.xUnit.unit.id == unit.id
        return super.equals(other)
    }

    //
    //  Event Bus
    //

    private fun onEventUnitDeepBlockRestore(e: EventUnitDeepBlockRestore) {
        if (e.unitId == xUnit.unit.id && xUnit.unit.status == API.STATUS_DEEP_BLOCKED) {
            adapter?.remove(this)
        }
    }

    private fun onCommentChange(e: EventCommentChange) {
        val unit = xUnit.unit as UnitComment
        if (e.unitId == unit.id) {
            unit.text = e.text
            unit.quoteId = e.quoteId
            unit.quoteText = e.quoteText
            unit.changed = true
            update()
        }
    }

}
