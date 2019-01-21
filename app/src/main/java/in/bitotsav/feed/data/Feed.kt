package `in`.bitotsav.feed.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

enum class FeedType{
    EVENT,
    RESULT,
    ANNOUNCEMENT,
    PM
}

@Entity
data class Feed(
    @PrimaryKey @SerializedName("_id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String,
    @SerializedName("time") val time: Long,
    @Expose(serialize = false, deserialize = false) var isStarred: Boolean = false,
    val eventId: Int? = null,
    @Expose(serialize = false, deserialize = false) var eventName: String? = null
)