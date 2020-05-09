package com.mygdx.httpwrapper

interface ServerConnector {
    fun startListen()
    fun dodo(info: DodoInfo)
    fun getState() : Pair<GameState, List<GameEvent>>
    fun dispose()
}
