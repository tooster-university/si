@file:JvmName("Z2")

package me.tooster.w1

import java.io.File

// IDEA 1 (too bothersome): write trie with node visitor, traverse text for each letter read constructing prefix, check
// if word is full, keep track of several node visitors - whenever whole word is found, check consult with other
// visitors if the split is meaningful. General observation - words are rarely longer than ~15 letters

// IDEA 2 (slightly slower): keep words in hashmap, if substring is a word save it as potential split point. Keep
// track of split points. Afterwards check best split. Approx. avg num of words per line should be around 5-10
// ^ this turned out to suck ass, potential split lists grew very big

// IDEA 3 (hashmap for words + DP for splits): best split ending at some point i will extend some best splits
// j < i whenever substring [j..i] is a word in hashmap.

//const val RESOURCE_PREFIX = "src/main/resources/w1/";

// optional args: words:<words file> in:<input file> out:<output file>
fun main(args: Array<String>) {

    val opts = args.associate { with(it.split(':')) { this[0] to this[1] } }

    val wordsPath = opts.getOrElse("words") { "words_for_ai1.txt" }
    val inputPath = opts.getOrElse("in") { "zad2_input.txt" }
    val outputPath = opts.getOrElse("out") { "zad2_output.txt" }

    val words = File(wordsPath).bufferedReader().useLines { it.toHashSet() }

    File(inputPath).bufferedReader().useLines { input ->
        File(outputPath).printWriter().use { output ->
            input.forEach { output.println(split(it, words)) }
        }
    }

}

// BestSplit(i) = max(BestSplit(j<i))
fun split(line: String, words: HashSet<String>): String {
    val dp = Array(line.length + 1) { idx -> 0 to idx } // <valid split's score ending before i, word start>
    (1..line.length).forEach { end ->
        (0 until end).forEach { start ->
            val word = line.substring(start, end)
            if (
                (start == 0 || dp[start].first > 0) && // consider only valid previous splits
                words.contains(word) &&
                dp[end].first < dp[start].first + word.length * word.length
            )
                dp[end] = dp[start].first + word.length * word.length to start
        }
    }
    var end = dp.size - 1
    val wordsReversed = mutableListOf<String>()
    while (end > 0) {
        wordsReversed.add(line.substring(dp[end].second, end))
        end = dp[end].second
    }
    return wordsReversed.asReversed().joinToString(" ")
}