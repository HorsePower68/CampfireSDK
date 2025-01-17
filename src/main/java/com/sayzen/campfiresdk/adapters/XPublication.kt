package com.sayzen.campfiresdk.adapters

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.post.PageUserActivity
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.publications.*
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections

class XPublication(
        val publication: Publication,
        val onChangedAccount: () -> Unit,
        val onChangedFandom: () -> Unit,
        val onChangedKarma: () -> Unit,
        val onChangedComments: () -> Unit,
        val onChangedReports: () -> Unit,
        val onChangedImportance: () -> Unit,
        val onChangedReactions: () -> Unit,
        val onRemove: () -> Unit
) {

    val eventBus = EventBus
            .subscribe(EventPostCloseChange::class) { if (it.publicationId == publication.id) publication.closed = it.closed }
            .subscribe(EventPostNotifyFollowers::class) { if (it.publicationId == publication.id) publication.tag_3 = 1 }
            .subscribe(EventPublicationRemove::class) { if (it.publicationId == publication.id) onRemove.invoke() }
            .subscribe(EventPublicationReactionAdd::class) {
                if (it.publicationId == publication.id && publication is PublicationComment){
                    publication.reactions = ToolsCollections.add(PublicationComment.Reaction(ControllerApi.account.id, it.reactionIndex), publication.reactions)
                    onChangedReactions.invoke()
                }
            }
            .subscribe(EventPublicationReactionRemove::class) {
                if (it.publicationId == publication.id && publication is PublicationComment){
                    publication.reactions = ToolsCollections.removeIf(publication.reactions) { it.accountId == ControllerApi.account.id && it.reactionIndex == it.reactionIndex }
                    onChangedReactions.invoke()
                }
            }
            .subscribe(EventPostMultilingualChange::class) {
                if (it.publicationId == publication.id) {
                    publication.languageId = it.languageId
                    publication.tag_5 = it.tag5
                    xFandom.languageId = it.languageId
                    onChangedFandom.invoke()
                }
            }
            .subscribe(EventPublicationImportantChange::class) {
                if (it.publicationId == publication.id) {
                    publication.important = it.important
                    onChangedImportance.invoke()
                }
            }

    val xAccount = XAccount(publication, publication.dateCreate) { onChangedAccount.invoke() }
    val xKarma = XKarma(publication) { onChangedKarma.invoke() }
    var xFandom = XFandom(publication, publication.dateCreate) { onChangedFandom.invoke() }
    val xComments = XComments(publication) { onChangedComments.invoke() }
    val xReports = XReports(publication) { onChangedReports.invoke() }

    init {
        if (publication is PublicationPost) {
            if (publication.status == API.Companion.STATUS_PUBLIC && publication.userActivity != null) {
                val page = PageUserActivity()
                page.userActivity = publication.userActivity!!
                publication.pages = ToolsCollections.add(page, publication.pages)
            }
        }
    }

}
