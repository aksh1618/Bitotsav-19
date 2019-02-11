package `in`.bitotsav.events.ui


import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentEventDetailBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.shared.utils.*
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.sharedViewModel

class EventDetailFragment : Fragment() {

    companion object {
        const val TAG = "EventDetailF"
    }

    private val scheduleViewModel by sharedViewModel<ScheduleViewModel>()
    private val eventViewModel by sharedViewModel<EventViewModel>()
    private val uiUtilViewModel by sharedViewModel<UiUtilViewModel>()

    private val args by navArgs<EventDetailFragmentArgs>()
    private val eventId by lazy { args.eventId }
    private lateinit var binding: FragmentEventDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Hide bottom nav, this fragment has its own bottom bar
        uiUtilViewModel.hideBottomNav()
        eventViewModel.mColor = TypedValue().apply {
            activity?.theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data

//        mColor = context?.getColorCompat(R.color.colorRed) ?: 0xFF0000

        binding = FragmentEventDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@EventDetailFragment
                viewModel = eventViewModel.apply { setCurrentEvent(eventId) }
                setObservers()
                setClickListeners(this)
            }
        return binding.root
    }

    private fun setObservers() {

        eventViewModel.toastMessage.setObserver(viewLifecycleOwner) { message ->
            message.isNotBlank().onTrue {
                toast(message)
                eventViewModel.toast("")
            }
        }

        scheduleViewModel.dayWiseEventsArray.forEach {
            it.setObserver(viewLifecycleOwner) {
                eventViewModel.setCurrentEvent(eventId)
            }
        }

        eventViewModel.deregistrationError.setObserver(viewLifecycleOwner) { error ->
            error.isNotBlank().onTrue {
                toast(error)
                eventViewModel.deregistrationError.value = ""
                binding.register.text = getString(R.string.event_label_deregister)
            }
        }

    }

    // TODO [Refactor]: Should be handled as menu or by live data events
    private fun setClickListeners(binding: FragmentEventDetailBinding) {

        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.share.setOnClickListener {
            // FIXME [WARN] : Use deep link here.
            eventViewModel.currentEvent.value?.let {
                val link = "bitotsav.in/event/${it.id}"
                val textToShare = getString(R.string.event_format_share_text, it.name, link)
                val shareTitle = getString(R.string.event_format_share_title, it.name)
                context?.shareText(shareTitle, textToShare)
            }
        }

        binding.register.setOnClickListener {

            eventViewModel.user.value?.let {
                eventViewModel.isUserAlreadyRegistered
                    .onTrue {
                        Log.d(TAG, "Deregistering...")
                        // FIXME [WARN]: Add Confirmation Dialog
                        toast("Deregistering...")
                        eventViewModel.deregister()
                    }
                    .onFalse {
                        findNavController().navigate(
                            EventDetailFragmentDirections.registerForEvent().setEventId(eventId)
                        )
                    }
            } ?: run {
                Log.v(TAG, "Attempting registration without logging in.")
                toast("Not Logged In !")
            }
        }
    }

    override fun onDestroyView() {
        uiUtilViewModel.showBottomNav()
        super.onDestroyView()
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
