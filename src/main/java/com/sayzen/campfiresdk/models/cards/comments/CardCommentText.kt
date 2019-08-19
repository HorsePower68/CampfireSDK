package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R

class CardCommentText(
        unit: UnitComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(if(miniSize) R.layout.card_comment_text_mini else R.layout.card_comment_text, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {

    }

}