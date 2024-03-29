@file:JvmName("Z3")

package me.tooster.w2

import me.tooster.common.*
import me.tooster.common.Direction.*
import me.tooster.w2.CommandoMap.Companion.ALL_MOVES
import java.io.File
import java.util.*
import kotlin.math.sign


/** tries to greedily decrease uncertainty */
fun CommandoMap.State.decreaseUncertaintyGreedy(
    maxSteps: Int = opts.getOrDefault("maxRandom", "20").toInt(),
    maxUncertainty: Int = opts.getOrDefault("maxGreedyUncertainty", "3").toInt(),
): CommandoMap.State {
    var s = this
    while (s.path.length < maxSteps) {
        if (s.uncertainty <= maxUncertainty) return s
        // make 2 steps, take step minimizing the uncertainty
        val (_, ds) = (ALL_MOVES cartesianProduct ALL_MOVES).map { (d1, d2) -> s.next(d1).next(d2) }
            .groupBy { it.uncertainty }.minByOrNull { it.key }!!
        s = ds.random()
    }
    return s
}

fun CommandoMap.State.bfsSolve(maxDepth: Int = opts.getOrDefault("maxDepth", "150").toInt())
        : CommandoMap.State {
    if (toReach == 0) return this
    val queue = LinkedList<CommandoMap.State>()
    val visited = HashSet<Int>()
    queue.add(this)
    visited.add(this.positions.hashCode())
    while (queue.isNotEmpty()) {
        val s = queue.remove()
        debugLog("{r--${s.uncertainty} (${s.path.length})--}")
        if (s.toReach == 0)
            return s
        if (s.path.length == maxDepth) {
            debugLog("{r--BFS - max depth $maxDepth reached. uncertainty: ${s.uncertainty}--}")
            return s
        }
        val bestState = ALL_MOVES.map { d -> s.next(d) }.onEach {
            if (it.positions.hashCode() !in visited) {
                queue.add(it)
                visited.add(it.positions.hashCode())
            }
        }.minByOrNull { it.uncertainty }!!

        if (bestState.uncertainty < s.uncertainty) {
            queue.clear()
            visited.clear()
            queue.add(bestState)
        }

    }
    return this
}

private fun CommandoMap.aStar(aStarDepth: Int = opts.getOrDefault("aStarDepth", "500").toInt())
        : CommandoMap.State {

    var s = getInitialState()
    val queue = PriorityQueue<Pair<CommandoMap.State, Double>> { (_, h1), (_, h2) -> sign(h1 - h2).toInt() }
    val visited = HashSet<Set<Vec2Int>>()

    queue.add(s to s.h1())
    visited.add(s.positions)

    while (queue.isNotEmpty()) {
        val (current, h) = queue.poll()
        s = current
        if (s.path.length == aStarDepth) {
            debugLog("{r*--A* reached max depth $aStarDepth with uncertainty ${s.uncertainty}, h:$h--}")
            return s
        }
        if (s.toReach == 0) return s
        for (dir in ALL_MOVES) {
            val nextState = s.next(dir)
            if (nextState.positions !in visited) {
                visited.add(nextState.positions)
                queue.add(nextState to nextState.h1())
            }
        }
    }
    return s
}

@Suppress("DuplicatedCode")
fun main(args: Array<String>) {
    opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad_output.txt" }
    val dirToCode = EnumMap(mapOf(N to "U", E to "R", S to "D", W to "L"))

    fun TreePath<Direction>.toMoves() = this.toList().joinToString(separator = "") { dirToCode[it]!! }

    File(outputPath).printWriter().use { output ->
        val mapString = File(inputPath).readLines()

        val commandoMap = CommandoMap(mapString)
        var s = commandoMap.getInitialState()

        if (opts.containsKey("ver2")) {
            s = commandoMap.aStar()
            debugLog("{r--A* length: ${s.path.length}--}")
        } else if (opts.containsKey("ver3")) {

        } else {
            val movesToCorner = List(commandoMap.cols) { N } + List(commandoMap.rows) { E }
            s = movesToCorner.fold(s) { state, dir -> state.next(dir) } // greedy move to corner
            s = s.decreaseUncertaintyGreedy()
            debugLog("{b--greedy: ${s.uncertainty} after (${s.path.length})--}")
            s = s.bfsSolve()
            debugLog("{b--BFS: ${s.uncertainty} after (${s.path.length})--}")
        }
        output.println(s.path.toMoves())

    }
}
