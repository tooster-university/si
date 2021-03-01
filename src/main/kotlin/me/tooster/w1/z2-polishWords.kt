package me.tooster.w1

import java.io.File

// IDEA 1 (too bothersome): write trie with node visitor, traverse text for each letter read constructing prefix, check
// if word is full, keep track of 3 node visitors - whenever whole word is found, check consult with other visitors if the split
// has a meaning

// IDEA 2 (slightly slower): keep words in hashmap, if substring is a word save it as potential split point. Keep
// track of split points. Afterwards check best split. Approx. avg num of words per line should be around 5-10

//const val RESOURCE_PREFIX = "src/main/resources/w1/";

// optional args: <words file> <input file> <output file>
fun main(args: Array<String>) {


    val wordsPath = args.getOrElse(0) { "polish_words.txt" }
    val inputPath = args.getOrElse(1) { "zad2_input.txt" }
    val outputPath = args.getOrElse(2) { "zad2_output.txt" }

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