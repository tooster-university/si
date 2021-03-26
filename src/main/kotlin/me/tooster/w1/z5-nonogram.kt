@file:JvmName("Z5")
@file:Suppress("DuplicatedCode")

package me.tooster.w1

import me.tooster.common.Nonogram
import me.tooster.common.opts
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

class SimpleNonogram(rows: List<Int>, cols: List<Int>) : Nonogram<Int>(rows, cols){

    override fun List<Boolean>.flipsLeft(D: Int): Int {
        val prefixSums = scan(0) { acc, c -> acc + if (c) 1 else 0 }.drop(1)
        val total = prefixSums.last()
        return (0..size - D).map {
            val segmentLit = (prefixSums.getOrElse(it + D - 1) { 0 } - prefixSums.getOrElse(it - 1) { 0 })
            0 + (total - segmentLit) + (D - segmentLit)
        }.minOrNull()!!
    }
}

private fun List<Boolean>.toRowString() = this.map { if (it) '#' else '.' }.joinToString("")
private fun String.fromRowString() = this.map { it == '#' }.toList()

fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad5_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad5_output.txt" }


    Scanner(FileReader(inputPath)).use { input ->
        File(outputPath).printWriter().use { output ->

            val (r, c) = (input.nextInt() to input.nextInt())
            val nonogram = SimpleNonogram(
                (1..r).map { input.nextInt() }.toList(),
                (1..c).map { input.nextInt() }.toList()
            )

            val picture = nonogram.trySolve()
            output.println(picture.map { it.toRowString() }.joinToString("\n"))
        }
    }
}
