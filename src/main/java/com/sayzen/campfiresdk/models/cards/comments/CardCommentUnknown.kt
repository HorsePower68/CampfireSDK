package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R

class CardCommentUnknown(
        unit: UnitComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(R.layout.card_comment_unknown, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {

    }

}