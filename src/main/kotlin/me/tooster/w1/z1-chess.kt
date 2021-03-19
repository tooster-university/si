@file:JvmName("Z1")

package me.tooster.w1

import me.tooster.w1.Piece.*
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import kotlin.math.abs


typealias Cords = Pair<Int, Int>

fun parseCords(str: String): Cords = "abcdefgh".indexOf(str[0]) to str.substring(1).toInt() - 1
fun Cords.toChessString(): String = "${"abcdefgh"[first]}${second + 1}"

private fun Cords.chebyshevDistance(other: Cords): Int = max(abs(first - other.first), abs(second - other.second))
private operator fun Pair<Int, Int>.plus(dc: Pair<Int, Int>): Cords {
    return Cords(first + dc.first, second + dc.second)
}

private fun Cords.idx() = first * 8 + second

enum class Piece { WHITE_ROOK, WHITE_KING, BLACK_KING }
data class Move(val piece: Piece, val cords: Cords, val previousMove: Move?)

class GameState(val pieces: EnumMap<Piece, Cords>, val whiteMove: Boolean, val lastMove: Move?) {

    private fun nextState(piece: Piece, newCords: Cords) = GameState(
        pieces.clone().apply { put(piece, newCords) },
        !whiteMove,
        Move(piece, newCords, lastMove)
    )

    companion object {
        val rays = listOf(Cords(-1, 0), Cords(+1, 0), Cords(0, -1), Cords(0, +1))
    }

    fun getRookMoves() = sequence {
        for (dc in rays) {
            var nextRookPos = pieces[WHITE_ROOK]!! + dc

            while (
                nextRookPos.first in 0..7 && nextRookPos.second in 0..7 && // bounds check
                !pieces.containsValue(nextRookPos) && ( // won't overlap on figure
                        nextRookPos.chebyshevDistance(pieces[BLACK_KING]!!) > 1 || // is far from king
                        nextRookPos.chebyshevDistance(pieces[WHITE_KING]!!) == 1   //  or is covered by our king
                                                      )
            ) {
                yield(nextState(WHITE_ROOK, nextRookPos))
                nextRookPos += dc
            }
        }
    }

    // returns ALL possible king moves - including attacks on white rook
    fun getKingMoves(king: Piece) = sequence {
        val opposingKing = if (king == WHITE_KING) BLACK_KING else WHITE_KING
        for (dr in -1..1) for (dc in -1..1) {
            val nextKingPos = pieces[king]!! + (dr to dc)
            if (dr == 0 && dc == 0 || // no move
                nextKingPos.first !in 0..7 || nextKingPos.second !in 0..7 || // out of board
                nextKingPos.chebyshevDistance(pieces[opposingKing]!!) < 2 || // to close to other king
                (king == BLACK_KING && isInRooksRay(nextKingPos))
            )
                continue
            yield(nextState(king, nextKingPos))
        }
    }

    // assumes valid position and returns true if white rook chec
    fun isInRooksRay(B: Cords): Boolean { // black
        val W = pieces[WHITE_KING]!!      // white
        val R = pieces[WHITE_ROOK]!!      // rook
        return B != R && ( // attacking rook is OK
                B.first == R.first && ( // same column as rook and no white king in between
                        W.first != B.first || W.first !in min(B.first, W.first)..max(B.first, W.first)) ||
                B.second == R.second && ( // same for rows
                        W.second != B.second || W.second !in min(B.second, W.second)..max(B.second, W.second))
                         )
    }

    // should generate list of valid states
    fun getNextStates(): Sequence<GameState> = sequence {
        if (whiteMove) {
            yieldAll(getKingMoves(WHITE_KING))
            yieldAll(getRookMoves())
        } else
        // filter out moves that attack rook
            yieldAll(getKingMoves(BLACK_KING).filterNot { it.pieces[BLACK_KING] == it.pieces[WHITE_ROOK] })
    }

    // we need here all possible king moves - including attacks on rook
    fun isCheckmate(): Boolean = getKingMoves(BLACK_KING).count() == 0 &&
                                 isInRooksRay(pieces[BLACK_KING]!!)

    override fun toString() = "B:${pieces[BLACK_KING]!!.toChessString()} W:${pieces[WHITE_KING]!!.toChessString()} " +
                              "R:${pieces[WHITE_ROOK]!!.toChessString()}"

    fun moves(): List<Move> = generateSequence(lastMove) { it.previousMove }.toList().asReversed()

    override fun hashCode(): Int {
        val B = pieces[BLACK_KING]!!
        val W = pieces[WHITE_KING]!!
        val R = pieces[WHITE_ROOK]!!

        return (B.idx() shl 0) + (W.idx() shl 6) + (R.idx() shl 12) + (if (whiteMove) 1 shl 28 else 0)
    }
}

fun BFS(initialState: GameState): GameState {
    if (initialState.isCheckmate())
        return initialState

    val queue = LinkedList<GameState>()
    val visitedStates = HashSet<Int>()

    queue.add(initialState)

    while (true) {
        val state = queue.remove()
//        System.err.print(depth)
//        try {
        for (nextState in state.getNextStates()) {
            if (nextState.isCheckmate())
                return nextState
            else if (nextState.hashCode() !in visitedStates) {
                visitedStates.add(nextState.hashCode())
                queue.add(nextState)
            }
        }

//        } catch (e: OutOfMemoryError) {
//            println("queue: ${queue.size} depth: ${state.moves().size}");
//        }
    }
}

fun main(args: Array<String>) {

    val opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val inputPath = opts.getOrElse("in") { "zad1_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad1_output.txt" }


    File(inputPath).bufferedReader().useLines { input ->
        File(outputPath).printWriter().use { output ->
            input.forEach {
                val l = it.split(" ")
                val initialState = GameState(
                    EnumMap(
                        mapOf(
                            WHITE_KING to parseCords(l[1]),
                            WHITE_ROOK to parseCords(l[2]),
                            BLACK_KING to parseCords(l[3]),
                        )
                    ), l[0] == "white",
                    null
                )

                val checkState = BFS(initialState)
                val moves = checkState.moves()

                output.println(moves.size)
                if (opts.contains("moves")) {
                    moves.forEach(::println)
                }
            }
        }
    }

}