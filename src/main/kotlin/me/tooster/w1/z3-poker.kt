@file:JvmName("Z3")

package me.tooster.w1

data class Card(val color: Int, val figure: Int)

fun randomHand(low: Boolean, exclude: List<Int> = emptyList()): List<Card> {
    val cards = if (low) 9 else 4
    val colors = 4
    return (0 until cards * colors).filter { it !in exclude }.shuffled().take(5).map { Card(it / cards, it % cards) }
}

fun List<Card>.score(): Int {

    // high card (default)
    var highscore = 1


    val figGroup = this.groupBy { it.figure }
    val pairs = figGroup.filter { it.value.size == 2 }

    // one pair
    if (pairs.size == 1)
        highscore = 2

    // two pairs
    if (pairs.size == 2)
        highscore = 3

    // triplet
    if (figGroup.filter { it.value.size == 3 }.isNotEmpty())
        highscore = 4

    // strit
    val strit = this.sortedBy { it.figure }.zipWithNext { a, b -> b.figure - a.figure }.all { it == 1 }
    if (strit)
        highscore = 5

    // color
    val color = this.all { it.color == this[0].color }
    if (color)
        highscore = 6

    // full house (triplet and double)
    if (figGroup.size == 2 && figGroup.any { it.value.size == 2 })
        highscore = 7

    // four of a kind
    if (figGroup.size == 2 && figGroup.any { it.value.size == 4 })
        highscore = 8

    if (strit && color)
        highscore = 9

    return highscore
}

fun experiment(drop: Int) {
    var loWins = 0
    val reps = 100000
    val exclude = (0 until 4 * 9).shuffled().take(drop)
    repeat(reps) {
        val (loHand, hiHand) = randomHand(true, exclude) to randomHand(false)
        val (loScore, hiScore) = loHand.score() to hiHand.score()
        if (loScore > hiScore) ++loWins
    }
    System.out.println("dropping $drop - win to plays ratio: ${loWins.toDouble() / reps}.")
}

fun propose() {
    val decks = 1000
    val sims = 1000


    for (size in 5..15) {
        var bestExclude: List<Int> = emptyList()
        var bestScore = 0.0
        repeat(decks) {
            val exclude = (0 until 4 * 9).shuffled().drop(size)
            var loWins = 0
            repeat(sims) {
                val (loHand, hiHand) = randomHand(true, exclude) to randomHand(false)
                val (loScore, hiScore) = loHand.score() to hiHand.score()
                if (loScore > hiScore) ++loWins
            }
            if (loWins.toDouble() / sims > bestScore) {
                bestExclude = exclude
                bestScore = loWins.toDouble() / sims
            }
        }
        System.out.println("Best statictical win for size $size: $bestScore while excluding: $bestExclude")
    }
}

fun main(args: Array<String>) {
    (0..20).forEach {
        experiment(drop = it)
    }

    propose()
}