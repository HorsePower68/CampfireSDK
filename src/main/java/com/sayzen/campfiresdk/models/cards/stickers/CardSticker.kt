package com.sayzen.campfiresdk.models.cards.stickers

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsThreads

class CardSticker(
        override val unit: UnitSticker,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = false
) : CardUnit(unit) {

    private val eventBus = EventBus

    private val xKarma = XKarma(unit) { update() }
    private var flash = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null
    var onClick: (UnitSticker) -> Unit = {}

    override fun getLayout() = R.layout.card_sticker

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vProgress: View = view.findViewById(R.id.vProgress)
        val vReports: TextView = view.findViewById(R.id.vReports)
        val vRootContainer: View = view.findViewById(R.id.vRootContainer)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vMenu: View = view.findViewById(R.id.vMenu)

        vTitle.visibility = if (isShowFullInfo) View.VISIBLE else View.GONE
        vMenu.visibility = if (isShowFullInfo || isShowReports) View.VISIBLE else View.GONE
        vReports.text = unit.reportsCount.toString() + ""
        vReports.visibility = if (unit.reportsCount > 0 && ControllerApi.can(API.LVL_ADMIN_MODER) && isShowReports) View.VISIBLE else View.GONE
        vTitle.text = ToolsResources.sCap(R.string.sticker_event_create_sticker, ToolsResources.sex(unit.creatorSex, R.string.he_add, R.string.she_add))

        vMenu.setOnClickListener { ControllerUnits.showStickerPopup(vMenu, 0, 0, unit)}

        if (isShowFullInfo){
            ToolsView.setOnLongClickCoordinates(vRootContainer) { v, x, y ->

            }
            view.setOnClickListener {  SStickersView.instanceBySticker(unit.id, Navigator.TO) }
        } else {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { v, x, y ->
                ControllerUnits.showStickerPopup(vRootContainer, x, y, unit)
            }
            view.setOnClickListener { onClick.invoke(unit) }
            vRootContainer.setBackgroundColor(0x00000000)
        }

        ToolsImagesLoader.loadGif(unit.imageId, unit.gifId, 0, 0, vImage, vProgress)
        updateFlash()
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(unit.imageId).intoCash()
    }

    private fun updateFlash() {
        if (getView() == null) return
        val vRootContainer: View = getView()!!.findViewById(R.id.vRootContainer)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (animationFlash != null) {
                vRootContainer.foreground = ColorDrawable(animationFlash!!.color)
            } else
                vRootContainer.foreground = ColorDrawable(0x00000000)
        } else {
            if (animationFlash != null) {
                vRootContainer.background = ColorDrawable(animationFlash!!.color)
            } else
                vRootContainer.background = ColorDrawable(0x00000000)
        }


        if (flash) {
            flash = false
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus_dark)), ToolsResources.getColor(R.color.focus_dark), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash?.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    { subscription ->
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
