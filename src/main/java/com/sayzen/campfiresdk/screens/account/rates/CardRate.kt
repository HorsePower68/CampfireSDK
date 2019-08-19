package com.sayzen.campfiresdk.screens.account.rates

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.Rate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.screens.fandoms.forums.view.SForumView
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.reviews.SReviews
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate
import java.lang.RuntimeException

class CardRate(
        val rate: Rate
) : CardAvatar(R.layout.card_rate), NotifyItem {

    init {
        var textR: Int
        var link: String

        when (rate.unitType) {
            API.UNIT_TYPE_POST -> {
                textR = R.string.profile_rate_post
                link = ControllerApi.linkToPost(rate.unitId)
                setOnClick { SPost.instance(rate.unitId, Navigator.TO) }
            }
            API.UNIT_TYPE_COMMENT -> {
                textR = R.string.profile_rate_comment
                link = ControllerApi.linkToComment(rate.unitId, rate.unitParentType, rate.unitParentId)
                setOnClick { ControllerUnits.toUnit(rate.unitParentType, rate.unitParentId, rate.unitId) }
            }
            API.UNIT_TYPE_MODERATION -> {
                textR = R.string.profile_rate_moderation
                link = ControllerApi.linkToModeration(rate.unitId)
                setOnClick { SModerationView.instance(rate.unitId, Navigator.TO) }
            }
            API.UNIT_TYPE_REVIEW -> {
                textR = R.string.profile_rate_review
                link = ControllerApi.linkToReview(rate.unitId)
                setOnClick { SReviews.instance(rate.unitParentId, rate.unitId, Navigator.TO) }
            }
            API.UNIT_TYPE_FORUM -> {
                textR = R.string.profile_rate_forum
                link = ControllerApi.linkToForum(rate.unitId)
                setOnClick { SForumView.instance(rate.unitId, Navigator.TO) }
            }
            API.UNIT_TYPE_STICKER -> {
                textR = R.string.profile_rate_sticker
                link = ControllerApi.linkToSticker(rate.unitId)
                setOnClick { SStickersView.instanceBySticker(rate.unitId, Navigator.TO) }
            }
            API.UNIT_TYPE_STICKERS_PACK -> {
                textR = R.string.profile_rate_stikers_pack
                link = ControllerApi.linkToStickersPack(rate.unitId)
                setOnClick { SStickersView.instance(rate.unitId, Navigator.TO) }
            }
            else -> throw RuntimeException("Unknown unit type ${rate.unitType}")
        }

        setTitle(ToolsResources.sCap(textR, ToolsResources.sex(rate.accountSex, R.string.he_rate, R.string.she_rate), link))
        setSubtitle(ToolsDate.dateToString(rate.date))
        setDividerVisible(true)

        setOnCLickAvatar { SFandom.instance(rate.fandomId, rate.fandomLanguageId, Navigator.TO) }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: TextView = view.findViewById(R.id.vRate)

        vRate.text = (rate.karmaCount / 100).toString()
        vRate.setTextColor(ToolsResources.getColor(if (rate.karmaCount > 0L) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        ControllerApi.makeLinkable(vAvatar.vTitle)
        ToolsImagesLoader.load(rate.fandomImageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(rate.fandomImageId).intoCash()
    }
}
