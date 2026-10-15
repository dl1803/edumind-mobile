package com.edumind.app.util

fun interface ClockProvider {
    fun currentTimeMillis(): Long
}
