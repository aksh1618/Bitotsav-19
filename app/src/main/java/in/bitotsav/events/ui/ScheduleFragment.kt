package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.FragmentScheduleBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.viewModel


class ScheduleFragment : Fragment() {

    private val scheduleViewModel by viewModel<ScheduleViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentScheduleBinding.inflate(inflater, container, false)
        binding.scheduleRecyclerView.adapter = ScheduleAdapter().apply { setObserver(this) }
        binding.filterFab.setOnClickListener {
            with(scheduleViewModel) {
                when (isFilterActive()) {
                    true -> clearFilter()
                    false -> filterByCategories(*getCategories().toTypedArray())
                }
            }
        }
        return binding.root
    }

    private fun setObserver(adapter: ScheduleAdapter) {
        scheduleViewModel.getVisibleEvents().observe(viewLifecycleOwner, Observer { events ->
            events?.let { adapter.submitList(it) }
        })
    }

    // TODO: Get from bottom sheet
    private fun getCategories() = listOf("Music", "Dance")
}
