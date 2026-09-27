package com.rxsoft.mobile.ui.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleConfig @Inject constructor() {
    private val _activeModules = MutableStateFlow<Set<AppModule>>(
        setOf(AppModule.POS, AppModule.INVENTORY, AppModule.SALES)
    )
    val activeModules: StateFlow<Set<AppModule>> = _activeModules.asStateFlow()

    fun setActiveModules(modules: Set<AppModule>) {
        _activeModules.value = modules
    }
}
