package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.post.Page

class EventPostChanged(var unitId: Long, var pages: Array<Page>)
