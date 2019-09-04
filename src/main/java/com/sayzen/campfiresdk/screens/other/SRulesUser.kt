package com.sayzen.campfiresdk.screens.other

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView

class SRulesUser(
        noNavigationMode:Boolean = false
) : Screen(R.layout.screen_other_rules_user) {

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)

    init {
        if(noNavigationMode){
            activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
            isNavigationVisible = false
            isNavigationAllowed = false
            isNavigationAnimation = false
            vCopyLink.visibility = View.GONE
        }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_USER)
            ToolsToast.show(R.string.app_copied)
        }

        addCard(R.string.rules_users_info)
        for (i in CampfireConstants.RULES_USER) addCard(i.text)
    }

    private fun addCard(text: Int) {
        val view: View = ToolsView.inflate(R.layout.view_card_with_text)
        val vText: TextView = view.findViewById(R.id.vText)
        vText.setText(text)
        vContainer.addView(view)
        if(vContainer.childCount > 1) (view.layoutParams as MarginLayoutParams).topMargin = ToolsView.dpToPx(24).toInt()
    }

}