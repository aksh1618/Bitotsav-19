package `in`.bitotsav.events.data

import `in`.bitotsav.shared.utils.onFalse
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.get
import java.util.*

@Entity
data class Event(
    @PrimaryKey @SerializedName("eventId") val id: Int,
    @SerializedName("eventName") val name: String,
    @SerializedName("eventVenue") val venue: String,
    @SerializedName("eventTime") val timeString: String,
    @SerializedName("eventDay") val day: Int,
    @SerializedName("eventDescription") val description: String,
    @SerializedName("eventRules") val rules: String,
    @SerializedName("eventContact1Name") val contact1Name: String,
    @SerializedName("eventContact1Number") val contact1Number: Long,
//    TODO: This can be null or empty. Handle it in UI accordingly
    @SerializedName("eventContact2Name") val contact2Name: String? = null,
    @SerializedName("eventContact2Number") val contact2Number: Long? = null,
    @SerializedName("eventPoints1") val points1: Int,
    @SerializedName("eventPoints2") val points2: Int,
    @SerializedName("eventPoints3") val points3: Int,
    // Can be "F" or "NF" for flagship and non-flagship respectively
    @SerializedName("eventType") val type: String,
    @SerializedName("eventMinimumMembers") val minimumMembers: Int,
    @SerializedName("eventMaximumMembers") val maximumMembers: Int,
    @SerializedName("eventCategory") val category: String,
    @SerializedName("eventStatus") val status: String,
    @SerializedName("eventPrize1") val prize1: Int,
    @SerializedName("eventPrize2") val prize2: Int,
    @SerializedName("eventPrize3") val prize3: Int,
//    TODO: Note: Default value is empty map
//    eventPosition1: {
//    teamLeader: req.body.eventPosition1,
//    teamLeaderName: map[req.body.eventPosition1].name,
//    championshipTeam: (team1 != null) ? team1 : "-1",
//    points: (team1 != null) ? event.eventPoints1 : 0
//}
    @SerializedName("eventPosition1") val position1: Map<String, String>?,
    @SerializedName("eventPosition2") val position2: Map<String, String>?,
    @SerializedName("eventPosition3") val position3: Map<String, String>?
) : KoinComponent {
    // Using @Transient also makes room ignore the property
    @Expose(serialize = false, deserialize = false)
    var timestamp = getTimestampFromString(day, timeString)

    @Expose(serialize = false, deserialize = false)
    var isStarred: Boolean = false

    fun setProperties(isStarred: Boolean) {
        this.timestamp = getTimestampFromString(day, timeString)
        this.isStarred = isStarred
    }

    private fun getTimestampFromString(day: Int, timeString: String): Long {
        val (hours, minutes) = timeString.split(":").map { it.toInt() }
        return GregorianCalendar(2019, 2, day + 14, hours, minutes).timeInMillis
    }

    fun toggleStarred() {
        isStarred = isStarred.not()
        CoroutineScope(Dispatchers.IO).async {
            get<EventRepository>().insert(this@Event)
            Log.d("EventKt", "Inserted: $name")
        }
    }

    fun setStarred() {
        isStarred.onFalse {
            isStarred = true
            CoroutineScope(Dispatchers.IO).async {
                get<EventRepository>().insert(this@Event)
                Log.d("EventKt", "Inserted: $name")
            }
        }
    }
}