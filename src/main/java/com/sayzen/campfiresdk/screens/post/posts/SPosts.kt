package com.sayzen.campfiresdk.screens.post.posts

import androidx.viewpager.widget.ViewPager
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorTitles

class SPosts : Screen(R.layout.screen_posts){

    val vIndicator: ViewPagerIndicatorTitles = findViewById(R.id.vIndicator)
    val vPager:ViewPager = findViewById(R.id.vPager)

    init {

        vIndicator.setTitles(R.string.app_drafts, R.string.app_bookmarks, R.string.app_pending)

    }

}