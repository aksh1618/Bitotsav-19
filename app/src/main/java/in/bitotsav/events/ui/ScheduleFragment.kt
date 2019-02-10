package `in`.bitotsav.events.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentScheduleBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.shared.utils.getColorCompat
import android.os.Bundle
import android.util.TypedValue
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
import org.koin.androidx.viewmodel.ext.viewModel

private var DAYS = 3

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()

    private var toast: Toast? = null

    private lateinit var sheetBehavior: BottomSheetBehavior<NestedScrollView>
    private val filterAdapter by lazy { ScheduleFilterAdapter(scheduleViewModel) }

    // TODO: Get colors from resources
    private val filterColors: List<Int> by lazy {
        listOf(
            context?.getColorCompat(R.color.colorRed) ?: 0xFF0000
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uiUtilViewModel.showBottomNav()
        // TODO: May need to account for sheet closed on swipe
        scheduleViewModel.hideFiltersSheet()
        scheduleViewModel.setupFilters()
        scheduleViewModel.filterColors = filterColors
        scheduleViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        return FragmentScheduleBinding.inflate(inflater, container, false)
            // TODO: Putting everything from here on in onActivityCreated may increase performance.
            .apply {
                lifecycleOwner = this@ScheduleFragment
                viewModel = scheduleViewModel
                bottomSheet.filterGridRecyclerView.adapter = filterAdapter
                sheetBehavior = BottomSheetBehavior.from(bottomSheet.filterSheet)
                dayPager.offscreenPageLimit = DAYS - 1
                dayPager.adapter = ScheduleDayAdapter(childFragmentManager)
                appBar.tabs.setupWithViewPager(dayPager)
                setObservers()
            }
            .root
    }

    private fun setObservers() {

        scheduleViewModel.isSheetVisible.observe(
            viewLifecycleOwner,
            Observer { isSheetVisible ->
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

        scheduleViewModel.filters.observe(viewLifecycleOwner, Observer { filters ->
            filterAdapter.submitList(filters)
            filterAdapter.notifyDataSetChanged()
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

