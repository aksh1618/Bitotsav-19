package `in`.bitotsav.events.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentScheduleBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.shared.utils.onTrue
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
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.sharedViewModel

var DAYS = 3

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()

    private var toast: Toast? = null

    private lateinit var sheetBehavior: BottomSheetBehavior<NestedScrollView>
    private lateinit var binding: FragmentScheduleBinding
    private val filterAdapter by lazy { ScheduleFilterAdapter(scheduleViewModel) }

    private val filterColors by lazy {
        context?.resources?.getIntArray(R.array.categoryColors)
            ?: intArrayOf(scheduleViewModel.mColor)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uiUtilViewModel.showBottomNav()
        // TODO [Refactor] : May need to account for sheet closed on swipe
        scheduleViewModel.hideFiltersSheet()
        scheduleViewModel.setupFilters()
        scheduleViewModel.filterColors = filterColors
        scheduleViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        binding = FragmentScheduleBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@ScheduleFragment
                viewModel = scheduleViewModel
                bottomSheet.filterGridRecyclerView.adapter = filterAdapter
                sheetBehavior = BottomSheetBehavior.from(bottomSheet.filterSheet)
                dayPager.offscreenPageLimit = DAYS
                dayPager.adapter = ScheduleDayAdapter(childFragmentManager)
                dayPager.addOnPageChangeListener(
                    object : ViewPager.SimpleOnPageChangeListener() {
                        override fun onPageSelected(position: Int) {
                            scheduleViewModel.hideFiltersSheet()
                            scheduleViewModel.filterFabVisible.value = when (position) {
                                DAYS -> false
                                else -> true
                            }
                        }
                    })

                appBar.tabs.setupWithViewPager(dayPager)
                setObservers()
            }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // To prevent hidden fab when user navigates away from nights and comes back
        (binding.dayPager.currentItem < DAYS).onTrue {
            binding.filterFab.show()
        }
    }

    private fun setObservers() {

        scheduleViewModel.isSheetVisible.observe(
            viewLifecycleOwner,
            Observer { isSheetVisible ->
                sheetBehavior.state = when (isSheetVisible) {
                    true -> BottomSheetBehavior.STATE_EXPANDED
                    else -> BottomSheetBehavior.STATE_HIDDEN
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
        override fun getCount() = DAYS + 1
        override fun getItem(position: Int): Fragment {
            return ScheduleDayFragment.newInstance(position + 1)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                DAYS -> return "Nights"
                else -> "Day ${position + 1}"
            }
        }
    }
}

