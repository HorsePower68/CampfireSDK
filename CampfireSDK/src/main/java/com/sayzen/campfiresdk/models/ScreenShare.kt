package com.sayzen.campfiresdk.models

import android.graphics.Bitmap
import android.net.Uri

interface ScreenShare {

    fun addText(text: String, postAfterAdd: Boolean = false)

    fun addImage(image: Uri, postAfterAdd: Boolean = false)

    fun addImage(image: Bitmap, postAfterAdd: Boolean = false)

}