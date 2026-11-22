package com.example.ui.customizer

import android.content.Context
import com.example.data.PanelPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

object PinnedCardsManager {
    fun getPinnedCardIds(category: CustomizeCategory, context: Context): StateFlow<List<String>> {
        return PanelPreferences.pinnedFeatures
    }

    fun isCardPinned(cardId: String): Boolean {
        return PanelPreferences.isFeaturePinned(cardId)
    }

    fun togglePin(cardId: String, category: CustomizeCategory, context: Context): Boolean {
        return PanelPreferences.toggleFeaturePinned(context, cardId)
    }

    fun getHiddenCardIds(): StateFlow<List<String>> {
        return PanelPreferences.hiddenFeatures
    }

    fun isCardHidden(cardId: String): Boolean {
        return PanelPreferences.isFeatureHidden(cardId)
    }

    fun toggleHide(cardId: String, context: Context): Boolean {
        return PanelPreferences.toggleFeatureHidden(context, cardId)
    }
}
