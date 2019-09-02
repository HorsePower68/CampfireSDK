package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.models.animations.DrawAnimationAutumn
import com.sayzen.campfiresdk.models.animations.DrawAnimationSummer
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.android.views.views.draw_animations.DrawAnimationExplosion
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

object ControllerScreenAnimations {

    private var key = 0L

    fun fireworks() {
        clearAnimation()
        val myKey = System.currentTimeMillis()
        key = myKey

        val cw = ToolsAndroid.getScreenW() / 2f
        val ch = ToolsAndroid.getScreenH() / 2f
        val of = ToolsView.dpToPx(128)

        ToolsThreads.thread {
            ToolsThreads.sleep(500)
            if (key != myKey) return@thread
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 60, cw, ch, 2f))
            ToolsThreads.sleep(500)
            if (key != myKey) return@thread
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 40, cw - of, ch, 2f))
            addAnimation(DrawAnimationExplosion(ToolsView.dpToPx(64), ToolsView.dpToPx(6), 40, cw + of, ch, 2f))
            ToolsThreads.sleep(1000)
            for (i in 0..10) {
                if (key != myKey) return@thread
                val size = ToolsView.dpToPx(ToolsMath.randomInt(40, 80))
                val sizeParticle = ToolsView.dpToPx(ToolsMath.randomInt(4, 8))
                val count = ToolsMath.randomInt(20, 40)
                val time = ToolsMath.randomFloat(1f, 3f)
                val xx = ToolsMath.randomFloat(cw - of, cw + of)
                val yy = ToolsMath.randomFloat(ch - of, ch + of)
                addAnimation(DrawAnimationExplosion(size, sizeParticle, count, xx, yy, time))
                ToolsThreads.sleep(ToolsMath.randomLong(100, 300))
            }
        }

    }

    fun summer(){
        clearAnimation()
        val myKey = System.currentTimeMillis()
        key = myKey

        addAnimation(DrawAnimationSummer())
    }

    fun autumn(){
        clearAnimation()
        val myKey = System.currentTimeMillis()
        key = myKey

        addAnimation(DrawAnimationAutumn())
    }

    private fun clearAnimation() {
        key = 0
        ToolsThreads.main { SupAndroid.activity!!.vActivityDrawAnimations!!.clear() }
    }

    private fun addAnimation(animation: DrawAnimation) {
        ToolsThreads.main { SupAndroid.activity!!.vActivityDrawAnimations!!.addAnimation(animation) }
    }

}