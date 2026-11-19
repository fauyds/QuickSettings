package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.model.PrivilegeMode
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.model.TileState
import com.example.system.NativeTileManager
import com.example.system.PrivilegeEngine
import com.example.system.TileRegistry
import com.example.ui.overlay.CircularQsTile
import com.example.ui.theme.QuickPanelTheme
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
class BatterySaverDarkModeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dark_mode tile state becomes UNAVAILABLE when battery saver is enabled`() {
        val offResult = TileRegistry.resolveDarkModeTileState(darkThemeOn = false, batterySaverOn = false)
        assertEquals(TileState.INACTIVE, offResult.first)
        assertEquals("Off", offResult.third)

        val onResult = TileRegistry.resolveDarkModeTileState(darkThemeOn = true, batterySaverOn = false)
        assertEquals(TileState.ACTIVE, onResult.first)
        assertEquals("On", onResult.third)

        val blockedWhenDarkOff = TileRegistry.resolveDarkModeTileState(darkThemeOn = false, batterySaverOn = true)
        assertEquals(TileState.UNAVAILABLE, blockedWhenDarkOff.first)
        assertEquals("Battery Saver", blockedWhenDarkOff.third)

        val blockedWhenDarkOn = TileRegistry.resolveDarkModeTileState(darkThemeOn = true, batterySaverOn = true)
        assertEquals(TileState.UNAVAILABLE, blockedWhenDarkOn.first)
        assertEquals("Battery Saver", blockedWhenDarkOn.third)
    }

    @Test
    fun `dark_mode tile UI is disabled and blocks clicks when UNAVAILABLE`() {
        var clickCount = 0
        val unavailableDarkTile = QsTileModel(
            id = "dark_mode",
            label = "Dark Theme",
            subtitle = "Battery Saver",
            state = TileState.UNAVAILABLE,
            category = TileCategory.SYSTEM
        )
        val activeDarkTile = unavailableDarkTile.copy(
            subtitle = "On",
            state = TileState.ACTIVE
        )

        var currentTile = unavailableDarkTile
        val tileState = androidx.compose.runtime.mutableStateOf(unavailableDarkTile)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                CircularQsTile(
                    tile = tileState.value,
                    morphProgress = 1f,
                    onClick = { clickCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()

        // While Battery Saver is on -> tile is UNAVAILABLE -> disabled & clicks ignored
        composeTestRule.onNodeWithTag("tile_dark_mode").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("tile_dark_mode").performClick()
        assertEquals(0, clickCount)

        // When Battery Saver turns off -> tile becomes ACTIVE/INACTIVE -> enabled & clickable
        tileState.value = activeDarkTile
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tile_dark_mode").assertIsEnabled()
        composeTestRule.onNodeWithTag("tile_dark_mode").performClick()
        assertEquals(1, clickCount)
    }

    @Test
    fun `NativeTileManager blocks toggleDarkMode when battery saver is enabled`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val privilegeEngine = PrivilegeEngine(context)
        val scope = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Unconfined
        )
        val manager = NativeTileManager(context, privilegeEngine, scope)

        // Ensure Battery Saver starts off
        assertFalse(manager.isBatterySaverEnabled.value)
        val initialDark = manager.isDarkThemeEnabled.value

        // Enable Battery Saver
        manager.toggleBatterySaver()
        assertTrue(manager.isBatterySaverEnabled.value)

        // Attempt to toggle Dark Mode -> must remain unchanged (blocked)
        manager.toggleDarkMode()
        assertEquals(initialDark, manager.isDarkThemeEnabled.value)

        // Also test handleUniversalTileClick with UNAVAILABLE / dark_mode under battery saver
        val darkTile = QsTileModel(
            id = "dark_mode",
            label = "Dark Theme",
            subtitle = "Battery Saver",
            state = TileState.UNAVAILABLE,
            category = TileCategory.SYSTEM
        )
        manager.handleUniversalTileClick(darkTile)
        assertEquals(initialDark, manager.isDarkThemeEnabled.value)
    }
}
