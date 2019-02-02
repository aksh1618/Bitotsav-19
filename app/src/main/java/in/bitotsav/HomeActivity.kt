package `in`.bitotsav

import `in`.bitotsav.events.ui.ScheduleViewModel
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.viewModel

class HomeActivity : AppCompatActivity() {

    private val scheduleViewModel by viewModel<ScheduleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        handlePlatformLimitations()
        setupBottomNavMenu()
        finalizeViewModels()
    }

    private fun setupBottomNavMenu() {
        val navController = Navigation.findNavController(this, R.id.mainFragment)
        findViewById<BottomNavigationView>(R.id.mainNavigation)
            .setupWithNavController(navController)
    }

    private fun finalizeViewModels() {
        scheduleViewModel.filterColors = filterColors
    }

    // TODO: Get colors from resources
    private val filterColors: List<Int>
        get() = listOf(ContextCompat.getColor(this, R.color.colorRed))

    private fun handlePlatformLimitations() {
        when {
            // Can't have light status bar in older than M due to white-only icons
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> with(window) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = filterColors[0]
            }
        }
    }
}
