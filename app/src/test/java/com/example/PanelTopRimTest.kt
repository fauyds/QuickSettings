package com.example

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.PanelPreferences
import com.example.data.QsPanelStyle
import com.example.ui.customizer.CustomizeScreen
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.QsDesignSection
import com.example.ui.notification.CustomShadowHelper
import com.example.ui.notification.TopRimOutlineDrawable
import com.example.ui.overlay.ContainerCard
import com.example.ui.overlay.QsOverlaySurface
import com.example.ui.overlay.QsOverlaySurfaceConfig
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class PanelTopRimTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PanelPreferences.init(context)
        PanelPreferences.setPanelStyle(context, QsPanelStyle.DEFAULT)
        PanelPreferences.setEnablePanelTopRim(context, false)
    }

    @Test
    fun `panel top rim preference persists across init`() {
        assertFalse(PanelPreferences.enablePanelTopRim.value)

        PanelPreferences.setEnablePanelTopRim(context, true)
        assertTrue(PanelPreferences.enablePanelTopRim.value)

        PanelPreferences.init(context)
        assertTrue(PanelPreferences.enablePanelTopRim.value)

        PanelPreferences.setEnablePanelTopRim(context, false)
        assertFalse(PanelPreferences.enablePanelTopRim.value)
    }

    @Test
    fun `search finds Panel Top Rim and toggles preference without pinned badge`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Panel Top Rim")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.DESIGN_PANEL_TOP_RIM.id}")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("PINNED", useUnmergedTree = true)
            .assertCountEquals(0)

        composeTestRule.onNodeWithTag("feature_panel_top_rim_switch")
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enablePanelTopRim.value)

        composeTestRule.onNodeWithTag("feature_panel_top_rim_switch")
            .performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.enablePanelTopRim.value)
    }

    @Test
    fun `QsDesignSection panel top rim toggle updates preference`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("panel_top_rim_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enablePanelTopRim.value)
    }

    @Test
    fun `ContainerCard and QsOverlaySurface render cleanly with top rim enabled`() {
        PanelPreferences.setEnablePanelTopRim(context, true)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column {
                    ContainerCard {
                        Text("Rim Container Content")
                    }
                    QsOverlaySurface(
                        config = QsOverlaySurfaceConfig(enablePanelTopRim = true),
                        testTagPrefix = "qs_overlay_preview"
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("Rim Container Content").assertCountEquals(1)
        composeTestRule.onNodeWithTag("qs_overlay_preview").assertIsDisplayed()
    }

    @Test
    fun `CustomShadowHelper attaches and clears TopRimOutlineDrawable on view foreground`() {
        val view = FrameLayout(context)
        val radii = FloatArray(8) { 20f }

        CustomShadowHelper.applyPanelTopRimForeground(view, enabled = true, radii = radii)
        assertTrue(view.foreground is TopRimOutlineDrawable)

        CustomShadowHelper.applyPanelTopRimForeground(view, enabled = false, radii = radii)
        assertNull(view.foreground)
    }
}
