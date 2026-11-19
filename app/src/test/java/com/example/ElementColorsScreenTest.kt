package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.ComplicationArcMonetMode
import com.example.data.PanelPreferences
import com.example.ui.customizer.ElementColorsScreen
import com.example.ui.theme.AppSettingsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class ElementColorsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `custom color option lives in color mode bar and toggles custom picker`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PanelPreferences.init(context)
        PanelPreferences.setSurfaceBgUseCustom(context, isDark = false, useCustom = false)
        PanelPreferences.setSurfaceBgColorMode(context, isDark = false, mode = ComplicationArcMonetMode.DEFAULT)

        composeTestRule.setContent {
            AppSettingsTheme(darkTheme = false) {
                ElementColorsScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        // Ensure we are editing Light tab and expand "1. Surface Background"
        composeTestRule.onNodeWithTag("mode_tab_light").performClick()
        composeTestRule.onNodeWithText("1. Surface Background").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Standalone "Set Custom Color" switch should no longer exist; "Custom" is in the Color Mode bar
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_use_custom_switch").assertDoesNotExist()
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_color_mode_bar", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_mode_custom")
            .performScrollTo()
            .assertIsDisplayed()

        // Select "Custom" in the color mode bar
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_mode_custom").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.getSurfaceBgUseCustom(isDark = false).value)
        // Color mode bar remains visible so the user can switch back from Custom to Monet modes
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_color_mode_bar", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()

        // Select "Tertiary" in the color mode bar -> exits Custom and sets TERTIARY
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_mode_tertiary")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertFalse(PanelPreferences.getSurfaceBgUseCustom(isDark = false).value)
        assertEquals(ComplicationArcMonetMode.TERTIARY, PanelPreferences.getSurfaceBgColorMode(isDark = false).value)
    }
}
