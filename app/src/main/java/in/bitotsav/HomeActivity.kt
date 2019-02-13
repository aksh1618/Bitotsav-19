package `in`.bitotsav

import `in`.bitotsav.databinding.ActivityHomeBinding
import `in`.bitotsav.shared.ui.UiUtilViewModel
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.thelittlefireman.appkillermanager.managers.KillerManager
import com.thelittlefireman.appkillermanager.ui.DialogKillerManagerBuilder
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.context.GlobalContext.get
import java.util.*

// To be used to display AppKillerManger prompt from second run onwards
private const val RUN_COUNTER = "runCounter"

class HomeActivity : AppCompatActivity() {

    enum class Theme(@StyleRes val themeRes: Int) {
        RED(R.style.AppThemeRed),
        GREEN(R.style.AppThemeGreen),
        BLUE(R.style.AppThemeIndigo),
        PURPLE(R.style.AppThemeFuchsia),
        ORANGE(R.style.AppThemeOrange)
    }

    companion object {
        const val KEY_ROTATED = "rotated"
    }

    private val uiUtilViewModel by viewModel<UiUtilViewModel>()
    private lateinit var binding: ActivityHomeBinding
    val primaryColor by lazy {
        TypedValue().apply {
            theme?.resolveAttribute(R.attr.colorPrimary, this, true)
        }.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.values().random().themeRes)
        DisplayMetrics().apply {
            windowManager.defaultDisplay.getMetrics(this)
            Log.wtf("MAIN", "$xdpi, $ydpi")
        }
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.viewModel = uiUtilViewModel
        uiUtilViewModel.mColor = primaryColor
        binding.lifecycleOwner = this
        handlePlatformLimitations()
        setupBottomNavMenu()

        if (savedInstanceState?.getBoolean(KEY_ROTATED) != true) {
            // AppKillerManager
            initAppKillerManager()
        }
    }

    private fun setupBottomNavMenu() {
        val navController = Navigation.findNavController(this, R.id.mainFragment)
        findViewById<BottomNavigationView>(R.id.mainNavigation)
            .setupWithNavController(navController)
    }

    private fun handlePlatformLimitations() {
        when {
            // Can't have light status bar in older than M due to white-only icons
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> with(window) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = primaryColor
            }
        }
    }

    private fun initAppKillerManager() {
        val runCount = get().koin.get<SharedPreferences>().getInt(RUN_COUNTER, 0)
        val calendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Kolkata"))
        calendar.set(2019, 1, 19, 0, 0)
        val endOfBitotsav = calendar.timeInMillis
        val isBitotsavOver = System.currentTimeMillis() > endOfBitotsav
        // Execute only if second run and Bitotsav not over
        if (runCount > 0 && !isBitotsavOver) {
            val manufacturer = android.os.Build.MANUFACTURER
            if ("xiaomi".equals(manufacturer, true) || "huawei".equals(manufacturer, true)) {
                startAppKillerManagerDialog(KillerManager.Actions.ACTION_AUTOSTART)
                startAppKillerManagerDialog(KillerManager.Actions.ACTION_NOTIFICATIONS)
                startAppKillerManagerDialog(KillerManager.Actions.ACTION_POWERSAVING)
            }
        } else if (runCount == 0) {
            get().koin.get<SharedPreferences>().edit().putInt(RUN_COUNTER, runCount + 1)
                .apply()
        }
    }

    private fun startAppKillerManagerDialog(actions: KillerManager.Actions) {
        DialogKillerManagerBuilder().setContext(this).setAction(actions).show()
    }

    override fun onSaveInstanceState(
        outState: Bundle?,
        outPersistentState: PersistableBundle?
    ) {
        outState?.putBoolean(KEY_ROTATED, true)
        super.onSaveInstanceState(outState, outPersistentState)
    }
}
