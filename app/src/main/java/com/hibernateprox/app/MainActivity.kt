package com.hibernateprox.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hibernateprox.app.core.datastore.UserPreferencesRepository
import com.hibernateprox.app.core.datastore.UserPrefs
import com.hibernateprox.app.core.designsystem.HibernateProTheme
import com.hibernateprox.app.ui.navigation.AdaptiveNavigationShell
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepository: UserPreferencesRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            val prefs by prefsRepository.prefsFlow.collectAsStateWithLifecycle(
                initialValue = UserPrefs(),
            )
            val windowSizeClass = calculateWindowSizeClass(this)
            HibernateProTheme(useDynamicColor = prefs.useDynamicColor) {
                AdaptiveNavigationShell(widthSizeClass = windowSizeClass.widthSizeClass)
            }
        }
    }
}
