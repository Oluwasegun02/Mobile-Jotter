package com.example.data

import androidx.room.TypeConverter

/**
 * Room TypeConverter to convert between List<String> and String for local database persistence.
 */
class Converters {

    @TypeConverter
    fun fromStringList(tags: List<String>?): String {
        if (tags.isNullOrEmpty()) return ""
        return tags.filter { it.isNotBlank() }.joinToString(",")
    }

    @TypeConverter
    fun toStringList(tagsString: String?): List<String> {
        if (tagsString.isNullOrBlank()) return emptyList()
        return tagsString.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
}
