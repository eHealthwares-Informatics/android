package com.ehealthwares.rxsoft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ehealthwares.rxsoft.ui.navigation.AppNavigation
import com.ehealthwares.rxsoft.ui.theme.RxSoftMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RxSoftMobileTheme {
                AppNavigation()
            }
        }
    }
}
