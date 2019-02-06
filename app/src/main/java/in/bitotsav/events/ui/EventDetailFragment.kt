package `in`.bitotsav.events.ui


import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentEventDetailBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.setObserver
import android.content.Intent
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Hide bottom nav, this fragment has its own bottom bar
        uiUtilViewModel.hideBottomNav()
        eventViewModel.mColor = context?.getColorCompat(R.color.colorRed) ?: 0xFF0000

        return FragmentEventDetailBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@EventDetailFragment
                viewModel = eventViewModel.apply { setCurrentEvent(eventId) }
                setObservers()
                setClickListeners(this)
            }
            .root
    }

    private fun setObservers() {

        scheduleViewModel.dayWiseEventsArray.forEach {
            it.setObserver(viewLifecycleOwner) {
                eventViewModel.setCurrentEvent(eventId)
            }
        }

        eventViewModel.deregistrationError.setObserver(viewLifecycleOwner) { error ->
            error.isNotBlank().onTrue {
                toast(error)
                eventViewModel.deregistrationError.value = ""
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
            val link = "TODO"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT, "Have you heard about " +
                            "${eventViewModel.currentEvent.value?.name} ? Check it out! $link"
                )
            }
            val shareTitle = getString(
                R.string.event_format_share_title,
                eventViewModel.currentEvent.value?.name ?: ""
            )
            startActivity(Intent.createChooser(shareIntent, shareTitle))
        }

        binding.register.setOnClickListener {

            CurrentUser.isLoggedIn.onFalse {
                toast("Not Logged In !")
                return@setOnClickListener
            }

            eventViewModel.isUserRegistered.value
                .onTrue {
                    // FIXME [WARN]: Add Confirmation Dialog
                    eventViewModel.deregister()
                }
                .onFalse {
                    // TODO :
                    findNavController().navigate(
                        EventDetailFragmentDirections.registerForEvent().setEventId(eventId)
                    )
                }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
