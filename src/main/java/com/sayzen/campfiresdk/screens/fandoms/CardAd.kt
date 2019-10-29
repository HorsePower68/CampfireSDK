package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.sayzen.campfiresdk.R
import com.sayzen.devsupandroidgoogle.ControllerAdsNative
import com.sup.dev.android.views.cards.Card

class CardAd(
        val ad: UnifiedNativeAd
) : Card(R.layout.screen_fandom_card_ad) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vAdView: UnifiedNativeAdView = view.findViewById(R.id.vAdView)
        val vIcon: ImageView = view.findViewById(R.id.vIcon)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vAdvertiser: TextView = view.findViewById(R.id.vAdvertiser)
        val vBody: TextView = view.findViewById(R.id.vBody)
        val vMediaView: MediaView = view.findViewById(R.id.vMediaView)
        val vAction: Button = view.findViewById(R.id.vAction)

        vAdView.headlineView = vTitle
        vAdView.mediaView = vMediaView
        vAdView.iconView = vIcon
        vAdView.bodyView = vBody
        vAdView.callToActionView = vAction
        vAdView.advertiserView = vAdvertiser

        vAdView.setNativeAd(ad)
        vMediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)

        vIcon.visibility = View.VISIBLE
        if (ad.icon != null && ad.icon.drawable != null) vIcon.setImageDrawable(ad.icon.drawable)
        else if (ad.icon != null && ad.icon.uri != null) vIcon.setImageURI(ad.icon.uri)
        else vIcon.visibility = View.GONE

        if (ad.mediaContent != null) {
            vMediaView.setMediaContent(ad.mediaContent)
            vMediaView.visibility = View.VISIBLE
        } else {
            vMediaView.setMediaContent(null)
            vMediaView.visibility = View.GONE
        }
        vMediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)

        if (ad.body != null) {
            vBody.text = ad.body
            vBody.visibility = View.VISIBLE
        } else {
            vBody.text = ""
            vBody.visibility = View.GONE
        }

        if (ad.headline != null) {
            vTitle.text = ad.headline
            vTitle.visibility = View.VISIBLE
        } else {
            vTitle.text = ""
            vTitle.visibility = View.GONE
        }

        if (ad.advertiser != null) {
            vAdvertiser.text = ad.advertiser
            vAdvertiser.visibility = View.VISIBLE
        } else {
            vAdvertiser.text = ""
            vAdvertiser.visibility = View.GONE
        }

        if (ad.callToAction != null) {
            vAction.text = ad.callToAction
            vAction.visibility = View.VISIBLE
        } else {
            vAction.text = ""
            vAction.visibility = View.GONE
        }

        if (ControllerAdsNative.getAd() == ad) ControllerAdsNative.reload()
    }

}