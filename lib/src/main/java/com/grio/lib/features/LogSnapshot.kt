package com.grio.lib.features

class LogSnapshot {

    lateinit var logs: String
        private set

    constructor(initializeWithLog: Boolean = false) {
        if(initializeWithLog) {
            updateSnapshot()
        }
    }

    fun updateSnapshot(): String {
        val logcat: Process
        var logList = listOf<String>()
        val logLinesLimit = 200

        try {
            // Get a logcat dump
            logcat = Runtime.getRuntime().exec(arrayOf("logcat", "-d"))

            // Read contents and append to log
            val reader = logcat.inputStream.buffered(1024).bufferedReader()

            // usesLines automatically closes the buffer reader on complete
            reader.useLines { logList = it.toList() }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // only grab last $logLinesLimit items
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            logs = logList.filterIndexed { index, _ ->  index > logList.size - logLinesLimit }
                .joinToString(separator = System.getProperty("line.separator"))

            return logs
        }
    }
}