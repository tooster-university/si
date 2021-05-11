package me.tooster.w3

import me.tooster.common.Nonogram
import me.tooster.common.opts
import me.tooster.w2.RegularNonogramV2
import java.io.File


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
