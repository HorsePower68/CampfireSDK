package com.sayzen.campfiresdk.adapters

import android.widget.TextView
import com.dzen.campfire.api.models.EventPublicationInstance
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentAdd
import com.sayzen.campfiresdk.models.events.publications.EventCommentRemove
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.eventBus.EventBus

class XComments(
        private val unit: Publication,
        var onChanged: () -> kotlin.Unit
) {

    companion object {
        private val eventBus = EventBus
                .subscribe(EventPublicationInstance::class) { set(it.publication.id, it.publication.subUnitsCount, System.currentTimeMillis()) }
                .subscribe(EventCommentAdd::class) { set(it.parentUnitId, get(it.parentUnitId) + 1, System.currentTimeMillis()) }
                .subscribe(EventCommentRemove::class) { set(it.parentPublicationId, get(it.parentPublicationId) - 1, System.currentTimeMillis()) }
                .subscribe(EventNotification::class) {
                    if (it.notification is NotificationComment) set(it.notification.publicationId, get(it.notification.publicationId) + 1, System.currentTimeMillis())
                    if (it.notification is NotificationCommentAnswer) set(it.notification.publicationId, get(it.notification.publicationId) + 1, System.currentTimeMillis())
                }

        private val comments = HashMap<Long, Item2<Long, Long>>()

        fun set(unitId: Long, commentsCount: Long, unitInstanceDate: Long) {
            if (!comments.containsKey(unitId)) {
                comments[unitId] = Item2(commentsCount, unitInstanceDate)
                EventBus.post(EventCommentsCountChanged(unitId, commentsCount, commentsCount))
            } else {
                if (comments[unitId]!!.a2 < unitInstanceDate && comments[unitId]!!.a1 != commentsCount) {
                    val old = comments[unitId]!!.a1
                    comments[unitId] = Item2(commentsCount, unitInstanceDate)
                    EventBus.post(EventCommentsCountChanged(unitId, commentsCount, commentsCount - old))
                }
            }
        }

        fun get(unitId: Long) = if (comments.containsKey(unitId)) comments[unitId]!!.a1 else 0L

    }

    private val eventBus = EventBus
            .subscribe(EventCommentsCountChanged::class) {
                if (it.publicationId == unit.id) {
                    unit.subUnitsCount = get(unit.id)
                    onChanged.invoke()
                }
            }

    fun setView(view: TextView) {
        view.text = "${unit.subUnitsCount}"
    }


}
