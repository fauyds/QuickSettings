package com.example

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.data.ComplicationArcMonetMode
import com.example.data.PanelPreferences
import com.example.data.SettingsIconLocation
import com.example.model.ComplicationAction
import com.example.model.ComplicationLayoutMode
import com.example.model.ComplicationSublinePlacement
import com.example.model.ComplicationTileModel
import com.example.model.QsGridLayout
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.model.TileState
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.UnifiedQsPanel
import com.example.ui.overlay.resolveFooterButtonIconTint
import com.example.ui.theme.QuickPanelTheme
import com.example.ui.theme.computeContrastForegroundForColor
import com.example.ui.theme.getInactiveTilesElementColor
import com.example.ui.theme.getInactiveTilesOnElementColor
import com.example.ui.tile.ComplicationTileView
import com.example.ui.tile.resolveComplicationForegroundColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class ComplicationTileInactiveColorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `filled inactive complication tile uses contrasting foreground color instead of inactiveTileColor`() {
        val customPurple = Color(0xFF7B1FA2)
        val expectedWhiteOnPurple = computeContrastForegroundForColor(customPurple)
        assertEquals(Color.White, expectedWhiteOnPurple)

        val resolvedDark = resolveComplicationForegroundColors(
            isDark = true,
            isActive = false,
            isTransparentInner = false,
            isInactiveTilesCustomized = true,
            animatedAccent = Color.Cyan,
            inactiveTileColor = customPurple,
            activeOnTileColor = Color.Black,
            inactiveOnTileColor = expectedWhiteOnPurple,
            onSurfaceColor = Color.White,
            onSurfaceVariantColor = Color.Gray
        )
        assertNotEquals(customPurple, resolvedDark.iconTint)
        assertEquals(Color.White, resolvedDark.iconTint)
        assertEquals(Color.White.copy(alpha = 0.92f), resolvedDark.insideSublineColor)

        val customBrightYellow = Color(0xFFFFEB3B)
        val expectedDarkOnYellow = computeContrastForegroundForColor(customBrightYellow)
        assertEquals(Color(0xFF1A1A1A), expectedDarkOnYellow)

        val resolvedBright = resolveComplicationForegroundColors(
            isDark = false,
            isActive = false,
            isTransparentInner = false,
            isInactiveTilesCustomized = true,
            animatedAccent = Color.Blue,
            inactiveTileColor = customBrightYellow,
            activeOnTileColor = Color.White,
            inactiveOnTileColor = expectedDarkOnYellow,
            onSurfaceColor = Color.Black,
            onSurfaceVariantColor = Color.DarkGray
        )
        assertNotEquals(customBrightYellow, resolvedBright.iconTint)
        assertEquals(Color(0xFF1A1A1A), resolvedBright.iconTint)
        assertEquals(Color(0xFF1A1A1A).copy(alpha = 0.92f), resolvedBright.insideSublineColor)
        assertEquals(Color.DarkGray, resolvedBright.belowSublineColor)
    }

    @Test
    fun `transparent inner respects inactiveIconTintOverride for filled parent cards and keeps outline tint otherwise`() {
        val customTeal = Color(0xFF00796B)
        val onCustomTeal = Color.White

        val outlineOnly = resolveComplicationForegroundColors(
            isDark = true,
            isActive = false,
            isTransparentInner = true,
            isInactiveTilesCustomized = true,
            animatedAccent = Color.Cyan,
            inactiveTileColor = customTeal,
            activeOnTileColor = Color.Black,
            inactiveOnTileColor = onCustomTeal,
            onSurfaceColor = Color.White,
            onSurfaceVariantColor = Color.Gray,
            inactiveIconTintOverride = null
        )
        assertEquals(customTeal, outlineOnly.iconTint)

        val filledParentCard = resolveComplicationForegroundColors(
            isDark = true,
            isActive = false,
            isTransparentInner = true,
            isInactiveTilesCustomized = true,
            animatedAccent = Color.Cyan,
            inactiveTileColor = customTeal,
            activeOnTileColor = Color.Black,
            inactiveOnTileColor = onCustomTeal,
            onSurfaceColor = Color.White,
            onSurfaceVariantColor = Color.Gray,
            inactiveIconTintOverride = onCustomTeal
        )
        assertNotEquals(customTeal, filledParentCard.iconTint)
        assertEquals(onCustomTeal, filledParentCard.iconTint)
    }

    @Test
    fun `getInactiveTilesOnElementColor contrasts with custom light and dark colors and renders ComplicationTileView`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PanelPreferences.init(context)
        PanelPreferences.setInactiveTilesUseCustom(context, isDark = true, useCustom = true)
        PanelPreferences.setInactiveTilesCustomColor(context, isDark = true, color = 0xFFFFEB3B.toInt())
        PanelPreferences.setComplicationOutlineEnabled(context, false)

        var resolvedBg: Color? = null
        var resolvedOnBg: Color? = null

        val inactiveModel = ComplicationTileModel(
            id = "test_comp",
            title = "Battery",
            subline = "64%",
            progress = 0.64f,
            icon = Icons.Default.BatteryFull,
            isActive = false,
            sublinePlacement = ComplicationSublinePlacement.INSIDE_RING,
            layoutMode = ComplicationLayoutMode.GRID_TILE,
            tapAction = ComplicationAction.Toggle("battery_saver"),
            settingsAction = android.provider.Settings.ACTION_SETTINGS
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                resolvedBg = getInactiveTilesElementColor(isDark = true)
                resolvedOnBg = getInactiveTilesOnElementColor(isDark = true)
                ComplicationTileView(
                    model = inactiveModel,
                    showRingBackground = true
                )
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(Color(0xFFFFEB3B.toInt()), resolvedBg)
        assertEquals(Color(0xFF1A1A1A), resolvedOnBg)
        assertNotEquals(resolvedBg, resolvedOnBg)
        composeTestRule.onNodeWithTag("complication_tile_test_comp").assertIsDisplayed()

        // Switch to dark custom color and verify contrast flips to white
        PanelPreferences.setInactiveTilesCustomColor(context, isDark = true, color = 0xFF1A237E.toInt())
        composeTestRule.waitForIdle()
        assertEquals(Color(0xFF1A237E.toInt()), resolvedBg)
        assertEquals(Color.White, resolvedOnBg)

        // Reset custom flag
        PanelPreferences.setInactiveTilesUseCustom(context, isDark = true, useCustom = false)
        PanelPreferences.setInactiveTilesColorMode(context, isDark = true, mode = ComplicationArcMonetMode.DEFAULT)
    }

    @Test
    fun `footer button icons use contrasting color when footer icon backgrounds and custom inactive color are active`() {
        val brightCustomBg = Color.White
        val brightContrastTint = computeContrastForegroundForColor(brightCustomBg)
        assertEquals(Color(0xFF1A1A1A), brightContrastTint)

        val resolvedWhenBgEnabled = resolveFooterButtonIconTint(
            footerIconBgEnabled = true,
            inactiveOnTileColor = brightContrastTint,
            defaultIconTint = Color.White
        )
        assertEquals(Color(0xFF1A1A1A), resolvedWhenBgEnabled)

        val resolvedWhenBgDisabled = resolveFooterButtonIconTint(
            footerIconBgEnabled = false,
            inactiveOnTileColor = brightContrastTint,
            defaultIconTint = Color(0xFFCCCCCC)
        )
        assertEquals(Color(0xFFCCCCCC), resolvedWhenBgDisabled)

        val context = ApplicationProvider.getApplicationContext<Context>()
        PanelPreferences.init(context)
        PanelPreferences.setFooterIconBackground(context, true)
        PanelPreferences.setSettingsIconLocation(context, SettingsIconLocation.EXPANDED_QS)
        PanelPreferences.setMoveCollapseToQqsFooter(context, true)
        PanelPreferences.setShowCarrier(context, false)
        PanelPreferences.setInactiveTilesUseCustom(context, isDark = true, useCustom = true)
        PanelPreferences.setInactiveTilesCustomColor(context, isDark = true, color = 0xFFFFFFFF.toInt())

        val sampleTiles = listOf(
            QsTileModel(id = "wifi", label = "Wi-Fi", subtitle = "Off", state = TileState.INACTIVE, category = TileCategory.CONNECTIVITY)
        )
        var resolvedFooterBg: Color? = null
        var resolvedFooterFg: Color? = null

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                resolvedFooterBg = getInactiveTilesElementColor(isDark = true)
                resolvedFooterFg = getInactiveTilesOnElementColor(isDark = true)
                UnifiedQsPanel(
                    expansionStage = QsExpansionStage.FULL_QS,
                    tiles = sampleTiles,
                    gridLayout = QsGridLayout.DEFAULT,
                    brightness = 128,
                    onBrightnessChange = {},
                    onTileClick = {},
                    onTileLongClick = {},
                    onExpandFull = {},
                    onCollapseToQqs = {},
                    onClosePanel = {},
                    notifications = emptyList(),
                    isNotificationPermissionGranted = true,
                    onNotificationClick = {},
                    onNotificationDismiss = {},
                    onNotificationActionClick = {},
                    onDismissAllNotifications = {},
                    onRequestNotificationAccess = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(Color.White, resolvedFooterBg)
        assertEquals(Color(0xFF1A1A1A), resolvedFooterFg)
        composeTestRule.onNodeWithTag("collapse_to_qqs_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("footer_edit_tiles_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("footer_settings_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("footer_power_button").assertIsDisplayed()

        // Clean up preferences
        PanelPreferences.setInactiveTilesUseCustom(context, isDark = true, useCustom = false)
        PanelPreferences.setFooterIconBackground(context, false)
    }
}
