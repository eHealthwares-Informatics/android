package com.rxsoft.mobile.ui.shop

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.AvailablePaymentProviderDto
import com.rxsoft.mobile.data.remote.dto.CreateSaleLine
import com.rxsoft.mobile.data.remote.dto.CreateSalePayment
import com.rxsoft.mobile.data.remote.dto.CreateSaleRequest
import com.rxsoft.mobile.data.remote.dto.InitializePaymentRequest
import com.rxsoft.mobile.data.remote.dto.PaymentMethodDto
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.data.repository.PaymentsRepository
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.UiState
import com.rxsoft.mobile.util.payment.PaymentAppDetector
import com.rxsoft.mobile.util.payment.PaymentProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/** One-shot events emitted by the checkout flow. */
sealed interface PaymentEvent {
    data class LaunchCheckout(
        val url: String,
        val reference: String,
        val provider: PaymentProviderType,
    ) : PaymentEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val posRepository: PosRepository,
    private val paymentsRepository: PaymentsRepository,
    private val paymentAppDetector: PaymentAppDetector,
    private val shopCart: ShopCart,
) : ViewModel() {

    val cartItems = shopCart.items

    private val _checkoutState = MutableStateFlow<UiState<SaleDto>>(UiState.Idle)
    val checkoutState: StateFlow<UiState<SaleDto>> = _checkoutState.asStateFlow()

    private val _selectedProvider = MutableStateFlow(PaymentProviderType.CASH)
    val selectedProvider: StateFlow<PaymentProviderType> = _selectedProvider.asStateFlow()

    private val _events = MutableSharedFlow<PaymentEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<PaymentEvent> = _events.asSharedFlow()

    private var paymentMethods: List<PaymentMethodDto> = emptyList()
    private var availableProviders: List<AvailablePaymentProviderDto> = emptyList()

    val providers: List<PaymentProviderType> = PaymentProviderType.values().toList()
    val subtotal: Double get() = cartItems.value.sumOf { it.totalPrice }

    init {
        loadPaymentMethods()
        loadPaymentProviders()
    }

    fun updateQuantity(productId: String, quantity: Int) = shopCart.updateQuantity(productId, quantity)

    fun removeItem(productId: String) = shopCart.remove(productId)

    fun selectProvider(provider: PaymentProviderType) {
        _selectedProvider.value = provider
    }

    fun isProviderAppInstalled(provider: PaymentProviderType): Boolean =
        paymentAppDetector.isInstalled(provider)

    fun buildCheckoutIntent(provider: PaymentProviderType, url: String): Intent =
        paymentAppDetector.resolveCheckoutIntent(provider, url)

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            posRepository.getPaymentMethods()
                .onSuccess { paymentMethods = it }
                .onFailure { e -> Log.w("CheckoutVM", "Payment methods failed: ${e.message}") }
        }
    }

    private fun loadPaymentProviders() {
        viewModelScope.launch {
            paymentsRepository.availableProviders()
                .onSuccess { availableProviders = it }
                .onFailure { e -> Log.w("CheckoutVM", "Providers failed: ${e.message}") }
        }
    }

    private fun methodFor(provider: PaymentProviderType): PaymentMethodDto? = when (provider) {
        PaymentProviderType.CASH ->
            paymentMethods.firstOrNull { it.methodType.equals("cash", true) || it.code.equals("CASH", true) }
        PaymentProviderType.OPAY, PaymentProviderType.MONIEPOINT, PaymentProviderType.PAYSTACK ->
            paymentMethods.firstOrNull { it.methodType.equals("card", true) }
                ?: paymentMethods.firstOrNull { it.methodType.equals("wallet", true) }
                ?: paymentMethods.firstOrNull()
    }

    private fun providerIdFor(provider: PaymentProviderType): String? =
        availableProviders.firstOrNull {
            it.providerType.equals(provider.providerCode, ignoreCase = true)
        }?.id

    /** Entry point for the Pay button. */
    fun pay() {
        val items = cartItems.value
        if (items.isEmpty()) {
            _checkoutState.value = UiState.Error("Cart is empty")
            return
        }
        val provider = _selectedProvider.value
        if (!provider.usesGateway) {
            completeSale(provider, paymentReference = null)
            return
        }
        if (!provider.supportsOnlineCheckout) {
            _checkoutState.value = UiState.Error(
                "${provider.displayName} online checkout isn't supported yet. Use OPay or Cash.",
            )
            return
        }

        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            paymentsRepository.initialize(
                InitializePaymentRequest(
                    amount = subtotal,
                    sourceType = "sale",
                    providerId = providerIdFor(provider),
                    paymentMethodId = methodFor(provider)?.id,
                    descriptor = "RxSoft shop order",
                    returnUrl = "https://rxsoft.health/payment-success",
                    callbackUrl = "https://rxsoft.health/payment-success",
                ),
            )
                .onSuccess { resp ->
                    _checkoutState.value = UiState.Idle
                    val url = resp.checkoutUrl
                    if (url.isNullOrBlank()) {
                        _checkoutState.value = UiState.Error(
                            "Payment provider did not return a checkout link",
                        )
                    } else {
                        _events.tryEmit(
                            PaymentEvent.LaunchCheckout(url, resp.reference, provider),
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("CheckoutVM", "Payment init failed: ${e.message}", e)
                    _checkoutState.value = UiState.Error(e.message ?: "Could not start payment")
                }
        }
    }

    /** Called after returning from the provider app / browser. */
    fun onCheckoutReturned(reference: String, provider: PaymentProviderType) {
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            paymentsRepository.verify(reference)
                .onSuccess { result ->
                    if (result.paid) {
                        completeSale(provider, reference)
                    } else {
                        _checkoutState.value = UiState.Error(
                            "Payment not completed (${result.status ?: "pending"})",
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("CheckoutVM", "Payment verify failed: ${e.message}", e)
                    _checkoutState.value = UiState.Error(e.message ?: "Could not verify payment")
                }
        }
    }

    private fun completeSale(provider: PaymentProviderType, paymentReference: String?) {
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            val config = posRepository.getUserPosConfig().getOrElse { e ->
                _checkoutState.value = UiState.Error(e.message ?: "Failed to load configuration")
                return@launch
            }
            val storeId = config.stockLocationId ?: config.storeId ?: run {
                _checkoutState.value = UiState.Error("Store configuration not set")
                return@launch
            }
            val methodId = methodFor(provider)?.id ?: run {
                _checkoutState.value = UiState.Error("No payment method configured")
                return@launch
            }

            val request = CreateSaleRequest(
                saleNumber = "MOBSHOP-${System.currentTimeMillis()}",
                saleChannel = "mobile",
                storeId = storeId,
                customerId = config.defaultCustomerId,
                stockLocationId = config.stockLocationId,
                lines = cartItems.value.map { c ->
                    CreateSaleLine(
                        itemId = c.product.id,
                        quantity = BigDecimal(c.quantity),
                        unitPrice = BigDecimal.valueOf(c.product.price),
                        uomId = c.product.uomId ?: "",
                    )
                },
                payments = listOf(
                    CreateSalePayment(
                        paymentMethodId = methodId,
                        amount = BigDecimal.valueOf(subtotal),
                        paymentReference = paymentReference,
                    ),
                ),
            )

            posRepository.createSale(request)
                .onSuccess { sale ->
                    shopCart.clear()
                    _checkoutState.value = UiState.Success(sale)
                }
                .onFailure { e ->
                    Log.e("CheckoutVM", "Checkout API failed: ${e.message}", e)
                    _checkoutState.value = UiState.Error(e.message ?: "Checkout failed")
                }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = UiState.Idle
    }
}
