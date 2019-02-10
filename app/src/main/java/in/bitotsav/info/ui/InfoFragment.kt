package `in`.bitotsav.info.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentInfoBinding
import `in`.bitotsav.info.ui.InfoPageFragment.InfoPage
import `in`.bitotsav.shared.utils.getColorCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.koin.androidx.viewmodel.ext.sharedViewModel


class InfoFragment : Fragment() {

    companion object {
        private const val TAG = "InfoF"
        private const val PAGES = 3
    }

    private val infoViewModel by sharedViewModel<InfoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        context?.let { infoViewModel.mColor = it.getColorCompat(R.color.colorRed) }

        return FragmentInfoBinding.inflate(inflater, container, false)
            .apply {
                viewModel = infoViewModel
                lifecycleOwner = this@InfoFragment
                infoPager.offscreenPageLimit = PAGES - 1
                infoPager.adapter = InfoPageAdapter(childFragmentManager)
                appBar.tabs.setupWithViewPager(infoPager)
            }
            .root

    }

    inner class InfoPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount() = InfoPage.values().size

        override fun getItem(position: Int): Fragment {
            return InfoPageFragment.newInstance(InfoPage.values()[position])
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getString(InfoPage.values()[position].title)
        }
    }

}
