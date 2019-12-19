package com.sayzen.campfiresdk.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationFandomChanged
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class XFandom(
        var publicationId: Long,
        var fandomId: Long,
        var languageId: Long,
        var name: String,
        var imageId: Long,
        var imageTitleId: Long,
        var imageTitleGifId: Long,
        var date: Long,
        var onChanged: () -> Unit
) {

    var showLanguage = true
    var allViewIsClickable = false

    private val eventBus = EventBus
            .subscribe(EventPublicationFandomChanged::class) { onEventPublicationFandomChanged(it) }
            .subscribe(EventFandomChanged::class) { onEventFandomChanged(it) }

    constructor(fandom: Fandom, date: Long = 0, onChanged: () -> Unit)
            : this(0, fandom.id, fandom.languageId, fandom.name, fandom.imageId, fandom.imageTitleId, fandom.imageTitleGifId, date, onChanged)

    constructor(publication: Publication, date: Long = 0, onChanged: () -> Unit)
            : this(publication.id, publication.fandomId, publication.languageId, publication.fandomName, publication.fandomImageId, 0, 0, date, onChanged)

    constructor(fandomId: Long, languageId: Long, name: String = "", imageId: Long = 0L, onChanged: () -> Unit)
            : this(0, fandomId, languageId, name, imageId, 0, 0, 0, onChanged)

    init {
        ImageLoader.load(imageId).intoCash()
    }

    fun setView(viewAvatar: ViewAvatar) {
        ImageLoader.load(imageId).into(viewAvatar.vImageView)
        viewAvatar.setChipIcon(0)
        viewAvatar.setChipText("")

        if (showLanguage && languageId != 0L && languageId != ControllerApi.getLanguageId()) {
            viewAvatar.vChip.setBackgroundColor(ToolsResources.getAccentColor(viewAvatar.context))
            ControllerApi.getIconForLanguage(languageId).setOnLoaded {
                viewAvatar.vChipIcon.visibility = View.VISIBLE
            }.into(viewAvatar.vChipIcon)
        }else{
            viewAvatar.vChip.setBackgroundColor(Color.TRANSPARENT)
            viewAvatar.vChipIcon.setImageDrawable(null)
            viewAvatar.vChipIcon.visibility = View.GONE
        }

        viewAvatar.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(fandomId, languageId, Navigator.TO) }
    }

    fun setView(viewAvatar: ViewAvatarTitle) {
        setView(viewAvatar.vAvatar)

        if (name.isNotEmpty()) viewAvatar.setTitle(name)
        if (date != 0L) viewAvatar.setSubtitle(ToolsDate.dateToString(date))
        if (allViewIsClickable) {
            viewAvatar.vAvatar.setOnClickListener(null)
            viewAvatar.setOnClickListener { ControllerCampfireSDK.onToFandomClicked(fandomId, languageId, Navigator.TO) }
        }
    }

    fun setView(vText: TextView, vImage: ImageView?, vImageBig: ImageView? = null) {
        setView(vText)
        if (vImage != null) setView(vImage)
        if (vImageBig != null) setViewBig(vImageBig)
    }

    fun setViewBig(vImage: ImageView) {
        if (imageTitleId != 0L) ImageLoader.loadGif(imageTitleId, imageTitleGifId, 0, 0, vImage, null)
        else vImage.setImageBitmap(null)
    }

    fun setView(vImage: ImageView) {
        ImageLoader.load(imageId).into(vImage)
    }

    fun setView(vText: TextView) {
        vText.text = name
    }

    //
    //  EventBus
    //

    private fun onEventFandomChanged(e: EventFandomChanged) {
        if (e.fandomId == fandomId) {
            if (e.name.isNotEmpty()) name = e.name
            if (e.imageId != -1L) imageId = e.imageId
            if (e.imageTitleId != -1L) imageTitleId = e.imageTitleId
            if (e.imageTitleGifId != -1L) imageTitleGifId = e.imageTitleGifId
            onChanged.invoke()
        }
    }

    private fun onEventPublicationFandomChanged(e: EventPublicationFandomChanged) {
        if (e.publicationId == publicationId) {
            fandomId = e.fandomId
            languageId = e.languageId
            name = e.fandomName
            imageId = e.fandomImageId
            onChanged.invoke()
        }
    }


    //
    //  Getters
    //

    fun linkTo() = ControllerLinks.linkToFandom(fandomId)

    fun linkToWithLanguage() = ControllerLinks.linkToFandom(fandomId, languageId)


}
