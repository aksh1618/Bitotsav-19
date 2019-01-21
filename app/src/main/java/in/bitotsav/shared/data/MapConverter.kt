package `in`.bitotsav.shared.data

import androidx.room.TypeConverter

class MapConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMap(value: Map<String, String>): String {
            return value.toString().drop(1).dropLast(1)
        }

        @TypeConverter
        @JvmStatic
        fun toMap(value: String): Map<String, String> {
            if (value.isNullOrEmpty()) {
                return mapOf()
            } else {
                return value.split(",").associate {
                    val (left, right) = it.split("=")
                    left to right
                }
            }
        }
    }
}