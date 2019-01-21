package `in`.bitotsav.shared.data

import androidx.room.TypeConverter
import java.util.*

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMap(value: Map<String, String>): String {
            return value.toString().drop(1).dropLast(1)
        }

        @TypeConverter
        @JvmStatic
        fun toMap(value: String): Map<String, String> {
            return value.split(",").associate {
                val (left, right) = it.split("=")
            left to right
            }
        }

        @TypeConverter
        @JvmStatic
        fun toGregorianCalendar(value: Long): GregorianCalendar {
            val gregorianCalendar = GregorianCalendar()
            gregorianCalendar.timeInMillis = value
            return gregorianCalendar
        }

        @TypeConverter
        @JvmStatic
        fun fromGregorianCalendar(value: GregorianCalendar): Long {
            return value.timeInMillis
        }
    }
}