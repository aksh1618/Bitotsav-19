package `in`.bitotsav.info.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentInfoBinding
import `in`.bitotsav.info.ui.InfoPageFragment.InfoPage
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.codemybrainsout.ratingdialog.RatingDialog
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

//        context?.let { infoViewModel.mColor = it.getColorCompat(R.color.colorRed) }
        infoViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

        return FragmentInfoBinding.inflate(inflater, container, false)
            .apply {
                viewModel = infoViewModel
                lifecycleOwner = this@InfoFragment
                infoPager.offscreenPageLimit = PAGES - 1
                infoPager.adapter = InfoPageAdapter(childFragmentManager)
                appBar.tabs.setupWithViewPager(infoPager)
                feedbackFab.setOnClickListener {
                    feedback()
                }
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

    private fun feedback() {
        context?.let {
            val ratingDialog = RatingDialog.Builder(it)
                .icon(AppCompatResources.getDrawable(it, `in`.bitotsav.R.drawable.ic_bitotsav_red_24dp)!!)
                .threshold(4f)
                .title("How was your experience with us?")
                .positiveButtonText("Not Now")
                .negativeButtonText("Never")
                .formTitle("Submit Feedback")
                .formHint("Tell us where we can improve")
                .formSubmitText("Submit")
                .formCancelText("Cancel")
                .playstoreUrl("YOUR_URL")
                .onRatingBarFormSumbit { feedback ->
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("ashankanshuman.dev@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Bitotsav '19 App Feedback")
                        putExtra(Intent.EXTRA_TEXT, feedback)
                    }
                    activity?.packageManager?.let { packageManager ->
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }.build()

            ratingDialog.show()
        }
    }

}
