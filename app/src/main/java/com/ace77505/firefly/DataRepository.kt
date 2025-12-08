// DataRepository.kt
package com.ace77505.firefly

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class DataRepository(private val context: Context) {

    fun loadDataFromAssets(filename: String = "firefly.csv"): List<FilterData> {
        val dataList = mutableListOf<FilterData>()

        try {
            context.assets.open(filename).use { inputStream ->
                // 一次性读取整个文件内容
                val fileContent = inputStream.readBytes().toString(Charsets.UTF_8)

                // 统一处理换行符
                val normalizedContent = fileContent
                    .replace("\r\n", "\n")  // Windows -> Unix
                    .replace("\r", "\n")    // Old Mac -> Unix

                // 按行分割
                val lines = normalizedContent.split("\n")

                var isFirstLine = true
                for (line in lines) {
                    if (line.isBlank()) continue // 跳过空行

                    if (isFirstLine) {
                        isFirstLine = false
                        continue // 跳过标题行
                    }

                    val values = parseCSVLine(line)
                    if (values.size >= 10) {
                        val data = FilterData(
                            id = values[0].trim(),
                            title = values[1].trim(),
                            recommend = values[2].trim().toIntOrNull() ?: -1,
                            filter1 = values[3].trim().toIntOrNull() ?: -1,
                            filter2 = values[4].trim().ifEmpty { "-1" },
                            filter3 = values[5].trim().toIntOrNull() ?: -1,
                            filter4 = values[6].trim().toIntOrNull() ?: -1,
                            filter5 = values[7].trim().ifEmpty { "-1" },
                            updateDate = values[8].trim(),
                            source = values[9].trim()
                        )
                        dataList.add(data)
                    } else {
                        // 打印有问题的行以便调试
                        println("Invalid CSV line (expected 10 columns, got ${values.size}): $line")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 打印加载的数据数量用于调试
        println("Loaded ${dataList.size} records from CSV")

        return dataList
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            when (val c = line[i]) {
                '"' -> {
                    inQuotes = !inQuotes
                    current.append(c) // 保留引号
                }
                ',' if !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(c)
                }
            }
        }
        result.add(current.toString())
        return result
    }
}