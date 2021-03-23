@file:JvmName("Z5")
@file:Suppress("DuplicatedCode")

package me.tooster.w1

import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

private data class SimpleNonogram(val rows: List<Int>, val cols: List<Int>) {

    fun nextRandomRow() = MutableList(cols.size) { Random.nextBoolean() }

    lateinit var picture: MutableList<MutableList<Boolean>>


    fun List<Boolean>.flipsLeft(D: Int): Int {
        val prefixSums = scan(0) { acc, c -> acc + if (c) 1 else 0 }.drop(1)
        val total = prefixSums.last()
        return (0..size - D).map {
            val segmentLit = (prefixSums.getOrElse(it + D - 1) { 0 } - prefixSums.getOrElse(it - 1) { 0 })
            0 + (total - segmentLit) + (D - segmentLit)
        }.minOrNull()!!
    }


    fun rowView(idx: Int): List<Boolean> = picture[idx]
    fun colView(idx: Int): List<Boolean> = picture.map { it[idx] }

    fun flipPixel(row: Int, col: Int) {
        assert(row in rows.indices && col in cols.indices)
        picture[row][col] = !picture[row][col]
    }

    fun tryFixPixel(p: Double = 0.05) {
        val rowsByMatching = rows.indices.groupBy { rowView(it).flipsLeft(rows[it]) == 0 }
        val colsByMatching = cols.indices.groupBy { colView(it).flipsLeft(cols[it]) == 0 }

        // suboptimal pixel fix
        if (Random.nextDouble() < p) {
            // totally random flip
            flipPixel(Random.nextInt(rows.indices), Random.nextInt(cols.indices))
        } else { // optimal pixel fix - choose the one that maximizes fit
            // flip on random wrong column
            val faultyRows = rowsByMatching[false]?.map { it to true } ?: emptyList()
            val faultyCols = colsByMatching[false]?.map { it to false } ?: emptyList()

            val candidates = faultyRows + faultyCols
            val (mainIdx, isRow) = candidates.random()
            val (mainView, crossView) = if (isRow) (::rowView to ::colView) else (::colView to ::rowView)
            val (mainDesc, crossDesc) = if (isRow) (rows to cols) else (cols to rows)
            val flip = { main: Int, cross: Int -> if (isRow) flipPixel(main, cross) else flipPixel(cross, main) }

            val faultyMain = mainView(mainIdx)
            // find best pixel to fix
            val bestCross = faultyMain.indices.minByOrNull { crossIdx ->
                flip(mainIdx, crossIdx)
                val match = mainView(mainIdx).flipsLeft(mainDesc[mainIdx]) +
                            crossView(crossIdx).flipsLeft(crossDesc[crossIdx])
                flip(mainIdx, crossIdx)
                match
            }!!
            // flip best pixel
            flip(mainIdx, bestCross)
        }

    }

    // suboptimal but whatevs
    fun isSolved() = rows.indices.all { rowView(it).flipsLeft(rows[it]) == 0 } &&
                     cols.indices.all { colView(it).flipsLeft(cols[it]) == 0 }

    fun trySolve(): List<List<Boolean>> {
        repeat(1000) {
            picture = MutableList(rows.size) { nextRandomRow() }
            repeat(1000) {
                if (isSolved()) return picture
                else tryFixPixel()
            }
        }
        return picture
    }
}

private fun List<Boolean>.toRowString() = this.map { if (it) '#' else '.' }.joinToString("")
private fun String.fromRowString() = this.map { it == '#' }.toList()

fun main(args: Array<String>) {
    val opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

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
