package com.ehealthwares.rxsoft.ui.settings

enum class AppModule(
    val title: String,
    val description: String
) {
    POS("POS", "Point of Sale terminal, orders"),
    SHOP("Shop", "Medicine catalog, prescriptions, checkout"),
    INVENTORY("Inventory", "Stock balances, adjustments"),
    SALES("Sales", "Sales reports, history")
}
