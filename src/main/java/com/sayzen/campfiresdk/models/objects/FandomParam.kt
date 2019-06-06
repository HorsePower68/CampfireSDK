package com.sayzen.campfiresdk.models.objects

import android.support.annotation.StringRes

import com.sup.dev.android.tools.ToolsResources

class FandomParam(val index: Long, @param:StringRes private val mame: Int) {

    val name: String
        get() = ToolsResources.s(mame)

}