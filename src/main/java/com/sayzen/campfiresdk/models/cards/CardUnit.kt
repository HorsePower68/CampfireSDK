package com.sayzen.campfiresdk.models.cards

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.PublicationComment
import com.dzen.campfire.api.models.UnitReview
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.events_admins.PublicationEventAdmin
import com.dzen.campfire.api.models.publications.events_fandoms.PublicationEventFandom
import com.dzen.campfire.api.models.publications.events_moderators.PublicationEventModer
import com.dzen.campfire.api.models.publications.events_user.PublicationEventUser
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.*
import com.sayzen.campfiresdk.models.cards.chat.CardChatMessage
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.cards.events.CardUnitEventAdmin
import com.sayzen.campfiresdk.models.cards.events.CardUnitEventFandom
import com.sayzen.campfiresdk.models.cards.events.CardUnitEventModer
import com.sayzen.campfiresdk.models.cards.events.CardUnitEventUser
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsThreads

abstract class CardUnit(
        layout: Int,
        unit: Publication
) : Card(layout), NotifyItem {

    companion object {
        fun instance(
                unit: Publication,
                vRecycler: RecyclerView? = null,
                showFandom: Boolean = false,
                dividers: Boolean = false,
                isShowFullInfo: Boolean = false,
                isShowReports: Boolean = true
        ): CardUnit {

            val cardUnit = when (unit) {
                is PublicationComment -> CardComment.instance(unit, dividers, false)
                is PublicationPost -> CardPost(vRecycler, unit)
                is PublicationChatMessage -> CardChatMessage.instance(unit)
                is PublicationModeration -> CardModeration(unit)
                is PublicationEventUser -> CardUnitEventUser(unit)
                is PublicationEventModer -> CardUnitEventModer(unit)
                is PublicationEventAdmin -> CardUnitEventAdmin(unit)
                is PublicationEventFandom -> CardUnitEventFandom(unit)
                is UnitReview -> CardReview(unit)
                is PublicationSticker -> CardSticker(unit, isShowFullInfo, isShowReports)
                is PublicationStickersPack -> CardStickersPack(unit, isShowFullInfo, isShowReports)
                else -> CardUnitUnknown(unit)
            }

            cardUnit.showFandom = showFandom

            return cardUnit

        }
    }

    val xUnit = XUnit(unit,
            onChangedAccount = { updateAccount() },
            onChangedFandom = { updateFandom() },
            onChangedKarma = { updateKarma() },
            onChangedComments = { updateComments() },
            onChangedReports = { updateReports() },
            onChangedImportance = { update() },
            onRemove = { adapter?.remove(this) }
    )
    private var flash = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null
    var showFandom = false
    var flashViewId = 0
    var useBackgroundToFlash = false
    var updateFandomOnBind = true

    override fun bindView(view: View) {
        super.bindView(view)

        updateKarma()
        updateAccount()
        updateComments()
        updateReports()
        if (updateFandomOnBind) updateFandom()
        updateFlash()
    }

    abstract fun updateAccount()

    abstract fun updateFandom()

    abstract fun updateKarma()

    abstract fun updateComments()

    abstract fun updateReports()

    fun updateFlash() {
        if (getView() == null) return
        val view: View = if (flashViewId > 0) getView()!!.findViewById(flashViewId) else getView()!!

        if (useBackgroundToFlash) {
            if (animationFlash != null) view.background = ColorDrawable(animationFlash!!.color)
            else view.background = ColorDrawable(0x00000000)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (animationFlash != null) view.foreground = ColorDrawable(animationFlash!!.color)
                else view.foreground = ColorDrawable(0x00000000)
            }
        }

        if (flash) {
            flash = false
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus_dark)), ToolsResources.getColor(R.color.focus_dark), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash?.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    {
                        animationFlash?.update()
                        ToolsThreads.main { updateFlash() }
                    },
                    {
                        ToolsThreads.main {
                            animationFlash = null
                            updateFlash()
                        }
                    })
        }
    }

    fun flash() {
        flash = true
        updateFlash()
    }


}

