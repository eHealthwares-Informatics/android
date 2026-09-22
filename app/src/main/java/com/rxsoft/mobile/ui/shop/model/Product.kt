package com.rxsoft.mobile.ui.shop.model

data class Product(
    val id: String = "",
    val name: String = "",
    val manufacturer: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val imageUrl: String? = null,
    val unit: String = "",
    val description: String = "",
    val category: String = "",
    val code: String? = null,
    val barcode: String? = null,
    val uomId: String? = null,
    var isFavourite: Boolean = false
)
