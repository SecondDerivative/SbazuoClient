package com.mygdx.dto

data class GravityRule(
        override val ruleId: String,
        val direction: Point
) : Rule
