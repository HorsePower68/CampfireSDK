package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R

class CardCommentText(
        unit: UnitComment,
        dividers: Boolean,
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(unit, dividers, onClick, onQuote, onGoTo) {

    override fun getLayout() = R.layout.card_comment_text

    override fun bind(view: View) {

    }

}