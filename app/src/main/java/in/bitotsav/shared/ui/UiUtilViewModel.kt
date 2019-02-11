package `in`.bitotsav.shared.ui

import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import androidx.lifecycle.LiveData

class UiUtilViewModel : BaseViewModel() {

    private val _showBottomNav = NonNullMutableLiveData(true)
    val bottomNavVisible: LiveData<Boolean>
        get() = _showBottomNav

    fun showBottomNav() {
        _showBottomNav.value = true
    }

    fun hideBottomNav() {
        _showBottomNav.value = false
    }

}