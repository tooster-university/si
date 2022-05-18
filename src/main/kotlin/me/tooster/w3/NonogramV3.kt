@file:JvmName("Z1")
@file:Suppress("DuplicatedCode")

package me.tooster.w3

import me.tooster.common.cartesianProduct
import me.tooster.common.opts
import me.tooster.common.AXIS.COLS
import me.tooster.common.AXIS.ROWS
import me.tooster.w3.RegularNonogramV2.Desc
import java.io.File
import java.io.FileReader
import java.lang.Integer.max
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.system.measureTimeMillis

typealias RowT = List<Boolean>

/**
 * Options:
 *  - debugPreprocess - to show preprocessing table
 *  - debugRepeat     - to show restarting with randomized picture
 *  - reps:n          - to specify number of re-rolls
 *  - pixelFixes:n    - to specify maximum number of pixel fixes
 *  - p:f             - to specify probability of success
 */
class RegularNonogramV2(val rows: List<Desc>, val cols: List<Desc>) {

    data class Desc(val length: Int, val blocks: List<Int>)

    private val picture = EnumMap(mapOf(
        ROWS to MutableList(rows.size) { 0 },
        COLS to MutableList(cols.size) { 0 }
    ))

    private val descCandidates = HashMap<Desc, MutableList<Int>>()

    init {
        val preprocessing = measureTimeMillis {
            assignCandidates()
        }
        System.err.println("{b--preprocessing took $preprocessing ns. Max cand. size =${
            descCandidates.values.maxOf { it.size }
        }--}")

        if (opts.containsKey("debugPreprocess"))
            println(descCandidates.entries.map { (k, vs) ->
                k.toString() + "\n" + vs.map { "   " + it.toViewString() }.joinToString("\n")
            }.joinToString("\n"))
    }

    private fun assignCandidates() {
        for (desc in rows + cols)
            descCandidates[desc] = mutableListOf()

        val blockToLen = (1..max(rows.size, cols.size)).associateBy { (1 shl it) - 1 }
        for (mask in 0 until (1 shl max(rows.size, cols.size))) { // approx 32k for len. 15
            val desc = mutableListOf<Int>()
            var m = mask
            do {
                m = m ushr m.countTrailingZeroBits()
                desc += (m.inv()).countTrailingZeroBits()
                m = m ushr desc.last()
            } while (m > 0)
            val (d1, d2) = Desc(rows.size, desc) to Desc(cols.size, desc)
            if (descCandidates.containsKey(d1))
                descCandidates[d1]!! += mask
            if (descCandidates.containsKey(d2) && rows.size != cols.size)
                descCandidates[d2]!! += mask
        }
    }

    private fun flipPixel(row: Int, col: Int) {
        picture[ROWS]!![row] = picture[ROWS]!![row] xor (1 shl col)
        picture[COLS]!![col] = picture[COLS]!![col] xor (1 shl row)
    }

    private infix fun Int.hamming(other: Int) = Integer.bitCount(this xor other)

    private fun Int.flipsLeft(D: Desc): Int = descCandidates[D]!!.minOf { c -> this hamming c }

    private fun randomize() {
        for ((r, c) in rows.indices cartesianProduct cols.indices)
            if (Random.nextDouble() < 0.5) flipPixel(r, c)
    }

    private fun isSolved() = rows.indices.all { picture[ROWS]!![it].flipsLeft(rows[it]) == 0 } &&
                             cols.indices.all { picture[COLS]!![it].flipsLeft(cols[it]) == 0 }

    private fun tryFixPixel(p: Double = 0.05) {

        val rowsByMatching = rows.indices.groupBy { picture[ROWS]!![it].flipsLeft(rows[it]) == 0 }
        val colsByMatching = cols.indices.groupBy { picture[COLS]!![it].flipsLeft(cols[it]) == 0 }

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
            val (mainAxis, crossAxis) = if (isRow) (ROWS to COLS) else (COLS to ROWS)
            val (mainDesc, crossDesc) = if (isRow) (rows to cols) else (cols to rows)
            val flip = { main: Int, cross: Int -> if (isRow) flipPixel(main, cross) else flipPixel(cross, main) }

            // find best pixel to fix
            val bestCross = picture[crossAxis]!!.indices.minByOrNull { crossIdx ->
                flip(mainIdx, crossIdx)
                // sum number of pixels left to flip
                val match = picture[mainAxis]!![mainIdx].flipsLeft(mainDesc[mainIdx]) +
                            picture[crossAxis]!![crossIdx].flipsLeft(crossDesc[crossIdx])
                flip(mainIdx, crossIdx)
                match
            }!!
            // flip best pixel
            flip(mainIdx, bestCross)
        }
    }

    fun trySolve(
        restarts: Int = Integer.parseInt(opts.getOrDefault("reps", "1000")),
        maxPixelFlips: Int = Integer.parseInt(opts.getOrDefault("pixelFixes", "1000")),
        p: Double = (opts.getOrDefault("p", "0.05").toDouble()),
    ):
            RegularNonogramV2 {
        repeat(restarts) {
            randomize()
            repeat(maxPixelFlips) {
                if (isSolved()) return this
                else tryFixPixel(p)
            }
            if (opts.containsKey("debugRepeat"))
                System.err.println("Restarting after:\n$this")
        }
        return this
    }

    fun Int.toViewString(): String = this.toString(2).padStart(cols.size, '0').reversed()
        .replace('0', '.').replace('1', '#')

    override fun toString(): String = picture[ROWS]!!.joinToString("\n") { it.toViewString() }
}


fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad_output.txt" }


    Scanner(FileReader(inputPath)).use { input ->
        File(outputPath).printWriter().use { output ->

            val wsRegex = Regex("""\s+""")

            val (r, c) = input.nextLine().split(wsRegex).map { Integer.parseInt(it) }
            val nonogram = RegularNonogramV2(
                (1..r).map { Desc(c, input.nextLine().split(wsRegex).map { Integer.parseInt(it) }) }.toList(),
                (1..c).map { Desc(r, input.nextLine().split(wsRegex).map { Integer.parseInt(it) }) }.toList(),
            )

            val picture = nonogram.trySolve(maxPixelFlips = 50000, p = 0.01)
            output.println(picture)
        }
    }
}