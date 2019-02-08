package `in`.bitotsav.feed.ui

import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.shared.ui.BaseViewModel

class FeedViewModel(feedRepository: FeedRepository) : BaseViewModel() {

    val feed = feedRepository.getAll()

}
