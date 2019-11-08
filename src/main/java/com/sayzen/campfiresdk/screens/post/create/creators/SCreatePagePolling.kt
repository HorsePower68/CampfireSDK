package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPagePolling
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.tools.ToolsText

import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

class SCreatePagePolling(
        private val requestPutPage:(page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page)->Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PagePolling?
) : Screen(R.layout.screen_post_create_widget_polling) {

    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vPageTitle: EditText = findViewById(R.id.vPgeTitle)
    private val vContainer: ViewGroup = findViewById(R.id.vContainer)
    private val vCreate: Button = findViewById(R.id.vCreate)
    private val vAdd: View = findViewById(R.id.vAdd)
    private val vLvl: SettingsField = findViewById(R.id.vLvl)
    private val vKarma: SettingsField = findViewById(R.id.vKarma)

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false

        vTitle.text = "${ToolsResources.s(R.string.app_naming)} (${ToolsResources.s(R.string.app_not_required)})"
        vPageTitle.addTextChangedListener(TextWatcherChanged { update() })
        if (oldPage != null) {
            vPageTitle.setText(oldPage.title)
            for (o in oldPage.options) addItem(o)
            if (!WidgetAlert.check("ALERT_POLLING_CHANGE"))
                ToolsThreads.main(100) {
                    WidgetAlert()
                            .setTopTitleText(R.string.app_attention)
                            .setCancelable(false)
                            .setTitleImageBackgroundRes(R.color.blue_700)
                            .setText(R.string.post_page_polling_change_alert)
                            .setChecker("ALERT_POLLING_CHANGE")
                            .setOnEnter(R.string.app_got_it)
                            .asSheetShow()
                }
            vCreate.setText(R.string.app_change)
            if(oldPage.minLevel > 0)vLvl.setText("${oldPage.minLevel/100}")
            if(oldPage.minKarma > 0)vKarma.setText("${oldPage.minKarma/100}")
        } else {
            addItem("")
            addItem("")
        }
        vLvl.addOnTextChanged { update() }
        vKarma.addOnTextChanged { update() }

        vLvl.setErrorChecker {
            try {
                if(it.isEmpty())return@setErrorChecker true
                if(!ToolsText.isOnly(it, ToolsText.NUMBERS_S))return@setErrorChecker false
                val x = it.toInt()
                return@setErrorChecker x >= 1
            }catch (e:Exception){
                return@setErrorChecker false
            }
        }

        vKarma.setErrorChecker {
            try {
                if(it.isEmpty())return@setErrorChecker true
                if(!ToolsText.isOnly(it, ToolsText.NUMBERS_S))return@setErrorChecker false
                val x = it.toInt()
                return@setErrorChecker x >= 0
            }catch (e:Exception){
                return@setErrorChecker false
            }
        }

        vAdd.setOnClickListener { addItem("") }
        vCreate.setOnClickListener { onEnter() }
        update()
    }

    override fun onResume() {
        super.onResume()
        ToolsThreads.main(500) { ToolsView.showKeyboard(vContainer.getChildAt(0)) }
    }

    override fun onBackPressed(): Boolean {
        onCancel()
        return true
    }

    private fun addItem(text: String) {
        val vItem: View = ToolsView.inflate(R.layout.screen_post_create_widget_polling_item)
        val vText: EditText = vItem.findViewById(R.id.vText)
        val vRemove: View = vItem.findViewById(R.id.vRemove)

        vText.setText(text)
        vText.addTextChangedListener(TextWatcherChanged {
            vText.error = if (vText.getText().length > API.PAGE_POLLING_OPTION_MAX_TEXT) " " else null
            update()
        })
        vText.requestFocus()
        vRemove.setOnClickListener {
            if (vText.getText().isNotEmpty()) {
                WidgetAlert()
                        .setText(R.string.post_page_polling_remove_confirm)
                        .setOnCancel(R.string.app_cancel)
                        .setOnEnter(R.string.app_remove) {
                            vContainer.removeView(vItem)
                            update()
                        }
                        .asSheetShow()
            } else {
                vContainer.removeView(vItem)
                update()
            }
        }
        vContainer.addView(vItem, vContainer.childCount)

        update()
    }

    private fun update() {
        vAdd.isEnabled = vContainer.childCount <= API.PAGE_POLLING_OPTION_MAX_COUNT

        val options = getOptions()

        var enabled = options.isNotEmpty()

        for (s in options)
            if (s.isEmpty() || s.length > API.PAGE_POLLING_OPTION_MAX_TEXT) {
                enabled = false
                break
            }

        vCreate.isEnabled = enabled && vPageTitle.text.length <= API.PAGE_POLLING_TITLE_MAX && !vLvl.isError() && !vKarma.isError()
    }

    private fun onEnter() {
        val page = PagePolling()
        page.title = vPageTitle.text.toString()
        page.options = getOptions()
        page.minLevel = if(vLvl.getText().isEmpty()) 0L else vLvl.getText().toLong() * 100
        page.minKarma = if(vKarma.getText().isEmpty()) 0L else vKarma.getText().toLong() * 100

        Navigator.back()
        val w = ToolsView.showProgressDialog()

        if(card == null)
            requestPutPage.invoke(page, this, w, { page1 -> CardPagePolling(null, page1 as PagePolling) }){}
        else
            requestChangePage.invoke(page, card, null, w){}
    }

    private fun onCancel() {
        if (notChanged()) Navigator.back()
        else WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val options = getOptions()

        return if (oldPage == null) {
            options.size == 2 && options[0].isEmpty() && options[1].isEmpty() && vPageTitle.text.isEmpty()
        } else {
            if (vPageTitle.text.toString() != oldPage.title) return false
            if (options.size != oldPage.options.size) return false
            for (i in 0 until options.size)
                if (options[i] != oldPage.options[i]) return false
            return true
        }
    }

    private fun getOptions() = Array(vContainer.childCount) {
        vContainer.getChildAt(it).findViewById<EditText>(R.id.vText).text.toString()
    }

}