package `in`.bitotsav.events.ui


import `in`.bitotsav.databinding.FragmentEventDetailBinding
import android.os.Bundle
import android.util.Log
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
    private val eventDay by lazy {
        args.eventDay
//        arguments?.let {
//            EventDetailFragmentArgs.fromBundle(it).eventDay.apply {
//                Log.v(TAG, "SafeArgs: Event day $this")
//            }
//        } ?: 0.apply { Log.e(TAG, "SafeArgs betrayed us") }
    }

    private val eventIndex by lazy {
        args.eventIndex
//        arguments?.let {
//            EventDetailFragmentArgs.fromBundle(it).eventIndex.apply {
//                Log.v(TAG, "SafeArgs: Event index $this")
//            }
//        } ?: 0.apply { Log.e(TAG, "SafeArgs betrayed us") }
    }
    private var eventId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return FragmentEventDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@EventDetailFragment
                viewModel = scheduleViewModel.apply {
                    with(dayWiseEventsArray[eventDay - 1]) {
                        currentEvent.value = value!![eventIndex]
                        eventId = value!![eventIndex].id
                    }
                    setObservers()
                }
            }
            .root
    }

    private fun setObservers() {
        scheduleViewModel.dayWiseEventsArray[eventDay - 1]
            .observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "Detected change in event ${it[eventIndex].name}")
                scheduleViewModel.currentEvent.value = when (eventId) {
                    it[eventIndex].id -> it[eventIndex]
                    // Might happen if event is added / deleted.
                    else -> it.find { event -> event.id == eventId }

                }
            })
    }

}
