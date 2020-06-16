package com.mygdx.httpwrapper

import com.mygdx.dto.GameEvent
import com.mygdx.dto.GameState
import com.mygdx.dto.Point

interface ServerConnector {
    // future - connection complete. result - playedId
    suspend fun startListen() : String
    // TODO: enums. need help
    suspend fun createBlock(position: Point, blockId: String, shapeId: String)
    suspend fun shoot(motionDirection: Point, projectileId : String)
    fun getState(): Pair<GameState, List<GameEvent>>
    fun dispose()
}
