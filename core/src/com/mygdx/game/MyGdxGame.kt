package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.httpwrapper.HttpConnector
import com.mygdx.httpwrapper.ServerConnector
import java.util.concurrent.Executors

class MyGdxGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var img: Texture
    private val e = Executors.newFixedThreadPool(1)
    private val connector : ServerConnector = HttpConnector(e, "127.0.0.1")

    override fun create() {

        connector.startListen() // TODO: mvp only
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
        connector.dispose()
        e.shutdown()
    }
}
