package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.FragmentScheduleDayBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ScheduleDayFragment : Fragment() {

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
                scheduleRecyclerView.apply {
                    adapter = ScheduleAdapter(viewLifecycleOwner).apply {
                        scheduleViewModel.dayWiseEventsArray[day - 1].observe(viewLifecycleOwner, Observer { events ->
                            submitList(events)
                            notifyDataSetChanged()
                        })
                    }
                }
            }
        return binding.root
    }

    companion object {
        private const val ARG_DAY = "day"
        fun newInstance(day: Int) = ScheduleDayFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_DAY, day)
            }
        }
    }
}
