package `in`.bitotsav.profile.utils

import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

// TODO: [REFACTOR] Make DSL out of this
class MutableLiveDataTextWithValidation(
    vararg validationErrorPairs: Pair<String.() -> Boolean, String>,
    defaultText: String = "",
    defaultErrorText: String = ""
) {

    val text = NonNullMutableLiveData(defaultText)
    val errorText = NonNullMediatorLiveData(defaultErrorText).apply {
        addSource(text) {
            // Break at the first violation
            validationErrorPairs.asSequence().any { (rule, error) ->
                val violation = !text.value.rule()
                violation.onTrue {
                    value = error
                }
            }.onFalse { value = defaultErrorText }
        }
    }
}

class NonNullMutableLiveData<T>(private val defaultValue: T) :
    MutableLiveData<T>() {
    override fun getValue(): T {
        return super.getValue() ?: defaultValue
    }
}

class NonNullMediatorLiveData<T>(private val defaultValue: T) :
    MediatorLiveData<T>() {
    override fun getValue(): T {
        return super.getValue() ?: defaultValue
    }
}
