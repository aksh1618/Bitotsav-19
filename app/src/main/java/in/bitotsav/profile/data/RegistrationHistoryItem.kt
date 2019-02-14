package `in`.bitotsav.profile.data

import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter

data class RegistrationHistoryItem(
    val eventId: Int,
    val eventName: String,
    val teamName: String,
    val rank: String,
    val members: List<String>
) : SimpleRecyclerViewAdapter.SimpleItem() {
    override fun getUniqueIdentifier() = eventId.toString()
}