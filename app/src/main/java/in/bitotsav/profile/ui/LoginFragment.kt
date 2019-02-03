package `in`.bitotsav.profile.ui

import `in`.bitotsav.R
import `in`.bitotsav.databinding.FragmentLoginBinding
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.utils.getColorCompat
import `in`.bitotsav.shared.utils.isProperEmail
import `in`.bitotsav.shared.utils.runOnMinApi
import `in`.bitotsav.shared.utils.toast
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.viewModel


class LoginFragment : Fragment() {

    private val loginViewModel by viewModel<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        if (CurrentUser.isLoggedIn)
            findNavController().navigate(R.id.action_destLogin_to_destProfile)

        context?.let { loginViewModel.mColor = it.getColorCompat(R.color.colorRed) }
        setObservers()

        return FragmentLoginBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = this@LoginFragment
                viewModel = loginViewModel
                register.setOnClickListener(
                    Navigation.createNavigateOnClickListener(
                        R.id.action_destLogin_to_destRegistration
                    )
                )
            }
            .root
    }


    private fun setObservers() {

        with(loginViewModel) {

            toastMessage.observe(viewLifecycleOwner, Observer { message ->
                this@LoginFragment.context?.let { message.toast(it) }
            })

            loggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
                if (isLoggedIn) {
                    Log.d("login observer", "CurrentUser logged in")
                    commitAutofillFields()
                    findNavController().navigate(R.id.action_destLogin_to_destProfile)
                }
            })

            loginEmail.observe(viewLifecycleOwner, Observer { emailText ->
                loginPasswordErrorText.value = ""
                loginEmailErrorText.value =
                    when (emailText.isEmpty() || emailText.isProperEmail()) {
                        true -> ""
                        false -> "Invalid Email"
                    }
            })

            loginPassword.observe(viewLifecycleOwner, Observer {
                loginPasswordErrorText.value = ""
            })
        }
    }

    @SuppressLint("NewApi")
    private fun commitAutofillFields() {
        runOnMinApi(26) {
            context?.getSystemService(AutofillManager::class.java)?.commit()
        }
    }
}
