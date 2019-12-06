package com.sayzen.campfiresdk.screens.other.rules

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView

class SRulesModerators : Screen(R.layout.screen_other_rules_moderators) {

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)

    init {
        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_MODER.asWeb())
            ToolsToast.show(R.string.app_copied)
        }

        addCard(R.string.rules_moderators_info)
        for (i in CampfireConstants.RULES_MODER) addCard(i)
    }

    private fun addCard(text: Int) {
        val view: View = ToolsView.inflate(R.layout.view_card_with_text)
        val vText: TextView = view.findViewById(R.id.vText)
        vText.setTextIsSelectable(true)
        vText.setText(text)
        vContainer.addView(view)
        if(vContainer.childCount > 1) (view.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(24).toInt()
    }

}