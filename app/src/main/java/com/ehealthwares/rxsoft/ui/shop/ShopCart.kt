package com.rxsoft.mobile.ui.shop

import com.rxsoft.mobile.ui.shop.model.CartItem
import com.rxsoft.mobile.ui.shop.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopCart @Inject constructor() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun add(product: Product, quantity: Int = 1) {
        if (quantity <= 0) return
        val current = _items.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _items.value = current
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            if (quantity <= 0) current.removeAt(idx)
            else current[idx] = current[idx].copy(quantity = quantity)
        }
        _items.value = current
    }

    fun remove(productId: String) {
        _items.value = _items.value.filter { it.product.id != productId }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
