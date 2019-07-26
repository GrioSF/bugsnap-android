package com.grio.lib.features

class LogSnapshotManager {

    companion object {

        fun getLogTail(limit: Int = 200): String {
            val logcat: Process
            var logList = listOf<String>()

            if (limit < 0) {
                throw IllegalArgumentException("Number of requested lines should be greater than zero.")
            }

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
                // only grab last $limit items
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                return logList.filterIndexed { index, _ -> index > logList.size - limit }
                    .joinToString(separator = System.getProperty("line.separator"))
            }
        }
    }
}