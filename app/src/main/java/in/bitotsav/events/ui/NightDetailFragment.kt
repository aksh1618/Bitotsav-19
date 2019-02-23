package `in`.bitotsav.events.ui


import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentNightDetailBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.shared.utils.GlideApp
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.koin.androidx.viewmodel.ext.sharedViewModel

class NightDetailFragment : Fragment() {

    companion object {
        const val TAG = "NightDetailF"
    }

    private val nightViewModel by sharedViewModel<NightViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()

    private val args by navArgs<NightDetailFragmentArgs>()
    private val nightId by lazy { args.nightId }
    private lateinit var binding: FragmentNightDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Hide bottom nav, this fragment has its own bottom bar
        uiUtilViewModel.hideBottomNav()
        nightViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        binding = FragmentNightDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@NightDetailFragment
                viewModel = nightViewModel.apply { setCurrentNight(nightId) }
                val heightToSet = DisplayMetrics().apply {
                    activity?.windowManager?.defaultDisplay?.getMetrics(this)
                }.heightPixels * 0.6
                content.artistPoster.updateLayoutParams { height = heightToSet.toInt() }
                context?.let {
                    GlideApp.with(it)
                        .asBitmap()
                        .load(nightViewModel.currentNight.value?.posterRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(content.artistPoster)
                }
            }
        return binding.root
    }

    // TODO [Refactor] : Create a back button somewhere
//    private fun setClickListeners(binding: FragmentEventDetailBinding) {
//
//        binding.back.setOnClickListener {
//            findNavController().navigateUp()
//        }
//
//    }

    override fun onDestroyView() {
        uiUtilViewModel.showBottomNav()
        super.onDestroyView()
    }

}
