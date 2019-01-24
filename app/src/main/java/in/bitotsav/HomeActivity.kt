package `in`.bitotsav

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_home.*

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

    }

    private fun setupBottomNavMenu() {
        val navController = Navigation.findNavController(this, R.id.mainFragment)
        mainNavigation.setupWithNavController(navController)
    }
}
