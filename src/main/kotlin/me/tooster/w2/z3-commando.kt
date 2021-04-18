@file:JvmName("Z3")

package me.tooster.w2

import me.tooster.common.AXIS
import me.tooster.common.BitMap2D
import me.tooster.common.Direction
import me.tooster.common.opts
import java.io.File
import java.util.*

class CommandoMap(val board: List<String>) {

    val rows: Int = board.size
    val cols: Int = board[0].length

    private val walls = BitMap2D(rows, cols)
    private val targets = BitMap2D(rows, cols)
    private val starts = BitMap2D(rows, cols)
    private val players: BitMap2D


    init {
        for ((r, s) in board.withIndex()) {
            for ((c, symbol) in s.withIndex()) {
                when (symbol) {
                    '#' -> walls[r, c] = true
                    'G' -> targets[r, c] = true
                    'S' -> starts[r, c] = true
                    'B' -> {
                        starts[r, c] = true
                        walls[r, c] = true
                    }
                }
            }
        }
        players = starts.clone()
    }

    companion object {
        val dirToCode = EnumMap<Direction, String> (mapOf(
            Direction.N to "U",
            Direction.E to "R",
            Direction.S to "D",
            Direction.W to "L"
        ))
    }

    /// returns moves as number of potential commando positions
    fun decreaseUncertainty(maxSteps: Int = opts.getOrDefault("randomSteps", "100").toInt() ): String{
        TODO()
    }

    val moveLog: MutableList<Direction> = mutableListOf()

    fun move(dir: Direction){
        moveLog.add(dir)
        fun doMove(axis: AXIS, idx: Int, delta: Int){
        }

        when (dir){
            Direction.N  -> TODO()
            Direction.E  -> TODO()
            Direction.S  -> TODO()
            Direction.W  -> TODO()
        }
    }

    fun solve() : String {
        TODO()
    }
}

@Suppress("DuplicatedCode")
fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad_output.txt" }


    File(outputPath).printWriter().use { output ->
        val mapString = File(inputPath).readLines();

        val commandoMap = CommandoMap(mapString)
        val sequence = commandoMap.solve()
    }
}
