package com.sayzen.campfiresdk.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.EventPublicationInstance
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.dzen.campfire.api.models.notifications.project.NotificationProjectABParamsChanged
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerHoliday
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountOnlineChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class XAccount(
        var accountId: Long,
        var imageId: Long,
        var lvl: Long,
        var karma30: Long,
        var name: String,
        var date: Long,
        var titleImageId: Long,
        var titleImageGifId: Long,
        var onChanged: () -> Unit
) {

    companion object {
        var online = HashMap<Long, Long>()

        private val eventBus = EventBus
                .subscribe(EventPublicationInstance::class) { this.set(it.publication.creatorId, it.publication.creatorLastOnlineTime) }
                .subscribe(EventNotification::class) {
                    if (it.notification is NotificationChatTyping) set(it.notification.accountId, System.currentTimeMillis())
                }

        fun get(accountId: Long): Long {
            return if (online.containsKey(accountId)) online[accountId]!! else 0
        }

        fun set(accountId: Long, time: Long) {
            if (!online.containsKey(accountId) || online[accountId]!! < time) {
                online[accountId] = time
                EventBus.post(EventAccountOnlineChanged(accountId, online[accountId]!!))
            }
        }

        fun isOnline(accountId: Long): Boolean {
            return accountId == ControllerApi.account.id || (online.containsKey(accountId) && online[accountId]!! > ControllerApi.currentTime() - 1000L * 60L * 15L)
        }

    }

    private var inited = false
    var sex = 0L

    private val eventBus = EventBus
            .subscribe(EventAccountChanged::class) { onEventAccountChanged(it) }
            .subscribe(EventAccountOnlineChanged::class) { onEventAccountOnlineChanged(it) }
            .subscribe(EventNotification::class) {
                if (it.notification is NotificationProjectABParamsChanged) onChanged.invoke()
            }

    constructor(account: Account, date: Long = 0, titleImageId: Long = 0, titleImageGifId: Long = 0, onNameChanged: () -> Unit)
            : this(account.id, account.imageId, account.lvl, account.karma30, account.name, date, titleImageId, titleImageGifId, onNameChanged) {
        set(account.id, account.lastOnlineDate)
        sex = account.sex
        inited = true
    }

    constructor(publication: com.dzen.campfire.api.models.publications.Publication, date: Long = 0, onNameChanged: () -> Unit)
            : this(publication.creatorId, publication.creatorImageId, publication.creatorLvl, publication.creatorKarma30, publication.creatorName, date, 0L, 0L, onNameChanged) {
        set(publication.creatorId, publication.creatorLastOnlineTime)
        inited = true
    }

    constructor(accountId: Long, imageId: Long, lvl: Long, karma30: Long, lastOnlineTime: Long, onNameChanged: () -> Unit = {})
            : this(accountId, imageId, lvl, karma30, "", 0, 0L, 0L, onNameChanged) {
        set(accountId, lastOnlineTime)
        inited = true
    }

    constructor(accountId: Long, name: String, imageId: Long = 0L, lvl: Long = 0L, karma30: Long = 0L, lastOnlineTime: Long = 0L, onChanged: () -> Unit = {})
            : this(accountId, imageId, lvl, karma30, name, 0, 0L, 0L, onChanged) {
        set(accountId, lastOnlineTime)
        inited = true
    }

    init {
        ImageLoader.load(imageId).intoCash()
    }

    fun setView(viewAvatar: ViewAvatar) {
        setView(viewAvatar.vImageView)
        viewAvatar.setChipIcon(0)

        if (isBot()) viewAvatar.vChip.setText("B")
        else viewAvatar.vChip.setText("${lvl / 100}")
        setOnline(viewAvatar.vChip)
        viewAvatar.vChip.visibility = if (lvl < 1) View.GONE else View.VISIBLE
        viewAvatar.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(accountId, Navigator.TO) }
    }

    fun setView(viewAvatar: ViewAvatarTitle) {
        setView(viewAvatar.vAvatar)
        viewAvatar.setOnClickListener { ControllerCampfireSDK.onToAccountClicked(accountId, Navigator.TO) }
        viewAvatar.setTitle(name)
        if (date != 0L) viewAvatar.setSubtitle(ToolsDate.dateToString(date))
    }

    fun setView(vTitle: TextView, vImage: ImageView, vImageBig: ImageView? = null) {
        setView(vTitle)
        setView(vImage)
        if (vImageBig != null) setViewBig(vImageBig)
    }

    fun setViewBig(vImage: ImageView) {
        if (titleImageId != 0L) {
            ImageLoader.loadGif(titleImageId, titleImageGifId, 0, 0, vImage, null)
        } else vImage.setImageBitmap(null)
    }

    fun setView(vImage: ImageView) {
        val holidayImage = ControllerHoliday.getAvatar(accountId, lvl, karma30)
        if(holidayImage != null){
            val background = ControllerHoliday.getBackground(accountId)
            ImageLoader.load(holidayImage).into(vImage)
            if(background != null) vImage.setBackgroundColor(background)
        }else{

            ImageLoader.load(imageId).into(vImage)
        }
    }

    fun setView(vTitle: TextView) {
        vTitle.text = name
    }

    private fun setOnline(vChip: ViewChipMini) {
        when {
            isBot() -> vChip.setBackgroundRes(R.color.black)
            isProtoadmin() -> vChip.setBackgroundRes(if (isOnline()) R.color.orange_a_700 else R.color.grey_500)
            isAdmin() -> vChip.setBackgroundRes(if (isOnline()) R.color.red_700 else R.color.grey_500)
            isModerator() -> vChip.setBackgroundRes(if (isOnline()) R.color.blue_700 else R.color.grey_500)
            else -> vChip.setBackgroundRes(if (isOnline()) R.color.green_700 else R.color.grey_500)
        }
    }

    private fun onEventAccountChanged(e: EventAccountChanged) {
        if (accountId == e.accountId) {
            if (e.name.isNotEmpty()) name = e.name
            if (e.imageId != -1L) imageId = e.imageId
            if (e.imageTitleId != -1L) titleImageId = e.imageTitleId
            if (e.imageTitleId != -1L) titleImageGifId = e.imageTitleGifId
            onChanged.invoke()
        }
    }

    private fun onEventAccountOnlineChanged(e: EventAccountOnlineChanged) {
        if (inited && accountId == e.accountId) {
            onChanged.invoke()
        }
    }

    //
    //  Setters
    //

    //
    //  Getters
    //

    fun can(lvl: LvlInfoAdmin) = ControllerApi.can(lvl)

    fun can(lvl: LvlInfoUser) = ControllerApi.can(lvl)

    fun isCurrentAccount() = ControllerApi.isCurrentAccount(accountId)

    fun isOnline() = isOnline(accountId)

    fun getLastOnlineTime() = get(accountId)

    fun isModerator() = ControllerApi.isModerator(lvl) && karma30 >= API.LVL_MODERATOR_BLOCK.karmaCount

    fun isAdmin() = ControllerApi.isAdmin(lvl) && karma30 >= API.LVL_ADMIN_MODER.karmaCount

    fun isProtoadmin() = ControllerApi.isProtoadmin(accountId, lvl)

    fun isBot() = ControllerApi.isBot(name)

}
