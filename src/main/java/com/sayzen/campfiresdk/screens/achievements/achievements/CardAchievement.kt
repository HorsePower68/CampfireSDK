package com.sayzen.campfiresdk.screens.achievements.achievements

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsSetRecruiter
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.widgets.WidgetRules
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.fandoms.suggest.SFandomSuggest
import com.sayzen.campfiresdk.app.CampfreConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class CardAchievement(
        val screen: PageAchievements,
        val achievement: AchievementInfo
) : Card(R.layout.screen_achievement_card_achievement) {
    var valueMultiplier = 1.0

    var progress = 0L
    var lvl = 0
    private var flash: Boolean = false
    private var animationFlash: AnimationPendulumColor? = null
    private var subscriptionFlash: Subscription? = null

    init {
        progress = screen.achiProgress(achievement)
        lvl = achievement.getLvl(progress)
    }

    fun setValueMultiplier(valueMultiplier: Double): CardAchievement {
        this.valueMultiplier = valueMultiplier
        return this
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vImage:ViewAvatar = view.findViewById(R.id.vImage)
        val vImage1:ViewAvatar = view.findViewById(R.id.vImage1)
        val vChip2:ViewChipMini = view.findViewById(R.id.vChip2)
        val vImagesContainer:View = view.findViewById(R.id.vImagesContainer)
        val vText:TextView = view.findViewById(R.id.vText)
        val vProgress:TextView = view.findViewById(R.id.vProgress)
        val vLine:ViewProgressLine = view.findViewById(R.id.vLine)

        val accountId = screen.accountId
        val ach = CampfreConstants.getAchievement(achievement)

        if (ControllerApi.isCurrentAccount(accountId) && ach.clickable && lvl != ach.info.maxLvl) view.setOnClickListener { v -> onClick() }
        else view.setOnClickListener(null)
        view.isClickable = ach.clickable && lvl != ach.info.maxLvl

        vText.text = ach.getText(lvl != ach.info.maxLvl && ControllerApi.isCurrentAccount(accountId))
        vLine.setLineColorR(ach.colorRes)
        if (progress != -1L && lvl != ach.info.maxLvl) {
            vProgress.text = (progress * valueMultiplier).toInt().toString() + " / " + (ach.info.getTarget(lvl) * valueMultiplier).toInt()
            vLine.visibility = View.VISIBLE
            vLine.setProgress(progress, ach.info.getTarget(lvl))
        } else {
            vLine.visibility = View.INVISIBLE
        }

        vImage1.vImageView.setBackgroundColorCircleRes(ach.colorRes)

        if (lvl == 0) {

            vImage.vImageView.setBackgroundColor(ToolsResources.getColor(R.color.focus))

            vImagesContainer.visibility = View.GONE
            vImage.visibility = View.VISIBLE
            vProgress.visibility = View.VISIBLE

            vImage.vChip.setText(ToolsText.numToStringRoundAndTrim(ach.info.getForce(), 2))
            vImage.setImage(R.drawable.ic_help_white_24dp)
        } else if (ach.info.maxLvl == lvl) {

            vImage.vImageView.setBackgroundColor(ToolsResources.getColor(ach.colorRes))

            vImagesContainer.visibility = View.GONE
            vImage.visibility = View.VISIBLE
            vProgress.visibility = View.GONE

            vImage.setImage(ach.image)
            vImage.vChip.setText(ToolsText.numToStringRoundAndTrim(ach.info.getForce() * lvl, 2))
        } else {

            vImage.vImageView.setBackgroundColor(ToolsResources.getColor(ach.colorRes))

            vImagesContainer.visibility = View.VISIBLE
            vImage.visibility = View.GONE
            vProgress.visibility = View.VISIBLE

            vImage1.setImage(ach.image)
            vImage1.vChip.setText(ToolsText.numToStringRoundAndTrim(ach.info.getForce() * lvl, 2))
            vChip2.setText( ToolsText.numToStringRoundAndTrim(ach.info.getForce(), 2))
        }

        if (animationFlash != null) {
            view.background = ColorDrawable(animationFlash!!.color)
        } else
            view.background = ColorDrawable(0x00000000)

        if (flash) {
            flash = false
            if (subscriptionFlash != null) subscriptionFlash!!.unsubscribe()

            if (animationFlash == null)
                animationFlash = AnimationPendulumColor(ToolsColor.setAlpha(0, ToolsResources.getColor(R.color.focus)), ToolsResources.getColor(R.color.focus), 500, AnimationPendulum.AnimationType.TO_2_AND_BACK)
            animationFlash!!.to_2()

            subscriptionFlash = ToolsThreads.timerThread((1000 / 30).toLong(), 1000,
                    { subscription ->
                        animationFlash!!.update()
                        ToolsThreads.main { update() }
                    },
                    {
                        ToolsThreads.main {
                            animationFlash = null
                            update()
                        }
                    })
        }

    }

    fun flash() {
        flash = true
        update()
    }

    //
    //  Click
    //

    private fun onClick() {
        val ach = CampfreConstants.getAchievement(achievement)
        when {
            ach.info.index == API.ACHI_APP_SHARE.index -> ControllerCampfireSDK.shareCampfireApp()
            ach.info.index == API.ACHI_ADD_RECRUITER.index -> onRecruiterClicked()
            ach.info.index == API.ACHI_LOGIN.index -> ControllerCampfireSDK.changeLogin()
            ach.info.index == API.ACHI_FANDOMS.index -> SFandomSuggest.instance(Navigator.TO)
            ach.info.index == API.ACHI_RULES_USER.index ->
                WidgetRules(R.string.rules_users_info, Array(CampfreConstants.RULES_USER.size) { CampfreConstants.RULES_USER[it].text })
                        .onFinish { ApiRequestsSupporter.executeProgressDialog(RAchievementsOnFinish(API.ACHI_RULES_USER.index)) { r -> } }
                        .asSheetShow()
            ach.info.index == API.ACHI_RULES_MODERATOR.index ->
                WidgetRules(R.string.rules_moderators_info, CampfreConstants.RULES_MODER)
                        .onFinish { ApiRequestsSupporter.executeProgressDialog(RAchievementsOnFinish(API.ACHI_RULES_MODERATOR.index)) { r -> } }
                        .asSheetShow()
        }

    }

    private fun onRecruiterClicked() {

        Navigator.to(SAccountSearch{ account ->
            RAccountsSetRecruiter(account.id)
                    .onComplete { r -> ToolsToast.show(R.string.app_done) }
                    .onNetworkError { ToolsToast.show(R.string.error_network) }
                    .send(api)
        })
    }

}