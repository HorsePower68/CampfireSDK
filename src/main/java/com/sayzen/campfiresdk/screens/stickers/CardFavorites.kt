package com.sayzen.campfiresdk.screens.stickers

import android.view.View
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card

class CardFavorites : Card(R.layout.screen_stickers_favorites){

    override fun bindView(view: View) {
        super.bindView(view)

        view.setOnClickListener {
            Navigator.to(SStickersViewFavorite())
        }
    }

}