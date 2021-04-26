package me.tooster.w2

import me.tooster.common.Direction
import me.tooster.common.TreePath
import me.tooster.common.Vec2Int

class CommandoMap(val board: List<String>) {
    val rows: Int = board.size
    val cols: Int = board[0].length

    val walls: Set<Vec2Int> = mutableSetOf()
    val goals: Set<Vec2Int> = mutableSetOf()
    val starts: Set<Vec2Int> = mutableSetOf()

    companion object{
        val ALL_MOVES = listOf(Direction.N, Direction.E, Direction.S, Direction.W)
    }

    init {
        walls as MutableSet
        goals as MutableSet
        starts as MutableSet

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
    }

    fun getInitialState() = State(starts.toSet())
}