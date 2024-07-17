package com.kiper.core.util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.Calendar
import java.util.Date

class FileUtil {
    companion object {
        fun isFileCreatedToday(filePath: String): Boolean {
            try {
                val path: Path = Paths.get(filePath)
                val attr: BasicFileAttributes =
                    Files.readAttributes(path, BasicFileAttributes::class.java)
                val creationDate = Date(attr.creationTime().toMillis())
                return isDateToday(creationDate)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        private fun isDateToday(date: Date): Boolean {
            val today = Calendar.getInstance()
            val fileDate = Calendar.getInstance().apply { time = date }

            return today.get(Calendar.YEAR) == fileDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == fileDate.get(Calendar.DAY_OF_YEAR)
        }
    }
}