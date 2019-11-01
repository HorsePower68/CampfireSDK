package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.requests.rubrics.RRubricsModerChangeName
import com.dzen.campfire.api.requests.rubrics.RRubricsModerChangeOwner
import com.dzen.campfire.api.requests.rubrics.RRubricsModerRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeName
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeOwner
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricRemove
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerRubrics {

    fun instanceMenu(rubric: Rubric) = WidgetMenu()
            .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToRubric(rubric.id));ToolsToast.show(R.string.app_copied) }
            .groupCondition(ControllerApi.can(rubric.fandomId, rubric.languageId, API.LVL_MODERATOR_RUBRIC))
            .add(R.string.app_change_naming) { _, _ -> edit(rubric) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .add(R.string.app_remove) { _, _ -> removeRubric(rubric) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .add(R.string.rubric_change_owner) { _, _ -> changeOwner(rubric) }.condition(!ControllerApi.isCurrentAccount(rubric.ownerId)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)


    private fun edit(rubric: Rubric) {
        WidgetField()
                .setTitle(R.string.app_change_naming)
                .setMin(API.RUBRIC_NAME_MIN)
                .setMax(API.RUBRIC_NAME_MAX)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_change) { w, newName ->
                    ControllerApi.moderation(R.string.app_change_naming, R.string.app_change, { RRubricsModerChangeName(rubric.id, newName, it) }) { r ->
                        EventBus.post(EventRubricChangeName(rubric.id, newName))
                        ToolsToast.show(R.string.app_done)
                    }
                }
                .asSheetShow()
    }

    private fun removeRubric(rubric: Rubric) {
        ControllerApi.moderation(R.string.app_remove, R.string.app_remove, { RRubricsModerRemove(rubric.id, it) }) { r ->
            EventBus.post(EventRubricRemove(rubric.id))
            ToolsToast.show(R.string.app_done)
        }
    }

    private fun changeOwner(rubric: Rubric) {
        Navigator.to(SAccountSearch(false, true) { account ->
            ControllerApi.moderation(R.string.rubric_change_owner, R.string.app_change, { RRubricsModerChangeOwner(rubric.id, account.id, it) }) { r ->
                EventBus.post(EventRubricChangeOwner(rubric.id, r.rubric.ownerId, r.rubric.ownerImageId, r.rubric.ownerName, r.rubric.ownerLevel, r.rubric.ownerKarma30, r.rubric.ownerLastOnlineTime))
                ToolsToast.show(R.string.app_done)
            }
        })
    }

}