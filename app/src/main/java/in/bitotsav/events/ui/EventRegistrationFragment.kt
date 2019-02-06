package `in`.bitotsav.events.ui

import `in`.bitotsav.databinding.FragmentEventRegistrationBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import `in`.bitotsav.shared.utils.setObserver
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
    }

    private val eventViewModel by sharedViewModel<EventViewModel>()

    private val args by navArgs<EventRegistrationFragmentArgs>()
    private val eventId by lazy { args.eventId }
    val adapter by lazy {
        EventRegistrationAdapter(
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

        return FragmentEventRegistrationBinding.inflate(inflater, container, false)
            .apply {
                viewModel = eventViewModel
                memberDetails.adapter = adapter.apply {
                    submitList(eventViewModel.membersToRegister)
                }
                lifecycleOwner = this@EventRegistrationFragment
                setObservers()
                Log.d(TAG, "How to ")
            }
            .root
    }

    private fun setObservers() {
        with(eventViewModel) {

            numMembersString.setObserver(viewLifecycleOwner) { numMembers ->
                generateMembersToRegister(numMembers.toInt())
                membersToRegister.forEach {
                    Log.d(
                        TAG,
                        "Member for recycler view: ${it.bitotsavId.text.value}, ${it.bitotsavId.errorText.value}"
                    )
                }
                adapter.submitList(membersToRegister)
                adapter.notifyDataSetChanged()
            }

            isUserRegistered.setObserver(viewLifecycleOwner) { registered ->
                if (registered) {
                    // TODO: Show success in some way
                    toast("Registration Successful !!")
                    findNavController().navigateUp()
                }
            }

        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
