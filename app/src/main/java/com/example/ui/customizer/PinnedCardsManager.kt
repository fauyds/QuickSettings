package com.example.ui.customizer

import android.content.Context
import com.example.data.PanelPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object PinnedCardsManager {
    fun getPinnedCardIds(category: CustomizeCategory, context: Context): StateFlow<List<String>> {
        return MutableStateFlow(PanelPreferences.pinnedFeatures.value).asStateFlow()
    }
}
