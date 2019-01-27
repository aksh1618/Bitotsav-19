package `in`.bitotsav

import `in`.bitotsav.shared.network.scheduleWork
import `in`.bitotsav.shared.workers.EventWorkType
import `in`.bitotsav.shared.workers.EventWorker
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import androidx.work.workDataOf
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

//    private val navHostFragment: NavHostFragment by lazy {
//        supportFragmentManager.findFragmentById(R.id.mainFragment) as NavHostFragment
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        val navController = navHostFragment.navController
        setupBottomNavMenu()
        // TODO: Convert this to DSL.
        scheduleWork<EventWorker>(workDataOf("type" to EventWorkType.FETCH_ALL_EVENTS.name))
    }

    private fun setupBottomNavMenu() {
        val navController = Navigation.findNavController(this, R.id.mainFragment)
        mainNavigation.setupWithNavController(navController)
    }
}
