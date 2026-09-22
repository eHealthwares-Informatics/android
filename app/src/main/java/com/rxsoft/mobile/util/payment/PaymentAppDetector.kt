package com.rxsoft.mobile.util.payment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects which payment provider apps are installed and builds the intent used
 * to hand a checkout URL to them. When the provider app is present we target it
 * directly (e.g. the OPay app for OPay); otherwise we fall back to a browser.
 */
@Singleton
class PaymentAppDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun installedPackage(type: PaymentProviderType): String? {
        if (type.packageCandidates.isEmpty()) return null
        val pm = context.packageManager
        return type.packageCandidates.firstOrNull { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun isInstalled(type: PaymentProviderType): Boolean = installedPackage(type) != null

    /**
     * Intent that opens [checkoutUrl] in the provider's own app when installed
     * (and able to handle it), otherwise in the default browser.
     */
    fun resolveCheckoutIntent(type: PaymentProviderType, checkoutUrl: String): Intent {
        val uri = Uri.parse(checkoutUrl)
        val pkg = installedPackage(type)
        if (pkg != null) {
            val direct = Intent(Intent.ACTION_VIEW, uri).setPackage(pkg)
            if (direct.resolveActivity(context.packageManager) != null) return direct
        }
        return Intent(Intent.ACTION_VIEW, uri)
    }
}
