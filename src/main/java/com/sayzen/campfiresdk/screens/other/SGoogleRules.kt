package com.sayzen.campfiresdk.screens.other

import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.android.views.widgets.WidgetAlert

class SGoogleRules(
    val onAccept: () -> Unit
) : Screen(R.layout.screen_google_rules) {

    companion object {

        var CHECK_FLAG_IN_ACCOUNT_SETTINGS = false

        fun acceptRulesScreen(action: NavigationAction, onAccept: () -> Unit) {
            if (check()) {
                onAccept.invoke()
                return
            }

            Navigator.action(action, SGoogleRules(onAccept))
        }

        fun acceptRulesDialog(onAccept: () -> Unit) {
            if (check()) {
                onAccept.invoke()
                return
            }

            WidgetAlert()
                .setText(instanceSpan())
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setChecker(CampfireConstants.CHECK_RULES_ACCEPTED, R.string.app_i_agree)
                .setLockUntilAccept(true)
                .setOnCancel(R.string.app_cancel)
                .setOnEnter(R.string.app_accept) { onAccept.invoke() }
                .asSheetShow()
        }

        fun check(): Boolean {
            if (WidgetAlert.check(CampfireConstants.CHECK_RULES_ACCEPTED)) return true
            if (CHECK_FLAG_IN_ACCOUNT_SETTINGS && ControllerSettings.rulesIsShowed) return true
            return false
        }

        fun instanceSpan(): Spannable {
            val tApp = ToolsResources.s(R.string.message_publication_rules_1)
            val tGoogle = ToolsResources.s(R.string.message_publication_rules_2)
            val t = ToolsResources.s(R.string.message_publication_rules, tApp, tGoogle)

            val span = Spannable.Factory.getInstance().newSpannable(t)
            span.setSpan(
                object : ClickableSpan() {
                    override fun onClick(v: View) {
                        ControllerCampfireSDK.openLink("https://play.google.com/intl/ru_ALL/about/restricted-content/inappropriate-content/")
                    }
                },
                t.indexOf(tGoogle),
                t.indexOf(tGoogle) + tGoogle.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(object : ClickableSpan() {
                override fun onClick(v: View) {
                    Navigator.to(SRulesUser(true))
                }
            }, t.indexOf(tApp), t.indexOf(tApp) + tApp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return span
        }

    }

    private val vText: ViewTextLinkable = findViewById(R.id.vText)
    private val vButton: TextView = findViewById(R.id.vButton)
    private val vCheck: CheckBox = findViewById(R.id.vCheck)

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        isNavigationVisible = false
        isNavigationAnimation = false

        vText.setText(instanceSpan())
        vButton.setOnClickListener {
            onAccept.invoke()
        }
        vButton.isEnabled = false
        vCheck.setOnCheckedChangeListener { compoundButton, b -> vButton.isEnabled = b }
        ToolsView.makeLinksClickable(vText)
    }


}