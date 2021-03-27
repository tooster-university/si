package me.tooster.common

infix fun <T> List<T>.cartesianProduct(other: List<T>) =
    this.flatMap { first -> other.map { second -> first to second } }