package `in`.bitotsav.feed

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class Feed(
    @PrimaryKey @SerializedName("_id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String,
    @SerializedName("time") val time: Long,
    @Expose(serialize = false, deserialize = false) val isStarred: Boolean = false,
    @Expose(serialize = false, deserialize = false) val eventId: Int? = null,
    @Expose(serialize = false, deserialize = false) val eventName: String? = null
)