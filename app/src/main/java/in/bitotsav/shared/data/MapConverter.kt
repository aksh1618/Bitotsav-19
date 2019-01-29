package `in`.bitotsav.shared.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

        @JvmStatic
        fun fromMapOfMap(value: Map<String, Map<String, String>>): String {
            return Gson().toJson(value)
        }

        @JvmStatic
        fun toMapOfMap(value: String): Map<String, Map<String, String>> {
            val type = object : TypeToken<Map<String, Map<String, String>>>() {}.type
            return Gson().fromJson<Map<String, Map<String, String>>>(value, type)
        }
    }
}