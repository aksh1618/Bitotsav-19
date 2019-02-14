package `in`.bitotsav.shared.ui

import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UiUtilViewModel : BaseViewModel() {

    private val _showBottomNav = NonNullMutableLiveData(true)
    val backPressed = NonNullMutableLiveData(false)
    val bottomNavVisible: LiveData<Boolean>
        get() = _showBottomNav

    fun showBottomNav() {
        _showBottomNav.value = true
    }

    fun hideBottomNav() {
        _showBottomNav.value = false
    }

}