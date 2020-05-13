package com.mygdx.util

import com.badlogic.gdx.Gdx

// TODO: add logger for test mode??
class Logger(private val className : String) {
    fun log(message : String) {
        if (Gdx.app != null) {
            Gdx.app.log(className, message)
        }
    }

    fun log(message : String, t : Throwable) {
        if (Gdx.app != null) {
            Gdx.app.log(className, message, t)
        }
    }

    fun error(message : String) {
        if (Gdx.app != null) {
            Gdx.app.error(className, message)
        }
    }

    fun error(message : String, t : Throwable) {
        if (Gdx.app != null) {
            Gdx.app.error(className, message, t)
        }
    }

    fun debug(message : String) {
        if (Gdx.app != null) {
            Gdx.app.debug(className, message)
        }
    }

    fun debug(message : String, t : Throwable) {
        if (Gdx.app != null) {
            Gdx.app.debug(className, message, t)
        }
    }
}
