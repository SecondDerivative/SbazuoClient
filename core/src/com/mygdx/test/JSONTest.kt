package com.mygdx.test

import com.mygdx.dto.*
import com.mygdx.util.Utility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test

class JSONTest {
    @Test
    fun simpleJSONTest() {
        val a = CreateBlockParams("123", Point(1.0, 2.0), "physical", "square")
        assertThat(Utility.gson.fromJson(Utility.gson.toJson(a), CreateBlockParams::class.java),
                equalTo(a))
    }

    private fun <T : Any> doTypedParseTest(value: T, typeToken: Class<in T>) {
        val json = Utility.gson.toJson(value);
        val newValue = Utility.gson.fromJson(json, typeToken);
        assertThat(newValue, instanceOf(value::class.java))
        assertThat(newValue as T, equalTo(value))
    }

    @Test
    fun testBlockParse() {
        doTypedParseTest(PhysicalBlock("id", "id", "shapeId", Point(0.0, 0.0), 1, 1),
                Block::class.java)
    }

    @Test
    fun testEventParse() {
        doTypedParseTest(StupidGameEvent("id"),
                GameEvent::class.java)
    }

    private data class ListEvents(
            val events: List<GameEvent>
    )

    @Test
    fun testListParse() {
        val id = "stupid"
        val json = "{\"events\": [{\"eventId\":\"$id\"}, {\"eventId\":\"$id\"}]}"
        val res = Utility.gson.fromJson(json, ListEvents::class.java)
        assertThat(res, equalTo(ListEvents(listOf(StupidGameEvent(id), StupidGameEvent(id)))))
    }
}
