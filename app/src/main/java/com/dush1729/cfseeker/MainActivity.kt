package com.dush1729.cfseeker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.AppDatabase
import com.dush1729.cfseeker.navigation.CFSeekerNavGraph
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.theme.CFSeekerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val contestViewModel: ContestViewModel by viewModels()

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var crashlyticsService: CrashlyticsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        crashlyticsService.setCustomKey("db_version", AppDatabase.VERSION)

        setContent {
            CFSeekerTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CFSeekerNavGraph(
                        navController = navController,
                        userViewModel = userViewModel,
                        contestViewModel = contestViewModel,
                        analyticsService = analyticsService,
                        crashlyticsService = crashlyticsService
                    )
                }
            }
        }
    }
}