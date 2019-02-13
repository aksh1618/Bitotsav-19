package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Night
import `in`.bitotsav.shared.ui.BaseViewModel
import androidx.lifecycle.MutableLiveData

class NightViewModel : BaseViewModel("NightVM") {

    lateinit var nightsList: List<Night>
    val currentNight = MutableLiveData<Night>()

    fun setCurrentNight(nightId: Int) {
        currentNight.value = nightsList[nightId - 1]
    }

}