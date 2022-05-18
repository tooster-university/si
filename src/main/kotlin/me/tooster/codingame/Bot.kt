package me.tooster.codingame

import Owner.ENEMY
import Owner.PLAYER
import java.lang.Integer.max
import java.util.*
import kotlin.math.abs
import kotlin.math.sign


const val CELL_CNT = 37
const val DAYS_TOTAL = 6
val ORIGIN = HexVec(0, 0)
val GROW_COST = listOf(0, 3, 7, 0) // 1->2 = 3,   2->3 = 7

// @fuckoff
val RINGS = listOf(
    listOf(HexVec(0, 0)),
    listOf(HexVec(2, 0),HexVec(1, 1),HexVec(-1, 1),HexVec(-2, 0),HexVec(-1, -1),HexVec(1, -1)),
    listOf(HexVec(4, 0),HexVec(3, 1),HexVec(2, 2),HexVec(0, 2),HexVec(-2, 2),HexVec(-3, 1),HexVec(-4, 0),HexVec(-3, -1),HexVec(-2, -2),HexVec(0, -2),HexVec(2, -2),HexVec(3, -1), ),
    listOf(HexVec(6, 0),HexVec(5, 1),HexVec(4, 2),HexVec(3, 3),HexVec(1, 3),HexVec(-1, 3),HexVec(-3, 3),HexVec(-4, 2),HexVec(-5, 1),HexVec(-6, 0),HexVec(-5, -1),HexVec(-4, -2),HexVec(-3, -3),HexVec(-1, -3),HexVec(1, -3),HexVec(3, -3),HexVec(4, -2),HexVec(5, -1)),
)
val CELL_TO_CORDS = RINGS.flatten();
val CORDS_TO_CELL: Map<HexVec, Int> = mapOf(HexVec(0, 0) to 0,HexVec(2, 0) to 1,HexVec(1, 1) to 2,HexVec(-1, 1) to 3,HexVec(-2, 0) to 4,HexVec(-1, -1) to 5,HexVec(1, -1) to 6,HexVec(4, 0) to 7,HexVec(3, 1) to 8,HexVec(2, 2) to 9,HexVec(0, 2) to 10,HexVec(-2, 2) to 11,HexVec(-3, 1) to 12,HexVec(-4, 0) to 13,HexVec(-3, -1) to 14,HexVec(-2, -2) to 15,HexVec(0, -2) to 16,HexVec(2, -2) to 17,HexVec(3, -1) to 18,HexVec(6, 0) to 19,HexVec(5, 1) to 20,HexVec(4, 2) to 21,HexVec(3, 3) to 22,HexVec(1, 3) to 23,HexVec(-1, 3) to 24,HexVec(-3, 3) to 25,HexVec(-4, 2) to 26,HexVec(-5, 1) to 27,HexVec(-6, 0) to 28,HexVec(-5, -1) to 29,HexVec(-4, -2) to 30,HexVec(-3, -3) to 31,HexVec(-1, -3) to 32,HexVec(1, -3) to 33,HexVec(3, -3) to 34,HexVec(4, -2) to 35,HexVec(5, -1) to 36)
// @fuckon

enum class Owner { PLAYER, ENEMY }


enum class HexDirection {
    /*
            ↑ +Y
       [2]     [1]
          NW NE
     [3] W  +  E [0] → +X
          SW SE
       [4]     [5]
    */
    NE, E, SE, SW, W, NW;

    enum class Relative {
        LEFT_FRONT, LEFT_BACK, BACK, RIGHT_BACK, RIGHT_FRONT
    }

    companion object {
        val values = values()
        val deltaX = listOf(2, 1, -1, -2, -1, 1)
        val deltaY = listOf(0, 1, 1, 0, -1, -1)
        val cwOffset = listOf(+1, +2, +3, +4, +5)

        /** value from rose, where 0=NE, 1=E ... and other are relative to N */
        fun getRose(index: Int) = values[Math.floorMod(index, values.size)]
    }

    fun get(relative: Relative): HexDirection = getRose(this.ordinal + cwOffset[relative.ordinal])

    val deltaX get() = HexDirection.deltaX[this.ordinal]
    val deltaY get() = HexDirection.deltaY[this.ordinal]
    val delta get() = HexVec(deltaX, deltaY)
}

/** cords on hex map increase by 2 in cardinal direction and 1 in diagonal direction */
data class HexVec(val x: Int, val y: Int) {

