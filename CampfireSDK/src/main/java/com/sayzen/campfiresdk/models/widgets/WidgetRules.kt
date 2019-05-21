package com.sayzen.campfiresdk.models.widgets

import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsThreads

class WidgetRules(titileText:Int, rules: Array<Int>) : Widget(R.layout.widget_rules) {

    private val maxTime = 10

    private val vText: TextView = findViewById(R.id.vText)
    private val vCancel: TextView = findViewById(R.id.vCancel)
    private val vAccept: TextView = findViewById(R.id.vAccept)
    private val vCount: TextView = findViewById(R.id.vCount)
    private val texts = Array(rules.size + 1){
        if(it==0) titileText
        else rules[it-1]
    }

    private var onFinish: (() -> Unit)? = null
    private var index = -1
    private var time = 0

    init {
        setCancelable(false)
        vAccept.setOnClickListener { next() }
        vCancel.setOnClickListener { hide() }
        next()
    }

    fun next() {
        index++
        if (index >= texts.size) {
            if (onFinish != null) onFinish!!.invoke()
            hide()
            return
        }

        if(vText.text.isEmpty()) vText.text = ToolsResources.s(texts[index])
        else ToolsView.setTextAnimate(vText, ToolsResources.s(texts[index]))

        time = maxTime
        vAccept.isEnabled = false
        vAccept.text = "" + time
        vCount.text = "" + index + "/" + (texts.size-1)
        ToolsThreads.timerMain(1000, 1000L * maxTime, {
            vAccept.text = "" + (time--)
        }, {
            vAccept.text = ToolsResources.s(R.string.app_accept)
            vAccept.isEnabled = true
        })

    }

    fun onFinish(onFinish: (() -> Unit)): WidgetRules {
        this.onFinish = onFinish
        return this
    }

}