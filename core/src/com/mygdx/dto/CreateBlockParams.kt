package com.mygdx.dto

data class CreateBlockParams(
        val token: String,
        val position: Point,
        val blockId: String,
        val shapeId: String
)
