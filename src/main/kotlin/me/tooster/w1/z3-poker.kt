@file:JvmName("Z3")

package me.tooster.w1

import java.util.*
import kotlin.random.Random

data class Card(val color: Int, val figure: Int)

fun randomHand(low: Boolean) : List<Card> {
    val cards = if(low) 9 else 4
    val colors = 4
    return (0 until cards*colors).shuffled().take(5).map { Card(it/cards, it%cards) }
}

fun List<Card>.score() : Int{

    // high card (default)
    var highscore = 1


    val figGroup = this.groupBy { it.figure }
    val pairs = figGroup.filter { it.value.size == 2 }

    // one pair
    if(pairs.size == 1)
        highscore = 2

    // two pairs
    if(pairs.size == 2)
        highscore = 3

    // triplet
    if(figGroup.filter { it.value.size == 3}.isNotEmpty())
        highscore = 4

    // strit
    val strit = this.sortedBy { it.figure }.zipWithNext { a, b -> b.figure - a.figure }.all { it == 1 }
    if(strit)
        highscore = 5

    // color
    val color = this.all { it.color == this[0].color }
    if(color)
        highscore = 6

    // full house (triplet and double)
    if(figGroup.size == 2 && figGroup.any {it.value.size == 2})
        highscore = 7

    // four of a kind
    if(figGroup.size == 2 && figGroup.any {it.value.size == 4})
        highscore = 8

    if(strit && color)
        highscore = 9

    return highscore
}

fun main(args: Array<String>) {
    var (loWins, hiWins) = 0 to 0
    val reps = 1000000
    repeat(reps) {
        val (loHand, hiHand) = randomHand(true) to randomHand(false)
        val (loScore, hiScore) = loHand.score() to hiHand.score()
        if(loScore > hiScore) ++loWins
        else ++hiWins
    }
    System.out.println("Win to plays ratio: " + loWins.toDouble()/reps)
}