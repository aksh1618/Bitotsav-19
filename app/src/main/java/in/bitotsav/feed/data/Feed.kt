package `in`.bitotsav.feed.data

import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import android.text.format.DateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

enum class FeedType {
    EVENT,
    RESULT,
    ANNOUNCEMENT,
    PM
}

@Entity
data class Feed(
    @PrimaryKey @SerializedName("feedId") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: Long, // Milliseconds
    @Expose(serialize = false, deserialize = false) var isStarred: Boolean = false,
    val eventId: Int? = null,
    @Expose(serialize = false, deserialize = false) var eventName: String? = null
) : SimpleRecyclerViewAdapter.SimpleItem() {

    fun setProperties(isStarred: Boolean, eventName: String?) {
        this.isStarred = isStarred
        this.eventName = eventName
    }

    fun getTypeLabel() =
        when (type) {
            FeedType.EVENT.name -> eventName ?: "Event Update"
            FeedType.RESULT.name -> eventName?.let { "$eventName Result" } ?: "Event Result"
            FeedType.ANNOUNCEMENT.name -> "Announcement"
            FeedType.PM.name -> "Private Message"
            else -> "Update"
        }

    fun getTimeString() = DateFormat.format(
        "MMM d, hh:mm aaa",
        GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
            timeInMillis = timestamp
        }
    ).toString()

    override fun getUniqueIdentifier(): String = id.toString()

}