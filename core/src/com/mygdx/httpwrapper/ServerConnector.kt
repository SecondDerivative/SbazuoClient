package com.mygdx.httpwrapper

import com.mygdx.dto.GameEvent
import com.mygdx.dto.GameState
import com.mygdx.dto.Point
import java.util.concurrent.CompletableFuture

interface ServerConnector {
    // future - connection complete. result - playedId
    fun startListen() : CompletableFuture<String>
    // TODO: enums. need help
    fun createBlock(position: Point, blockId: String, shapeId: String)
    fun shoot(motionDirection: Point, projectileId : String)
    fun getState(): Pair<GameState, List<GameEvent>>
    fun dispose()
}
