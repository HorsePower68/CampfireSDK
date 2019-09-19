package com.sayzen.campfiresdk.models.events.wiki

import com.dzen.campfire.api.models.units.post.Page

class EventWikiPagesChanged(
        var itemId: Long,
        val languageId: Long,
        var pages: Array<Page>)
