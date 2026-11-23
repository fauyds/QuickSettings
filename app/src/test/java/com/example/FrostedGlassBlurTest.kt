package com.example

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.PanelPreferences
import com.example.ui.overlay.BlurredContainerCard
import com.example.ui.overlay.FrostedPreviewBackdrop
import com.example.ui.overlay.applyWindowBlurBehind
import com.example.ui.overlay.computeWindowBlurBehindRadiusPx
import com.example.ui.overlay.frostedContainerColor
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
class FrostedGlassBlurTest {

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
    }

    @Test
    fun `computeWindowBlurBehindRadiusPx scales with density and animation fraction`() {
        assertEquals(0, computeWindowBlurBehindRadiusPx(enabled = false, density = 3f, fraction = 1f))
        assertEquals(0, computeWindowBlurBehindRadiusPx(enabled = true, density = 3f, fraction = 0f))
        assertEquals(0, computeWindowBlurBehindRadiusPx(enabled = true, density = 3f, fraction = 0.005f))

        // Full reveal: 24.dp * 3.0 = 72px
        assertEquals(72, computeWindowBlurBehindRadiusPx(enabled = true, density = 3f, fraction = 1f))

        // Mid reveal quantizes into 4px steps between 4 and 72
        val midPx = computeWindowBlurBehindRadiusPx(enabled = true, density = 3f, fraction = 0.5f)
        assertTrue(midPx in 4..72)
        assertEquals(0, midPx % 4)
    }

    @Test
    fun `applyWindowBlurBehind configures FLAG_BLUR_BEHIND and blurBehindRadius on Android 12+`() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // Initially no blur flag
        assertEquals(0, params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        assertEquals(0, params.blurBehindRadius)

        // Enabling sets flag and radius and returns true (changed)
        assertTrue(applyWindowBlurBehind(params, 64))
        assertTrue((params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND) != 0)
        assertEquals(64, params.blurBehindRadius)

        // Re-applying identical radius returns false (no redundant updateViewLayout needed)
        assertFalse(applyWindowBlurBehind(params, 64))

        // Setting radius 0 clears FLAG_BLUR_BEHIND and resets radius
        assertTrue(applyWindowBlurBehind(params, 0))
        assertEquals(0, params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        assertEquals(0, params.blurBehindRadius)

        // Re-applying 0 returns false
        assertFalse(applyWindowBlurBehind(params, 0))
    }

    @Test
    fun `frostedContainerColor preserves RGB while applying translucent alpha only when enabled`() {
        val baseColor = Color(0xFF1E2429)

        assertEquals(baseColor, frostedContainerColor(baseColor, enableBlur = false, isDark = true))
        assertEquals(baseColor, frostedContainerColor(baseColor, enableBlur = false, isDark = false))

        val darkFrosted = frostedContainerColor(baseColor, enableBlur = true, isDark = true)
        assertEquals(baseColor.red, darkFrosted.red, 0.003f)
        assertEquals(baseColor.green, darkFrosted.green, 0.003f)
        assertEquals(baseColor.blue, darkFrosted.blue, 0.003f)
        assertEquals(0.56f, darkFrosted.alpha, 0.003f)

        val lightFrosted = frostedContainerColor(baseColor, enableBlur = true, isDark = false)
        assertEquals(baseColor.red, lightFrosted.red, 0.003f)
        assertEquals(baseColor.green, lightFrosted.green, 0.003f)
        assertEquals(baseColor.blue, lightFrosted.blue, 0.003f)
        assertEquals(0.68f, lightFrosted.alpha, 0.003f)
    }

    @Test
    fun `BlurredContainerCard and FrostedPreviewBackdrop blur only underlying layer while panel stays crisp`() {
        PanelPreferences.setEnableContainerBlur(context, true)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                var clicks by remember { mutableIntStateOf(0) }
                Box(modifier = Modifier.fillMaxSize()) {
                    FrostedPreviewBackdrop(
                        enableBlur = true,
                        dimAlpha = 0.34f,
                        modifier = Modifier.matchParentSize()
                    )
                    BlurredContainerCard(
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("frosted_panel_card_under_test")
                    ) {
                        Button(
                            onClick = { clicks++ },
                            modifier = Modifier
                                .padding(16.dp)
                                .testTag("sharp_tile_button")
                        ) {
                            Text("Wi-Fi Crisp Tile ($clicks)")
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        // Both underlying backdrop layer & sharp panel card exist in hierarchy
        composeTestRule.onNodeWithTag("frosted_preview_backdrop_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_preview_backdrop_layer", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_preview_scrim", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_panel_card_under_test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wi-Fi Crisp Tile (0)").assertIsDisplayed()

        composeTestRule.onNodeWithTag("sharp_tile_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Wi-Fi Crisp Tile (1)").assertIsDisplayed()
    }

    @Test
    fun `enableContainerBlur and blurOnlyBehindQsCard persist across PanelPreferences re-initialization`() {
        assertFalse(PanelPreferences.enableContainerBlur.value)
        assertFalse(PanelPreferences.blurOnlyBehindQsCard.value)

        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setBlurOnlyBehindQsCard(context, true)
        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertTrue(PanelPreferences.blurOnlyBehindQsCard.value)

        // Simulate app restart re-reading SharedPreferences
        PanelPreferences.init(context)
        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertTrue(PanelPreferences.blurOnlyBehindQsCard.value)

        PanelPreferences.setEnableContainerBlur(context, false)
        PanelPreferences.setBlurOnlyBehindQsCard(context, false)
        PanelPreferences.init(context)
        assertFalse(PanelPreferences.enableContainerBlur.value)
        assertFalse(PanelPreferences.blurOnlyBehindQsCard.value)
    }

    @Test
    fun `computeQsBlurWindowPlan routes blur exclusively to full-screen or QS card window`() {
        val fullPlan = com.example.ui.overlay.computeQsBlurWindowPlan(
            desiredRadiusPx = 72,
            blurOnlyBehindQsCard = false
        )
        assertEquals(72, fullPlan.fullScreenBlurRadiusPx)
        assertEquals(0, fullPlan.qsCardBlurRadiusPx)

        val qsOnlyPlan = com.example.ui.overlay.computeQsBlurWindowPlan(
            desiredRadiusPx = 72,
            blurOnlyBehindQsCard = true
        )
        assertEquals(0, qsOnlyPlan.fullScreenBlurRadiusPx)
        assertEquals(72, qsOnlyPlan.qsCardBlurRadiusPx)
    }

    @Test
    fun `applyQsBlurBackdropParams sizes window to QS card bounds without FLAG_BLUR_BEHIND`() {
        val params = com.example.ui.overlay.createQsBlurBackdropWindowParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        )
        val bounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 36f,
            top = 120f,
            right = 1044f,
            bottom = 860f,
            topCornerRadiusPx = 48f,
            bottomCornerRadiusPx = 48f
        )

        assertTrue(com.example.ui.overlay.applyQsBlurBackdropParams(params, bounds, radiusPx = 72))
        assertEquals(36, params.x)
        assertEquals(120, params.y)
        assertEquals(1008, params.width)
        assertEquals(740, params.height)
        // Must NEVER set FLAG_BLUR_BEHIND (which blurs the full display); bounded blur uses Window.setBackgroundBlurRadius
        assertEquals(0, params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        assertEquals(0, params.blurBehindRadius)

        // Identical params returns false
        assertFalse(com.example.ui.overlay.applyQsBlurBackdropParams(params, bounds, radiusPx = 72))

        // Turning off collapses the card blur window and keeps FLAG_BLUR_BEHIND cleared
        assertTrue(com.example.ui.overlay.applyQsBlurBackdropParams(params, bounds, radiusPx = 0))
        assertEquals(0, params.width)
        assertEquals(0, params.height)
        assertEquals(0, params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        assertEquals(0, params.blurBehindRadius)
    }

    @Test
    fun `QsCardBlurWindowHost manages bounded background blur lifecycle cleanly`() {
        val host = com.example.ui.overlay.QsCardBlurWindowHost(
            context = context,
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        )
        val bounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 24f,
            top = 100f,
            right = 1056f,
            bottom = 780f,
            topCornerRadiusPx = 48f,
            bottomCornerRadiusPx = 48f
        )

        host.showBehindOverlay()
        host.update(bounds = bounds, radiusPx = 72)
        host.update(bounds = bounds, radiusPx = 0)
        host.dismiss()
    }

    @Test
    fun `FrostedPreviewBackdrop mounts QS-only clip region only when blurOnlyBehindQsCard is active`() {
        val showQsOnly = androidx.compose.runtime.mutableStateOf(true)
        val sampleBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 24f,
            top = 80f,
            right = 900f,
            bottom = 500f,
            topCornerRadiusPx = 40f,
            bottomCornerRadiusPx = 40f
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                FrostedPreviewBackdrop(
                    enableBlur = true,
                    dimAlpha = 0.28f,
                    blurOnlyBehindQsCard = showQsOnly.value,
                    qsCardBounds = sampleBounds
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_preview_backdrop_layer", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_preview_qs_blur_region", useUnmergedTree = true).assertIsDisplayed()

        showQsOnly.value = false
        composeTestRule.waitForIdle()
        assertEquals(
            0,
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasTestTag("frosted_preview_qs_blur_region"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().size
        )
    }

    @Test
    fun `frosted_glass_qs_only_toggle in QsDesignSection toggles blurOnlyBehindQsCard independently`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = false) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    com.example.ui.customizer.QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.blurOnlyBehindQsCard.value)
        assertFalse(PanelPreferences.enableContainerBlur.value)
    }

    @Test
    fun `frostedContainerColor applies custom transparency alpha when provided`() {
        val baseColor = Color(0xFF20252A)

        val alpha60 = frostedContainerColor(baseColor, enableBlur = true, isDark = true, transparency = 0.40f)
        assertEquals(0.60f, alpha60.alpha, 0.005f)

        val alpha25 = frostedContainerColor(baseColor, enableBlur = true, isDark = false, transparency = 0.75f)
        assertEquals(0.25f, alpha25.alpha, 0.005f)

        // Clamped max transparency 95% -> minimum 5% opacity
        val minAlpha = frostedContainerColor(baseColor, enableBlur = true, isDark = true, transparency = 1.0f)
        assertEquals(0.05f, minAlpha.alpha, 0.005f)

        // Disabled blur ignores transparency
        assertEquals(baseColor, frostedContainerColor(baseColor, enableBlur = false, isDark = true, transparency = 0.75f))
    }

    @Test
    fun `containerBlurRadiusDp and containerBlurTransparency persist and sync with ThemeManager`() {
        assertEquals(24f, PanelPreferences.containerBlurRadiusDp.value, 0.01f)
        assertEquals(0.40f, PanelPreferences.containerBlurTransparency.value, 0.01f)

        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setContainerBlurRadiusDp(context, 42f)
        PanelPreferences.setContainerBlurTransparency(context, 0.70f)

        // Reload preferences cleanly
        PanelPreferences.init(context)
        assertEquals(42f, PanelPreferences.containerBlurRadiusDp.value, 0.01f)
        assertEquals(0.70f, PanelPreferences.containerBlurTransparency.value, 0.01f)

        // Save theme snapshot, reset values, and re-apply saved theme
        val saved = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Glass Test")
        PanelPreferences.setEnableContainerBlur(context, false)
        PanelPreferences.setContainerBlurRadiusDp(context, 10f)
        PanelPreferences.setContainerBlurTransparency(context, 0.15f)

        assertTrue(com.example.data.ThemeManager.applyTheme(context, saved))
        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertEquals(42f, PanelPreferences.containerBlurRadiusDp.value, 0.01f)
        assertEquals(0.70f, PanelPreferences.containerBlurTransparency.value, 0.01f)
    }

    @Test
    fun `blur sensitivity and transparency sliders show only when frosted glass blur is enabled`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    com.example.ui.customizer.QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // Initially hidden while blur is disabled
        assertEquals(
            0,
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasTestTag("frosted_glass_blur_sensitivity_slider"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().size
        )
        assertEquals(
            0,
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasTestTag("frosted_glass_transparency_slider"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().size
        )

        // Enable frosted glass blur
        composeTestRule.onNodeWithTag("frosted_glass_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableContainerBlur.value)

        // Both sliders and value readouts appear when blur is enabled
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_value", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_glass_transparency_value", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_glass_transparency_slider")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `Android 16 BAKLAVA frosted glass RenderEffect and AGSL surface pipeline execute cleanly on API 36`() {
        assertTrue(com.example.ui.overlay.isAndroid16FrostedGlassSupported())

        // Verify RenderEffect chain builds cleanly for both dark and light themes at valid blur radii
        val darkEffect = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 72f,
            isDark = true,
            blurRadiusDp = 24f,
            transparency = 0.40f
        )
        val lightEffect = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 48f,
            isDark = false,
            blurRadiusDp = 16f,
            transparency = 0.65f
        )
        assertTrue(darkEffect != null)
        assertTrue(lightEffect != null)

        // Sub-pixel / zero blur produces no RenderEffect
        assertEquals(
            null,
            com.example.ui.overlay.createFrostedBackdropRenderEffect(
                blurRadiusPx = 0f,
                isDark = true,
                blurRadiusDp = 0f
            )
        )

        // Verify FrostedPreviewBackdrop + BlurredContainerCard render crisply using Android 16 pipeline
        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setContainerBlurRadiusDp(context, 36f)
        PanelPreferences.setContainerBlurTransparency(context, 0.55f)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FrostedPreviewBackdrop(
                        enableBlur = true,
                        dimAlpha = 0.30f,
                        blurRadiusDp = 36f,
                        transparency = 0.55f,
                        modifier = Modifier.matchParentSize()
                    )
                    BlurredContainerCard(
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("api36_frosted_card")
                    ) {
                        Text(
                            text = "Android 16 Frosted Card",
                            modifier = Modifier
                                .padding(16.dp)
                                .testTag("api36_crisp_label")
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_preview_backdrop_layer", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("api36_frosted_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("api36_crisp_label", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    @Config(sdk = [35])
    fun `Android 15 fallback disables Android 16 AGSL check while preserving RenderEffect blur`() {
        assertFalse(com.example.ui.overlay.isAndroid16FrostedGlassSupported())

        val api35Effect = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 48f,
            isDark = true,
            blurRadiusDp = 16f,
            transparency = 0.40f
        )
        assertTrue(api35Effect != null)
    }

    @Test
    fun `monetSurfaceTransparency and glassRefraction persist and sync with ThemeManager`() {
        assertEquals(0.0f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertTrue(PanelPreferences.enableGlassRefraction.value)
        assertEquals(0.5f, PanelPreferences.glassRefractionSensitivity.value, 0.01f)

        PanelPreferences.setShowMonetSurfaceBackground(context, true)
        PanelPreferences.setMonetSurfaceTransparency(context, 0.45f)
        PanelPreferences.setEnableGlassRefraction(context, true)
        PanelPreferences.setGlassRefractionSensitivity(context, 0.85f)

        // Reload preferences cleanly
        PanelPreferences.init(context)
        assertTrue(PanelPreferences.showMonetSurfaceBackground.value)
        assertEquals(0.45f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertTrue(PanelPreferences.enableGlassRefraction.value)
        assertEquals(0.85f, PanelPreferences.glassRefractionSensitivity.value, 0.01f)

        // Save theme snapshot and restore
        val savedTheme = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Refraction Test")
        PanelPreferences.setMonetSurfaceTransparency(context, 0.10f)
        PanelPreferences.setEnableGlassRefraction(context, false)
        PanelPreferences.setGlassRefractionSensitivity(context, 0.20f)

        assertTrue(com.example.data.ThemeManager.applyTheme(context, savedTheme))
        assertEquals(0.45f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertTrue(PanelPreferences.enableGlassRefraction.value)
        assertEquals(0.85f, PanelPreferences.glassRefractionSensitivity.value, 0.01f)
    }

    @Test
    fun `Solid Monet Surface transparency slider displays and updates value when toggled`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    com.example.ui.customizer.QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // Initially hidden
        assertEquals(
            0,
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasTestTag("monet_surface_transparency_slider"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().size
        )

        // Toggle Solid Monet Surface on
        composeTestRule.onNodeWithTag("monet_surface_background_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(PanelPreferences.showMonetSurfaceBackground.value)

        // Slider and value readout appear
        composeTestRule.onNodeWithTag("monet_surface_transparency_value", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("monet_surface_transparency_slider")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `Glass Edge Refraction toggle and sensitivity slider display and update in QsDesignSection`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    com.example.ui.customizer.QsDesignSection()
                }
            }
        }
        composeTestRule.waitForIdle()

        // Toggle frosted glass blur on so the frosted card options expand
        composeTestRule.onNodeWithTag("frosted_glass_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Glass edge refraction toggle is visible
        composeTestRule.onNodeWithTag("glass_refraction_toggle")
            .performScrollTo()
            .assertIsDisplayed()

        // Sensitivity slider is visible when enabled
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_value", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
