package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.MyGdxGame

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()
    LwjglApplication(MyGdxGame(), config)
}
