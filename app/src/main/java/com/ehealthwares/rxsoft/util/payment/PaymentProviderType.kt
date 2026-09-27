package com.rxsoft.mobile.util.payment

/**
 * Payment options offered at checkout. `providerCode` matches the backend
 * `payment-providers` code; `packageCandidates` are the Android packages used to
 * detect whether the provider's own app is installed (in which case we hand the
 * checkout off to it via an intent instead of a browser).
 */
enum class PaymentProviderType(
    val displayName: String,
    val providerCode: String,
    val packageCandidates: List<String>,
    /** Whether the backend provider supports browser/app checkout (vs POS-only). */
    val supportsOnlineCheckout: Boolean = true,
) {
    CASH("Cash", "CASH", emptyList()),
    OPAY(
        "OPay",
        "OPAY",
        listOf("team.opay.pay", "com.opay.pay", "com.opay.mobile", "com.opay.pos"),
    ),
    MONIEPOINT(
        "Moniepoint",
        "MONIEPOINT",
        listOf(
            "com.moniepoint.pos",
            "com.moniepoint.personal",
            "com.moniepoint.business",
            "com.moniepoint.agency",
            "ng.moniepoint",
        ),
        supportsOnlineCheckout = false,
    ),
    PAYSTACK("Paystack", "PAYSTACK", emptyList()),
    ;

    val usesGateway: Boolean get() = this != CASH
}
