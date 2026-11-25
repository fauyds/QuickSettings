package com.example

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.PanelPreferences
import com.example.data.ThemeManager
import com.example.model.PrivilegeMode
import com.example.system.PrivilegeEngine
import com.example.ui.customizer.CustomizeCategory
import com.example.ui.customizer.DebugSection
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.HiddenFeatureCatalog
import com.example.ui.customizer.MiscellaneousSection
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class EndlessRootModeTest {

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
        context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        PanelPreferences.init(context)
        ThemeManager.init(context)
        PanelPreferences.setPinnedFeatures(context, emptyList())
        PanelPreferences.unhideAllFeatures(context)
    }

    @Test
    fun `endlessRootMode defaults false, persists, and syncs with ThemeManager`() {
        assertFalse(PanelPreferences.endlessRootMode.value)

        PanelPreferences.setEndlessRootMode(context, true)
        assertTrue(PanelPreferences.endlessRootMode.value)

        // Reloads cleanly on init
        PanelPreferences.init(context)
        assertTrue(PanelPreferences.endlessRootMode.value)

        // Syncs with saved themes
        val savedTheme = ThemeManager.saveCurrentSettingsAsTheme(context, "Endless Root Test Theme")
        PanelPreferences.setEndlessRootMode(context, false)
        assertFalse(PanelPreferences.endlessRootMode.value)

        assertTrue(ThemeManager.applyTheme(context, savedTheme))
        assertTrue(PanelPreferences.endlessRootMode.value)
    }

    @Test
    fun `PrivilegeEngine grants root and elevates mode when Endless Root Mode is enabled`() {
        val engine = PrivilegeEngine(context)
        PanelPreferences.setEndlessRootMode(context, false)
        assertFalse(engine.isEndlessRootActive())

        PanelPreferences.setEndlessRootMode(context, true)
        assertTrue(engine.isEndlessRootActive())
        assertTrue(engine.isRootGranted())
        assertEquals(PrivilegeMode.ROOT, engine.currentMode.value)

        PanelPreferences.setEndlessRootMode(context, false)
        assertFalse(engine.isEndlessRootActive())
    }

    @Test
    fun `DebugSection displays Endless Root Mode card and toggle`() {
        PanelPreferences.setEndlessRootMode(context, false)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DebugSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("endless_root_mode_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("endless_root_mode_toggle").assertIsDisplayed()

        composeTestRule.onNodeWithTag("endless_root_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.endlessRootMode.value)

        composeTestRule.onNodeWithTag("endless_root_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.endlessRootMode.value)
    }

    @Test
    fun `MiscellaneousSection displays Endless Root Mode under Debug section`() {
        PanelPreferences.setEndlessRootMode(context, false)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    MiscellaneousSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("endless_root_mode_toggle")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.endlessRootMode.value)
    }

    @Test
    fun `HiddenFeaturesRegistry and FavoriteFeatures catalog resolve Endless Root Mode cleanly`() {
        val resolved = HiddenFeatureCatalog.resolve("endless_root_mode_card")
        assertEquals("Endless Root Mode", resolved.title)
        assertEquals(CustomizeCategory.MISCELLANEOUS.title, resolved.categoryTitle)
        assertEquals(Icons.Default.Terminal, resolved.icon)

        val favorite = FavoriteFeature.fromId("misc_endless_root")
        assertNotNull(favorite)
        assertEquals("Endless Root Mode", favorite?.title)
        assertEquals(CustomizeCategory.MISCELLANEOUS, favorite?.category)
        assertEquals(Icons.Default.Terminal, favorite?.icon)
    }
}
