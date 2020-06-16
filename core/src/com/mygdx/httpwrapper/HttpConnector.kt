package com.mygdx.httpwrapper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpRequestBuilder
import java.util.concurrent.ExecutorService
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.net.HttpParametersUtils
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.mygdx.dto.*
import com.mygdx.util.Logger
import com.mygdx.util.Utility
import kotlinx.coroutines.*
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

const val GET_PATH = "Game/Get"
const val JOIN_PATH = "Session/Join";
const val CREATE_BLOCK_PATH = "Game/CreateBlock"
const val SHOOT_PATH = "Game/Shoot"
val LOGGER = Logger("HttpConnector")

class HttpConnector(private val e: ExecutorService, private val url: String) : ServerConnector {
    companion object {
        // static for unit test
        fun parseGet(s: String): Optional<Pair<GameState, List<GameEvent>>> {
            try {
                val res = Utility.gson.fromJson(JsonParser.parseString(s), GetResponse::class.java)
                return Optional.of(res.state to res.events)
            } catch (e: IllegalStateException) {
                LOGGER.error("invalid response format from server\n$s", e)
            } catch (e: JsonSyntaxException) {
                LOGGER.error("invalid response format from server\n$s", e)
            } catch (e: NullPointerException) {
                LOGGER.error("invalid response format from server\n$s", e)
            }
            return Optional.empty()
        }
    }

    @Volatile
    private var close = false

    private lateinit var lastGameState: GameState
    private var notSendedEvents = ArrayList<GameEvent>()
    private val stateMutex = Object()

    private lateinit var token: String

    override suspend fun startListen(): String {
        val respString = sendPost(JOIN_PATH, "").await().resultAsString
        val resp = Utility.gson.fromJson(respString, JoinResponse::class.java)
        token = resp.token
        GlobalScope.launch {
            while (!close) {
                sendGet()
            }
        }
        return resp.playerId
    }

    private suspend fun sendGet() {
        val httpResp = send(HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.GET)
                .content(HttpParametersUtils.convertHttpParameters(
                        mapOf("idPlayer" to "4",
                                "Evil" to "Class")))
                .url("$url/$GET_PATH")
                .build())
                .await()

        if (httpResp.status.statusCode == 200) {
            parseGet(httpResp.resultAsString).ifPresent { (gs, events) ->
                synchronized(stateMutex) {
                    lastGameState = gs
                    events.forEach { event ->
                        notSendedEvents.add(event)
                    }
                }
            }
        } else {
            LOGGER.error("Bad http code" + httpResp.status.statusCode)
        }
    }

    override suspend fun createBlock(position: Point, blockId: String, shapeId: String) {
        sendPost(CREATE_BLOCK_PATH, Utility.gson.toJson(CreateBlockParams(token, position, blockId, shapeId))).await()
    }

    override suspend fun shoot(motionDirection: Point, projectileId: String) {
        sendPost(SHOOT_PATH, Utility.gson.toJson(ShootParams(token, projectileId, motionDirection))).await()
    }

    override fun getState(): Pair<GameState, List<GameEvent>> {
        synchronized(stateMutex) {
            val mem = notSendedEvents
            notSendedEvents = ArrayList()
            return lastGameState to mem
        }
    }

    override fun dispose() {
        close = true
    }

    private fun sendPost(name: String, json: String) = send(HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .content(json)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .url("$url/$name")
            .build())

    private fun send(req: Net.HttpRequest): Deferred<Net.HttpResponse> {
        val res = CompletableDeferred<Net.HttpResponse>()
        try {
            Gdx.net.sendHttpRequest(req,
                    object : HttpResponseListener {
                        override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                            res.complete(httpResponse)
                        }

                        override fun failed(t: Throwable) {
                            LOGGER.error("something went wrong", t)
                            res.completeExceptionally(t)
                        }

                        override fun cancelled() {
                            LOGGER.error("wtf cancel")
                            res.completeExceptionally(RuntimeException("Http request was canceled"))
                        }
                    })
        } catch (t: Throwable) {
            LOGGER.error("Can't send http request", t)
            res.completeExceptionally(t)
        }
        return res
    }
}
