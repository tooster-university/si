package me.tooster.common

import me.tooster.common.Nonogram.AXIS.COLS
import me.tooster.common.Nonogram.AXIS.ROWS
import kotlin.random.Random
import kotlin.random.nextInt

abstract class Nonogram<DescT>(val rows: List<DescT>, val cols: List<DescT>) {
    lateinit var picture: MutableList<MutableList<Boolean>>

    enum class AXIS { ROWS, COLS }

    fun nextRandomRow() = MutableList(cols.size) { Random.nextBoolean() }


    protected fun List<Boolean>.toViewString() = this.map { if (it) '#' else '.' }.joinToString("")
    protected fun String.parseView() = this.map { it == '#' }.toList()


    /**
     * Returns i-th row/column(specified by axis) of picture.
     */
    protected fun viewOf(axis: AXIS, idx: Int) = when (axis) {
        ROWS -> picture[idx]
        COLS -> picture.map { it[idx] }
    }

    override fun toString() = rows.indices.map { viewOf(ROWS, it).toViewString() }.joinToString("\n")

    private fun flipPixel(row: Int, col: Int) {
        assert(row in rows.indices && col in cols.indices)
        picture[row][col] = !picture[row][col]
    }

    abstract fun List<Boolean>.flipsLeft(D: DescT): Int

    private fun tryFixPixel(p: Double = 0.05) {
        val rowsByMatching = rows.indices.groupBy { viewOf(ROWS, it).flipsLeft(rows[it]) == 0 }
        val colsByMatching = cols.indices.groupBy { viewOf(COLS, it).flipsLeft(cols[it]) == 0 }

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

            val faultyMain = viewOf(mainAxis, mainIdx)
            // find best pixel to fix
            val bestCross = faultyMain.indices.minByOrNull { crossIdx ->
                flip(mainIdx, crossIdx)
                val match = viewOf(mainAxis, mainIdx).flipsLeft(mainDesc[mainIdx]) +
                            viewOf(crossAxis, crossIdx).flipsLeft(crossDesc[crossIdx])
                flip(mainIdx, crossIdx)
                match
            }!!
            // flip best pixel
            flip(mainIdx, bestCross)
        }

    }

    // suboptimal but whatevs
    private fun isSolved() = rows.indices.all { viewOf(ROWS, it).flipsLeft(rows[it]) == 0 } &&
                             cols.indices.all { viewOf(COLS, it).flipsLeft(cols[it]) == 0 }

    fun trySolve(restarts: Int = 1000, maxPixelFlips: Int = 1000, p: Double = 0.05): List<List<Boolean>> {
        repeat(restarts) {
            picture = MutableList(rows.size) { nextRandomRow() }
            repeat(maxPixelFlips) {
                if (isSolved()) return picture
                else tryFixPixel(p)
            }
            if(opts.containsKey("debugRepeat"))
                System.err.println("Restarting after:\n$this")
        }
        return picture
    }
}

