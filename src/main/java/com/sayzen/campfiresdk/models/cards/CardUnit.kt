package com.sayzen.campfiresdk.models.cards

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.UnitReview
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.UnitForum
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.models.units.events_admins.UnitEventAdmin
import com.dzen.campfire.api.models.units.events_fandoms.UnitEventFandom
import com.dzen.campfire.api.models.units.events_moderators.UnitEventModer
import com.dzen.campfire.api.models.units.events_user.UnitEventUser
import com.dzen.campfire.api.models.units.moderations.UnitModeration
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
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
        unit: Unit
) : Card(layout), NotifyItem {

    companion object {
        fun instance(
                unit: Unit,
                vRecycler: RecyclerView? = null,
                showFandom: Boolean = false,
                dividers: Boolean = false,
                isShowFullInfo: Boolean = false,
                isShowReports: Boolean = true
        ): CardUnit {

            val cardUnit = when (unit) {
                is UnitComment -> CardComment.instance(unit, dividers, false)
                is UnitPost -> CardPost(vRecycler, unit)
                is UnitChatMessage -> CardChatMessage.instance(unit)
                is UnitModeration -> CardModeration(unit)
                is UnitEventUser -> CardUnitEventUser(unit)
                is UnitEventModer -> CardUnitEventModer(unit)
                is UnitEventAdmin -> CardUnitEventAdmin(unit)
                is UnitEventFandom -> CardUnitEventFandom(unit)
                is UnitReview -> CardReview(unit)
                is UnitForum -> CardForum(unit)
                is UnitSticker -> CardSticker(unit, isShowFullInfo, isShowReports)
                is UnitStickersPack -> CardStickersPack(unit, isShowFullInfo, isShowReports)
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

