package me.tooster.w2

import me.tooster.common.*
import java.lang.Integer.max
import java.util.*
import kotlin.collections.HashMap

class CommandoMap(val board: List<String>) {
    val rows: Int = board.size
    val cols: Int = board[0].length

    val walls: Set<Vec2Int> = mutableSetOf()
    val goals: Set<Vec2Int> = mutableSetOf()
    val starts: Set<Vec2Int> = mutableSetOf()
    val dists: Map<Vec2Int, Int> = HashMap()

    companion object {
        val ALL_MOVES = listOf(Direction.N, Direction.E, Direction.S, Direction.W)
    }

    init {
        walls as MutableSet
        goals as MutableSet
        starts as MutableSet
        dists as HashMap

        board.flatMapIndexed { r, s -> s.mapIndexed { c, symbol -> Triple(r, c, symbol) } }.forEach { (r, c, symbol) ->
            val cords = Vec2Int(c, rows - r - 1)
            when (symbol) {
                '#' -> walls.add(cords)
                'G' -> goals.add(cords)
                'S' -> starts.add(cords)
                'B' -> {
                    starts.add(cords)
                    goals.add(cords)
                }
            }
        }

        // calculate distances from all points to closest target
        forOrigin@ for (origin in Vec2Int(0, 0) until Vec2Int(cols, rows)) {
            if (origin in walls) continue@forOrigin

            val visited = mutableSetOf<Vec2Int>()
            val queue = LinkedList<Pair<Vec2Int, Int>>()

            queue.add(origin to 0)
            visited.add(origin)

            while (queue.isNotEmpty()) {
                val (pos, d) = queue.remove()

                if (pos in goals) {
                    dists[origin] = d
                    continue@forOrigin
                }

                for (dir in ALL_MOVES) {
                    val newPos = pos + dir.delta
                    if (newPos !in walls && newPos !in visited) {
                        queue.add(newPos to d + 1)
                        visited.add(newPos)
                    }
                }
            }
        }
    }

    /** represents state of commando including move history */
    inner class State(val positions: Set<Vec2Int>) {
        var path = TreePath<Direction>(); private set

        /** returns next state after making a move in specific direction */
        fun next(d: Direction): State = State(positions.map {
            val translated = it.translate(d)
            if (translated in walls) it else translated // make a move or not if next to wall
        }.toSet()).also { it.path = TreePath(d, path) }


        /** uncertainty is number of troops that are not on the goals*/
        val uncertainty: Int get() = positions.size

        /** number of troops to reach the goal */
        val toReach: Int get() = positions.count { it !in goals }

        override fun hashCode(): Int = positions.hashCode()
        // wacky behavior and hash collision problems :/

        fun h1(mult: Double = opts.getOrDefault("mult", "1").toDouble()): Double =
            positions.maxOfOrNull { dists[it]!! }!! * mult + path.length
    }

    fun getInitialState() = State(starts.toSet())
}