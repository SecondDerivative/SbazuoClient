package com.mygdx.httpwrapper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpRequestBuilder
import java.util.concurrent.ExecutorService
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.net.HttpParametersUtils
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.mygdx.util.Logger
import com.mygdx.util.Utility
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

class HttpConnector(private val e: ExecutorService, private val url: String) : ServerConnector {
    companion object {
        private const val GET_PATH = "api/get"
        private const val DODO_PATH = "api/dodo"
        private val LOGGER = Logger("ServerConnector")
        private val EVENT_TO_CLASS = mapOf<String, Class<out GameEvent>>(
                "stupid" to StupidGameEvent::class.java)

        // static for unit test
        fun parseGet(s: String): Optional<Pair<GameState, List<GameEvent>>> {
            try {
                val json = JsonParser.parseString(s).asJsonObject
                val gs = Utility.gson.fromJson(json["state"], GameState::class.java)

                val events = json["events"].asJsonArray
                val parsedEvents = ArrayList<GameEvent>()
                events.forEach { event ->
                    val eventName = event.asJsonObject["name"].asString
                    parsedEvents.add(Utility.gson.fromJson(event, EVENT_TO_CLASS[eventName]))
                }
                return Optional.of(gs to parsedEvents)
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

    override fun startListen() {
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

    override fun dodo(info: DodoInfo) {
        sendPost(DODO_PATH, Utility.gson.toJson(info)) { resp -> }
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
