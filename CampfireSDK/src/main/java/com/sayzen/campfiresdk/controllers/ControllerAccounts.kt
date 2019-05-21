package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.models.events.account.EventAccountOnlineChanged
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerAccounts{

    //
    //  Online
    //

    var online = HashMap<Long, Long>()

    fun getLastOnlineTime(accountId: Long): Long {
        return if (online.containsKey(accountId)) online[accountId]!! else 0
    }

    fun updateOnline(accountId: Long, time: Long) {
        if (!online.containsKey(accountId) || online[accountId]!! < time) {
            online[accountId] = time
            EventBus.post(EventAccountOnlineChanged(accountId, online[accountId]!!))
        }
    }

    fun isOnline(accountId: Long): Boolean {
        return online.containsKey(accountId) && online[accountId]!! > ControllerApi.currentTime() - 1000L * 60L * 5L
    }

}