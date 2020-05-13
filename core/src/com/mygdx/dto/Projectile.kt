package com.mygdx.dto

data class Projectile(
        val projectileId: String,
        val shape: Circle,
        val motionVector: Point,
        val ownerId: String,
        val health: Int,
        val maxHealth: Int
)
