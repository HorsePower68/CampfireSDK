package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.models.units.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGet
import com.dzen.campfire.api.requests.post.RPostPagePollingVote
import com.sayzen.campfiresdk.models.events.units.EventPollingChanged
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

object ControllerPolling {

    private val results = HashMap<Long, Result>()

    fun clear() {
        results.clear()
    }

    fun get(pollingId: Long, callback: (Result) -> Unit) {
        val result = results[pollingId]
        if (result != null) {
            callback.invoke(result)
            if (result.dataCreate < System.currentTimeMillis() - 1000L * 30) load(pollingId)
        } else {
            load(pollingId, callback)
        }
    }

    fun reload(pollingId: Long) {
        results.remove(pollingId)
    }

    private fun load(pollingId: Long, callback: (Result) -> Unit = {}, tryCount: Int = 3) {
        RPostPagePollingGet(pollingId)
                .onComplete { r ->
                    val result = Result(r.results)
                    results[pollingId] = result
                    callback.invoke(result)
                }
                .onError {
                    if (tryCount > -1) ToolsThreads.main(1000) { load(pollingId, callback, tryCount - 1) }
                }
                .send(api)
    }

    fun vote(pollingId: Long, itemId: Long) {
        get(pollingId) { result ->
            ApiRequestsSupporter.executeProgressDialog(RPostPagePollingVote(pollingId, itemId)) { _ ->

                result.totalVotes++
                result.myVoteItemId = itemId
                result.voted = true

                var updated = false
                for (item in result.results) {
                    if (item.itemId == itemId) {
                        item.count++
                        item.myVote = true
                        updated = true
                        break
                    }
                }

                if (!updated) {
                    result.results = Array(result.results.size + 1) {
                        if (it < result.results.size) result.results[it]
                        else {
                            val item = PagePolling.Result()
                            item.itemId = itemId
                            item.myVote = true
                            item.count = 1
                            item
                        }
                    }
                }

                EventBus.post(EventPollingChanged(pollingId))
            }
                    .onApiError(RPostPagePollingVote.E_ALREADY) {
                        reload(pollingId)
                        EventBus.post(EventPollingChanged(pollingId))
                    }
        }
    }

    class Result(var results: Array<PagePolling.Result>) {

        var voted = false
        var myVoteItemId = -1L
        var totalVotes = 0L
        var dataCreate = System.currentTimeMillis()

        init {
            totalVotes = 0L
            voted = false

            for (i in 0 until results.size) {
                totalVotes += results[i].count
                voted = voted || results[i].myVote
                if (results[i].myVote) myVoteItemId = results[i].itemId
            }
        }

        fun count(itemId: Long): Long {
            for (i in 0 until results.size)
                if (results[i].itemId == itemId) return results[i].count
            return 0L
        }

    }


}