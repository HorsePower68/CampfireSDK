package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.Rubric
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerRubrics
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeName
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeOwner
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricRemove
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardRubric(val rubric: Rubric) : Card(R.layout.card_rubric) {

    val xAccount = XAccount(rubric.ownerId, rubric.ownerName, rubric.ownerImageId, rubric.ownerLevel, rubric.ownerKarma30, rubric.ownerLastOnlineTime) { update() }
    val xFandom = XFandom(rubric.fandomId, rubric.languageId, rubric.fandomName, rubric.fandomImageId) { update() }
    var onClick: ((Rubric) -> Unit)? = null
    var showFandom = false

    val eventBus = EventBus
            .subscribe(EventRubricChangeName::class) {
                if (rubric.id == it.rubricId) {
                    rubric.name = it.rubricName
                    update()
                }
            }
            .subscribe(EventRubricChangeOwner::class) {
                if (rubric.id == it.rubricId) {
                    rubric.ownerId = it.ownerId
                    rubric.ownerImageId = it.ownerImageId
                    rubric.ownerName = it.ownerName
                    rubric.ownerLevel = it.ownerLevel
                    rubric.ownerKarma30 = it.ownerKarma30
                    rubric.ownerLastOnlineTime = it.ownerLastOnlineTime
                    update()
                }
            }
            .subscribe(EventRubricRemove::class) {
                if (rubric.id == it.rubricId) {
                    adapter?.remove(this)
                }
            }

    override fun bindView(view: View) {

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRate: TextView = view.findViewById(R.id.vRate)
        val vWaitForPost: View = view.findViewById(R.id.vWaitForPost)

        if (showFandom) xFandom.setView(vAvatar) else xAccount.setView(vAvatar)
        vAvatar.setTitle(rubric.name)
        vAvatar.setSubtitle(rubric.ownerName)
        vAvatar.vAvatar.isClickable = onClick == null
        vAvatar.isClickable = false

        vRate.text = ToolsText.numToStringRound(rubric.karmaCof / 100.0, 2)
        vWaitForPost.visibility = if (rubric.isWaitForPost) View.VISIBLE else View.GONE

        view.setOnClickListener {
            if (onClick == null) {
                Navigator.to(SRubricPosts(rubric))
            } else {
                onClick?.invoke(rubric)
            }
        }

        view.setOnLongClickListener {
            ControllerRubrics.instanceMenu(rubric).asSheetShow()
            true
        }
    }


}