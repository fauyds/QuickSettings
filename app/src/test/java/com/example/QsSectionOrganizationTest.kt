package com.example

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import com.example.data.PanelPreferences
import com.example.data.QsPanelStyle
import com.example.ui.customizer.CustomizeCategory
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.QsDesignSection
import com.example.ui.customizer.QsTweaksSection
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class QsSectionOrganizationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PanelPreferences.setPanelStyle(context, QsPanelStyle.DEFAULT)
        PanelPreferences.setShowCarrier(context, true)
        PanelPreferences.setCollapseExpandSubheaders(context, false)
    }

    @Test
    fun `moved options appear in Quick Settings Tweaks and are absent from Quick Settings Design`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    QsTweaksSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // 1. Horizontal Scroll QS Tiles
        composeTestRule.onAllNodesWithTag("enable_qqs_horizontal_scroll_card", useUnmergedTree = true)
            .assertCountEquals(1)
        // 2. Disable Clear All Label
        composeTestRule.onAllNodesWithTag("disable_clear_all_label_card", useUnmergedTree = true)
            .assertCountEquals(1)
        // 3. Show SIM Card Carrier
        composeTestRule.onAllNodesWithTag("show_carrier_toggle", useUnmergedTree = true)
            .assertCountEquals(1)
        // 4. Divider Between Carrier & QQS Tiles
        composeTestRule.onAllNodesWithTag("show_carrier_divider_toggle", useUnmergedTree = true)
            .assertCountEquals(1)
        // 5. Replace Carrier Divider With Line
        composeTestRule.onAllNodesWithTag("carrier_divider_as_line_toggle", useUnmergedTree = true)
            .assertCountEquals(1)
        // 6. Hide Carrier in Expanded QS
        composeTestRule.onAllNodesWithTag("hide_carrier_in_expanded_card", useUnmergedTree = true)
            .assertCountEquals(1)

        // Items moved to Quick Settings Design must NOT exist in Quick Settings Tweaks
        composeTestRule.onAllNodesWithTag("footer_icon_background_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("footer_outer_spacing_toggle", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("qs_page_indicator_style_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("header_status_background_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("show_brightness_controls_background_card", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `moved options appear in Quick Settings Design and are absent from Quick Settings Tweaks`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // 1. Footer Icon Backgrounds
        composeTestRule.onAllNodesWithTag("footer_icon_background_card", useUnmergedTree = true)
            .assertCountEquals(1)
        // 2. Outer 3dp Spacing Between Buttons
        composeTestRule.onAllNodesWithTag("footer_outer_spacing_toggle", useUnmergedTree = true)
            .assertCountEquals(1)
        // 3. QS Page Indicator Style
        composeTestRule.onAllNodesWithTag("qs_page_indicator_style_card", useUnmergedTree = true)
            .assertCountEquals(1)
        // 4. Header & Status Backgrounds
        composeTestRule.onAllNodesWithTag("header_status_background_card", useUnmergedTree = true)
            .assertCountEquals(1)
        // 5. Brightness Control Background
        composeTestRule.onAllNodesWithTag("show_brightness_controls_background_card", useUnmergedTree = true)
            .assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Brightness Controls Background", useUnmergedTree = true)
            .assertCountEquals(1)

        // Items moved to Quick Settings Tweaks must NOT exist in Quick Settings Design
        composeTestRule.onAllNodesWithTag("enable_qqs_horizontal_scroll_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("disable_clear_all_label_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("show_carrier_toggle", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("carrier_divider_card", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("hide_carrier_in_expanded_card", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `FavoriteFeatures categories match moved carrier options`() {
        assertEquals(CustomizeCategory.QS_TWEAKS, FavoriteFeature.DESIGN_SIM_CARRIER.category)
        assertEquals(CustomizeCategory.QS_TWEAKS, FavoriteFeature.DESIGN_CARRIER_DIVIDER.category)
    }
}
