package me.tooster.common

/** Global map storing optional program arguments */
lateinit var opts: Map<String, String>

fun debugLog(str: String) {
    if (opts.containsKey("debug"))
        System.err.println(str)
}