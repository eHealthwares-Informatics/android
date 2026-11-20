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
import io.sentry.Sentry
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RxSoftMobileTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                    // Visible Sentry test trigger (floats above the app UI).
                    ExtendedFloatingActionButton(
                        onClick = { triggerTestException() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    ) {
                        Text(text = "Sentry Test")
                    }
                }
            }
        }
    }

    private fun triggerTestException() {
        try {
            throw Exception("Sentry MCP Test Error: Android manual exception at ${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Sentry.captureException(e)
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
