package `in`.bitotsav.profile.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentProfileBinding
import `in`.bitotsav.databinding.ItemRegistrationHistoryBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.RegistrationHistoryItem
import `in`.bitotsav.shared.ui.SimpleRecyclerViewAdapter
import `in`.bitotsav.shared.utils.executeAfter
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.setObserver
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.sharedViewModel

class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileF"
    }

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    private val adapter by lazy {
        SimpleRecyclerViewAdapter<RegistrationHistoryItem>(
            { inflater, parent, bool ->
                ItemRegistrationHistoryBinding.inflate(inflater, parent, bool)
            },
            { itemBinding, item ->
                (itemBinding as ItemRegistrationHistoryBinding).executeAfter {
                    this.item = item
                    this.color = profileViewModel.mColor
                    lifecycleOwner = this@ProfileFragment
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        context?.let {
            profileViewModel.mColor = it.getColorCompat(R.color.colorRed)
        }

        when (CurrentUser.isLoggedIn) {
            true -> profileViewModel.syncUser()
            false -> findNavController().navigate(R.id.action_destProfile_to_destLogin)
        }

        return FragmentProfileBinding.inflate(inflater, container, false)
            .apply {
                viewModel = profileViewModel
                lifecycleOwner = this@ProfileFragment
                content.registrations.adapter = adapter.apply {
                    submitList(profileViewModel.user.value?.getRegistrationHistory())
                }
                setObservers()
            }
            .root
    }

    private fun setObservers() {
        profileViewModel.user.setObserver(viewLifecycleOwner) { user ->
            Log.d(TAG, "${user?.name} received")
            with(adapter) {
                submitList(profileViewModel.user.value?.getRegistrationHistory())
                notifyDataSetChanged()
            }
        }

        profileViewModel.loggedOut.setObserver(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                profileViewModel.loggedOut.value = false
                findNavController().navigate(R.id.action_destProfile_to_destLogin)
            }
        }
    }

    override fun onDestroyView() {
        profileViewModel.waitingForLogout.value = false
        super.onDestroyView()
    }
}
