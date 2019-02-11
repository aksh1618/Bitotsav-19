package `in`.bitotsav.shared.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMap(value: Map<String, String>): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun toMap(value: String): Map<String, String> {
            return value.let {
                val type = object : TypeToken<Map<String, String>>() {}.type
                Gson().fromJson<Map<String, String>>(value, type)
            } ?: mapOf()
        }

        @TypeConverter
        @JvmStatic
        fun fromMapOfMap(value: Map<String, Map<String, String>>): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun toMapOfMap(value: String): Map<String, Map<String, String>> {
            val type = object : TypeToken<Map<String, Map<String, String>>>() {}.type
            return Gson().fromJson<Map<String, Map<String, String>>>(value, type)
        }
    }
}