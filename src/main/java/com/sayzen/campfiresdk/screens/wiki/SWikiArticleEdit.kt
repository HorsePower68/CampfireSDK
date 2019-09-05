package com.sayzen.campfiresdk.screens.wiki

import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen

class SWikiArticleEdit(
        val item: WikiTitle,
        val pages: WikiPages,
        var languageId: Long
) : Screen(R.layout.screen_wiki_article) {

    init {

    }

}
