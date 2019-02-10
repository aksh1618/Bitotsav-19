package `in`.bitotsav.info.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentInfoPageAboutBinding
import `in`.bitotsav.databinding.FragmentInfoPageBitotsavBinding
import `in`.bitotsav.databinding.FragmentInfoPageContactBinding
import `in`.bitotsav.info.ui.InfoPageFragment.InfoPage.*
import `in`.bitotsav.shared.utils.getColorCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.sharedViewModel

class InfoPageFragment : Fragment() {

    companion object {
        private const val TAG = "InfoPageF"
        private const val ARG_PAGE = "page"

        fun newInstance(page: InfoPage) = InfoPageFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_PAGE, page)
            }
        }
    }

    private val page: InfoPage by lazy {
        (arguments?.getSerializable(ARG_PAGE) ?: error("Invalid page argument")) as InfoPage
    }

    private val infoViewModel by sharedViewModel<InfoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        context?.let { infoViewModel.mColor = it.getColorCompat(R.color.colorRed) }

        return when (page) {

            BITOTSAV -> FragmentInfoPageBitotsavBinding
                .inflate(inflater, container, false)
                .apply { color = infoViewModel.mColor }

            CONTACT -> FragmentInfoPageContactBinding
                .inflate(inflater, container, false)
                .apply { color = infoViewModel.mColor }

            ABOUT -> FragmentInfoPageAboutBinding
                .inflate(inflater, container, false)
                .apply { color = infoViewModel.mColor }

        }.root
    }

    enum class InfoPage(val title: Int) {
        BITOTSAV(R.string.info_title_bitotsav_bit),
        CONTACT(R.string.info_title_contact),
        ABOUT(R.string.info_title_about_app)
    }

}
