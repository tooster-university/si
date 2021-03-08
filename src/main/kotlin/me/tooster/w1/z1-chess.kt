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
fun Cords.dFirst(offset: Int): Cords = first + offset to second
fun Cords.dSecond(offset: Int): Cords = first to second + offset

fun Cords.chebyshevDistance(other: Cords): Int = max(abs(first - other.first), abs(second - other.second))

enum class Piece { WHITE_ROOK, WHITE_KING, BLACK_KING }

data class Move(val piece: Piece, val cords: Cords)

class GameState(val pieces: EnumMap<Piece, Cords>, val whiteMove: Boolean, val moves: List<Move>) {

    fun getRookMoves(): Sequence<Move> {
        return sequence {
            val rookPos = pieces[WHITE_ROOK]!!
            var (dMinusF, dPlusF) = rookPos.copy().dFirst(-1) to rookPos.copy().dFirst(+1)
            var (dMinusS, dPlusS) = rookPos.copy().dSecond(-1) to rookPos.copy().dSecond(+1)
            while (dMinusF.first >= 0 && dMinusF !in pieces.values) {
                yield(Move(WHITE_ROOK, dMinusF))
                dMinusF = dMinusF.dFirst(-1)
            }
            while (dPlusF.first < 8 && dPlusF !in pieces.values) {
                yield(Move(WHITE_ROOK, dPlusF))
                dPlusF = dPlusF.dFirst(1)
            }
            while (dMinusS.second >= 0 && dMinusS !in pieces.values) {
                yield(Move(WHITE_ROOK, dMinusS))
                dMinusS = dMinusS.dSecond(-1)
            }
            while (dPlusS.second < 8 && dPlusS !in pieces.values) {
                yield(Move(WHITE_ROOK, dPlusS))
                dPlusS = dPlusS.dSecond(1)
            }
        }
            .filter { it.cords !in pieces.values } // don't attack
            .filter { // only allow standing next to opposing king if the position is covered by our king
                it.cords.chebyshevDistance(pieces[BLACK_KING]!!) > 1 ||
                        it.cords.chebyshevDistance(pieces[WHITE_KING]!!) == 1
            }
    }

    fun isPositionThreatenedByWhiteRook(pos: Cords): Boolean {
        val rookPos = pieces[WHITE_ROOK]!!
        val wKingPos = pieces[WHITE_KING]!!
        // check white king shielding raycast
        return (pos.first == rookPos.first &&
                wKingPos.first !in min(pos.first, rookPos.first)..max(pos.first, rookPos.first) ||
                pos.second == rookPos.second &&
                wKingPos.second !in min(pos.second, rookPos.second)..max(pos.second, rookPos.second))
    }

    fun getKingMoves(movingKing: Piece): Sequence<Move> {
        val (moving, other) = if (movingKing == WHITE_KING) WHITE_KING to BLACK_KING else BLACK_KING to WHITE_KING
        val (movingPos, otherPos) = pieces[moving]!! to pieces[other]!!
        val rookPos = pieces[WHITE_ROOK]!!
        val rookMoves = getRookMoves().toList()

        return sequence {
            (max(0, movingPos.first - 1)..min(7, movingPos.first + 1)).forEach { f ->
                (max(0, movingPos.second - 1)..min(7, movingPos.second + 1)).forEach { s ->
                    yield(Move(moving, f to s))
                }
            }
        }
            .filter { it.cords !in pieces.values } // exclude move onto self position
            .filter { it.cords.chebyshevDistance(otherPos) > 1 } // kings minimum distance
//            .filterNot { !whiteMove && rookMoves.contains(it) } // black can't enter white rooks hit path
            .filterNot { !whiteMove && isPositionThreatenedByWhiteRook(it.cords) }
    }

    fun getMoves(): Sequence<Move> = sequence {
        if (whiteMove) {
            yieldAll(getKingMoves(WHITE_KING))
            yieldAll(getRookMoves())
        } else
            yieldAll(getKingMoves(BLACK_KING))
    }

    fun isCheckmate(): Boolean = getKingMoves(BLACK_KING).count() == 0 &&
            isPositionThreatenedByWhiteRook(pieces[BLACK_KING]!!)

    // returns new state after move
    fun performMove(move: Move): GameState {
        val newBoard = pieces.clone()
        newBoard[move.piece] = move.cords
        return GameState(newBoard, !whiteMove, moves + move)
    }
}

fun BFS(initialState: GameState): Int {
    val queue = LinkedList<Pair<GameState, Int>>()
    val visitedStates = HashSet<GameState>()
    queue.add(initialState to 0)
    while (true) {
        val (state, depth) = queue.remove()
        val moves = state.getMoves()
        System.err.print(depth)
        for (move in moves) {
            val nextState = state.performMove(move)
            if (nextState.isCheckmate())
                return depth + 1
            else if (nextState !in visitedStates) {
                visitedStates.add(nextState)
                queue.add(nextState to depth + 1)
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
                    ), l[0] == "white",
                    emptyList()
                )

                output.println(BFS(initialState))

            }
        }
    }

}