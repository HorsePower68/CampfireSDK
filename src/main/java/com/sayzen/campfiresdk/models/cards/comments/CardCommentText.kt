package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.PublicationComment
import com.sayzen.campfiresdk.R

class CardCommentText(
        unit: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(if(miniSize) R.layout.card_comment_text_mini else R.layout.card_comment_text, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {

    }

}