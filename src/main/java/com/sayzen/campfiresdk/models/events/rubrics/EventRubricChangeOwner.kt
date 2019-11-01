package com.sayzen.campfiresdk.models.events.rubrics

class EventRubricChangeOwner(
        val rubricId: Long,
        val ownerId: Long,
        val ownerImageId: Long,
        val ownerName: String,
        val ownerLevel: Long,
        val ownerKarma30: Long,
        val ownerLastOnlineTime: Long

)