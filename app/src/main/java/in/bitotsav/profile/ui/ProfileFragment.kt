package `in`.bitotsav.profile.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentProfileBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.toast
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.viewModel

class ProfileFragment : Fragment() {

    private val profileViewModel by viewModel<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        context?.let {
            profileViewModel.mainColor = it.getColorCompat(R.color.colorRed)
        }
        profileViewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            this@ProfileFragment.context?.let { message.toast(it) }
        })
        profileViewModel.user.observe(viewLifecycleOwner, Observer {
            Log.d("user observer", "${it.name} received")
        })

        when (CurrentUser.isLoggedIn) {
            true -> profileViewModel.syncUser()
            false -> findNavController().navigate(R.id.action_destProfile_to_destLogin)
        }

        return FragmentProfileBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@ProfileFragment
                // TODO: May not be needed.
                viewModel = profileViewModel
            }
            .root
    }
}
