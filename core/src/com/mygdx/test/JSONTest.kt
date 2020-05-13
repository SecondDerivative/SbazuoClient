package com.mygdx.test

import com.mygdx.httpwrapper.*
import com.mygdx.util.Utility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.*

class JSONTest {
    @Test
    fun simpleJSONTest() {
        val a = DodoInfo(2)
        assertThat(Utility.gson.fromJson(Utility.gson.toJson(a), DodoInfo::class.java),
                equalTo(DodoInfo(2)))
    }

    @Test
    fun testParseGetSuccess() {
        val s = "{" +
                "   \"state\": {" +
                "       \"field\": 2" +
                "   }, " +
                "   \"events\": [" +
                "       {\"name\": \"stupid\"}," +
                "       {\"name\": \"stupid\"}" +
                "   ]" +
                "}"
        val result = HttpConnector.parseGet(s)
        assertThat(result, equalTo(Optional.of(
                GameState(2) to listOf(StupidGameEvent("stupid"), StupidGameEvent("stupid")) as List<GameEvent>)))
    }

    @Test
    fun testParseGetFailed() {
        val s = "{" +
                "   \"state\": {" +
                "       \"field\": 2" +
                "   }, " +
                "   \"events\": \"Isn't array\"" +
                "}"
        val result = HttpConnector.parseGet(s)
        assertThat(result, equalTo(Optional.empty()))
    }
}