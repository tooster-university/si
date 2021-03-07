@file:JvmName("Z4")

package me.tooster.w1

import java.io.File
import javax.print.attribute.IntegerSyntax

// O(|w|)
fun optDist(word: String, D: Int): Int {
    assert(D <= word.length)
    val prefixSums = word.scan(0) { acc, c -> acc + if (c == '1') 1 else 0 }.drop(1)
    val total = prefixSums.last()
    return (0..word.length - D).map {
        val segmentLit = (prefixSums.getOrElse(it + D - 1) { 0 } - prefixSums.getOrElse(it - 1) { 0 })
        0 + (total - segmentLit) + (D - segmentLit)
    }.minOrNull()!!
}

fun main(args: Array<String>) {
//    println(optDist("0000000001", 1)) // 0
//    println(optDist("0010001000", 5)) // 3
//    println(optDist("0010001000", 4)) // 4
//    println(optDist("0010001000", 3)) // 3
//    println(optDist("0010001000", 2)) // 2
//    println(optDist("0010001000", 1)) // 1
//    println(optDist("0010001000", 0)) // 2
    val opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad4_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad4_output.txt" }


    File(inputPath).bufferedReader().useLines { input ->
        File(outputPath).printWriter().use { output ->
            input.forEach {
                val l = it.split(" ")
                output.println(optDist(l[0], Integer.parseInt(l[1])))
            }
        }
    }
}