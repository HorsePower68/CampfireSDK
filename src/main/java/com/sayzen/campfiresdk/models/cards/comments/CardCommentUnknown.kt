package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.PublicationComment
import com.sayzen.campfiresdk.R

class CardCommentUnknown(
        unit: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(R.layout.card_comment_unknown, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {

    }

}