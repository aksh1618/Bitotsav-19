package `in`.bitotsav.profile.ui


import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentChampionshipRegistrationBinding
import `in`.bitotsav.shared.utils.onFalse
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
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ChampionshipRegistrationFragment : Fragment() {

    companion object {
        private const val TAG = "ChampionshipRegF"
        private const val KEY_BACK_PRESSED = "back_pressed"
    }

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    private val adapter by lazy {
        TeamRegistrationAdapter(
            this@ChampionshipRegistrationFragment,
            profileViewModel
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.getBoolean(KEY_BACK_PRESSED)?.onFalse {
            Log.v(TAG, "Preserving instance state.")
        } ?: run {
            // Clear fields if back was pressed
            Log.d(TAG, "Clearing fields")
            profileViewModel.prepareForRegistration()
        }

        return FragmentChampionshipRegistrationBinding.inflate(inflater, container, false)
            .apply {
                viewModel = profileViewModel
                memberDetails.adapter = adapter.apply {
                    submitList(profileViewModel.membersToRegister)
                }
                lifecycleOwner = this@ChampionshipRegistrationFragment
                setObservers()
            }
            .root
    }

    private fun setObservers() {
        with(profileViewModel) {

            championshipTeamRegistered.setObserver(viewLifecycleOwner) { registered ->
                if (registered) {
                    // TODO: Show success in some way
                    this@ChampionshipRegistrationFragment.toast(
                        getString(
                            R.string.profile_format_registration_message,
                            teamName.text.value
                        )
                    )
                    findNavController().navigateUp()
                }
            }

            numMembersString.setObserver(viewLifecycleOwner) { numMembers ->
                generateMembersToRegister(numMembers.toInt())
                adapter.submitList(membersToRegister)
                adapter.notifyDataSetChanged()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_BACK_PRESSED, false)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        profileViewModel.championshipTeamRegistered.value = false
        super.onDestroyView()
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}