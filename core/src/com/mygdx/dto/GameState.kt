package com.mygdx.dto

data class GameState(
        val conditionId: Int,
        val currentPlayerId: String,
        val blocks: List<Block>,
        val projectiles: List<Projectile>,
        val rules: List<Rule>
)
