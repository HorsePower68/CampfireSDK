package com.sayzen.campfiresdk.models.widgets

import android.widget.Button
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsReviewChange
import com.dzen.campfire.api.requests.fandoms.RFandomsReviewCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomReviewCreated
import com.sayzen.campfiresdk.tools.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus

class WidgetReview(
        val fandomId: Long,
        val languageId: Long,
        val changeReviewRate: Long?,
        val changeReviewText: String?,
        val onCreate: ()->Unit
) : Widget(R.layout.wiget_review) {

    private val vField: EditText = findViewById(R.id.vField)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vStar1: ViewIcon = findViewById(R.id.vStar1)
    private val vStar2: ViewIcon = findViewById(R.id.vStar2)
    private val vStar3: ViewIcon = findViewById(R.id.vStar3)
    private val vStar4: ViewIcon = findViewById(R.id.vStar4)
    private val vStar5: ViewIcon = findViewById(R.id.vStar5)

    private var rate = 0L

    init {
        vCancel.setOnClickListener { onCancel() }
        vEnter.setOnClickListener { sendReview() }

        vField.addTextChangedListener(TextWatcherChanged { update() })

        vStar1.setOnClickListener { setRate(1L) }
        vStar2.setOnClickListener { setRate(2L) }
        vStar3.setOnClickListener { setRate(3L) }
        vStar4.setOnClickListener { setRate(4L) }
        vStar5.setOnClickListener { setRate(5L) }

        if (changeReviewRate != null) {
            setRate(changeReviewRate)
            vField.setText(changeReviewText)
            vField.setSelection(changeReviewText!!.length)
            vEnter.setText(R.string.app_change)
        } else {
            setRate(0)
        }

        update()
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vField)
    }

    private fun onCancel() {
        if ((changeReviewRate == null && vField.text.isNotEmpty()) || (changeReviewRate != null && vField.text.toString() != changeReviewText)) {
            WidgetAlert()
                    .setText(R.string.review_close_text)
                    .setOnCancel(R.string.app_cancel)
                    .setOnEnter(R.string.app_close) { hide() }
                    .asSheetShow()
        } else {
            hide()
        }
    }

    private fun update() {
        if (changeReviewRate != null) {
            vEnter.isEnabled = isEnabled
                    && vField.text.isNotEmpty()
                    && rate > 0
                    && vField.text.length <= API.REVIEW_MAX_L
                    && (changeReviewText != vField.text.toString() || changeReviewRate != rate)
        } else {
            vEnter.isEnabled = isEnabled
                    && vField.text.isNotEmpty()
                    && rate > 0
                    && vField.text.length <= API.REVIEW_MAX_L
        }
    }

    private fun setRate(rate: Long) {
        this.rate = rate
        vStar1.setImageResource(ToolsResources.getDrawableAttrId(if (rate >= 1L) R.attr.ic_star_36dp else R.attr.ic_star_border_36dp))
        vStar2.setImageResource(ToolsResources.getDrawableAttrId(if (rate >= 2L) R.attr.ic_star_36dp else R.attr.ic_star_border_36dp))
        vStar3.setImageResource(ToolsResources.getDrawableAttrId(if (rate >= 3L) R.attr.ic_star_36dp else R.attr.ic_star_border_36dp))
        vStar4.setImageResource(ToolsResources.getDrawableAttrId(if (rate >= 4L) R.attr.ic_star_36dp else R.attr.ic_star_border_36dp))
        vStar5.setImageResource(ToolsResources.getDrawableAttrId(if (rate >= 5L) R.attr.ic_star_36dp else R.attr.ic_star_border_36dp))
        update()
    }

    override fun setEnabled(enabled: Boolean): Widget {
        vField.isEnabled = enabled
        vCancel.isEnabled = enabled
        vEnter.isEnabled = enabled
        vStar1.isEnabled = enabled
        vStar2.isEnabled = enabled
        vStar3.isEnabled = enabled
        vStar4.isEnabled = enabled
        vStar5.isEnabled = enabled
        return super.setEnabled(enabled)
    }

    private fun sendReview() {
        if (changeReviewRate == null) {
            ApiRequestsSupporter.executeEnabled(this, RFandomsReviewCreate(fandomId, languageId, vField.text.toString(), rate)) {
                onCreate.invoke()
                EventBus.post(EventFandomReviewCreated(fandomId, languageId, rate, vField.text.toString()))
                hide()
            }
        } else {
            ApiRequestsSupporter.executeEnabled(this, RFandomsReviewChange(fandomId, languageId, vField.text.toString(), rate)) {
                onCreate.invoke()
                EventBus.post(EventFandomReviewChanged(fandomId, languageId, changeReviewRate, rate, vField.text.toString()))
                hide()
            }
        }
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

}