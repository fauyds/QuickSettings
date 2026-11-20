package com.example

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.BatteryPercentageLocation
import com.example.data.DatePosition
import com.example.data.EditButtonPosition
import com.example.data.HeadsUpAction
import com.example.data.PanelPreferences
import com.example.data.ProgressBarStyle
import com.example.data.QsPanelStyle
import com.example.data.ThemeMode
import com.example.model.QsGridLayout
import com.example.ui.customizer.CustomizeCategory
import com.example.ui.customizer.CustomizeScreen
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.GenericPinnedFeatureCard
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class CustomizeSearchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PanelPreferences.setPinnedFeatures(context, emptyList())
        PanelPreferences.setDatePosition(context, DatePosition.BELOW_TIME)
        PanelPreferences.setEditButtonPosition(context, EditButtonPosition.FOOTER)
        PanelPreferences.setEnabledTileShapeId(context, "circle")
        PanelPreferences.setProgressBarStyle(context, ProgressBarStyle.DEFAULT)
        PanelPreferences.setThemeMode(context, ThemeMode.SYSTEM)
        PanelPreferences.setPanelStyle(context, QsPanelStyle.DEFAULT)
        PanelPreferences.setGridLayout(context, QsGridLayout.GRID_4X3)
        PanelPreferences.setBatteryPercentageLocation(context, BatteryPercentageLocation.HEADER)
        PanelPreferences.setHeadsUpEdgeToEdge(context, false)
        PanelPreferences.setEnableSystemHeadsUp(context, false)
        PanelPreferences.setHeadsUpSwipeUpAction(context, HeadsUpAction.DISMISS)
        PanelPreferences.setDisablePanelAnimations(context, false)
        PanelPreferences.setShowBrightnessControlsBackground(context, true)
        com.example.ui.shape.QsShapeManager.init(context)
    }

    @Test
    fun `search results hide PINNED badge even when feature is queried`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Date Position")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.TWEAKS_DATE_POSITION.id}")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PINNED", useUnmergedTree = true)
            .assertCountEquals(0)

        composeTestRule.onNodeWithTag("feature_date_pos_${DatePosition.HEADER_RIGHT.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(DatePosition.HEADER_RIGHT, PanelPreferences.datePosition.value)
    }

    @Test
    fun `pinned card outside search still displays PINNED badge by default`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                GenericPinnedFeatureCard(feature = FavoriteFeature.TWEAKS_DATE_POSITION)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("pinned_feature_${FavoriteFeature.TWEAKS_DATE_POSITION.id}")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PINNED", useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun `search result horizontal choosers render and update preferences`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Edit Pencil")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("feature_edit_pos_${EditButtonPosition.BOTH.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(EditButtonPosition.BOTH, PanelPreferences.editButtonPosition.value)
    }

    @Test
    fun `search result tile shape theme mode and progress style choosers update preferences`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Active QS Tile Shape")
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("feature_enabled_tile_shape_squircle", useUnmergedTree = true)
            .assertCountEquals(1)
        composeTestRule.onNodeWithTag("feature_enabled_tile_shape_squircle")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals("squircle", PanelPreferences.enabledTileShapeId.value)
    }

    @Test
    fun `search result theme mode and progress style segmented choosers update preferences`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("App Theme Mode")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("feature_theme_mode_${ThemeMode.DARK.name.lowercase()}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(ThemeMode.DARK, PanelPreferences.themeMode.value)

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextReplacement("Sliders & Progress Bar Style")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("feature_progress_style_${ProgressBarStyle.WAVY.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(ProgressBarStyle.WAVY, PanelPreferences.progressBarStyle.value)
    }

    @Test
    fun `all 8 customize categories have searchable features`() {
        val visibleCategories = listOf(
            CustomizeCategory.HEADS_UP,
            CustomizeCategory.HEADS_UP_DESIGN,
            CustomizeCategory.BASIC,
            CustomizeCategory.QS_DESIGN,
            CustomizeCategory.QS_TWEAKS,
            CustomizeCategory.THEMES,
            CustomizeCategory.MISCELLANEOUS,
            CustomizeCategory.ABOUT
        )
        visibleCategories.forEach { cat ->
            val items = FavoriteFeature.getByCategory(cat)
            assertTrue("Expected searchable features in ${cat.name}", items.isNotEmpty())
        }
    }

    @Test
    fun `expanded search finds and controls features from all 8 categories without pinned badge`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        val searchInput = composeTestRule.onNodeWithTag("global_customize_search_input")

        // 1. HEADS_UP_DESIGN: Edge-to-Edge Banner switch
        searchInput.performTextInput("Edge-to-Edge")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.HEADS_UP_DESIGN_EDGE_TO_EDGE.id}")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PINNED", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onNodeWithTag("feature_hu_edge_to_edge_switch").performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, PanelPreferences.headsUpEdgeToEdge.value)

        // 2. HEADS_UP: System Heads-Up Fallback + Swipe Action chooser
        searchInput.performTextReplacement("System Heads-Up")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.HEADS_UP_SYSTEM.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_heads_up_system_switch").performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, PanelPreferences.enableSystemHeadsUp.value)

        searchInput.performTextReplacement("Heads-Up Swipe")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("feature_hu_up_${HeadsUpAction.SNOOZE_APP.id}")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(HeadsUpAction.SNOOZE_APP, PanelPreferences.headsUpSwipeUpAction.value)

        // 3. QS_DESIGN: Quick Settings Panel Style & Brightness Controls Background
        searchInput.performTextReplacement("Panel Style")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.DESIGN_PANEL_STYLE.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_panel_style_${QsPanelStyle.ANDROID_OREO.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(QsPanelStyle.ANDROID_OREO, PanelPreferences.panelStyle.value)

        searchInput.performTextReplacement("Brightness Controls Background")
        composeTestRule.waitForIdle()
        val beforeBg = PanelPreferences.showBrightnessControlsBackground.value
        composeTestRule.onNodeWithTag("feature_brightness_controls_bg_switch").performClick()
        composeTestRule.waitForIdle()
        assertEquals(!beforeBg, PanelPreferences.showBrightnessControlsBackground.value)

        // 4. QS_TWEAKS: Grid Layout & Battery Percentage Location
        searchInput.performTextReplacement("Grid Layout")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.TWEAKS_GRID_LAYOUT.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_grid_col_${QsGridLayout.GRID_3X3.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(QsGridLayout.GRID_3X3, PanelPreferences.gridLayout.value)

        searchInput.performTextReplacement("Battery Percentage")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("feature_battery_pos_${BatteryPercentageLocation.QQS_RIGHT.id}")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(BatteryPercentageLocation.QQS_RIGHT, PanelPreferences.batteryPercentageLocation.value)

        // 5. THEMES: Save Current Theme Profile
        searchInput.performTextReplacement("Save Current Theme")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.THEMES_SAVE.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_theme_save_btn").assertIsDisplayed()

        // 6. MISCELLANEOUS: Disable Panel Animations
        searchInput.performTextReplacement("Disable Panel Animations")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.MISC_ANIMATION_SPEED.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_disable_animations_switch").performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, PanelPreferences.disablePanelAnimations.value)

        // 7. ABOUT: Telegram Channel
        searchInput.performTextReplacement("Telegram")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.ABOUT_TELEGRAM.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_about_telegram_open_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_about_telegram_copy_btn").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("PINNED", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Access options in section body below", useUnmergedTree = true)
            .assertCountEquals(0)
    }
}
