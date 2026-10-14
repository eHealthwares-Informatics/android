package com.ehealthwares.rxsoft

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ehealthwares.rxsoft.ui.auth.FacebookAuthBridge
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

    /** Facebook Login still resolves through the classic callback manager; the
     * bridge routes every activity result to the SDK and one-shot handler. */
    @Deprecated("Required by the Facebook SDK callback path")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (FacebookAuthBridge.onActivityResult(requestCode, resultCode, data)) return
        super.onActivityResult(requestCode, resultCode, data)
    }
}
