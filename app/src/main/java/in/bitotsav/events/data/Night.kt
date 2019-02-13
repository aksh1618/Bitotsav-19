package `in`.bitotsav.events.data

import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import android.graphics.Color

data class Night(
    val id: Int,
    val artistName: String,
    val venue: String,
    val day: Int,
    val timeString: String,
    val type: String,
    val description: String,
    val artistRes: Int,
    val posterRes: Int
): SimpleRecyclerViewAdapter.SimpleItem() {
    var bgColor = NonNullMutableLiveData(Color.WHITE)
    override fun getUniqueIdentifier() = id.toString()
}
