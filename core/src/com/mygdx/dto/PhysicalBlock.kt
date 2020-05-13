package com.mygdx.dto

data class PhysicalBlock(
        override val blockId: String,
        override val ownerId: String,
        override val shapeId: String,
        override val shapePos: Point,
        val health: Int,
        val maxHp: Int
) : Block
