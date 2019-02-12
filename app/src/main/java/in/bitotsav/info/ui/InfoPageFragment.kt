package `in`.bitotsav.info.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentInfoPageAboutBinding
import `in`.bitotsav.databinding.FragmentInfoPageBitotsavBinding
import `in`.bitotsav.databinding.FragmentInfoPageContactBinding
import `in`.bitotsav.info.ui.InfoPageFragment.InfoPage.*
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.smarteist.autoimageslider.*
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

//        context?.let { infoViewModel.mColor = it.getColorCompat(R.color.colorRed) }
        infoViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        return when (page) {

            BITOTSAV -> FragmentInfoPageBitotsavBinding
                .inflate(inflater, container, false)
                .apply {
                    color = infoViewModel.mColor
                    gallerySlider.setPagerIndicatorVisibility(false)
                    gallerySlider.apply {
                        setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION)
                        scrollTimeInSec = 1
                        setIndicatorAnimation(IndicatorAnimations.THIN_WORM)
                        addSliderViews(this)
                    }
                }

            CONTACT -> FragmentInfoPageContactBinding
                .inflate(inflater, container, false)
                .apply { color = infoViewModel.mColor }

            ABOUT -> FragmentInfoPageAboutBinding
                .inflate(inflater, container, false)
                .apply { color = infoViewModel.mColor }

        }.root
    }

    private fun addSliderViews(sliderLayout: SliderLayout) {
        galleryDrawables.shuffled().forEach {
            DefaultSliderView(context).apply {
                setImageDrawable(it)
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }.let { sliderLayout.addSliderView(it) }
        }
    }

    enum class InfoPage(val title: Int) {
        BITOTSAV(R.string.info_title_bitotsav_bit),
        CONTACT(R.string.info_title_contact),
        ABOUT(R.string.info_title_about_app)
    }

    val galleryDrawables = listOf(
        R.drawable.img_gallery1,
        R.drawable.img_gallery2,
        R.drawable.img_gallery3,
        R.drawable.img_gallery4,
//        R.drawable.img_gallery5,
        R.drawable.img_gallery6,
        R.drawable.img_gallery7,
//        R.drawable.img_gallery8,
        R.drawable.img_gallery9,
        R.drawable.img_gallery10,
        R.drawable.img_gallery11,
//        R.drawable.img_gallery12,
        R.drawable.img_gallery13,
        R.drawable.img_gallery14,
        R.drawable.img_gallery15,
        R.drawable.img_gallery16,
        R.drawable.img_gallery17,
        R.drawable.img_gallery18,
        R.drawable.img_gallery19,
        R.drawable.img_gallery20,
        R.drawable.img_gallery21
    )

}
