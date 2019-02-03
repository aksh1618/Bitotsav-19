package `in`.bitotsav.profile.ui

import `in`.bitotsav.databinding.FragmentRegistrationStepOneBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepThreeBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepTwoBinding
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
class RegistrationStepFragment : Fragment() {

    companion object {
        const val TAG = "RegStepF"
    }

    private val registrationViewModel by sharedViewModel<RegistrationViewModel>()
    private val currentStep by lazy {
        arguments?.let {
            RegistrationStepFragmentArgs.fromBundle(it).registrationStepNumber.apply {
                Log.v(TAG, "SafeArgs: Registration step $this")
            }
        } ?: 1.apply { Log.e(TAG, "SafeArgs betrayed us") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate current step layout
        val binding = when (currentStep) {
            2 -> FragmentRegistrationStepTwoBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = registrationViewModel.fields
                    viewModel = registrationViewModel
                    setStepTwoObservers()
                }
            3 -> FragmentRegistrationStepThreeBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = registrationViewModel.fields
                    viewModel = registrationViewModel
                    setStepThreeObservers()
                }
            else -> FragmentRegistrationStepOneBinding
                .inflate(inflater, container, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    fields = registrationViewModel.fields
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
                    Log.v(TAG, "Waiting for captcha...")
                    // TODO: [Refactor] Should this be called directly from xml?
                    //  Or should xml only have access to the ViewModel (Or is that
                    //  ridiculous as it already has access to this Fragment anyway,
                    //  because it IS the fragment, thus the inflation code) ?
                    fetchCaptchaResponseToken()
                }
            }

            // TODO: [Refactor] This could be common, single observer
            nextStep.setObserver { nextStep ->
                if (nextStep == 2) {
                    commitAutofillFields()
                    findNavController().navigate(
                        RegistrationStepFragmentDirections.nextStep()
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
                    Log.v(TAG, "Waiting for otp verification")
                    completeStepTwo()
                }
            }

            nextStep.setObserver { nextStep ->
                if (nextStep == 3) {
                    Log.v(TAG, "Finished step 2")
                    findNavController().navigate(
                        RegistrationStepFragmentDirections.nextStep()
                    )
                }
            }
        }
    }

    private fun setStepThreeObservers() {
        with(registrationViewModel) {

            registrationViewModel.fetchCollegeList()

            waiting.setObserver { isWaiting ->
                if (isWaiting) {
                    Log.v(TAG, "Waiting for bitotsav id")
                    completeStepThree()
                }
            }

            allDone.setObserver { allDone ->
                if (allDone) {
                    Log.v(TAG, "Logging In")
                    login()
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

    private inline fun <T> LiveData<T>.setObserver(crossinline block: (T) -> Unit) {
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
