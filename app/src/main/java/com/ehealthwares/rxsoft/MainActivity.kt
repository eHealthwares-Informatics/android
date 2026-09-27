package com.rxsoft.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rxsoft.mobile.ui.navigation.AppNavigation
import com.rxsoft.mobile.ui.theme.RxSoftMobileTheme
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
