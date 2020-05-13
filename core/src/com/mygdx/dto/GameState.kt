package com.mygdx.dto

data class GameState(
        val conditionId: Int,
        val currentPlayerId: String,
        //TODO: change to array if problems with GSON
        val blocks: List<Block>,
        val projectiles: List<Projectile>,
        val rules: List<Rule>
)
