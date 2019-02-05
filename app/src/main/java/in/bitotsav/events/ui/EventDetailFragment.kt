package `in`.bitotsav.events.ui


import `in`.bitotsav.databinding.FragmentEventDetailBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.sharedViewModel

class EventDetailFragment : Fragment() {

    companion object {
        const val TAG = "EventDetailF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()

    private val args by navArgs<EventDetailFragmentArgs>()
    private val eventDay by lazy { args.eventDay }
    private val eventId by lazy { args.eventId }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return FragmentEventDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@EventDetailFragment
                viewModel = scheduleViewModel.apply {
                    dayWiseEventsArray[eventDay - 1].value?.let {
                        currentEvent.value = it.find { event -> event.id == eventId }
                    }
                    setObservers()
                }
            }
            .root
    }

    private fun setObservers() {
        scheduleViewModel.dayWiseEventsArray[eventDay - 1]
            .observe(viewLifecycleOwner, Observer {
                scheduleViewModel.currentEvent.value = it.find { event -> event.id == eventId }
            })
    }

}
