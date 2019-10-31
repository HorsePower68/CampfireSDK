package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Rubric
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.tools.ToolsText

class CardRubric(val rubric: Rubric) : Card(R.layout.card_rubric) {

    val xAccount = XAccount(rubric.ownerId, rubric.ownerName, rubric.ownerImageId, rubric.ownerLevel, rubric.ownerKarma30, rubric.ownerLastOnlineTime) { update() }
    val xFandom = XFandom(rubric.fandomId, rubric.languageId, rubric.fandomName, rubric.fandomImageId) { update() }
    var onClick: ((Rubric) -> Unit)? = null
    var showFandom = false

    override fun bindView(view: View) {

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRate: TextView = view.findViewById(R.id.vRate)

        if (showFandom) xFandom.setView(vAvatar) else xAccount.setView(vAvatar)
        vAvatar.setTitle(rubric.name)
        vAvatar.setSubtitle(rubric.ownerName)
        vAvatar.vAvatar.isClickable = onClick == null
        vAvatar.isClickable = false

        vRate.text = ToolsText.numToStringRound(rubric.karmaCof / 100.0, 2)

        view.setOnClickListener {
            if (onClick == null) {
                Navigator.to(SRubricPosts(rubric.id, rubric.name))
            } else {
                onClick?.invoke(rubric)
            }
        }

        view.setOnLongClickListener {
            WidgetMenu()
                    .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToRubric(rubric.id));ToolsToast.show(R.string.app_copied) }
                    .add(R.string.rubric_change_owner) { _, _ -> changeOwner() }.condition(ControllerApi.isCurrentAccount(rubric.ownerId))
                    .groupCondition(ControllerApi.can(rubric.fandomId, rubric.languageId, API.LVL_MODERATOR_RUBRIC))
                    .add(R.string.app_edit) { _, _ -> edit() }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                    .add(R.string.app_remove) { _, _ -> removeRubric() }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                    .add(R.string.rubric_change_owner) { _, _ -> chageOwner() }.condition(!ControllerApi.isCurrentAccount(rubric.ownerId)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                    .asSheetShow()
            true
        }
    }

    private fun changeOwner() {

    }

    private fun edit() {

    }

    private fun removeRubric() {

    }

    private fun chageOwner() {

    }

}