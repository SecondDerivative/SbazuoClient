package com.mygdx.httpwrapper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.HttpRequestBuilder
import java.util.concurrent.ExecutorService
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.net.HttpParametersUtils
import com.mygdx.util.Logger


class HttpConnector(private val e: ExecutorService, private val url: String) : ServerConnector {
    companion object {
        private const val GET_PATH = "api/get"
        private const val DODO_PATH = "api/dodo"
        private val LOGGER = Logger("ServerConnector")
    }

    private @Volatile var close = false

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
                // TODO: handle
            } else {
                LOGGER.error("Bad http code" + resp.status.statusCode)
            }
            if (!close) {
                startListen()
            }
        }
    }

    override fun dodo(info: DodoInfo) {
        val s = "{\"a\":${info.a}}"
        sendPost(DODO_PATH, s){ resp -> }
    }

    override fun getState() : Pair<GameState, List<GameEvent>> {
        TODO("todo")
    }

    override fun dispose() {
        close = true
    }

    private fun sendPost(name : String, json : String, handler: (Net.HttpResponse) -> Unit) {
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
                println(t.message)
                t.printStackTrace()
            }
        }
    }
}
