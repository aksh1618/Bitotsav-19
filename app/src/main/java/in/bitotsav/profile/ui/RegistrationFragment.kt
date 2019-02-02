package `in`.bitotsav.profile.ui

import `in`.bitotsav.databinding.FragmentRegistrationStepOneBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepThreeBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepTwoBinding
import `in`.bitotsav.profile.data.RegistrationFields
import `in`.bitotsav.shared.utils.runOnMinApi
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import org.koin.androidx.viewmodel.ext.sharedViewModel

// TODO: [Refactor] Try removing as many observers as possible, observing should be
//  done by data binding view only, preferably.
class RegistrationFragment : Fragment() {

    companion object {
        const val TAG = "RegistrationFragment"
        const val KEY_CURSTEP = "curStep"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "pass"
    }

    private val registrationViewModel by sharedViewModel<RegistrationViewModel>()
//    private val args by navArgs<RegistrationFragmentArgs>()
//    private val curStep = args.registrationStepNumber

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Restore state
        val currentStep =
            savedInstanceState?.getInt(KEY_CURSTEP)?.let { it }
                ?: registrationViewModel.currentStep.value
        savedInstanceState?.getString(KEY_EMAIL)?.let {
            RegistrationFields.email.text.value = it
        }
        savedInstanceState?.getCharArray(KEY_PASSWORD)?.let {
            RegistrationFields.password.text.value = it.toString()
        }

        // Inflate current step layout
        val binding = when (currentStep) {
            2 -> FragmentRegistrationStepTwoBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = RegistrationFields
                    viewModel = registrationViewModel
                    setStepTwoObservers()
                }
            3 -> FragmentRegistrationStepThreeBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = RegistrationFields
                    viewModel = registrationViewModel
                    setStepThreeObservers()
                }
            else -> FragmentRegistrationStepOneBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = RegistrationFields
                    viewModel = registrationViewModel
                    setStepOneObservers()
                }
        }
        return binding.root
    }

    private fun setStepOneObservers() {
        with(registrationViewModel) {

            waiting.setObserver { isWaiting ->
                if (isWaiting) {
                    Log.d(TAG, "Waiting for captcha...")
                    // TODO: [Refactor] Should this be called directly from xml?
                    //  Or should xml only have access to the ViewModel (Or is that
                    //  ridiculous as it already has access to this Fragment anyway,
                    //  because it IS the fragment, thus the inflation code) ?
                    fetchCaptchaResponseToken()
                }
            }

            // TODO: [Refactor] This could be common, single observer
            currentStep.setObserver { nextStep ->
                if (nextStep == 2) {
                    commitAutofillFields()
                    findNavController().navigate(
                        RegistrationFragmentDirections.nextStep()
                    )
                }
            }
        }
    }

    // TODO: Autofill this OTP if same device.
    private fun setStepTwoObservers() {
        with(registrationViewModel) {

            waiting.setObserver { isWaiting ->
                if (isWaiting) {
                    Log.d(TAG, "Waiting for otp verification")
                    completeStepTwo()
                }
            }

            currentStep.setObserver { nextStep ->
                if (nextStep == 3) {
                    findNavController().navigate(
                        RegistrationFragmentDirections.nextStep()
                    )
                }
            }
        }
    }

    private fun setStepThreeObservers() {
        with(registrationViewModel) {

            waiting.setObserver { isWaiting ->
                if (isWaiting) {
                    Log.d(TAG, "Waiting for bitotsav id")
                    completeStepThree()
                }
            }

            allDone.setObserver { allDone ->
                if (allDone) {
                    Log.d(TAG, "Logging In")
                    login()
                }
            }

            currentStep.setObserver { nextStep ->
                if (nextStep == 1) {
                    findNavController().navigate(
                        RegistrationFragmentDirections.nextStep()
                    )
                }
            }

        }
    }

    private fun fetchCaptchaResponseToken() {
        if (context == null) return
        SafetyNet.getClient(activity as Activity)
            .verifyWithRecaptcha("6LeFRY4UAAAAAG3VLvn5cwTkmq41Y2U5HrkPIH69")
            .addOnSuccessListener(activity as Activity) { response ->
                if (response?.tokenResult?.isNotEmpty() == true) {
                    registrationViewModel.completeStepOne(response.tokenResult)
                }
            }
            .addOnFailureListener(activity as Activity) { e ->
                if (e is ApiException) {
                    // An error occurred when communicating with the
                    // reCAPTCHA service. TODO: Refer to the status code to
                    // handle the error appropriately.
                    Log.e(
                        "RegistrationVM.captcha",
                        "Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}",
                        e
                    )
                } else {
                    // A different, unknown type of error occurred.
                    Log.e("RegistrationVM.captcha", "Error: ${e.message}", e)
                }
                with(registrationViewModel) {
                    waiting.value = false
                    registrationError.value = "Captcha error, try again."
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_CURSTEP, registrationViewModel.currentStep.value)
        outState.putString(KEY_EMAIL, RegistrationFields.email.text.value)
        outState.putCharArray(KEY_PASSWORD, RegistrationFields.password.text.value.toCharArray())
        super.onSaveInstanceState(outState)
    }

    private fun <T> LiveData<T>.setObserver(block: (T) -> Unit) {
        observe(viewLifecycleOwner, Observer {
            block.invoke(it)
        })
    }

    @SuppressLint("NewApi")
    private fun commitAutofillFields() {
        runOnMinApi(26) {
            context?.getSystemService(AutofillManager::class.java)?.commit()
        }
    }
}
