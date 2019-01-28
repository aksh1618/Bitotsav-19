package `in`.bitotsav.events.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentScheduleBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.viewModel


class ScheduleFragment : Fragment() {

    private val scheduleViewModel by viewModel<ScheduleViewModel>()
    private lateinit var binding: FragmentScheduleBinding
    private var toast: Toast? = null
    private val sheetBehavior by lazy {
        BottomSheetBehavior.from(binding.bottomSheet.layout)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel.filterColors = getFilterColors()
        binding = FragmentScheduleBinding
            .inflate(inflater, container, false)
            // TODO: Putting everything from here on in onActivityCreated may increase performance.
            .apply {
                lifecycleOwner = this@ScheduleFragment
                viewModel = scheduleViewModel
                scheduleRecyclerView.apply {
                    adapter = ScheduleAdapter().apply { setListObserver(this) }
                }
                bottomSheet.filterGridRecyclerView.apply {
                    adapter = ScheduleFilterAdapter(scheduleViewModel).apply { setListObserver(this) }
                    // This causes the adapter to not populate holders on coming back from another fragment.
                    // setHasFixedSize(true)
                }
            }
        // TODO: May need to account for sheet closed on swipe
        scheduleViewModel.isSheetVisible.observe(viewLifecycleOwner, Observer { isSheetVisible ->
            when (isSheetVisible) {
                true -> sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                else -> sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })
        scheduleViewModel.toastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            if (!toastMessage.isNullOrEmpty()) {
                toast?.cancel()
                toast = Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
                toast?.show()
            }
        })
        return binding.root
    }

    // TODO: Get colors from resources
    private fun getFilterColors(): List<Int> =
        listOf(context?.let { ContextCompat.getColor(context!!, R.color.colorRed) } ?: 0)

    private fun <T> setListObserver(adapter: ListAdapter<T, *>) {
        val liveDataListToObserve = when (adapter) {
            is ScheduleAdapter -> scheduleViewModel.events
            is ScheduleFilterAdapter -> scheduleViewModel.filters
            else -> throw IllegalArgumentException("Unsupported adapter passed to setListObserver in ScheduleFragment")
        }
        liveDataListToObserve.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                adapter.submitList(it as List<T>)
            }
        })
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        binding.viewModel = scheduleViewModel
//        val sheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.layout)
//        binding.bottomSheet.filterGridRecyclerView.apply {
//            adapter = ScheduleFilterAdapter(scheduleViewModel).apply { setListObserver(this) }
//            setHasFixedSize(true)
//        }

//        sheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//            }
//
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_EXPANDED ->
//                }
//            }
//        })

//        filterAdapter = ScheduleFilterAdapter(scheduleViewModel)
//        scheduleViewModel.eventFilters.observe(this, Observer { filterAdapter.submitEventFilterList(it) })
//    }
}
