package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.models.notifications.project.NotificationProjectABParamsChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json

object ControllerABParams {

    private var ABParams: Json = Json()

    private val eventBus = EventBus.subscribe(EventNotification::class) {
        if (it.notification is NotificationProjectABParamsChanged) set((it).notification.ABParams)
    }

    fun set(ABParams: Json) {
        this.ABParams = ABParams
    }

    //
    //  New year
    //
/*
    val NG_Back = arrayOf(ToolsResources.getColor(R.color.grey_950))
    val NG_User = arrayOf(R.drawable.x_kid1, R.drawable.x_kid2, R.drawable.x_kid3, R.drawable.x_kid4, R.drawable.x_kid5, R.drawable.x_kid6, R.drawable.x_kid7, R.drawable.x_kid8, R.drawable.x_kid9, R.drawable.x_kid10)
    val NG_Moder = arrayOf(R.drawable.x_elf1, R.drawable.x_elf2, R.drawable.x_elf3, R.drawable.x_elf4)
    val NG_Admin = arrayOf(R.drawable.x_deer1, R.drawable.x_deer2, R.drawable.x_deer3, R.drawable.x_deer4, R.drawable.x_deer5)

    fun NG_Back(id: Long) = NG_Back[(id % NG_Back.size).toInt()]
    fun NG_User(id: Long) = NG_User[(id % NG_User.size).toInt()]
    fun NG_Moder(id: Long) = NG_Moder[(id % NG_Moder.size).toInt()]
    fun NG_Admin(id: Long) = NG_Admin[(id % NG_Admin.size).toInt()]

    fun NG_isPig(id: Long): Boolean {
        val pigs = ABParams.getString("NG_PIGS") ?: ""
        val split = pigs.split("-")
        for (i in split) if (i == id.toString()) return true
        return false
    }

    fun NG_PIGS_WITH(id: Long):String {
        val pigs = ABParams.getString("NG_PIGS") ?: ""
        val split = pigs.split("-")
        var newPigs = id.toString()
        for (i in split) newPigs += "-$i"
        return newPigs
    }

    fun NG_PIGS_WITHOUT(id: Long) : String {
        val pigs = ABParams.getString("NG_PIGS") ?: ""
        val split = pigs.split("-")
        var newPigs = ""
        for (i in split) if(i != id.toString()) newPigs += "-$i"
        return newPigs
    }

    fun NG_isStarted() = ABParams.getString("NG", "false").equals("true")
*/
}