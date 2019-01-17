package `in`.bitotsav.teams

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Team(
    @PrimaryKey @SerializedName("teamName") val name: String,
    @SerializedName("eventId") val eventId: Int = 0,
    @SerializedName("teamLeaderId") val teamLeaderId: String,
    @SerializedName("teamMembers") val members: List<String>,
    @SerializedName("score") val totalScore: Int
//    TODO: Define the entity
)