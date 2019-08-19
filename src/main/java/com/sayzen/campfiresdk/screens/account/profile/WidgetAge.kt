package com.sayzen.campfiresdk.screens.account.profile

import android.widget.Button
import android.widget.EditText
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsMapper

class WidgetAge(
    private val currentAge:Long,
    private val onEnter: (WidgetAge, Long) -> Unit
) : Widget(R.layout.screen_account_widget_age){

    private val vField: EditText = findViewById(R.id.vField)
    private val vCancel:Button = findViewById(R.id.vCancel)
    private val vEnter:Button = findViewById(R.id.vEnter)

    init {

        vCancel.setOnClickListener { hide() }
        vField.setText(currentAge.toString())
        vField.setSelection(vField.text.length)
        vField.addTextChangedListener(TextWatcherChanged{ update() })

        vEnter.setOnClickListener { onEnter.invoke(this, vField.text.toString().toLong()) }
        update()
    }

    private fun update(){
        vEnter.isEnabled = vField.text.length < 4 && ToolsMapper.isLongCastable(vField.text) && vField.text.toString().toLong() != currentAge
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vField)
    }

}