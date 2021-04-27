package me.tooster.w3

import me.tooster.common.Direction
import me.tooster.common.TreePath
import me.tooster.common.debugLog
import me.tooster.common.opts
import me.tooster.w2.CommandoMap
import me.tooster.w2.aStar
import me.tooster.w2.bfsSolve
import me.tooster.w2.decreaseUncertaintyGreedy
import java.io.File
import java.util.*


@Suppress("DuplicatedCode")
fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad_output.txt" }

    File(outputPath).printWriter().use { output ->
        val lines = File(inputPath).readLines()
//        output.println(s.path.toMoves())
    }
}
