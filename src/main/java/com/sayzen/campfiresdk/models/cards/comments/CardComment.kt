package com.sayzen.campfiresdk.models.cards.comments

import android.annotation.SuppressLint
import android.text.Html
import android.util.LongSparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.PublicationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.requests.comments.RCommentReactionAdd
import com.dzen.campfire.api.requests.comments.RCommentReactionRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventCommentChange
import com.sayzen.campfiresdk.models.events.publications.EventCommentRemove
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
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
        publication: PublicationComment,
        private val dividers: Boolean,
        protected val miniSize: Boolean,
        private val onClick: ((PublicationComment) -> Boolean)? = null,
        private val onQuote: ((PublicationComment) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null
) : CardPublication(layout, publication) {

    companion object {

        fun instance(publication: PublicationComment, dividers: Boolean, miniSize: Boolean, onClick: ((PublicationComment) -> Boolean)? = null, onQuote: ((PublicationComment) -> Unit)? = null, onGoTo: ((Long) -> Unit)? = null): CardComment {
            when (publication.type) {
                PublicationComment.TYPE_TEXT -> return CardCommentText(publication, dividers, miniSize, onClick, onQuote, onGoTo)
                PublicationComment.TYPE_IMAGE, PublicationComment.TYPE_GIF -> return CardCommentImage(publication, dividers, miniSize, onClick, onQuote, onGoTo)
                PublicationComment.TYPE_IMAGES -> return CardCommentImages(publication, dividers, miniSize, onClick, onQuote, onGoTo)
                PublicationComment.TYPE_STICKER -> return CardCommentSticker(publication, dividers, miniSize, onClick, onQuote, onGoTo)
                else -> return CardCommentUnknown(publication, dividers, miniSize, onClick, onQuote, onGoTo)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventCommentChange::class) { e: EventCommentChange -> this.onCommentChange(e) }
            .subscribe(EventPublicationDeepBlockRestore::class) { onEventPublicationDeepBlockRestore(it) }

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
        val publication = xPublication.publication as PublicationComment

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
            ControllerNotifications.removeNotificationFromNew(NotificationComment::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationCommentAnswer::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, publication.id)
        }

        if (vSwipe != null) {
            vSwipe.onClick = { _, _ -> if (onClick()) showMenu() }
            vSwipe.onLongClick = { _, _ -> showMenu() }
            vSwipe.swipeEnabled = quoteEnabled && onQuote != null
            if (onQuote != null) {
                vSwipe.onClick = { _, _ ->
                    if (ControllerApi.isCurrentAccount(publication.creatorId)) showMenu()
                    else onClick()
                }
                vSwipe.onSwipe = { onQuote.invoke(publication) }
            }
        }

        if (vQuoteContainer != null) {
            vQuoteContainer.visibility = if (publication.quoteText.isEmpty() && publication.quoteImages.isEmpty()) View.GONE else View.VISIBLE
            vQuoteContainer.setOnClickListener {
                if (onGoTo != null) onGoTo!!.invoke(publication.quoteId)
            }
        }

        if (vQuoteText != null) {
            vQuoteText.text = publication.quoteText
            ControllerApi.makeLinkable(vQuoteText)
        }

        if (vQuoteImage != null) {
            vQuoteImage.clear()
            vQuoteImage.visibility = View.VISIBLE
            if (publication.quoteStickerId != 0L) {
                vQuoteImage.add(publication.quoteStickerImageId, onClick = { SStickersView.instanceBySticker(publication.quoteStickerId, Navigator.TO) })
            } else if (publication.quoteImages.isNotEmpty()) {
                for (i in publication.quoteImages) vQuoteImage.add(i)
            } else {
                vQuoteImage.visibility = View.GONE
            }
        }

        if (vText != null) {
            vText.text = publication.text
            ControllerApi.makeLinkable(vText) {
                val myName = ControllerApi.account.name + ","
                if (publication.text.startsWith(myName)) vText.text = Html.fromHtml(ToolsHTML.font_color(myName, "#FF6D00") + publication.text.substring(myName.length))
            }
        }



        if (vLabelName != null) vLabelName.text = publication.creatorName
        if (vLabelDate != null) vLabelDate.text = "${ToolsDate.dateToString(publication.dateCreate)}${if (publication.changed) " " + ToolsResources.s(R.string.app_edited) else ""}"
        if (vLabel != null) vLabel.text = publication.creatorName + "   " + ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + ToolsResources.s(R.string.app_edited) else "")
        if (vDivider != null) vDivider.visibility = if (dividers) View.VISIBLE else View.GONE

        updateReactions()

        bind(view)
    }

    fun showMenu() {
        val publication = xPublication.publication as PublicationComment
        WidgetMenu()
                .add(R.string.app_copy_link) { _, _ ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToComment(publication))
                    ToolsToast.show(R.string.app_copied)
                }
                .groupCondition(ControllerApi.isCurrentAccount(publication.creatorId))
                .add(R.string.app_remove) { _, _ -> ControllerApi.removePublication(publication.id, R.string.comment_remove_confirm, R.string.comment_error_gone) { EventBus.post(EventCommentRemove(publication.id, publication.parentPublicationId)) } }
                .add(R.string.app_change) { _, _ -> WidgetComment(publication).asSheetShow() }.condition(changeEnabled)
                .clearGroupCondition()
                .add(R.string.app_copy) { _, _ ->
                    ToolsAndroid.setToClipboard(publication.text)
                    ToolsToast.show(R.string.app_copied)
                }.condition(copyEnabled)
                .add(R.string.app_quote) { _, _ -> onQuote?.invoke(publication) }.condition(quoteEnabled && onQuote != null)
                .add(R.string.app_history) { _, _ -> Navigator.to(SPublicationHistory(publication.id)) }.condition(ControllerPost.ENABLED_HISTORY)
                .add(R.string.app_reaction) { _, _ -> reaction() }.condition(publication.type == PublicationComment.TYPE_GIF || publication.type == PublicationComment.TYPE_IMAGE || publication.type == PublicationComment.TYPE_IMAGES || publication.type == PublicationComment.TYPE_TEXT)
                .groupCondition(!ControllerApi.isCurrentAccount(publication.creatorId))
                .add(R.string.app_report) { _, _ -> ControllerApi.reportPublication(publication.id, R.string.comment_report_confirm, R.string.comment_error_gone) }
                .add(R.string.app_clear_reports) { _, _ -> ControllerApi.clearReportsPublication(publication.id, publication.publicationType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_BLOCK) && publication.reportsCount > 0)
                .add(R.string.app_block) { _, _ -> ControllerPublications.block(publication) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ControllerApi.can(publication.fandomId, publication.languageId, API.LVL_MODERATOR_BLOCK))
                .clearGroupCondition()
                .add("Востановить") { _, _ -> ControllerPublications.restoreDeepBlock(publication.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && publication.status == API.STATUS_DEEP_BLOCKED)
                .asSheetShow()
    }

    private fun reaction() {
        WidgetReactions()
                .onSelected { sendReaction(it) }
                .asSheetShow()
    }

    private fun sendReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentReactionAdd(xPublication.publication.id, reactionIndex)) { _ ->
            xPublication.publication as PublicationComment
            xPublication.publication.reactions = ToolsCollections.add(PublicationComment.Reaction(ControllerApi.account.id, reactionIndex), xPublication.publication.reactions)
            updateReactions()
            ToolsToast.show(R.string.app_done)
        }
                .onApiError(API.ERROR_ALREADY) { ToolsToast.show(R.string.app_done) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(R.string.comment_error_gone) }
    }

    private fun removeReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentReactionRemove(xPublication.publication.id, reactionIndex)) { _ ->
            xPublication.publication as PublicationComment
            xPublication.publication.reactions = ToolsCollections.removeIf(xPublication.publication.reactions) { it.accountId == ControllerApi.account.id && it.reactionIndex == reactionIndex }
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


        xPublication.publication as PublicationComment
        val map = LongSparseArray<ViewChip>()
        for (i in xPublication.publication.reactions) {
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
        if (vKarma != null) xPublication.xKarma.setView(vKarma)
    }

    override fun updateAccount() {
        if (getView() == null) return
        if (showFandom && xPublication.xFandom.imageId == 0L) xPublication.xFandom.imageId = xPublication.xAccount.imageId
        val vAvatar: ViewAvatar = getView()!!.findViewById(R.id.vAvatar)
        if (!showFandom) xPublication.xAccount.setView(vAvatar)
        else xPublication.xFandom.setView(vAvatar)
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
        if (vReports != null) xPublication.xReports.setView(vReports)
    }

    private fun onClick(): Boolean {
        val publication = xPublication.publication as PublicationComment
        if (onClick == null) {
            if (publication.parentPublicationType == 0L) {
                ToolsToast.show(R.string.post_error_gone)
            } else {
                ControllerPublications.toPublication(publication.parentPublicationType, publication.parentPublicationId, publication.id)
            }
            return false
        } else {
            return !onClick.invoke(publication)
        }
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationComment
        ToolsImagesLoader.load(publication.creatorImageId).intoCash()
    }

    //
    //  Methods
    //

    override fun equals(other: Any?): Boolean {
        val publication = xPublication.publication as PublicationComment
        if (other is CardComment) return other.xPublication.publication.id == publication.id
        return super.equals(other)
    }

    //
    //  Event Bus
    //

    private fun onEventPublicationDeepBlockRestore(e: EventPublicationDeepBlockRestore) {
        if (e.publicationId == xPublication.publication.id && xPublication.publication.status == API.STATUS_DEEP_BLOCKED) {
            adapter?.remove(this)
        }
    }

    private fun onCommentChange(e: EventCommentChange) {
        val publication = xPublication.publication as PublicationComment
        if (e.publicationId == publication.id) {
            publication.text = e.text
            publication.quoteId = e.quoteId
            publication.quoteText = e.quoteText
            publication.changed = true
            update()
        }
    }

}
