package com.mygdx.util

import com.badlogic.gdx.Gdx

class Logger(private val className : String) {
    fun log(message : String) {
        Gdx.app.log(className, message)
    }

    fun log(message : String, t : Throwable) {
        Gdx.app.log(className, message, t)
    }

    fun error(message : String) {
        Gdx.app.error(className, message)
    }

    fun error(message : String, t : Throwable) {
        Gdx.app.error(className, message, t)
    }

    fun debug(message : String) {
        Gdx.app.debug(className, message)
    }

    fun debug(message : String, t : Throwable) {
        Gdx.app.debug(className, message, t)
    }
}
