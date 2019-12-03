package com.sayzen.campfiresdk.controllers

import android.app.Activity
import com.appodeal.ads.*
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.debug.log


object ControllerAppodeal {

    private var video = false
    private var native = false
    private var interstitial = false

    fun init(activity: Activity, appId: String, video: Boolean, native: Boolean, interstitial: Boolean) {
        if (!video && !native && !interstitial) return

        this.video = video
        this.native = native
        this.interstitial = interstitial

        Appodeal.setAutoCache(Appodeal.NON_SKIPPABLE_VIDEO, false)
        Appodeal.disableLocationPermissionCheck()
        Appodeal.disableWriteExternalStoragePermissionCheck()
        Appodeal.setTesting(ToolsAndroid.isDebug())
        Appodeal.setNativeAdType(Native.NativeAdType.Auto)

        var adKeys = 0
        if (video) adKeys = adKeys or Appodeal.NON_SKIPPABLE_VIDEO
        if (native) adKeys = adKeys or Appodeal.NATIVE
        if (interstitial) adKeys = adKeys or Appodeal.INTERSTITIAL

        Appodeal.initialize(activity, appId, adKeys, false)

        if (video) initVideo()
        if (native) initNative()
        if (interstitial) initInterstitial()

    }

    //
    //  Video
    //

    private var onVideoFinished: () -> Unit = {}

    private fun initVideo() {
        if (!video) return
        Appodeal.setNonSkippableVideoCallbacks(object : NonSkippableVideoCallbacks {
            override fun onNonSkippableVideoLoaded(p0: Boolean) {
                info("ControllerAppodeal", "onNonSkippableVideoLoaded p0[$p0]")
            }

            override fun onNonSkippableVideoClosed(p0: Boolean) {
                info("ControllerAppodeal", "onNonSkippableVideoClosed p0[$p0]")
            }

            override fun onNonSkippableVideoShowFailed() {
                info("ControllerAppodeal", "onNonSkippableVideoShowFailed")
            }

            override fun onNonSkippableVideoExpired() {
                info("ControllerAppodeal", "onNonSkippableVideoExpired")
            }

            override fun onNonSkippableVideoFinished() {
                info("ControllerAppodeal", "onNonSkippableVideoFinished")
                onVideoFinished.invoke()
            }

            override fun onNonSkippableVideoShown() {
                info("ControllerAppodeal", "onNonSkippableVideoShown")
            }

            override fun onNonSkippableVideoFailedToLoad() {
                info("ControllerAppodeal", "onNonSkippableVideoFailedToLoad")
            }

        })
    }

    fun showVideo(onVideoFinished: () -> Unit) {
        if (!video) return
        info("ControllerAppodeal", "showVideo")
        this.onVideoFinished = onVideoFinished
        Appodeal.show(SupAndroid.activity!!, Appodeal.NON_SKIPPABLE_VIDEO)
    }

    fun isLoadedVideo(): Boolean {
        if (!video) return false
        return Appodeal.isLoaded(Appodeal.NON_SKIPPABLE_VIDEO)
    }

    fun cashVideo() {
        if (!video) return
        if (isLoadedVideo()) return
        info("ControllerAppodeal", "cashVideo")
        Appodeal.cache(SupAndroid.activity!!, Appodeal.NON_SKIPPABLE_VIDEO)
    }

    //
    //  Native
    //

    private fun initNative() {
        if (!native) return

        Appodeal.setNativeCallbacks(object : NativeCallbacks{
            override fun onNativeLoaded() {
                info("ControllerAppodeal", "onNativeLoaded")
            }

            override fun onNativeClicked(p0: NativeAd?) {
                info("ControllerAppodeal", "onNativeClicked p0[$p0]")
            }

            override fun onNativeFailedToLoad() {
                info("ControllerAppodeal", "onNativeFailedToLoad")
            }

            override fun onNativeShown(p0: NativeAd?) {
                info("ControllerAppodeal", "onNativeShown p0[$p0]")
            }

            override fun onNativeShowFailed(p0: NativeAd?) {
                info("ControllerAppodeal", "onNativeShowFailed p0[$p0]")
            }

            override fun onNativeExpired() {
                info("ControllerAppodeal", "onNativeExpired")
            }

        })
    }

    fun isLoadedNative(): Boolean {
        if (!native) return false
        return Appodeal.getAvailableNativeAdsCount() > 0
    }

    fun reCashNative() {
        if (!native) return
        info("ControllerAppodeal", "reCashNative")
        Appodeal.cache(SupAndroid.activity!!, Appodeal.NATIVE)
    }

    fun cashNative() {
        if (!native) return
        if (isLoadedNative()) return
        info("ControllerAppodeal", "cashNative")
        Appodeal.cache(SupAndroid.activity!!, Appodeal.NATIVE)
    }

    fun getNative(): NativeAd? {
        if (!native) return null
        val nativeAds = Appodeal.getNativeAds(1)
        if(nativeAds.isEmpty()) return null
        return nativeAds[0]
    }

    //
    //  Interstitial
    //

    private var lastInterstitialShow = System.currentTimeMillis()

    private fun initInterstitial() {
        if(!interstitial)return
        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialLoaded(isPrecache: Boolean) {
                info("ControllerAppodeal", "onInterstitialLoaded")
            }

            override fun onInterstitialFailedToLoad() {
                log("ControllerAppodeal", "onInterstitialFailedToLoad")
            }

            override fun onInterstitialShown() {
                log("ControllerAppodeal", "onInterstitialShown")
            }

            override fun onInterstitialShowFailed() {
                log("ControllerAppodeal", "onInterstitialShowFailed")
            }

            override fun onInterstitialClicked() {
                log("ControllerAppodeal", "onInterstitialClicked")
            }

            override fun onInterstitialClosed() {
                log("ControllerAppodeal", "onInterstitialClosed")
            }

            override fun onInterstitialExpired() {
                log("ControllerAppodeal", "onInterstitialExpired")
            }
        })
    }

    fun showInterstitial() {
        if(!interstitial)return
        if (System.currentTimeMillis() <= lastInterstitialShow + 1000 * 60 * 2) return

        if (Appodeal.show(SupAndroid.activity!!, Appodeal.INTERSTITIAL)) {
            lastInterstitialShow = System.currentTimeMillis()
        }
    }

    fun isLoadedInterstitial():Boolean {
        if(!interstitial)return false
        return Appodeal.isLoaded(Appodeal.INTERSTITIAL)
    }


}