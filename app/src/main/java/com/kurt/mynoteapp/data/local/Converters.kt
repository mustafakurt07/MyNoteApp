package com.kurt.mynoteapp.data.local

import androidx.room.TypeConverter

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<String> =
        value?.takeIf { it.isNotBlank() }?.split("|")?.let { listOf(*it.toTypedArray()) } ?: emptyList()

    @TypeConverter
    @JvmStatic
    fun listToString(list: List<String>?): String =
        list?.joinToString(separator = "|") ?: ""
}


