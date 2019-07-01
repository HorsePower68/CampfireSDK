package com.sayzen.campfiresdk.screens.post.create.creator

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.PageText
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.ScreenProtected
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsText

class SCreatePageText(
        private val screen: SPostCreate,
        private val card: CardPage?,
        private val oldPage: PageText?
) : Screen(R.layout.screen_post_create_widget_text), ScreenProtected {

    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vTextTitle: ViewIcon = findViewById(R.id.vTextTitle)

    private var size = PageText.SIZE_0

    private val maxL: Int
        get() = if (size == PageText.SIZE_0) API.PAGE_TEXT_MAX_L else API.PAGE_TEXT_TITLE_MAX_L

    init {

        vField.setSingleLine(false)
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { s -> update() })

        vTextTitle.setOnClickListener { v ->
            if (vTextTitle.isIconSelected)
                onTextSizeClicked(PageText.SIZE_0)
            else
                onTextSizeClicked(PageText.SIZE_1)
        }


        if (oldPage != null) {
            vField.setText(this.oldPage.text)
            vField.setSelection(vField.text.length)
            onTextSizeClicked(this.oldPage.size)
        } else {
            onTextSizeClicked(size)
        }

        vFab.setOnClickListener { v -> onEnter() }
        update()
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    private fun onTextSizeClicked(size: Int) {
        this.size = size
        vTextTitle.isIconSelected = size == PageText.SIZE_1
        update()
    }

    private fun update() {
        val s = vField.text.toString()

        vField.textSize = if (s.length < 200) 22f  else 16f

        ToolsView.setFabEnabledR(vFab, !s.isEmpty() && s.length <= maxL, R.color.green_700)
    }

    private fun onEnter() {
        val page = PageText()
        page.text = vField.getText().toString().trim { it <= ' ' }
        page.size = size
        if(oldPage == null)
            screen.putPage(page, this, null, { page1 -> CardPageText(null, page1) }, null, true)
        else
            screen.changePage(page, card!!, this, null)
    }

    override fun onBackPressed(): Boolean {
        if (notChanged())
            return false
        else {
            WidgetAdd.showConfirmCancelDialog(this)
            return true
        }
    }

    override fun onProtectedClose(onClose: () -> Unit) {
        if (notChanged())
            onClose.invoke()
        else {
            WidgetAdd.showConfirmCancelDialog(this){
                onClose.invoke()
            }
        }
    }

    private fun notChanged(): Boolean {
        val s = vField.text.toString()
        return if (oldPage == null) {
            s.isEmpty()
        } else {
            ToolsText.equals(oldPage.text, s)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vFab.isEnabled = enabled
        vField.isEnabled = enabled
        vTextTitle.isEnabled = enabled
    }

}
