package `in`.bitotsav.teams.championship.data

import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class ChampionshipTeam(
    @PrimaryKey @SerializedName("teamName") val name: String,
    @SerializedName("teamMembers") val members: Map<String, String>,
    @SerializedName("teamPoints") val totalScore: Int,
    @Expose(serialize = false, deserialize = false) var rank: Int = 1
): SimpleRecyclerViewAdapter.SimpleItem() {
    override fun getUniqueIdentifier() = name
}