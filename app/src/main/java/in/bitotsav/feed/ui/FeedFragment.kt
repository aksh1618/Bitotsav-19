package `in`.bitotsav.feed.ui

import `in`.bitotsav.NavBitotsavDirections
import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentFeedBinding
import `in`.bitotsav.databinding.ItemFeedBinding
import `in`.bitotsav.databinding.ItemRegistrationHistoryBinding
import `in`.bitotsav.feed.data.Feed
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.setObserver
import `in`.bitotsav.shared.utils.shareText
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.viewModel


class FeedFragment : Fragment() {

    private val feedViewModel by viewModel<FeedViewModel>()

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<Feed>(
            { inflater, parent, bool ->
                ItemFeedBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, feedItem ->
                (itemBinding as ItemFeedBinding).executeAfter {
                    this.feed = feedItem
                    this.color = feedViewModel.mColor
                    this.listener = getFeedItemListener(feedItem)
                    this.shareListener = getFeedItemShareListener(feedItem)
                    lifecycleOwner = this@FeedFragment
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        context?.let {
            feedViewModel.mColor = it.getColorCompat(R.color.colorRed)
        }

        return FragmentFeedBinding.inflate(inflater, container, false)
            .apply {
                viewModel = feedViewModel
                feed.adapter = adapter.apply {
                    submitList(feedViewModel.feed.value)
                }
                lifecycleOwner = viewLifecycleOwner
                setObservers()
            }
            .root
    }

    private fun setObservers() {
        feedViewModel.feed.setObserver(viewLifecycleOwner) { feed ->
            with(adapter) {
                submitList(feed)
                notifyDataSetChanged()
            }
        }
    }

    private fun getFeedItemListener(feedItem: Feed) = View.OnClickListener {
        feedItem.eventId?.let { eventId ->
            it.findNavController().navigate(
                NavBitotsavDirections.actionGlobalDestEventDetail(eventId)
            )
        } ?: run {}
    }

    private fun getFeedItemShareListener(feedItem: Feed) = View.OnClickListener {
        it.context.shareText(
            getString(R.string.feed_format_share_title, feedItem.getTypeLabel()),
            getString(R.string.feed_format_share_text, feedItem.content)
        )
    }

}
