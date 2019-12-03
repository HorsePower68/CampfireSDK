package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.appodeal.ads.NativeAd
import com.appodeal.ads.NativeAdView
import com.appodeal.ads.NativeIconView
import com.appodeal.ads.NativeMediaView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerAppodeal
import com.sup.dev.android.views.cards.Card

class CardAd(
        val nativeAd: NativeAd
) : Card(R.layout.screen_fandom_card_ad) {

    override fun bindView(view: View) {
        super.bindView(view)

        if (ControllerAppodeal.getNative() == nativeAd) ControllerAppodeal.reCashNative()

        val nativeAdView: NativeAdView = view.findViewById(R.id.vNativeAdView)
        val tvTitle:TextView = nativeAdView.findViewById(R.id.vTitle)
        val tvDescription:TextView = nativeAdView.findViewById(R.id.vBody)
        val ctaButton:Button = nativeAdView.findViewById(R.id.vAction)
        val providerViewContainer: FrameLayout = nativeAdView.findViewById(R.id.vAdvertiser)
        val nativeIconView:NativeIconView = nativeAdView.findViewById(R.id.vIcon)
        val nativeMediaView:NativeMediaView = nativeAdView.findViewById(R.id.vMediaView)

        nativeAdView.unregisterViewForInteraction()
        nativeAdView.titleView = tvTitle
        nativeAdView.descriptionView = tvDescription
        nativeAdView.callToActionView = ctaButton
        nativeAdView.nativeMediaView = nativeMediaView


        nativeAdView.setNativeIconView(nativeIconView)
        nativeAdView.registerView(nativeAd)

        tvTitle.text = nativeAd.title
        tvDescription.text = nativeAd.description
        ctaButton.text = nativeAd.callToAction

        val providerView:View? = nativeAd.getProviderView(view.context)
        if (providerView != null) {
            if (providerView.parent != null && providerView.parent is ViewGroup) {
                (providerView.parent as ViewGroup).removeView(providerView)
            }
            providerViewContainer.addView(providerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            nativeAdView.providerView = providerView
        }

    }

}