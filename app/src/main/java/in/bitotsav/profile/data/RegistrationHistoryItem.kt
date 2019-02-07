package `in`.bitotsav.profile.data

import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter

class RegistrationHistoryItem(
    val index: Int,
    val eventName: String,
    val teamName: String,
    val rank: String
) : SimpleRecyclerViewAdapter.SimpleItem(index)