    constructor(pair: Pair<Int, Int>) : this(pair.first, pair.second)

    operator fun unaryMinus(): HexVec = HexVec(-x, -y)
    operator fun plus(other: HexVec): HexVec = HexVec(x + other.x, y + other.y)
    operator fun minus(other: HexVec): HexVec = HexVec(x - other.x, y - other.y)
    operator fun times(scalar: Int): HexVec = HexVec(x * scalar, y * scalar)
    operator fun div(scalar: Int): HexVec = HexVec(x / scalar, y / scalar)
    operator fun Int.times(other: HexVec): HexVec = other * this

    fun translated(d: HexDirection, distance: Int = 1): HexVec = this + (d.delta * distance)

    /** radius from origin (0,0) */
    val radius: Int get() = (abs(x) + abs(y)) / 2
    infix fun hexDist(other: HexVec) = (other - this).radius

    override fun toString(): String = "$x $y"
}


data class Cell(
    var richness: Int = 0,
    var tree: Int = 0,
    var inShadow: Boolean = false,
    var owner: Owner? = null, // doesn't matter if tree = 0
    var dormant: Boolean = false,
)

typealias Forest = List<Cell>
typealias Stat<T> = EnumMap<Owner, T>

sealed class Action(val owner: Owner, val idx: Int, val cost: Int) {
    abstract fun perform(state: GameState): GameState

    class ChopAction(owner: Owner, idx: Int, cost: Int) : Action(owner, idx, cost) {
        override fun perform(state: GameState) = with(state) {
            val cell = forest[idx]

            assert(cell.tree > 0 && cell.richness != 0)

            copy(
                forest = forest.toMutableList().also { it[idx] = cell.copy(tree = 0, owner = null) },
                nutrients = max(0, nutrients - 1),
                sunPoints = Stat(sunPoints).apply { computeIfPresent(cell.owner) { _, sun -> sun - cost } },
                points = Stat(points).apply {
                    computeIfPresent(cell.owner) { _, s -> s + nutrients + (cell.richness - 1) * 2 }
                },
            )
        }

        override fun toString(): String = "COMPLETE $idx"
    }

    class GrowAction(owner: Owner, idx: Int, cost: Int) : Action(owner, idx, cost) {
        override fun perform(state: GameState): GameState = with(state) {
            val cell = forest[idx]

            assert(cell.tree in 1..2)

            return copy(
                forest = forest.toMutableList()
                    .also { it[idx] = Cell(cell.richness, cell.tree + 1, cell.inShadow, cell.owner) },
                sunPoints = Stat(sunPoints).apply { computeIfPresent(cell.owner) { _, sun -> sun - cost } },
            )
        }

        override fun toString(): String = "GROW $idx"
    }

    class WaitAction(owner: Owner) : Action(owner, 0, 0) {
        override fun perform(state: GameState): GameState = with(state) {
            return state.copy(waiting = Stat(mapOf(PLAYER to true, ENEMY to waiting[ENEMY]!!)))
        }

        override fun toString(): String = "WAIT $idx"
    }
}

