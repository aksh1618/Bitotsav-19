package `in`.bitotsav.events.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentScheduleDayBinding
import `in`.bitotsav.databinding.ItemEventBinding
import `in`.bitotsav.databinding.ItemNightBinding
import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.Night
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.GlideApp
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.setObserver
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ScheduleDayFragment : Fragment() {

    companion object {
        private const val TAG = "SchedDayF"
        private const val ARG_DAY = "day"
        fun newInstance(day: Int) = ScheduleDayFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_DAY, day)
            }
        }
    }

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<Event>(
            { inflater, parent, bool ->
                ItemEventBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, eventItem ->
                (itemBinding as ItemEventBinding).executeAfter {
                    this.event = eventItem
                    this.color = scheduleViewModel.mColor
                    this.listener = getEventItemListener(eventItem)
                    lifecycleOwner = this@ScheduleDayFragment
                }
            }
        )
    }

    private val nightAdapter by lazy {
        SimpleRecyclerViewAdapter<Night>(
            { inflater, parent, bool ->
                ItemNightBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, nightItem ->
                (itemBinding as ItemNightBinding).executeAfter {
                    loadImageAndColor(nightItem, itemBinding)
                    this.night = nightItem
                    this.listener = getNightItemListener(nightItem)
                    lifecycleOwner = this@ScheduleDayFragment
                }
            }
        )
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val nightViewModel by sharedViewModel<NightViewModel>()
    private val day: Int by lazy {
        arguments?.getInt(ARG_DAY) ?: throw IllegalStateException("No day argument")
    }
    private lateinit var binding: FragmentScheduleDayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleDayBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@ScheduleDayFragment
                scheduleRecyclerView.adapter = when (day) {
                    DAYS + 1 -> nightAdapter.apply {
                        nightViewModel.nightsList = getNightsList()
                        submitList(nightViewModel.nightsList)
                    }
                    else -> adapter.apply {
                        // TODO [Refactor]: is this necessary ?
                        submitList(scheduleViewModel.dayWiseEventsArray[day - 1].value)
                        setObservers()
                    }
                }
            }
        return binding.root
    }

    private fun getNightsList(): List<Night> {
        val nightArtists = resources.getStringArray(R.array.night_artists)
        val nightTypes = resources.getStringArray(R.array.night_types)
        val nightVenues = resources.getStringArray(R.array.night_venues)
        val nightTimes = resources.getStringArray(R.array.night_times)
        val nightDescriptions = resources.getStringArray(R.array.night_descriptions)
        val nightPosterDrawables = resources.obtainTypedArray(R.array.night_poster_drawables)
        val nightArtistDrawables = resources.obtainTypedArray(R.array.night_artist_drawables)
        val nightDays = resources.getIntArray(R.array.night_days)
        return (1..nightArtists.size).map { i ->
            Night(
                i,
                nightArtists[i - 1],
                nightVenues[i - 1],
                nightDays[i - 1],
                nightTimes[i - 1],
                nightTypes[i - 1],
                nightDescriptions[i - 1],
                nightArtistDrawables.getResourceId(i - 1, -1),
                nightPosterDrawables.getResourceId(i - 1, -1)
            )
        }.apply { nightArtistDrawables.recycle(); nightPosterDrawables.recycle() }
    }


    private fun loadImageAndColor(nightItem: Night, binding: ItemNightBinding) {
        GlideApp.with(binding.artistImage.context)
            .asBitmap()
            .load(nightItem.artistRes)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap?>?,
                    isFirstResource: Boolean
                ) = false

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { bitmap ->
                        // nightItem.bgColor.value =
                        binding.color =
                            Palette.from(bitmap).generate().getDarkVibrantColor(Color.RED)
                    }
                    return false
                }
            })
            .into(binding.artistImage)
    }

    private fun setObservers() {

        scheduleViewModel.dayWiseEventsArray[day - 1].setObserver(viewLifecycleOwner) {
            with(adapter) {
                submitList(it)
                notifyDataSetChanged()
            }
        }
    }

    private fun getEventItemListener(eventItem: Event) = View.OnClickListener {
        it.findNavController().navigate(
            ScheduleFragmentDirections.showEventDetail(eventItem.id)
        )
    }

    private fun getNightItemListener(nightItem: Night) = View.OnClickListener {
        it.findNavController().navigate(
            ScheduleFragmentDirections.showNightDetail(nightItem.id)
        )
    }

}
