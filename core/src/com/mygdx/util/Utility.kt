package com.mygdx.util

import com.google.gson.*
import com.mygdx.dto.Block
import com.mygdx.dto.GameEvent
import com.mygdx.dto.PhysicalBlock
import com.mygdx.dto.StupidGameEvent
import java.lang.reflect.Type

object Utility {
    // TODO: add smart parse
    val gson = GsonBuilder()
            .registerTypeAdapter(Block::class.java, object : JsonDeserializer<Block> {
                override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Block {
                    return context.deserialize(json, PhysicalBlock::class.java)
                }
            })
            .registerTypeAdapter(GameEvent::class.java, object : JsonDeserializer<GameEvent> {
                override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GameEvent {
                    return context.deserialize(json, StupidGameEvent::class.java)
                }
            })
            // TODO: parser for Projectile.
            .create()
}
