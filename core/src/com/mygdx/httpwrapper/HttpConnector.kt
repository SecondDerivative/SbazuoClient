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
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

const val GET_PATH = "api/get"
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

    override fun startListen(): CompletableFuture<String> {
        TODO("join and get token/playedId")
        send(HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.GET)
                .content(HttpParametersUtils.convertHttpParameters(
                        mapOf("idPlayer" to "4",
                                "Evil" to "Class")))
                .url("$url/$GET_PATH")
                .build()
        ) { resp ->
            if (resp.status.statusCode == 200) {
                parseGet(resp.resultAsString).ifPresent { (gs, events) ->
                    synchronized(stateMutex) {
                        lastGameState = gs
                        events.forEach { event ->
                            notSendedEvents.add(event)
                        }
                    }
                }
            } else {
                LOGGER.error("Bad http code" + resp.status.statusCode)
            }
            if (!close) {
                startListen()
            }
        }
    }

    override fun createBlock(position: Point, blockId: String, shapeId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shoot(motionDirection: Point, projectileId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun sendPost(name: String, json: String, handler: (Net.HttpResponse) -> Unit) {
        send(HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.POST)
                .content(json)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .url("$url/$name")
                .build(), handler)
    }

    private fun send(req: Net.HttpRequest, handler: (Net.HttpResponse) -> Unit) {
        e.submit {
            try {
                Gdx.net.sendHttpRequest(req,
                        object : HttpResponseListener {
                            override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                                handler(httpResponse)
                            }

                            override fun failed(t: Throwable) {
                                LOGGER.error("something went wrong", t)
                            }

                            override fun cancelled() {
                                LOGGER.error("wtf cancel")
                            }
                        })
            } catch (t: Throwable) {
                LOGGER.error("Can't send http request", t)
            }
        }
    }
}
