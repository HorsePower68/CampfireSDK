package com.sayzen.campfiresdk.screens.other.offline

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.requests.project.RProjectVersionGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsThreads

class SOfflineScreen(
        val onConnection:()->Unit
) : Screen(R.layout.screen_offline){

    companion object{

        fun setScore(humans:Long, robots:Long){
            ToolsStorage.put("SOfflineScreen_humans", humans)
            ToolsStorage.put("SOfflineScreen_robots", robots)
        }

        fun getScoreHumans() = ToolsStorage.getLong("SOfflineScreen_humans", 0)
        fun getScoreRobots() = ToolsStorage.getLong("SOfflineScreen_robots", 0)
        fun getScoreAddHumans() = ToolsStorage.getLong("SOfflineScreen_add_humans", 0)
        fun getScoreAddRobots() = ToolsStorage.getLong("SOfflineScreen_add_robots", 0)

        fun clearAdd(){
            ToolsStorage.put("SOfflineScreen_add_humans", 0L)
            ToolsStorage.put("SOfflineScreen_add_robots", 0L)
        }

        fun addScoreAddHumans(){
            ToolsStorage.put("SOfflineScreen_add_humans", getScoreAddHumans() + 1)
            ToolsStorage.put("SOfflineScreen_humans", getScoreHumans() + 1)
        }
        fun addScoreAddRobots(){
            ToolsStorage.put("SOfflineScreen_add_robots", getScoreAddRobots() + 1)
            ToolsStorage.put("SOfflineScreen_robots", getScoreRobots() + 1)
        }

    }

    private val vMiniGame:ViewMiniGame = findViewById(R.id.vMiniGame)
    private val vScoreRobots:TextView = findViewById(R.id.vScoreRobots)
    private val vScoreHumans:TextView = findViewById(R.id.vScoreHumans)

    init {
        isNavigationVisible = false
        isNavigationAnimation = false
        isBackStackAllowed = false

        vScoreRobots.visibility = View.INVISIBLE
        vScoreHumans.visibility = View.INVISIBLE
        vScoreRobots.text = ToolsResources.s(R.string.into_mini_game_robots, getScoreRobots())
        vScoreHumans.text = ToolsResources.s(R.string.into_mini_game_humans, getScoreHumans())

        vMiniGame.scaleX = 0.20f
        vMiniGame.scaleY = 0.20f
        vMiniGame.onWinHuman = {
            addScoreAddHumans()
            vScoreHumans.text = ToolsResources.s(R.string.into_mini_game_humans, getScoreHumans())
        }
        vMiniGame.onWinRobot = {
            addScoreAddRobots()
            vScoreRobots.text = ToolsResources.s(R.string.into_mini_game_robots, getScoreRobots())
        }
        vMiniGame.onFocus = {
            ToolsThreads.timerMain(10, 1000, { s->
                var x = vMiniGame.scaleX + 0.02f
                if(x > 1)  x = 1f
                vMiniGame.scaleX = x
                vMiniGame.scaleY = x
            }, {
                ToolsView.fromAlpha(vScoreRobots)
                ToolsView.fromAlpha(vScoreHumans)
            })
        }

        request()
    }

    private fun request(){
        RProjectVersionGet()
                .onComplete{ onConnection.invoke() }
                .onError{ ToolsThreads.main(1000) { request() } }
                .send(api)
    }


}