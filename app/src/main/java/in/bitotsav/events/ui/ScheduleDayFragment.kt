package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.FragmentScheduleDayBinding
import `in`.bitotsav.databinding.ItemEventBinding
import `in`.bitotsav.events.data.Event
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.setObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ScheduleDayFragment : Fragment() {

    companion object {
        private const val TAG = "SchedDayF"
        private const val ARG_DAY = "day"
        fun newInstance(day: Int) = ScheduleDayFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_DAY, day)
            }
        }
    }

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<Event>(
            { inflater, parent, bool ->
                ItemEventBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, eventItem ->
                (itemBinding as ItemEventBinding).executeAfter {
                    this.event = eventItem
                    this.color = scheduleViewModel.mColor
                    this.listener = getEventItemListener(eventItem)
                    lifecycleOwner = this@ScheduleDayFragment
                }
            }
        )
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val day: Int by lazy {
        arguments?.getInt(ARG_DAY) ?: throw IllegalStateException("No day argument")
    }
    private lateinit var binding: FragmentScheduleDayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleDayBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@ScheduleDayFragment
                scheduleRecyclerView.adapter = adapter.apply {
                    // TODO [Refactor]: is this necessary ?
                    submitList(scheduleViewModel.dayWiseEventsArray[day - 1].value)
                }
                setObservers()
            }
        return binding.root
    }

    private fun setObservers() {

        scheduleViewModel.dayWiseEventsArray[day - 1].setObserver(viewLifecycleOwner) {
            with(adapter) {
                submitList(it)
                notifyDataSetChanged()
            }
        }
    }

    private fun getEventItemListener(eventItem: Event) = View.OnClickListener {
        it.findNavController().navigate(
            ScheduleFragmentDirections.showEventDetail(eventItem.id)
        )
    }

}
