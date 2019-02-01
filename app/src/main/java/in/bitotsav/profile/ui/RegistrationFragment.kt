package `in`.bitotsav.profile.ui

import `in`.bitotsav.databinding.FragmentRegistrationStepOneBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepThreeBinding
import `in`.bitotsav.databinding.FragmentRegistrationStepTwoBinding
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import org.koin.androidx.viewmodel.ext.sharedViewModel
import java.util.concurrent.Executor

class RegistrationFragment : Fragment() {

    companion object {
        const val TAG = "RegistrationFragment"
    }
    private val registrationViewModel by sharedViewModel<RegistrationViewModel>()
//    private val args by navArgs<RegistrationFragmentArgs>()
//    private val curStep = args.registrationStepNumber

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return when (registrationViewModel.currentStep.value) {
            2 -> FragmentRegistrationStepTwoBinding
                .inflate(inflater, container, false)
                .apply { viewModel = registrationViewModel; setStepTwoObservers() }
            3 -> FragmentRegistrationStepThreeBinding
                .inflate(inflater, container, false)
                .apply { viewModel = registrationViewModel; setStepThreeObservers() }
            else -> FragmentRegistrationStepOneBinding
                .inflate(inflater, container, false)
                .apply { viewModel = registrationViewModel; setStepOneObservers() }
        }
            .apply {
                lifecycleOwner = viewLifecycleOwner
            }
            .root
    }

    private fun setStepOneObservers() {
        with(registrationViewModel) {

            waiting.setObserver{ isWaiting ->
                if (isWaiting) {
                    Log.d(TAG, "Waiting for captcha...")
                    fetchCaptchaResponseToken()
                }
            }

            recaptchaResponse.setObserver { token ->
                if (token.isNotEmpty()) {
                    completeStepOne()
                }
            }

            currentStep.setObserver { nextStep ->
                if (nextStep == 2) {
                    findNavController().navigate(
                        RegistrationFragmentDirections.nextStep()
                    )
                }
            }

            phone.setObserver { Log.d(TAG, "observed phone: $it") }
            phone.errorText.setObserver { Log.d(TAG, "observed phone error: $it") }
        }
    }

    private fun setStepTwoObservers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setStepThreeObservers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun fetchCaptchaResponseToken() {
        if (context == null) return
        SafetyNet.getClient(activity as Activity)
            .verifyWithRecaptcha("6LeFRY4UAAAAAG3VLvn5cwTkmq41Y2U5HrkPIH69")
            .addOnSuccessListener(activity as Activity) { response ->
                if (response?.tokenResult?.isNotEmpty() == true) {
                    registrationViewModel.recaptchaResponse.value = response.tokenResult
                }
            }
            .addOnFailureListener(activity as Activity) { e ->
                if (e is ApiException) {
                    // An error occurred when communicating with the
                    // reCAPTCHA service. Refer to the status code to
                    // handle the error appropriately.
                    Log.e(
                        "RegistrationVM.captcha",
                        "Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}",
                        e
                    )
                } else {
                    // A different, unknown type of error occurred.
                    Log.d("RegistrationVM.captcha", "Error: ${e.message}")
                }
            }
    }

    fun <T> LiveData<T>.setObserver(block: (T) -> Unit) {
        observe(viewLifecycleOwner, Observer {
            block.invoke(it)
        })
    }
}
