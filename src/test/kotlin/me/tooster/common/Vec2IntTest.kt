package me.tooster.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Vec2IntTest {

    @Test
    fun encodeDecodeTest() {
        val cases = listOf(
            5 to 13,
            -5 to 13,
            5 to -13,
            -5 to -13,
            0 to -13,
            -13 to 0,
            0 to 13,
            1 to 0,
            -1 to Int.MAX_VALUE,
            -1 to Int.MIN_VALUE,
            Integer.MAX_VALUE to Integer.MIN_VALUE,
            Integer.MIN_VALUE to Integer.MAX_VALUE,
            ).map { it.toVec2Int() }

        cases.forEach {
            assertEquals(it, Vec2Int.decode(it.encode()))
        }
    }
}