package com.sayzen.campfiresdk.screens.account.story

import android.widget.TextView
import com.dzen.campfire.api.requests.accounts.RAccountsGetStory
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewTextLinkable

class SStory(
        accountName: String,
        r : RAccountsGetStory.Response
) : Screen(R.layout.screen_account_story){

    companion object {

        fun instance(accountId: Long, accountName: String, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RAccountsGetStory(accountId)) { r ->
                SStory(accountName, r)
            }
        }
    }

    private val vKarmaTotal:TextView = findViewById(R.id.vKarmaTotal)
    private val vKarmaTotalPlus:TextView = findViewById(R.id.vKarmaTotalPlus)
    private val vKarmaTotalMinus:TextView = findViewById(R.id.vKarmaTotalMinus)
    private val vRatesTotal:TextView = findViewById(R.id.vRatesTotal)
    private val vRatesTotalPlus:TextView = findViewById(R.id.vRatesTotalPlus)
    private val vRatesTotalMinus:TextView = findViewById(R.id.vRatesTotalMinus)

    private val vPosts:TextView = findViewById(R.id.vPosts)
    private val vComments:TextView = findViewById(R.id.vComments)
    private val vMessages:TextView = findViewById(R.id.vMessages)
    private val vReviews:TextView = findViewById(R.id.vReviews)
    private val vBestPost:ViewTextLinkable = findViewById(R.id.vBestPost)
    private val vBestComment:ViewTextLinkable = findViewById(R.id.vBestComment)
    private val vBestReview:ViewTextLinkable = findViewById(R.id.vBestReview)

    init {
        setTitle(accountName + " " +ToolsResources.s(R.string.profile_story))

        vKarmaTotal.text = "${(r.totalKarmaPlus + r.totalKarmaMinus)/100}"
        vKarmaTotalPlus.text = "${r.totalKarmaPlus/100}"
        vKarmaTotalMinus.text = "${r.totalKarmaMinus/100}"
        vKarmaTotal.setTextColor(ToolsResources.getColor(if(r.totalKarmaPlus + r.totalKarmaMinus < 0)R.color.red_700 else R.color.green_700))

        vRatesTotal.text = "${(r.totalRatesPlus + r.totalRatesMinus)/100}"
        vRatesTotalPlus.text = "${r.totalRatesPlus/100}"
        vRatesTotalMinus.text = "${r.totalRatesMinus/100}"
        vRatesTotal.setTextColor(ToolsResources.getColor(if(r.totalRatesPlus + r.totalRatesMinus < 0)R.color.red_700 else R.color.green_700))

        vPosts.text = "${r.totalPosts}"
        vComments.text = "${r.totalComments}"
        vMessages.text = "${r.totalMessages}"
        vReviews.text = "${r.totalReviews}"
        vBestPost.text = if(r.bestPost == 0L) "-" else ControllerApi.linkToPost(r.bestPost)
        vBestComment.text = if(r.bestComment == 0L) "-" else ControllerApi.linkToComment(r.bestComment, r.bestCommentUnitType, r.bestCommentUnitId)
        vBestReview.text = if(r.bestReview == 0L) "-" else ControllerApi.linkToReview(r.bestReview)

        ControllerApi.makeLinkable(vBestPost)
        ControllerApi.makeLinkable(vBestComment)
        ControllerApi.makeLinkable(vBestReview)
    }


}
