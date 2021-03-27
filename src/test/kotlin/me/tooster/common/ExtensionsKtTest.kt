package me.tooster.common

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ExtensionsKtTest {

    @Test
    fun cartesianProduct() {
        val A = listOf(1,2,3)
        val B = listOf(0, 10)
        val result = A cartesianProduct B
        assertEquals(
            listOf(1 to 0, 1 to 10, 2 to 0, 2 to 10, 3 to 0, 3 to 10)
            ,result)
    }
}