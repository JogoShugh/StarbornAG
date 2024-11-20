package org.starbornag.api

object LogTimer { // Using an object to hold the initialTime
    private var initialTime: Long = 0

    @JvmStatic
    fun logNow(label: String, reset: Boolean = false) {
        if (initialTime == 0L || reset) {
            initialTime = System.currentTimeMillis()
            println("$label: $initialTime")
        } else {
            val elapsed = System.currentTimeMillis() - initialTime
            val seconds = elapsed / 1000
            val milliseconds = elapsed % 1000
            println("$label: $seconds:$milliseconds")
        }
    }
}