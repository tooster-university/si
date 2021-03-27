@file:JvmName("Z1")

package me.tooster.w2

import me.tooster.common.Nonogram
import me.tooster.common.opts
import me.tooster.w2.RegularNonogram.Desc
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

typealias RowT = List<Boolean>

class RegularNonogram(rows: List<Desc>, cols: List<Desc>) : Nonogram<Desc>(rows, cols) {

    data class Desc(val length: Int, val blocks: List<Int>)

    private val descCandidates = HashMap<Desc, List<RowT>>() // <length, description> -> <candidates>

    init {
        val preprocessing = measureTimeMillis {
            val descs = (rows + cols).distinct()
            descs.associateWithTo(descCandidates) { desc -> generateCandidates(desc) }
        }
        System.err.println("preprocessing took $preprocessing ns. Max cand. size =${
            descCandidates.values.maxOf { it.size }
        }")
    }


    // FIXME: generating all shifts is a lil bit trickier
    private fun generateCandidates(D: Desc): List<RowT> {
        val offsets = D.blocks.scan(0) { start, d -> start + d + 1 }.dropLast(1).toMutableList()
        val candidates = mutableListOf<RowT>()

        fun List<Int>.offsetToColoring(): MutableList<Boolean> {
            val colors = MutableList(D.length) { false }
            for ((offset, d) in this.zip(D.blocks)) (offset until offset + d).forEach { colors[it] = true }
            return colors
        }

        var hasShifted = true // flag to stop trying pushing segments back
        while (hasShifted) {
            hasShifted = false
            candidates.add(offsets.offsetToColoring())
            findPush@ for (idx in offsets.indices) { // try pushing first possible segment back - guarantees all
                // generated
                val end = if (idx == offsets.lastIndex) D.length else offsets[idx + 1] - 1
                if (offsets[idx] + D.blocks[idx] < end) {
                    ++offsets[idx]
                    hasShifted = true
                    break@findPush
                }
            }
        }
        return candidates
    }

    private fun RowT.hammingDistance(other: RowT): Int =
        this.zip(other) { a, b -> a != b }.count { it }

    override fun RowT.flipsLeft(D: Desc): Int {
        val candidates = descCandidates[D]
        return candidates!!.minOf { c -> this.hammingDistance(c) }
    }
}

private fun RowT.toRowString() = this.map { if (it) '#' else '.' }.joinToString("")
private fun String.fromRowString() = this.map { it == '#' }.toList()

fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad_output.txt" }


    Scanner(FileReader(inputPath)).use { input ->
        File(outputPath).printWriter().use { output ->

            val wsRegex = Regex("""\s+""")

            val (r, c) = input.nextLine().split(wsRegex).map { Integer.parseInt(it) }
            val nonogram = RegularNonogram(
                (1..r).map { Desc(c, input.nextLine().split(wsRegex).map { Integer.parseInt(it) }) }.toList(),
                (1..c).map { Desc(r, input.nextLine().split(wsRegex).map { Integer.parseInt(it) }) }.toList(),
            )

            val picture = nonogram.trySolve(maxPixelFlips = 50000, p = 0.01)
            output.println(picture.map { it.toRowString() }.joinToString("\n"))
        }
    }
}