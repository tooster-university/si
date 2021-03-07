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

fun Cords.chebyshevDistance(other: Cords): Int = max(abs(first - other.first), abs(second - other.second))

enum class Piece { WHITE_ROOK, WHITE_KING, BLACK_KING }

data class Move(val piece: Piece, val cords: Cords)

class GameState(val pieces: EnumMap<Piece, Cords>, val whiteMove: Boolean) {
    fun getRookMoves(): Sequence<Move> {
        return sequence {
            val rookPos = pieces[WHITE_ROOK]!!
            (0 until 8).forEach { yield(Move(WHITE_ROOK, it to rookPos.second)) }
            (0 until 8).forEach { yield(Move(WHITE_ROOK, rookPos.first to it)) }
        }.filter { m -> m.cords !in pieces.values }
    }

    fun getKingMoves(movingKing: Piece): Sequence<Move> {
        val (moving, other) = if (movingKing == WHITE_KING) WHITE_KING to BLACK_KING else BLACK_KING to WHITE_KING
        val (movingPos, otherPos) = pieces[moving]!! to pieces[other]!!
        val rookPos = pieces[WHITE_ROOK]!!

        return sequence {
            (max(0, movingPos.first - 1)..min(7, movingPos.first + 1)).forEach { f ->
                (max(0, movingPos.second - 1)..min(7, movingPos.second + 1)).forEach { s ->
                    yield(Move(moving, f to s))
                }
            }
        }
            .filter { it.cords.chebyshevDistance(otherPos) > 1 } // kings minimum distance
            .filter {
                whiteMove || (it.cords.first != rookPos.first && it.cords.second != pieces[WHITE_ROOK]!!.second)
            }
    }

    fun getMoves(): Sequence<Move> = sequence {
        if (whiteMove) {
            yieldAll(getRookMoves())
            yieldAll(getKingMoves(WHITE_KING))
        } else
            yieldAll(getKingMoves(BLACK_KING))
    }

    fun isCheckmate(): Boolean = getKingMoves(BLACK_KING).count() == 0

    // returns new state after move
    fun performMove(move: Move): GameState {
        val newBoard = pieces.clone()
        newBoard[move.piece] = move.cords
        return GameState(newBoard, !whiteMove)
    }
}

fun BFS(initialState: GameState): Int {
    val queue = LinkedList<Pair<GameState, Int>>()
    val visitedStates = HashSet<GameState>()
    queue.add(initialState to 0)
    while (true) {
        val (state, depth) = queue.remove()
        val moves = state.getMoves()
        moves.forEach {
            val nextState = state.performMove(it)
            if (nextState.isCheckmate())
                return depth + 1
            else if (nextState !in visitedStates) {
                visitedStates.add(nextState)
                queue.addLast(nextState to depth + 1)
            }
        }
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
                    ), l[0] == "white"
                )

                output.println(BFS(initialState))

            }
        }
    }

}