package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.FragmentScheduleBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.sharedViewModel

private var DAYS = 3

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()
    private lateinit var binding: FragmentScheduleBinding
    private var toast: Toast? = null
    private lateinit var sheetBehavior: BottomSheetBehavior<NestedScrollView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiUtilViewModel.showBottomNav()
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
            // TODO: Putting everything from here on in onActivityCreated may increase performance.
            .apply {
                lifecycleOwner = this@ScheduleFragment
                viewModel = scheduleViewModel
                bottomSheet.filterGridRecyclerView.apply {
                    adapter = ScheduleFilterAdapter(scheduleViewModel).apply { setListObserver(this) }
                    // This causes the adapter to not populate holders on coming back from another fragment.
                    // setHasFixedSize(true)
                }
                dayPager.offscreenPageLimit = DAYS - 1
                dayPager.adapter = ScheduleDayAdapter(childFragmentManager)
                appBar.tabs.setupWithViewPager(dayPager)
            }
        sheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.filterSheet)
        // TODO: May need to account for sheet closed on swipe
        scheduleViewModel.hideFiltersSheet()
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

    private fun setListObserver(adapter: ScheduleFilterAdapter) {
        scheduleViewModel.filters.observe(viewLifecycleOwner, Observer { filters ->
            adapter.submitList(filters)
        })
    }

    inner class ScheduleDayAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount() = DAYS
        override fun getItem(position: Int): Fragment {
            return ScheduleDayFragment.newInstance(position + 1)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return "Day ${position + 1}"
        }
    }
}
