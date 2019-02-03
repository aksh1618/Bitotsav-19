package `in`.bitotsav.profile.ui


import `in`.bitotsav.databinding.FragmentRegistrationBinding
import `in`.bitotsav.profile.data.RegistrationFields
import `in`.bitotsav.shared.utils.onTrue
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.sharedViewModel

class RegistrationFragment : Fragment() {

    private val registrationViewModel by sharedViewModel<RegistrationViewModel>()

    companion object {
        const val TAG = "RegStepF"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "pass"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        savedInstanceState?.getString(KEY_EMAIL)?.let {
            RegistrationFields.email.text.value = it
        }
        savedInstanceState?.getString(KEY_PASSWORD)?.let {
            RegistrationFields.password.text.value = it
        }

        return FragmentRegistrationBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@RegistrationFragment
                viewModel = registrationViewModel
                // FIXME: Animate this !!
                with(registrationViewModel.currentStep) {
                    progress.progress = ((value * 100) / 3) + 1
                    observe(viewLifecycleOwner, Observer {
                        progress.progress = ((it * 100) / 3) + 1
                    })
                }
            }
            .root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        RegistrationFields.email.text.value.let {
            it.isNotEmpty().onTrue { outState.putString(KEY_EMAIL, it) }
        }
        RegistrationFields.password.text.value.let {
            it.isNotEmpty().onTrue { outState.putString(KEY_PASSWORD, it) }
        }
        Log.d(TAG, "Saved email and password state")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        with(registrationViewModel) {
            waiting.value = false
            allDone.value = false
            loggedIn.value = false
            registrationError.value = ""
            currentStep.value = 1
        }
        RegistrationFields.resetAll()
        Log.d(TAG, "Reset all fields")
        super.onDestroyView()
    }

}
