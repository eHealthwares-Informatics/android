package com.rxsoft.mobile.ui.shop.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val totalPrice: Double get() = product.price * quantity
}
