package com.sayzen.campfiresdk.screens.notifications

import android.text.Html
import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.notifications.*
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsAdd
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomAccepted
import com.dzen.campfire.api.models.notifications.fandom.NotificationModerationRejected
import com.dzen.campfire.api.models.notifications.rubrics.*
import com.dzen.campfire.api.models.notifications.publications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.publications.NotificationKarmaAdd
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.profile.SAccount
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationsCountChanged
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardNotification(
        val screen: SNotifications,
        val notification: Notification
) : CardAvatar(R.layout.screen_notifications_card), NotifyItem {

    private val eventBus = EventBus.subscribe(EventNotificationsCountChanged::class) { updateRead() }

    init {

        setChipIconPadding(ToolsView.dpToPx(3).toInt())
        setOnClick {
            ControllerNotifications.removeNotificationFromNew(notification.id)
            ControllerNotifications.parser(notification).doAction()
            update()
        }
        setDividerVisible(true)
        setChipIcon(0)
        setOnCLickAvatar(null)
        setSubtitle(ToolsDate.dateToString(notification.dateCreate))

        when (notification) {
            is NotificationAccountsFollowsAdd -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationFollowsPublication -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationComment -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationCommentAnswer -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationKarmaAdd -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationChatAnswer -> setOnCLickAvatar { SAccount.instance(notification.publicationChatMessage.creatorId, Navigator.TO) }
            is NotificationAchievement -> setOnCLickAvatar { SAchievements.instance(ControllerApi.account.id, ControllerApi.account.name, notification.achiIndex, false, Navigator.TO) }
            is NotificationFandomAccepted -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
            is NotificationMention -> setOnCLickAvatar { SAccount.instance(notification.fromAccountId, Navigator.TO) }
            is NotificationModerationRejected -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsChangeName -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsChangeOwner -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsKarmaCofChanged -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsMakeOwner -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsRemove -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationPublicationReaction -> setOnCLickAvatar { SAccount.instance(notification.accountId, Navigator.TO) }
        }


    }

    override fun bindView(view: View) {
        super.bindView(view)
        updateRead()

        val vTouch: ViewSwipe = view.findViewById(R.id.vTouch)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        vDate.text = ToolsDate.dateToString(notification.dateCreate)

        vTouch.onSwipe = {
            ControllerNotifications.removeNotificationFromNew(notification.id)
            updateRead()
        }

        val parser = ControllerNotifications.parser(notification)
        val title = parser.getTitle()
        if (title.isNotEmpty()) {
            vAvatar.setTitle(title)
            vAvatar.vSubtitle.text = Html.fromHtml(ControllerNotifications.parser(notification).asString(true))
            vDate.visibility = View.VISIBLE
        } else {
            vAvatar.setTitle(null)
            vAvatar.vTitle.text = Html.fromHtml(ControllerNotifications.parser(notification).asString(true))
            vDate.visibility = View.GONE
        }
        vAvatar.vSubtitle.maxLines = 2
        ControllerLinks.makeLinkable(vAvatar.vSubtitle)

        vAvatar.vAvatar.vChip.setBackgroundColor(ToolsResources.getAccentColor(vAvatar.context))
        if (notification.imageId > 0)
            ToolsImagesLoader.load(notification.imageId).into(vAvatar.vAvatar.vImageView)
        else
            vAvatar.vAvatar.setImage(R.drawable.logo_campfire_128x128)

    }

    private fun updateRead() {
        if (getView() == null) return
        val vNotRead: View = getView()!!.findViewById(R.id.vNotRead)
        vNotRead.visibility = if (ControllerNotifications.isNew(notification.id)) View.VISIBLE else View.GONE
    }

    override fun notifyItem() {
        if (notification.imageId > 0) ToolsImagesLoader.load(notification.imageId).intoCash()
    }
}
