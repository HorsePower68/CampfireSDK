package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R

class CardCommentText(
        publication: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(
        if(miniSize) R.layout.card_comment_text_mini else if(dividers) R.layout.card_comment_text else R.layout.card_comment_text_card,
        publication, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {

    }

}