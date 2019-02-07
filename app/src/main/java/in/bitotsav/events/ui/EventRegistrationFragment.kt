package `in`.bitotsav.events.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentEventRegistrationBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import `in`.bitotsav.shared.utils.setObserver
import `in`.bitotsav.teams.ui.TeamRegistrationAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.sharedViewModel

class EventRegistrationFragment : Fragment() {

    companion object {
        private const val TAG = "EventRegF"
        private const val KEY_BACK_PRESSED = "back_pressed"
    }

    private val eventViewModel by sharedViewModel<EventViewModel>()

    private val args by navArgs<EventRegistrationFragmentArgs>()
    private val eventId by lazy { args.eventId }
    val adapter by lazy {
        TeamRegistrationAdapter(
            this@EventRegistrationFragment,
            eventViewModel
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        Boolean.or(
            (CurrentUser.isLoggedIn.not()).onTrue { toast("Not logged in") },
            (eventViewModel.currentEvent.value == null).onTrue { toast("Event not found!") }
        ).onTrue { findNavController().navigateUp() }

        savedInstanceState?.getBoolean(KEY_BACK_PRESSED)?.onFalse {
            Log.v(TAG, "Preserving instance state.")
        } ?: run {
            // Clear fields if back was pressed
            Log.v(TAG, "Clearing fields")
            eventViewModel.prepareForRegistration(eventViewModel.currentEvent.value!!)
        }

        return FragmentEventRegistrationBinding.inflate(inflater, container, false)
            .apply {
                viewModel = eventViewModel
                memberDetails.adapter = adapter.apply {
                    submitList(eventViewModel.membersToRegister)
                }
                lifecycleOwner = this@EventRegistrationFragment
                setObservers()
            }
            .root
    }

    private fun setObservers() {
        with(eventViewModel) {

            numMembersString.setObserver(viewLifecycleOwner) { numMembers ->
                generateMembersToRegister(numMembers.toInt())
                adapter.submitList(membersToRegister)
                adapter.notifyDataSetChanged()
            }

            isUserRegistered.setObserver(viewLifecycleOwner) { registered ->
                if (registered) {
                    // TODO: Show success in some way
                    this@EventRegistrationFragment.toast(
                        getString(
                            R.string.event_format_registration_message,
                            eventViewModel.currentEvent.value?.name ?: ""
                        )
                    )
                    findNavController().navigateUp()
                }
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_BACK_PRESSED, false)
        super.onSaveInstanceState(outState)
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}

// FIXME [WARN]: Ask for confirmation before going back from registration