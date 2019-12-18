package com.sayzen.campfiresdk.screens.post.bookmarks

import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.BookmarksFolder
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderChanged
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderCreate
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderRemove
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsArrow
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections

class CardButtons : Card(R.layout.screen_bookmarks_card_buttons) {

    private val eventBus = EventBus
            .subscribe(EventBookmarkFolderCreate::class){ update() }
            .subscribe(EventBookmarkFolderChanged::class){ update() }
            .subscribe(EventBookmarkFolderRemove::class){ update() }

    override fun bindView(view: View) {
        super.bindView(view)
        val vAddNew: Settings = view.findViewById(R.id.vAddNew)
        val vContainer: ViewGroup = view.findViewById(R.id.vContainer)

        vAddNew.setOnClickListener {
            if (ControllerSettings.bookmarksFolders.size > API.BOOKMARKS_FOLDERS_MAX) {
                ToolsToast.show(R.string.bookmarks_folder_error_max)
                return@setOnClickListener
            }
            WidgetField()
                    .setHint(R.string.app_name_s)
                    .setOnCancel(R.string.app_cancel)
                    .setMax(API.BOOKMARKS_FOLDERS_NAME_MAX.toInt())
                    .setOnEnter(R.string.app_create) { w, text ->
                        val folder = BookmarksFolder()
                        folder.name =text
                        folder.id = System.currentTimeMillis()
                        ControllerSettings.bookmarksFolders = ToolsCollections.add(folder, ControllerSettings.bookmarksFolders)
                        EventBus.post(EventBookmarkFolderCreate(folder))
                        ToolsToast.show(R.string.app_done)
                    }
                    .asSheetShow()
        }

        val childAt = vContainer.getChildAt(vContainer.childCount - 1)
        vContainer.removeAllViews()

        for (f in ControllerSettings.bookmarksFolders) {
            val view = SettingsArrow(SupAndroid.activity!!, null)
            view.setTitle(f.name)
            view.setOnClickListener {
                Navigator.to(SBookmarksFolder(f))
            }
            view.setOnLongClickListener {
                WidgetMenu()
                        .add(R.string.app_change) { w, i -> change(f) }
                        .add(R.string.app_remove) { w, i -> remove(f) }
                        .asSheetShow()
                true
            }
            vContainer.addView(view)
        }

        vContainer.addView(childAt)

    }

    private fun change(folder: BookmarksFolder) {
        WidgetField()
                .setHint(R.string.app_name_s)
                .setText(folder.name)
                .setOnCancel(R.string.app_cancel)
                .setMax(API.BOOKMARKS_FOLDERS_NAME_MAX.toInt())
                .setOnEnter(R.string.app_change) { w, text ->

                    folder.name = text
                    for(f in ControllerSettings.bookmarksFolders) if(f.id == folder.id) f.name = text
                    ControllerSettings.onSettingsUpdated()
                    EventBus.post(EventBookmarkFolderChanged(folder))
                    ToolsToast.show(R.string.app_done)
                }
                .asSheetShow()
    }

    private fun remove(folder: BookmarksFolder) {
        WidgetAlert()
                .setText(R.string.bookmarks_folder_remove_alert)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_remove){
                    ControllerSettings.bookmarksFolders = ToolsCollections.removeIf(ControllerSettings.bookmarksFolders){it.id == folder.id}
                    EventBus.post(EventBookmarkFolderRemove(folder.id))
                    ToolsToast.show(R.string.app_done)
                }
                .asSheetShow()
    }

}
