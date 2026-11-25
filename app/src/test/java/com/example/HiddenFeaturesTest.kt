package com.example

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.PanelPreferences
import com.example.data.ThemeManager
import com.example.ui.customizer.CustomizeCategory
import com.example.ui.customizer.CustomizeScreen
import com.example.ui.customizer.ElementColorsScreen
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.HeadsUpDesignSection
import com.example.ui.customizer.HiddenFeatureCatalog
import com.example.ui.customizer.QsDesignSection
import com.example.ui.customizer.QsTweaksSection
import com.example.ui.customizer.ThemesSection
import com.example.ui.customizer.TileCommandsScreen
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class HiddenFeaturesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(PanelPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        PanelPreferences.init(context)
        ThemeManager.init(context)
        PanelPreferences.setPinnedFeatures(context, emptyList())
        PanelPreferences.unhideAllFeatures(context)
    }

    @Test
    fun `holding any option opens Pin dropdown with Hide option and hides card persistently`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HeadsUpDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("heads_up_edge_to_edge_card").assertIsDisplayed()

        // Long-press option card -> Pin dropdown opens with both Pin and Hide
        composeTestRule.onNodeWithTag("heads_up_edge_to_edge_card")
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hide").assertIsDisplayed()

        // Click Hide -> card is hidden and persisted
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.isFeatureHidden("heads_up_edge_to_edge_card"))
        composeTestRule.onAllNodesWithTag("heads_up_edge_to_edge_card", useUnmergedTree = true)
            .assertCountEquals(0)

        // Simulate app restart via PanelPreferences.init
        PanelPreferences.init(context)
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.isFeatureHidden("heads_up_edge_to_edge_card"))
        composeTestRule.onAllNodesWithTag("heads_up_edge_to_edge_card", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `hiding all features in a section shows recovery card and restoring brings them back`() {
        val allHeadsUpDesignCards = listOf(
            "heads_up_design_test_card",
            "heads_up_edge_to_edge_card",
            "heads_up_corner_radius_card",
            "heads_up_elevation_card",
            "heads_up_top_margin_card"
        )
        PanelPreferences.setHiddenFeatures(context, allHeadsUpDesignCards)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HeadsUpDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("all_section_features_hidden_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_section_show_hidden_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hidden_features_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unhide_all_features_btn").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.hiddenFeatures.value.isEmpty())
        composeTestRule.onNodeWithTag("heads_up_edge_to_edge_card").assertIsDisplayed()
    }

    @Test
    fun `CustomizeScreen button shows every hidden feature and supports individual or bulk restore`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        // Hold About category button and hide it
        val aboutCategoryTag = "category_btn_${CustomizeCategory.ABOUT.name.lowercase()}"
        composeTestRule.onNodeWithTag(aboutCategoryTag)
            .performScrollTo()
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.isFeatureHidden("cat_about"))
        composeTestRule.onAllNodesWithTag(aboutCategoryTag, useUnmergedTree = true)
            .assertCountEquals(0)

        // Also hide another option directly so multiple items appear in Hidden Features dialog
        PanelPreferences.hideFeature(context, "panel_top_rim_card")
        composeTestRule.waitForIdle()

        // Hidden feature's search result is also excluded from Customize global search
        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Panel Top Rim")
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("search_result_${FavoriteFeature.DESIGN_PANEL_TOP_RIM.id}", useUnmergedTree = true)
            .assertCountEquals(0)

        // Tap button that shows every hidden feature
        composeTestRule.onNodeWithTag("show_hidden_features_btn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show Hidden (2)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("show_hidden_features_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hidden_features_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hidden_feature_item_cat_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hidden_feature_item_panel_top_rim_card").assertIsDisplayed()

        // Restore one feature individually
        composeTestRule.onNodeWithTag("unhide_feature_btn_cat_about").performClick()
        composeTestRule.waitForIdle()

        assertFalse(PanelPreferences.isFeatureHidden("cat_about"))
        assertTrue(PanelPreferences.isFeatureHidden("panel_top_rim_card"))
        composeTestRule.onAllNodesWithTag("hidden_feature_item_cat_about", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onNodeWithTag("hidden_feature_item_panel_top_rim_card").assertIsDisplayed()

        // Show all remaining hidden features
        composeTestRule.onNodeWithTag("unhide_all_features_btn").performClick()
        composeTestRule.waitForIdle()

        assertEquals(emptyList<String>(), PanelPreferences.hiddenFeatures.value)
        composeTestRule.onAllNodesWithTag("hidden_features_dialog", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.DESIGN_PANEL_TOP_RIM.id}")
            .assertIsDisplayed()
    }

    @Test
    fun `Quick Settings Style panel_style_card hides reliably via dropdown and search sync`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Quick Settings Style").assertIsDisplayed()

        // Long-press Quick Settings Style card -> Pin/Hide dropdown -> Hide
        composeTestRule.onNodeWithText("Quick Settings Style")
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.isFeatureHidden("panel_style_card"))
        assertTrue(
            HiddenFeatureCatalog.isSearchFeatureHidden(
                FavoriteFeature.DESIGN_PANEL_STYLE,
                PanelPreferences.hiddenFeatures.value
            )
        )
        composeTestRule.onAllNodesWithText("Quick Settings Style", useUnmergedTree = true)
            .assertCountEquals(0)

        // Legacy alias and favorite ID should also hide panel_style_card even if unpinned/pinned
        PanelPreferences.unhideAllFeatures(context)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Quick Settings Style").assertIsDisplayed()

        PanelPreferences.hideFeature(context, "android_oreo_style_card")
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Quick Settings Style", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `collapsible header rows with clickable expand still open Pin Hide dropdown on long press`() {
        PanelPreferences.setCollapseExpandOptions(context, true)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    QsDesignSection()
                    QsTweaksSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // Hold directly on the inner clickable title of tile_shape_selection_card
        composeTestRule.onNodeWithText("Tile Shapes (XML Paths)")
            .performScrollTo()
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.isFeatureHidden("tile_shape_selection_card"))
        composeTestRule.onAllNodesWithTag("tile_shape_selection_card", useUnmergedTree = true)
            .assertCountEquals(0)

        // Hold directly on the inner clickable header of qs_tile_font_size_card
        composeTestRule.onNodeWithText("Tile Label & Subtitle Font Size")
            .performScrollTo()
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.isFeatureHidden("qs_tile_font_size_card"))
        composeTestRule.onAllNodesWithTag("qs_tile_font_size_card", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `ThemesSection hides SAVED THEMES header and shows recovery card when all theme cards hidden`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ThemesSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("SAVED THEMES").assertIsDisplayed()

        PanelPreferences.setHiddenFeatures(
            context,
            listOf("save_theme_card", "theme_transfer_card", "empty_themes_card")
        )
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("SAVED THEMES", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onNodeWithTag("all_section_features_hidden_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("empty_section_show_hidden_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unhide_all_features_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("SAVED THEMES").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_theme_card").assertIsDisplayed()
    }

    @Test
    fun `ElementColorsScreen expandable cards hide with stable IDs and restore from app bar button`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                ElementColorsScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        // Intro card should NOT open Pin/Hide dropdown (canPin = false)
        composeTestRule.onNodeWithText("Per-Element Color & Accent Customization")
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("dropdown_hide_button", useUnmergedTree = true)
            .assertCountEquals(0)

        // Expandable element color card header should open Pin/Hide dropdown and save stable ID
        composeTestRule.onNodeWithText("1. Surface Background")
            .performScrollTo()
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_pin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dropdown_hide_button").performClick()
        composeTestRule.waitForIdle()

        val hidden = PanelPreferences.hiddenFeatures.value
        assertTrue(hidden.contains("elem_color_surface_bg"))
        assertTrue(hidden.none { it.startsWith("option_") })
        composeTestRule.onAllNodesWithTag("activity_surface_bg_tertiary_toggle_card", useUnmergedTree = true)
            .assertCountEquals(0)

        // TopBar Hidden button opens dialog and restores it
        composeTestRule.onNodeWithTag("element_colors_show_hidden_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("element_colors_show_hidden_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hidden_features_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unhide_feature_btn_elem_color_surface_bg").performClick()
        composeTestRule.waitForIdle()

        assertFalse(PanelPreferences.isFeatureHidden("elem_color_surface_bg"))
        composeTestRule.onNodeWithTag("activity_surface_bg_tertiary_toggle_card").assertIsDisplayed()
    }

    @Test
    fun `non-Customize SettingsCards with canPin false never show broken Pin or Hide dropdown`() {
        val privilegeEngine = com.example.system.PrivilegeEngine(context)
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                TileCommandsScreen(privilegeEngine = privilegeEngine)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tile_cmd_enable_all_card")
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("dropdown_pin_button", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("dropdown_hide_button", useUnmergedTree = true)
            .assertCountEquals(0)
        assertTrue(PanelPreferences.hiddenFeatures.value.isEmpty())
    }

    @Test
    fun `SettingsCard without stable ID suppresses Pin and Hide dropdown even if canPin defaults true`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                com.example.ui.customizer.SettingsCard(
                    modifier = Modifier.testTag("guard_test_card")
                ) {
                    androidx.compose.material3.Text("Unpinned Guard Test")
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Unpinned Guard Test")
            .performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("dropdown_pin_button", useUnmergedTree = true)
            .assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("dropdown_hide_button", useUnmergedTree = true)
            .assertCountEquals(0)
        assertTrue(PanelPreferences.hiddenFeatures.value.isEmpty())
    }

    @Test
    fun `Glass Backdrop card displays and switches glassBackdropMode successfully`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_panel_card")
            .performScrollTo()
            .assertIsDisplayed()

        assertFalse(PanelPreferences.enableContainerBlur.value)
        assertEquals(com.example.data.GlassBackdropMode.OFF, PanelPreferences.glassBackdropMode.value)

        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertEquals(com.example.data.GlassBackdropMode.FROSTED, PanelPreferences.glassBackdropMode.value)
        assertFalse(PanelPreferences.enableGlassRefraction.value)
    }
}
