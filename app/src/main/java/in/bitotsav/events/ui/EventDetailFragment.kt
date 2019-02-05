package `in`.bitotsav.events.ui


import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentEventDetailBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.sharedViewModel

class EventDetailFragment : Fragment() {

    companion object {
        const val TAG = "EventDetailF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()

    private val args by navArgs<EventDetailFragmentArgs>()
    private val eventDay by lazy { args.eventDay }
    private val eventId by lazy { args.eventId }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        uiUtilViewModel.hideBottomNav()
        return FragmentEventDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@EventDetailFragment
                viewModel = scheduleViewModel.apply {
                    dayWiseEventsArray[eventDay - 1].value?.let {
                        currentEvent.value = it.find { event -> event.id == eventId }
                    }
                    setObservers()
                }
                setClickListeners(this)
            }
            .root
    }

    private fun setObservers() {
        scheduleViewModel.dayWiseEventsArray[eventDay - 1]
            .observe(viewLifecycleOwner, Observer {
                scheduleViewModel.currentEvent.value = it.find { event -> event.id == eventId }
            })
    }

    private fun setClickListeners(binding: FragmentEventDetailBinding) {

        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.share.setOnClickListener {
            // FIXME [WARN] : Use deep link here.
            val link = "TODO"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT, "Have you heard about " +
                            "${scheduleViewModel.currentEvent.value?.name} ? Check it out! $link"
                )
            }
            val shareTitle = getString(
                R.string.event_format_share_title,
                scheduleViewModel.currentEvent.value?.name ?: ""
            )
            startActivity(Intent.createChooser(shareIntent, shareTitle))
        }
    }

}
