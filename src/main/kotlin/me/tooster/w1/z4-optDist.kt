@file:JvmName("Z4")

package me.tooster.w1

// O(|w|)
fun optDist(word: String, D: Int): Int {
    assert(D <= word.length)
    val prefixSums = word.scan(0) { acc, c -> acc + if (c == '1') 1 else 0 }.drop(1)
    val total = prefixSums.last()
    return (0 until word.length - D).map {
        val segmentLit = (prefixSums.getOrElse(it + D - 1) { 0 } - prefixSums.getOrElse(it - 1) { 0 })
        0 + (total - segmentLit) + (D - segmentLit)
    }.minOrNull()!!
}

fun main(args: Array<String>) {
    println(optDist("0010001000", 5)) // 3
    println(optDist("0010001000", 4)) // 4
    println(optDist("0010001000", 3)) // 3
    println(optDist("0010001000", 2)) // 2
    println(optDist("0010001000", 1)) // 1
    println(optDist("0010001000", 0)) // 2
}