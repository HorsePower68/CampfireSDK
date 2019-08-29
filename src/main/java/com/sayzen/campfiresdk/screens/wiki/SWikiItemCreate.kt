package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources

class SWikiItemCreate(
        val fandomId:Long,
        val languageId:Long,
        val parentItemId:Long
) : Screen(R.layout.wiki_item_create){

    private val vNameEnglish:EditText = findViewById(R.id.vNameEnglish)
    private val vNamesContainer:ViewGroup = findViewById(R.id.vNamesContainer)
    private val vAddTranslate: View = findViewById(R.id.vAddTranslate)

    init {
        vNameEnglish.setHint(ToolsResources.s(R.string.wiki_item_create_name, API.LANGUAGES[0]))
    }

}