data class Scored<T>(val o: T, val score: Double)
data class GameState(
    val forest: Forest = List(CELL_CNT) { Cell() },
    val day: Int,
    val nutrients: Int,
    val sunPoints: Stat<Int>,
    val points: Stat<Int>,
    val waiting: Stat<Boolean>,
) {

    // computation cache for state
    /// --------------------------
    val counts: Map<Pair<Owner, Int>, Int> by lazy {
        forest.filter { it.owner != null }.groupingBy { it.owner!! to it.tree }.eachCount().withDefault { 0 }
    }

    /** how many sun points are generated by the forest */
    val sunPotential: Stat<Int> by lazy {
        val stat = mutableMapOf(PLAYER to 0, ENEMY to 0)
        forest.filter { it.tree > 0 }
            .forEach { cell -> stat.computeIfPresent(cell.owner!!) { _, sun -> sun + cell.tree } }
        Stat(stat)
    }

    val isTerminal: Boolean = day == DAYS_TOTAL && getLegalActions().filter { it.owner == PLAYER }.count() == 0
    /// --------------------------

    fun getChopActions(): Sequence<Action.ChopAction> = forest.asSequence().mapIndexedNotNull { idx, cell ->
        if (cell.tree != 3) null else Action.ChopAction(cell.owner!!, idx, 4)
    }

    fun getGrowActions(): Sequence<Action.GrowAction> {
        return forest.asSequence().mapIndexedNotNull { idx, cell ->
            if (cell.tree !in 1..2) null else Action.GrowAction(
                cell.owner!!,
                idx,
                GROW_COST[cell.tree] + counts.getValue(cell.owner!! to cell.tree)
            )
        }
    }

    fun getLegalActions(): Sequence<Action> {
        return (getChopActions() + getGrowActions()).filter { it.cost <= sunPoints[it.owner]!! }
    }

    // todo: depending on days left, sun potential, richness, nutrient,

    fun Action.hSun(): Double = when (this) {
        // todo: include expected sun points
        is Action.ChopAction -> -3.0 * (1.0 / sunPotential[owner]!!)
        is Action.GrowAction -> (DAYS_TOTAL - day) * forest[idx].tree * (1.0 / sunPotential[owner]!!)
        is Action.WaitAction -> 0.0
    }

    fun Action.hScore(): Double = when (this) {
        // todo: include average spending
        is Action.ChopAction -> 3.0 + nutrients + forest[idx].richness
        is Action.GrowAction -> (DAYS_TOTAL - day) * 0.5 * (forest[idx].tree + 1)
        is Action.WaitAction -> 0.0
    }

    fun Action.prune(): Boolean = false // TODO

    fun beam(width: Int = 10, depth: Int = 5): Scored<Action> {
        val candidates = getLegalActions().map { Scored(it, it.hSun() + it.hScore() * 3) }
            .take(width).sortedByDescending { it.score }
        if (depth == 0) return candidates.first()

        return candidates.map { it.o.perform(this).beam(width, depth - 1) }.take(width)
            .sortedByDescending { it.score }.first()
    }

    fun beamSearch(width: Int) {
        val queue = PriorityQueue<Pair<Scored<GameState>, Action>> { a1, a2 -> sign(a1.score - a2.score).toInt() }
        val currentState = Scored(this, 0.0)
        while (!currentState.isTerminal) {

            currentState = queue.poll()
        }
    }
}


/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val numberOfCells = input.nextInt() // 37

    val forest: Forest = MutableList(CELL_CNT) { Cell() }

    repeat(numberOfCells) {
        val index = input.nextInt() // 0 is the center cell, the next cells spiral outwards
        val richness = input.nextInt() // 0 if the cell is unusable, 1-3 for usable cells
        val neigh0 = input.nextInt() // the index of the neighbouring cell for each direction
        val neigh1 = input.nextInt()
        val neigh2 = input.nextInt()
        val neigh3 = input.nextInt()
        val neigh4 = input.nextInt()
        val neigh5 = input.nextInt()
        forest as MutableList
        forest[index] = Cell(richness)
    }

    // game loop
    while (true) {
        val day = input.nextInt() // the game lasts 24 days: 0-23
        val nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        val sun = input.nextInt() // your sun points
        val score = input.nextInt() // your current score
        val oppSun = input.nextInt() // opponent's sun points
        val oppScore = input.nextInt() // opponent's score
        val oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        val numberOfTrees = input.nextInt() // the current amount of trees

        val state = GameState(
            forest,
            day,
            nutrients,
            Stat(mapOf(PLAYER to sun, ENEMY to oppSun)),
            Stat(mapOf(PLAYER to score, ENEMY to oppScore)),
            Stat(mapOf(PLAYER to false, ENEMY to oppIsWaiting)),
        )

        for (i in 0 until numberOfTrees) {
            val cellIndex = input.nextInt() // location of this tree
            val size = input.nextInt() // size of this tree: 0-3
            val isMine = input.nextInt() != 0 // 1 if this is your tree
            val isDormant = input.nextInt() != 0 // 1 if this tree is dormant

            val cellInfo = forest[cellIndex]
            cellInfo.owner = if (isMine) PLAYER else ENEMY
            cellInfo.tree = size
            cellInfo.dormant = isDormant
        }
        val numberOfPossibleActions = input.nextInt() // all legal actions
        if (input.hasNextLine()) {
            input.nextLine()
        }
        for (i in 0 until numberOfPossibleActions) {
            val possibleAction = input.nextLine() // try printing something from here to start with
        }

        val action = state.beam().first
        println()
        // Write an action using println()
        // To debug: System.err.println("Debug messages...");


        // GROW cellIdx | SEED sourceIdx targetIdx | COMPLETE cellIdx | WAIT <message>
        println("WAIT")
    }
}