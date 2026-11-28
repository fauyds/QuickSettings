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
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.GlassBackdropMode
import com.example.data.PanelPreferences
import com.example.data.RefractionStyle
import com.example.data.ThemeManager
import com.example.ui.overlay.BlurredContainerCard
import com.example.ui.overlay.FrostedPreviewBackdrop
import com.example.ui.overlay.applyWindowBlurBehind
import com.example.ui.overlay.computeWindowBlurBehindRadiusPx
import com.example.ui.overlay.frostedContainerColor
import com.example.ui.overlay.reportQsCardBlurBounds
import com.example.ui.theme.QuickPanelTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

        // When glass mode is OFF, frosted_glass_qs_only_toggle is hidden
        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle").assertDoesNotExist()

        // Switch to Frosted Glass mode so frosted_glass_qs_only_toggle appears
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.blurOnlyBehindQsCard.value)
        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertEquals(GlassBackdropMode.FROSTED, PanelPreferences.glassBackdropMode.value)
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

        // Verify minimum blur radius goes all the way down to 0% (0f dp)
        PanelPreferences.setContainerBlurRadiusDp(context, 0f)
        PanelPreferences.init(context)
        assertEquals(0f, PanelPreferences.containerBlurRadiusDp.value, 0.001f)

        PanelPreferences.setContainerBlurRadiusDp(context, -5f)
        assertEquals(0f, PanelPreferences.containerBlurRadiusDp.value, 0.001f)
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

        // Enable Frosted Glass mode
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableContainerBlur.value)
        assertEquals(GlassBackdropMode.FROSTED, PanelPreferences.glassBackdropMode.value)

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

        // Setting blur radius to 0f displays 0% cleanly
        PanelPreferences.setContainerBlurRadiusDp(context, 0f)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_value", useUnmergedTree = true)
            .let { node ->
                node.fetchSemanticsNode().let { sem ->
                    val texts = sem.config[androidx.compose.ui.semantics.SemanticsProperties.Text]
                    assertEquals("0%", texts.joinToString("") { it.text })
                }
            }
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
    fun `Android 15 disables Frosted and Liquid glass support cleanly`() {
        assertFalse(com.example.ui.overlay.isAndroid16FrostedGlassSupported())
        assertFalse(com.example.ui.overlay.isRuntimeShaderRefractionSupported())
        assertFalse(PanelPreferences.isGlassBackdropSupported())

        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)
        assertEquals(GlassBackdropMode.OFF, PanelPreferences.effectiveGlassBackdropMode())
        assertFalse(PanelPreferences.isAnyGlassActive())
        assertFalse(PanelPreferences.isFrostedGlassActive())
        assertFalse(PanelPreferences.isLiquidGlassActive())
    }

    @Test
    fun `monetSurfaceTransparency and glassBackdropMode persist and sync with ThemeManager`() {
        assertEquals(0.0f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertEquals(GlassBackdropMode.OFF, PanelPreferences.glassBackdropMode.value)
        assertFalse(PanelPreferences.enableGlassRefraction.value)
        assertEquals(0.5f, PanelPreferences.glassRefractionSensitivity.value, 0.01f)

        PanelPreferences.setShowMonetSurfaceBackground(context, true)
        PanelPreferences.setMonetSurfaceTransparency(context, 0.45f)
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)
        PanelPreferences.setGlassRefractionSensitivity(context, 0.85f)

        // Reload preferences cleanly
        PanelPreferences.init(context)
        assertTrue(PanelPreferences.showMonetSurfaceBackground.value)
        assertEquals(0.45f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertEquals(GlassBackdropMode.LIQUID, PanelPreferences.glassBackdropMode.value)
        assertTrue(PanelPreferences.enableGlassRefraction.value)
        assertTrue(PanelPreferences.isLiquidGlassActive())
        assertEquals(0.85f, PanelPreferences.glassRefractionSensitivity.value, 0.01f)

        // Save theme snapshot and restore
        val savedTheme = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Refraction Test")
        PanelPreferences.setMonetSurfaceTransparency(context, 0.10f)
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.FROSTED)
        PanelPreferences.setGlassRefractionSensitivity(context, 0.20f)
        assertFalse(PanelPreferences.enableGlassRefraction.value)
        assertTrue(PanelPreferences.isFrostedGlassActive())

        assertTrue(com.example.data.ThemeManager.applyTheme(context, savedTheme))
        assertEquals(0.45f, PanelPreferences.monetSurfaceTransparency.value, 0.01f)
        assertEquals(GlassBackdropMode.LIQUID, PanelPreferences.glassBackdropMode.value)
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
    fun `Liquid Glass mode displays Refraction Sensitivity slider while Frosted mode keeps it hidden`() {
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

        // Switch to Frosted Glass mode -> blur sliders appear, refraction slider remains hidden
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider").assertDoesNotExist()

        // Switch to Liquid Glass mode -> refraction sensitivity slider & notification card blur appear
        composeTestRule.onNodeWithTag("glass_mode_liquid")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(GlassBackdropMode.LIQUID, PanelPreferences.glassBackdropMode.value)
        assertTrue(PanelPreferences.enableGlassRefraction.value)

        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_value", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `NotificationsCard container is clean and individual notifications receive frosted glass effect`() {
        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setContainerBlurTransparency(context, 0.40f)

        val sampleNotification = com.example.model.NotificationItem(
            id = 101,
            key = "test_pkg|101",
            packageName = "com.google.android.gm",
            appName = "Gmail",
            title = "New Email",
            text = "Meeting at 3 PM today",
            postTime = System.currentTimeMillis(),
            isOngoing = false,
            isClearable = true
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    com.example.ui.overlay.NotificationsCard(
                        notifications = listOf(sampleNotification),
                        isPermissionGranted = true
                    )
                    com.example.ui.overlay.NotificationItemRow(
                        item = sampleNotification,
                        onClick = {},
                        onDismiss = {},
                        onActionClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Root container is present and clean
        composeTestRule.onNodeWithTag("notifications_card").assertIsDisplayed()

        // Verify individual Compose NotificationItemRow renders crisply with frosted glass container
        composeTestRule.onNodeWithTag("notification_item_test_pkg|101").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meeting at 3 PM today").assertIsDisplayed()
    }

    @Test
    fun `FrostedGlassSurfaceDrawable uses Android 16 BAKLAVA RuntimeColorFilter and draws cleanly`() {
        val colorFilter = com.example.ui.overlay.createAndroid16FrostedSurfaceColorFilter(
            isDark = true,
            transparency = 0.45f
        )
        val xfermode = com.example.ui.overlay.createAndroid16FrostedSurfaceXfermode(
            isDark = true,
            transparency = 0.45f
        )
        assertTrue(colorFilter != null)
        assertTrue(xfermode != null)

        val drawable = com.example.ui.notification.FrostedGlassSurfaceDrawable(
            cornerRadius = 24f,
            isDark = true,
            transparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.7f,
            density = 3f
        )
        assertTrue(drawable.usesAndroid16Api)

        val view = android.view.View(context)
        val content = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = 24f
            setColor(android.graphics.Color.argb(140, 30, 36, 41))
        }
        com.example.ui.notification.CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = content,
            cornerRadius = 24f,
            elevation = 8f,
            isDark = true,
            enableFrostedGlass = true,
            frostedTransparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.7f
        )

        val layerBg = view.background as android.graphics.drawable.LayerDrawable
        val hasFrostedLayer = (0 until layerBg.numberOfLayers).any { idx ->
            val layer = layerBg.getDrawable(idx)
            layer is com.example.ui.notification.FrostedGlassSurfaceDrawable && layer.usesAndroid16Api
        }
        assertTrue(hasFrostedLayer)

        val bmp = android.graphics.Bitmap.createBitmap(200, 120, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        layerBg.setBounds(0, 0, 200, 120)
        layerBg.draw(canvas)
        bmp.recycle()
    }

    @Test
    @Config(sdk = [35])
    fun `FrostedGlassSurfaceDrawable falls back gracefully below Android 16`() {
        val drawable = com.example.ui.notification.FrostedGlassSurfaceDrawable(
            cornerRadius = 24f,
            isDark = false,
            transparency = 0.40f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.5f,
            density = 3f
        )
        assertFalse(drawable.usesAndroid16Api)

        val bmp = android.graphics.Bitmap.createBitmap(160, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, 160, 100)
        drawable.draw(canvas)
        bmp.recycle()
    }

    @Test
    fun `NotificationAdapter attaches Android 16 FrostedGlassSurfaceDrawable to notification cards only when blur is enabled`() {
        val themedContext = android.view.ContextThemeWrapper(
            context,
            com.example.R.style.Theme_MyApplication
        )
        val parent = android.widget.FrameLayout(themedContext)
        val adapter = com.example.ui.notification.NotificationAdapter(
            onNotificationClick = {},
            onNotificationDismiss = {},
            onActionClick = {}
        ).apply {
            isDark = true
        }

        val item = com.example.model.NotificationItem(
            id = 202,
            key = "notif_pkg|202",
            packageName = "com.example.chat",
            appName = "Messages",
            title = "Alex",
            text = "Are we still on for lunch?",
            postTime = System.currentTimeMillis(),
            isOngoing = false,
            isClearable = true
        )

        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setContainerBlurTransparency(context, 0.45f)
        PanelPreferences.setEnableGlassRefraction(context, true)
        PanelPreferences.setGlassRefractionSensitivity(context, 0.65f)
        adapter.submitNotifications(listOf(item))

        val vh = adapter.onCreateViewHolder(parent, adapter.getItemViewType(0))
        adapter.onBindViewHolder(vh, 0)

        val rootCard = vh.itemView.findViewById<android.view.View>(R.id.fl_notif_root) ?: vh.itemView
        val bgWhenEnabled = rootCard.background as android.graphics.drawable.LayerDrawable
        assertTrue(
            (0 until bgWhenEnabled.numberOfLayers).any { idx ->
                val layer = bgWhenEnabled.getDrawable(idx)
                layer is com.example.ui.notification.FrostedGlassSurfaceDrawable && layer.usesAndroid16Api
            }
        )

        PanelPreferences.setEnableContainerBlur(context, false)
        adapter.submitNotifications(listOf(item), forceVisualRefresh = true)
        adapter.onBindViewHolder(vh, 0)
        val bgWhenDisabled = rootCard.background
        val hasFrostedWhenDisabled = bgWhenDisabled is android.graphics.drawable.LayerDrawable &&
            (0 until bgWhenDisabled.numberOfLayers).any { idx ->
                bgWhenDisabled.getDrawable(idx) is com.example.ui.notification.FrostedGlassSurfaceDrawable
            }
        assertFalse(hasFrostedWhenDisabled)
    }

    @Test
    fun `QQS carrier footer and QS card report independent blur regions to QsMultiCardBlurWindowHost and FrostedPreviewBackdrop`() {
        val reportedRegions = androidx.compose.runtime.mutableStateMapOf<String, com.example.ui.overlay.QsCardBlurBounds>()
        val carrierVisible = androidx.compose.runtime.mutableStateOf(true)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.ui.overlay.LocalOnBlurRegionBoundsChanged provides { key, bounds ->
                        if (bounds.isValid) {
                            reportedRegions[key] = bounds
                        } else {
                            reportedRegions.remove(key)
                        }
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FrostedPreviewBackdrop(
                            enableBlur = true,
                            dimAlpha = 0.28f,
                            blurOnlyBehindQsCard = true,
                            qsCardBounds = reportedRegions["qs_card"],
                            extraBlurRegions = listOfNotNull(reportedRegions["qqs_carrier_footer"]),
                            modifier = Modifier.matchParentSize()
                        )
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .reportQsCardBlurBounds(
                                        topCornerRadiusDp = 24f,
                                        bottomCornerRadiusDp = 8f,
                                        regionKey = "qs_card"
                                    )
                            ) {
                                Text("QQS Main Card")
                            }
                            Box(
                                modifier = Modifier.reportQsCardBlurBounds(
                                    topCornerRadiusDp = 8f,
                                    bottomCornerRadiusDp = 24f,
                                    regionKey = "qqs_carrier_footer",
                                    isVisible = carrierVisible.value
                                )
                            ) {
                                Text("Carrier Footer")
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        assertTrue(reportedRegions["qs_card"]?.isValid == true)
        assertTrue(reportedRegions["qqs_carrier_footer"]?.isValid == true)
        composeTestRule.onNodeWithTag("frosted_preview_qs_blur_region", useUnmergedTree = true).assertIsDisplayed()

        // Multi-card window host accepts both regions cleanly
        val multiHost = com.example.ui.overlay.QsMultiCardBlurWindowHost(
            context = context,
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        )
        multiHost.showBehindOverlay()
        multiHost.updateAll(reportedRegions, radiusPx = 72)
        multiHost.updateAll(reportedRegions, radiusPx = 0)
        multiHost.dismiss()

        // Hiding carrier footer clears only qqs_carrier_footer region
        carrierVisible.value = false
        composeTestRule.waitForIdle()
        assertTrue(reportedRegions["qs_card"]?.isValid == true)
        assertFalse(reportedRegions.containsKey("qqs_carrier_footer"))
    }

    @Test
    fun `enableNotificationBlur persists syncs with ThemeManager and toggles in QsDesignSection`() {
        assertTrue(PanelPreferences.enableNotificationBlur.value)

        PanelPreferences.setEnableNotificationBlur(context, false)
        PanelPreferences.init(context)
        assertFalse(PanelPreferences.enableNotificationBlur.value)

        val savedTheme = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Notif Blur Off")
        PanelPreferences.setEnableNotificationBlur(context, true)
        assertTrue(PanelPreferences.enableNotificationBlur.value)

        assertTrue(com.example.data.ThemeManager.applyTheme(context, savedTheme))
        assertFalse(PanelPreferences.enableNotificationBlur.value)

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

        assertFalse(PanelPreferences.enableNotificationBlur.value)

        // When glass mode is OFF, notification_card_blur_toggle is hidden
        composeTestRule.onNodeWithTag("notification_card_blur_toggle").assertDoesNotExist()

        // Enable Frosted Glass mode and QS-only blur to display notification blur toggle
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableNotificationBlur.value)
        assertTrue(PanelPreferences.enableContainerBlur.value)

        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.enableNotificationBlur.value)
    }

    @Test
    fun `NotificationAdapter disables FrostedGlassSurfaceDrawable when enableNotificationBlur is false`() {
        val themedContext = android.view.ContextThemeWrapper(
            context,
            com.example.R.style.Theme_MyApplication
        )
        val parent = android.widget.FrameLayout(themedContext)
        val adapter = com.example.ui.notification.NotificationAdapter(
            onNotificationClick = {},
            onNotificationDismiss = {},
            onActionClick = {}
        ).apply {
            isDark = true
        }

        val item = com.example.model.NotificationItem(
            id = 303,
            key = "notif_pkg|303",
            packageName = "com.example.mail",
            appName = "Mail",
            title = "Invoice",
            text = "Monthly summary attached",
            postTime = System.currentTimeMillis(),
            isOngoing = false,
            isClearable = true
        )

        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setEnableNotificationBlur(context, false)
        adapter.submitNotifications(listOf(item))

        val vh = adapter.onCreateViewHolder(parent, adapter.getItemViewType(0))
        adapter.onBindViewHolder(vh, 0)

        val rootCard = vh.itemView.findViewById<android.view.View>(R.id.fl_notif_root) ?: vh.itemView
        val bgWhileDisabled = rootCard.background
        val hasFrostedWhileDisabled = bgWhileDisabled is android.graphics.drawable.LayerDrawable &&
            (0 until bgWhileDisabled.numberOfLayers).any { idx ->
                bgWhileDisabled.getDrawable(idx) is com.example.ui.notification.FrostedGlassSurfaceDrawable
            }
        assertFalse(hasFrostedWhileDisabled)

        PanelPreferences.setEnableNotificationBlur(context, true)
        adapter.submitNotifications(listOf(item), forceVisualRefresh = true)
        adapter.onBindViewHolder(vh, 0)

        val bgWhileEnabled = rootCard.background as android.graphics.drawable.LayerDrawable
        assertTrue(
            (0 until bgWhileEnabled.numberOfLayers).any { idx ->
                val layer = bgWhileEnabled.getDrawable(idx)
                layer is com.example.ui.notification.FrostedGlassSurfaceDrawable && layer.usesAndroid16Api
            }
        )
    }

    @Test
    fun `QsMultiCardBlurWindowHost and NotificationListView manage per-card notification blur bounds cleanly`() {
        val card1 = com.example.ui.overlay.QsCardBlurBounds(
            left = 32f, top = 420f, right = 1048f, bottom = 620f,
            topCornerRadiusPx = 40f, bottomCornerRadiusPx = 40f
        )
        val card2 = com.example.ui.overlay.QsCardBlurBounds(
            left = 32f, top = 640f, right = 1048f, bottom = 840f,
            topCornerRadiusPx = 40f, bottomCornerRadiusPx = 40f
        )

        val multiHost = com.example.ui.overlay.QsMultiCardBlurWindowHost(
            context = context,
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        )
        multiHost.showBehindOverlay()
        multiHost.updateAll(
            regions = mapOf(
                "qs_card" to com.example.ui.overlay.QsCardBlurBounds(24f, 80f, 1056f, 380f, 48f, 48f)
            ),
            radiusPx = 72,
            notificationRegions = listOf(card1, card2)
        )
        multiHost.updateAll(
            regions = emptyMap(),
            radiusPx = 72,
            notificationRegions = listOf(card1)
        )
        multiHost.updateAll(
            regions = emptyMap(),
            radiusPx = 0,
            notificationRegions = emptyList()
        )
        multiHost.dismiss()

        val themedContext = android.view.ContextThemeWrapper(
            context,
            com.example.R.style.Theme_MyApplication
        )
        val rootFrame = android.widget.FrameLayout(themedContext)
        val notifListView = com.example.ui.notification.NotificationListView(themedContext)
        rootFrame.addView(
            notifListView,
            android.widget.FrameLayout.LayoutParams(900, 1200)
        )
        var reportedRegions: List<com.example.ui.overlay.QsCardBlurBounds> = emptyList()
        notifListView.composeHostView = rootFrame
        notifListView.onNotificationBlurRegionsChanged = { regions ->
            reportedRegions = regions
        }

        PanelPreferences.setEnableContainerBlur(context, true)
        PanelPreferences.setEnableNotificationBlur(context, true)
        notifListView.setBlurRegionReportingEnabled(true)

        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(900, android.view.View.MeasureSpec.EXACTLY)
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(1200, android.view.View.MeasureSpec.EXACTLY)
        rootFrame.measure(widthSpec, heightSpec)
        rootFrame.layout(0, 0, 900, 1200)

        notifListView.updateData(
            notifications = listOf(
                com.example.model.NotificationItem(
                    id = 404,
                    key = "pkg|404",
                    packageName = "pkg",
                    appName = "Chat",
                    title = "Hi",
                    text = "Individual card blur test",
                    postTime = System.currentTimeMillis(),
                    isOngoing = false,
                    isClearable = true
                )
            ),
            isPermissionGranted = true,
            isDark = true,
            primaryColor = android.graphics.Color.CYAN,
            panelCardBackgroundColor = android.graphics.Color.DKGRAY
        )
        rootFrame.measure(widthSpec, heightSpec)
        rootFrame.layout(0, 0, 900, 1200)

        // Disabling reporting cleanly clears callback regions
        notifListView.setBlurRegionReportingEnabled(false)
        assertTrue(reportedRegions.isEmpty())
    }

    @Test
    fun `AGSL optical backdrop refraction and bevel RuntimeShaders compile and chain cleanly on API 33+`() {
        assertTrue(com.example.ui.overlay.isRuntimeShaderRefractionSupported())

        val qsRegion = com.example.ui.overlay.QsCardBlurBounds(
            left = 40f, top = 80f, right = 1040f, bottom = 580f,
            topCornerRadiusPx = 64f, bottomCornerRadiusPx = 24f
        )
        val notifRegion = com.example.ui.overlay.QsCardBlurBounds(
            left = 40f, top = 620f, right = 1040f, bottom = 840f,
            topCornerRadiusPx = 48f, bottomCornerRadiusPx = 48f
        )

        val backdropEffect = com.example.ui.overlay.createLiquidGlassRefractionRuntimeEffect(
            regions = listOf(qsRegion, notifRegion),
            viewportWidthPx = 1080f,
            viewportHeightPx = 1920f,
            density = 3f,
            sensitivity = 0.75f,
            isDark = true
        )
        assertNotNull(backdropEffect)

        val emptyRegionsEffect = com.example.ui.overlay.createLiquidGlassRefractionRuntimeEffect(
            regions = emptyList(),
            viewportWidthPx = 1080f,
            viewportHeightPx = 1920f,
            density = 3f,
            sensitivity = 0.75f,
            isDark = true
        )
        assertNull(emptyRegionsEffect)

        val bevelShader = com.example.ui.overlay.createFrostedGlassCardBevelShader(
            widthPx = 600f,
            heightPx = 320f,
            topRadiusPx = 64f,
            bottomRadiusPx = 24f,
            density = 3f,
            sensitivity = 0.75f,
            isDark = true
        )
        assertNotNull(bevelShader)

        val chainedWithRefraction = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 48f,
            isDark = true,
            blurRadiusDp = 24f,
            transparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.8f,
            refractionRegions = listOf(qsRegion, notifRegion),
            viewportWidthPx = 1080f,
            viewportHeightPx = 1920f,
            density = 3f
        )
        assertNotNull(chainedWithRefraction)

        val zeroBlurWithRefraction = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 0f,
            isDark = true,
            blurRadiusDp = 0f,
            transparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.8f,
            refractionRegions = listOf(qsRegion, notifRegion),
            viewportWidthPx = 1080f,
            viewportHeightPx = 1920f,
            density = 3f
        )
        assertNotNull(zeroBlurWithRefraction)

        val chainedWithoutRefraction = com.example.ui.overlay.createFrostedBackdropRenderEffect(
            blurRadiusPx = 48f,
            isDark = false,
            blurRadiusDp = 24f,
            transparency = 0.35f,
            enableGlassRefraction = false
        )
        assertNotNull(chainedWithoutRefraction)

        val drawableWithShader = com.example.ui.notification.FrostedGlassSurfaceDrawable(
            cornerRadius = 48f,
            isDark = true,
            transparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.75f,
            density = 3f
        )
        assertTrue(drawableWithShader.usesRefractionShader)

        val drawableWithoutShader = com.example.ui.notification.FrostedGlassSurfaceDrawable(
            cornerRadius = 48f,
            isDark = true,
            transparency = 0.45f,
            enableGlassRefraction = false,
            glassRefractionSensitivity = 0.75f,
            density = 3f
        )
        assertFalse(drawableWithoutShader.usesRefractionShader)

        val bmp = android.graphics.Bitmap.createBitmap(240, 140, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        drawableWithShader.setBounds(0, 0, 240, 140)
        drawableWithShader.draw(canvas)
        bmp.recycle()
    }

    @Test
    @Config(sdk = [35])
    fun `RuntimeShader refraction cleanly reports unsupported at and below API 35`() {
        assertFalse(com.example.ui.overlay.isRuntimeShaderRefractionSupported())
        val drawable = com.example.ui.notification.FrostedGlassSurfaceDrawable(
            cornerRadius = 32f,
            isDark = true,
            transparency = 0.45f,
            enableGlassRefraction = true,
            glassRefractionSensitivity = 0.65f,
            density = 2.625f
        )
        assertFalse(drawable.usesRefractionShader)
    }

    @Test
    fun `LiveScreenRefractionBackdrop renders when captured bitmap and active regions are present`() {
        val sampleScreen = android.graphics.Bitmap.createBitmap(400, 800, android.graphics.Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        val enableRefractionState = androidx.compose.runtime.mutableStateOf(true)
        val bitmapState = androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(sampleScreen)

        val qsBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 20f, top = 40f, right = 380f, bottom = 300f,
            topCornerRadiusPx = 48f, bottomCornerRadiusPx = 16f
        )
        val notifBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 20f, top = 340f, right = 380f, bottom = 500f,
            topCornerRadiusPx = 32f, bottomCornerRadiusPx = 32f
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    com.example.ui.overlay.LiveScreenRefractionBackdrop(
                        screenBitmap = bitmapState.value,
                        enableBlur = true,
                        enableRefraction = enableRefractionState.value,
                        refractionSensitivity = 0.7f,
                        blurRadiusDp = 24f,
                        transparency = 0.45f,
                        isDark = true,
                        revealAlpha = 1f,
                        qsCardBounds = qsBounds,
                        extraBlurRegions = listOf(notifBounds),
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("live_screen_refraction_backdrop").assertIsDisplayed()

        // Toggling refraction off removes the live refraction backdrop node cleanly
        enableRefractionState.value = false
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("live_screen_refraction_backdrop").assertDoesNotExist()

        enableRefractionState.value = true
        bitmapState.value = null
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("live_screen_refraction_backdrop").assertDoesNotExist()

        sampleScreen.recycle()
    }

    @Test
    fun `FrostedPreviewBackdrop accepts real refraction sensitivity and multi-region bounds`() {
        val qsBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 24f, top = 48f, right = 388f, bottom = 320f,
            topCornerRadiusPx = 48f, bottomCornerRadiusPx = 16f
        )
        val carrierBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 24f, top = 330f, right = 388f, bottom = 400f,
            topCornerRadiusPx = 16f, bottomCornerRadiusPx = 48f
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FrostedPreviewBackdrop(
                        enableBlur = true,
                        dimAlpha = 0.30f,
                        blurOnlyBehindQsCard = false,
                        qsCardBounds = qsBounds,
                        extraBlurRegions = listOf(carrierBounds),
                        enableGlassRefraction = true,
                        glassRefractionSensitivity = 0.85f,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_preview_backdrop_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_preview_backdrop_layer").assertIsDisplayed()
    }

    @Test
    fun `Frosted and Liquid Glass segmented selector reveals mode-specific controls cleanly`() {
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.OFF)
        PanelPreferences.setBlurOnlyBehindQsCard(context, false)
        PanelPreferences.setEnableNotificationBlur(context, false)

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

        // 1. When Glass Backdrop Effect is OFF, all sub-controls MUST BE HIDDEN
        composeTestRule.onNodeWithTag("glass_mode_off").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_mode_frosted").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_mode_liquid").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("notification_card_blur_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider").assertDoesNotExist()

        // 2. Select Frosted Glass mode -> Blur Behind QS/QQS Only appears; Notification & Refraction stay hidden
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("notification_card_blur_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider").assertDoesNotExist()

        // 3. Enable Blur Behind QS/QQS Only in Frosted mode -> Notification Card Blur appears (Refraction stays hidden)
        composeTestRule.onNodeWithTag("frosted_glass_qs_only_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider").assertDoesNotExist()

        // 4. Select Liquid Glass mode -> Refraction Sensitivity slider appears cleanly
        composeTestRule.onNodeWithTag("glass_mode_liquid")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .assertIsDisplayed()

        // 5. Select Off -> everything collapses back cleanly
        composeTestRule.onNodeWithTag("glass_mode_off")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.enableContainerBlur.value)
        assertFalse(PanelPreferences.enableGlassRefraction.value)
    }

    @Test
    @Config(sdk = [35])
    fun `Android 15 and below completely hide Glass Backdrop card, search entry, and disable custom shadow glass`() {
        // Even if previously pinned or enabled in prefs, API 35 hides and disables Glass Backdrop
        if (!com.example.ui.customizer.PinnedCardsManager.isCardPinned("frosted_glass_panel_card")) {
            com.example.ui.customizer.PinnedCardsManager.togglePin(
                "frosted_glass_panel_card",
                com.example.ui.customizer.CustomizeCategory.QS_DESIGN,
                context
            )
        }
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)

        assertTrue(com.example.ui.customizer.HiddenFeatureCatalog.isCardHidden("frosted_glass_panel_card", emptyList()))
        assertTrue(
            com.example.ui.customizer.HiddenFeatureCatalog.isSearchFeatureHidden(
                com.example.ui.customizer.FavoriteFeature.DESIGN_FROSTED_GLASS,
                emptyList()
            )
        )
        assertFalse(PanelPreferences.isAnyGlassActive())

        val view = android.view.View(context)
        val content = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = 24f
            setColor(android.graphics.Color.DKGRAY)
        }
        com.example.ui.notification.CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = content,
            cornerRadius = 24f,
            elevation = 8f,
            isDark = true,
            enableFrostedGlass = true,
            enableGlassRefraction = true
        )
        val hasFrostedLayer = (view.background as? android.graphics.drawable.LayerDrawable)?.let { layers ->
            (0 until layers.numberOfLayers).any { idx ->
                layers.getDrawable(idx) is com.example.ui.notification.FrostedGlassSurfaceDrawable
            }
        } ?: false
        assertFalse(hasFrostedLayer)

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

        composeTestRule.onNodeWithTag("frosted_glass_panel_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_mode_off").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_mode_frosted").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_mode_liquid").assertDoesNotExist()
    }

    @Test
    fun `BlurredContainerCard matches asymmetric panel corner shape in Liquid and Frosted modes`() {
        val asymmetricShape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 8.dp,
            bottomEnd = 8.dp
        )

        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                BlurredContainerCard(
                    shape = asymmetricShape,
                    modifier = Modifier
                        .padding(12.dp)
                        .testTag("matched_corner_glass_card")
                ) {
                    Text(
                        text = "Matched Glass Card",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("matched_corner_glass_card").assertIsDisplayed()

        // Switching to Frosted mode disables Liquid refraction while preserving Frosted blur
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.FROSTED)
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.isFrostedGlassActive())
        assertFalse(PanelPreferences.isLiquidGlassActive())
    }

    @Test
    fun `isSystemUiShadeWindow detects expanded SystemUI shade while ignoring thin status bar and volume dialogs`() {
        val density = 3f
        val screenH = 2400

        // Thin status bar at top: NOT an expanded shade
        assertFalse(
            com.example.service.QuickPanelAccessibilityService.isSystemUiShadeWindow(
                packageName = "com.android.systemui",
                title = "StatusBar",
                windowType = android.view.accessibility.AccessibilityWindowInfo.TYPE_SYSTEM,
                boundsHeightPx = 120,
                screenHeightPx = screenH,
                density = density
            )
        )

        // Volume dialog: ignored
        assertFalse(
            com.example.service.QuickPanelAccessibilityService.isSystemUiShadeWindow(
                packageName = "com.android.systemui",
                title = "VolumeDialog",
                windowType = android.view.accessibility.AccessibilityWindowInfo.TYPE_SYSTEM,
                boundsHeightPx = 900,
                screenHeightPx = screenH,
                density = density
            )
        )

        // Expanded NotificationShade: detected cleanly
        assertTrue(
            com.example.service.QuickPanelAccessibilityService.isSystemUiShadeWindow(
                packageName = "com.android.systemui",
                title = "NotificationShade",
                windowType = android.view.accessibility.AccessibilityWindowInfo.TYPE_SYSTEM,
                boundsHeightPx = 2400,
                screenHeightPx = screenH,
                density = density
            )
        )

        // Own overlay window: never treated as SystemUI stock shade
        assertFalse(
            com.example.service.QuickPanelAccessibilityService.isSystemUiShadeWindow(
                packageName = "com.android.systemui",
                title = "NotificationShade",
                windowType = android.view.accessibility.AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY,
                boundsHeightPx = 2400,
                screenHeightPx = screenH,
                density = density
            )
        )
    }

    @Test
    fun `glassPerformanceMode defaults false, persists, and syncs with ThemeManager`() {
        assertFalse(PanelPreferences.glassPerformanceMode.value)

        PanelPreferences.setGlassPerformanceMode(context, true)
        assertTrue(PanelPreferences.glassPerformanceMode.value)

        PanelPreferences.init(context)
        assertTrue(PanelPreferences.glassPerformanceMode.value)

        val savedTheme = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Perf Mode On")
        PanelPreferences.setGlassPerformanceMode(context, false)
        assertFalse(PanelPreferences.glassPerformanceMode.value)

        assertTrue(com.example.data.ThemeManager.applyTheme(context, savedTheme))
        assertTrue(PanelPreferences.glassPerformanceMode.value)
    }

    @Test
    fun `Optimize Glass and Blur toggle follows glass mode visibility and toggles performance mode`() {
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.OFF)
        PanelPreferences.setGlassPerformanceMode(context, false)

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

        // Hidden when Glass Backdrop is Off
        composeTestRule.onNodeWithTag("glass_performance_mode_toggle").assertDoesNotExist()

        // Visible in Frosted mode and toggles preference
        composeTestRule.onNodeWithTag("glass_mode_frosted").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glass_performance_mode_toggle")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.glassPerformanceMode.value)

        // Remains visible in Liquid Glass mode and toggles back off
        composeTestRule.onNodeWithTag("glass_mode_liquid").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glass_performance_mode_toggle")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.glassPerformanceMode.value)

        // Collapses when set back to Off
        composeTestRule.onNodeWithTag("glass_mode_off").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glass_performance_mode_toggle").assertDoesNotExist()
    }

    @Test
    fun `computeWindowBlurBehindRadiusPx skips negligible blur and quantizes 8px steps in performance mode`() {
        // Negligible blur (< 0.75 dp) skipped in performance mode
        assertEquals(
            0,
            computeWindowBlurBehindRadiusPx(
                enabled = true,
                density = 3f,
                fraction = 1f,
                maxBlurDp = 0.5f,
                performanceMode = true
            )
        )
        assertEquals(
            0,
            computeWindowBlurBehindRadiusPx(
                enabled = true,
                density = 3f,
                fraction = 1f,
                maxBlurDp = 0f,
                performanceMode = true
            )
        )

        // Non-negligible blur quantizes mid-reveal steps into 8px increments
        val midPerfPx = computeWindowBlurBehindRadiusPx(
            enabled = true,
            density = 3f,
            fraction = 0.5f,
            maxBlurDp = 24f,
            performanceMode = true
        )
        assertTrue(midPerfPx in 8..72)
        assertEquals(0, midPerfPx % 8)

        // Full reveal hits exact max target radius
        assertEquals(
            72,
            computeWindowBlurBehindRadiusPx(
                enabled = true,
                density = 3f,
                fraction = 1f,
                maxBlurDp = 24f,
                performanceMode = true
            )
        )
    }

    @Test
    fun `quantizeBlurRegionsForPerformance snaps sub-pixel layout jitter cleanly`() {
        val frame1 = listOf(
            com.example.ui.overlay.QsCardBlurBounds(
                left = 20.1f, top = 40.3f, right = 380.4f, bottom = 300.2f,
                topCornerRadiusPx = 32f, bottomCornerRadiusPx = 16f
            )
        )
        val frame2 = listOf(
            com.example.ui.overlay.QsCardBlurBounds(
                left = 20.4f, top = 39.8f, right = 379.7f, bottom = 299.9f,
                topCornerRadiusPx = 32f, bottomCornerRadiusPx = 16f
            )
        )

        val q1 = com.example.ui.overlay.quantizeBlurRegionsForPerformance(frame1, stepPx = 2f)
        val q2 = com.example.ui.overlay.quantizeBlurRegionsForPerformance(frame2, stepPx = 2f)

        assertEquals(q1, q2)
        assertEquals(20f, q1.first().left, 0.001f)
        assertEquals(40f, q1.first().top, 0.001f)
        assertEquals(380f, q1.first().right, 0.001f)
        assertEquals(300f, q1.first().bottom, 0.001f)
    }

    @Test
    fun `createFrostedBackdropRenderEffect skips negligible blur while keeping liquid refraction in performance mode`() {
        val qsBounds = listOf(
            com.example.ui.overlay.QsCardBlurBounds(
                left = 24f, top = 48f, right = 388f, bottom = 320f,
                topCornerRadiusPx = 0f, bottomCornerRadiusPx = 0f
            )
        )

        // Without refraction, negligible blur (< 0.75 dp) skips blur pass completely in performance mode
        assertNull(
            com.example.ui.overlay.createFrostedBackdropRenderEffect(
                blurRadiusPx = 1.5f,
                isDark = true,
                blurRadiusDp = 0.5f,
                transparency = 0.40f,
                enableGlassRefraction = false,
                performanceMode = true
            )
        )

        // With Liquid Glass enabled at 0% / negligible blur, refraction still builds cleanly
        assertNotNull(
            com.example.ui.overlay.createFrostedBackdropRenderEffect(
                blurRadiusPx = 0f,
                isDark = true,
                blurRadiusDp = 0f,
                transparency = 0.40f,
                enableGlassRefraction = true,
                glassRefractionSensitivity = 0.65f,
                refractionRegions = qsBounds,
                viewportWidthPx = 1080f,
                viewportHeightPx = 2400f,
                density = 3f,
                performanceMode = true
            )
        )

        // With full blur + Liquid Glass in performance mode, chained RenderEffect builds cleanly
        assertNotNull(
            com.example.ui.overlay.createFrostedBackdropRenderEffect(
                blurRadiusPx = 72f,
                isDark = true,
                blurRadiusDp = 24f,
                transparency = 0.40f,
                enableGlassRefraction = true,
                glassRefractionSensitivity = 0.65f,
                refractionRegions = qsBounds,
                viewportWidthPx = 1080f,
                viewportHeightPx = 2400f,
                density = 3f,
                performanceMode = true
            )
        )
    }

    @Test
    fun `FrostedPreviewBackdrop hides center placeholder card and bottom dock icons`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FrostedPreviewBackdrop(
                        enableBlur = true,
                        dimAlpha = 0.34f,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("frosted_preview_backdrop_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("frosted_preview_backdrop_layer", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("72° Partly Sunny").assertDoesNotExist()
        composeTestRule.onNodeWithText("Underlying app & wallpaper layer").assertDoesNotExist()
    }

    @Test
    fun `All 14 refraction styles build AGSL RenderEffect and card bevel shaders cleanly`() {
        val expectedIds = listOf(
            "liquid", "grid", "ribbed", "hex", "hammer", "voronoi",
            "chevron", "wavy", "diamond", "scales", "louvre", "plasma", "octagon", "scan"
        )
        assertEquals(expectedIds, RefractionStyle.entries.map { it.id })

        val regions = listOf(
            com.example.ui.overlay.QsCardBlurBounds(
                left = 32f, top = 64f, right = 1048f, bottom = 1120f,
                topCornerRadiusPx = 48f, bottomCornerRadiusPx = 48f
            )
        )

        RefractionStyle.entries.forEach { style ->
            val effect = com.example.ui.overlay.createFrostedBackdropRenderEffect(
                blurRadiusPx = 36f,
                isDark = true,
                blurRadiusDp = 12f,
                transparency = 0.40f,
                enableGlassRefraction = true,
                glassRefractionSensitivity = 0.72f,
                refractionRegions = regions,
                viewportWidthPx = 1080f,
                viewportHeightPx = 2400f,
                density = 3f,
                performanceMode = false,
                refractionStyle = style,
                patternScale = 0.55f,
                dispersion = 0.65f
            )
            assertNotNull("Expected backdrop RenderEffect for ${style.label}", effect)

            val bevel = com.example.ui.overlay.createFrostedGlassCardBevelShader(
                widthPx = 1000f,
                heightPx = 800f,
                topRadiusPx = 48f,
                bottomRadiusPx = 48f,
                density = 3f,
                sensitivity = 0.72f,
                isDark = true,
                refractionStyle = style,
                patternScale = 0.55f,
                dispersion = 0.65f
            )
            assertNotNull("Expected bevel shader for ${style.label}", bevel)
        }
    }

    @Test
    fun `Per-style refraction sliders persist independently and sync active style`() {
        ThemeManager.init(context)
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)

        PanelPreferences.setRefractionStyle(context, RefractionStyle.GRID)
        PanelPreferences.setRefractionStyleSensitivity(context, RefractionStyle.GRID, 0.85f)
        PanelPreferences.setRefractionStyleScale(context, RefractionStyle.GRID, 0.72f)
        PanelPreferences.setRefractionStyleDispersion(context, RefractionStyle.GRID, 0.90f)

        PanelPreferences.setRefractionStyleSensitivity(context, RefractionStyle.VORONOI, 0.35f)
        PanelPreferences.setRefractionStyleScale(context, RefractionStyle.VORONOI, 0.25f)
        PanelPreferences.setRefractionStyleDispersion(context, RefractionStyle.VORONOI, 0.40f)

        // Active style remains GRID with GRID values
        assertEquals(RefractionStyle.GRID, PanelPreferences.refractionStyle.value)
        assertEquals(0.85f, PanelPreferences.glassRefractionSensitivity.value, 0.001f)
        assertEquals(0.72f, PanelPreferences.glassRefractionScale.value, 0.001f)
        assertEquals(0.90f, PanelPreferences.glassRefractionDispersion.value, 0.001f)

        // Switching to VORONOI restores VORONOI's independent slider values
        PanelPreferences.setRefractionStyle(context, RefractionStyle.VORONOI)
        assertEquals(0.35f, PanelPreferences.glassRefractionSensitivity.value, 0.001f)
        assertEquals(0.25f, PanelPreferences.glassRefractionScale.value, 0.001f)
        assertEquals(0.40f, PanelPreferences.glassRefractionDispersion.value, 0.001f)

        // Reload from SharedPreferences and verify persistence
        PanelPreferences.init(context)
        assertEquals(RefractionStyle.VORONOI, PanelPreferences.refractionStyle.value)
        assertEquals(0.35f, PanelPreferences.glassRefractionSensitivity.value, 0.001f)
        assertEquals(0.85f, PanelPreferences.refractionStyleSensitivities.value[RefractionStyle.GRID] ?: 0f, 0.001f)
        assertEquals(0.72f, PanelPreferences.refractionStyleScales.value[RefractionStyle.GRID] ?: 0f, 0.001f)
        assertEquals(0.90f, PanelPreferences.refractionStyleDispersions.value[RefractionStyle.GRID] ?: 0f, 0.001f)

        // ThemeManager export & apply preserves refraction style and per-style sliders
        val savedTheme = ThemeManager.saveCurrentSettingsAsTheme(context, "Refraction Styles Theme")
        PanelPreferences.setRefractionStyle(context, RefractionStyle.SCAN)
        PanelPreferences.setRefractionStyleSensitivity(context, RefractionStyle.GRID, 0.15f)
        assertTrue(ThemeManager.applyTheme(context, savedTheme))
        assertEquals(RefractionStyle.VORONOI, PanelPreferences.refractionStyle.value)
        assertEquals(0.85f, PanelPreferences.refractionStyleSensitivities.value[RefractionStyle.GRID] ?: 0f, 0.001f)
    }

    @Test
    fun `QsDesignSection shows all refraction style options and per-style sliders when Liquid mode is selected`() {
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)
        PanelPreferences.setRefractionStyle(context, RefractionStyle.LIQUID)

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

        // All 14 style options are present and selectable
        RefractionStyle.entries.forEach { style ->
            composeTestRule.onNodeWithTag("refraction_style_${style.id}")
                .performScrollTo()
                .assertIsDisplayed()
        }

        // Selecting Hex updates active refractionStyle
        composeTestRule.onNodeWithTag("refraction_style_hex")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(RefractionStyle.HEX, PanelPreferences.refractionStyle.value)

        // Active style's Strength, Pattern Scale, and Chromatic Split sliders are visible
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_scale_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_dispersion_slider")
            .performScrollTo()
            .assertIsDisplayed()

        // Redundant expandable "Sliders for Each Refraction Type" card is hidden
        composeTestRule.onNodeWithTag("refraction_per_style_sliders_toggle").assertDoesNotExist()
    }

    @Test
    fun `Style-specific refraction sliders and Diamond crossed cut toggle persist and update UI and AGSL shaders`() {
        ThemeManager.init(context)
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)

        val expectedTitles = mapOf(
            RefractionStyle.GRID to "Tile Curvature",
            RefractionStyle.HAMMER to "Hammer Depth",
            RefractionStyle.VORONOI to "Shard Tilt",
            RefractionStyle.CHEVRON to "Chevron Width",
            RefractionStyle.DIAMOND to "Facet Depth",
            RefractionStyle.SCALES to "Scallop Curve",
            RefractionStyle.LOUVRE to "Prism Ramp",
            RefractionStyle.PLASMA to "Molten Warp",
            RefractionStyle.SCAN to "Raster Shift"
        )
        expectedTitles.forEach { (style, title) ->
            assertEquals(title, style.customSliderTitle)
        }
        assertNull(RefractionStyle.LIQUID.customSliderTitle)
        assertNull(RefractionStyle.RIBBED.customSliderTitle)
        assertNull(RefractionStyle.HEX.customSliderTitle)
        assertNull(RefractionStyle.WAVY.customSliderTitle)
        assertNull(RefractionStyle.OCTAGON.customSliderTitle)

        // Persist independent custom params per style & Diamond crossed cut
        PanelPreferences.setRefractionStyle(context, RefractionStyle.GRID)
        PanelPreferences.setRefractionStyleCustomParam(context, RefractionStyle.GRID, 0.92f)
        PanelPreferences.setRefractionStyleCustomParam(context, RefractionStyle.VORONOI, 0.22f)
        PanelPreferences.setDiamondCrossedCut(context, true)

        assertEquals(0.92f, PanelPreferences.glassRefractionCustomParam.value, 0.001f)
        assertTrue(PanelPreferences.diamondCrossedCut.value)

        PanelPreferences.init(context)
        assertEquals(0.92f, PanelPreferences.glassRefractionCustomParam.value, 0.001f)
        assertEquals(0.22f, PanelPreferences.refractionStyleCustomParams.value[RefractionStyle.VORONOI] ?: 0f, 0.001f)
        assertTrue(PanelPreferences.diamondCrossedCut.value)

        // ThemeManager round-trips custom params & diamondCrossedCut
        val saved = ThemeManager.saveCurrentSettingsAsTheme(context, "Custom Refraction Params")
        PanelPreferences.setRefractionStyleCustomParam(context, RefractionStyle.GRID, 0.10f)
        PanelPreferences.setDiamondCrossedCut(context, false)
        assertTrue(ThemeManager.applyTheme(context, saved))
        assertEquals(0.92f, PanelPreferences.glassRefractionCustomParam.value, 0.001f)
        assertTrue(PanelPreferences.diamondCrossedCut.value)

        // AGSL shaders build cleanly across extremes including Diamond crossed = true/false
        val regions = listOf(
            com.example.ui.overlay.QsCardBlurBounds(
                left = 32f, top = 64f, right = 1048f, bottom = 1120f,
                topCornerRadiusPx = 48f, bottomCornerRadiusPx = 48f
            )
        )
        expectedTitles.keys.forEach { style ->
            listOf(0f, 0.5f, 1f).forEach { paramVal ->
                val effect = com.example.ui.overlay.createFrostedBackdropRenderEffect(
                    blurRadiusPx = 24f,
                    isDark = true,
                    blurRadiusDp = 8f,
                    transparency = 0.40f,
                    enableGlassRefraction = true,
                    glassRefractionSensitivity = 0.70f,
                    refractionRegions = regions,
                    viewportWidthPx = 1080f,
                    viewportHeightPx = 2400f,
                    density = 3f,
                    refractionStyle = style,
                    patternScale = 0.50f,
                    dispersion = 0.60f,
                    styleParam = paramVal,
                    diamondCrossed = (style == RefractionStyle.DIAMOND && paramVal > 0.4f)
                )
                assertNotNull("Expected RenderEffect for ${style.label} param=$paramVal", effect)
            }
        }

        // Compose UI verifies per-style sliders appear for target styles & toggle works for Diamond
        PanelPreferences.setRefractionStyle(context, RefractionStyle.HEX)
        PanelPreferences.setDiamondCrossedCut(context, false)
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

        // HEX has no extra custom slider or Diamond toggle
        composeTestRule.onNodeWithTag("glass_refraction_custom_param_slider").assertDoesNotExist()
        composeTestRule.onNodeWithTag("glass_refraction_diamond_crossed_toggle").assertDoesNotExist()

        // Check each of the 9 styles shows its exact custom slider title
        expectedTitles.forEach { (style, title) ->
            composeTestRule.onNodeWithTag("refraction_style_${style.id}")
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(title)
                .performScrollTo()
                .assertIsDisplayed()
            composeTestRule.onNodeWithTag("glass_refraction_custom_param_slider")
                .performScrollTo()
                .assertIsDisplayed()

            if (style == RefractionStyle.DIAMOND) {
                composeTestRule.onNodeWithTag("glass_refraction_diamond_crossed_toggle")
                    .performScrollTo()
                    .assertIsDisplayed()
                    .performClick()
                composeTestRule.waitForIdle()
                assertTrue(PanelPreferences.diamondCrossedCut.value)
            } else {
                composeTestRule.onNodeWithTag("glass_refraction_diamond_crossed_toggle").assertDoesNotExist()
            }
        }
    }

    @Test
    fun `Glass backdrop nested control cards have 4dp spacing and expand or collapse cleanly`() {
        PanelPreferences.setGlassBackdropMode(context, GlassBackdropMode.LIQUID)

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

        // Scroll to the grouped stack once, then compare unclipped bounds without scrolling between reads
        composeTestRule.onNodeWithTag("glass_controls_grouped_stack", useUnmergedTree = true).performScrollTo()
        composeTestRule.waitForIdle()

        val refractionBounds = composeTestRule.onNodeWithTag("glass_refraction_controls_card", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val blurBounds = composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_card", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val transparencyBounds = composeTestRule.onNodeWithTag("frosted_glass_transparency_card", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()

        val gap1 = (blurBounds.top - refractionBounds.bottom).value
        val gap2 = (transparencyBounds.top - blurBounds.bottom).value
        assertEquals(4f, gap1, 1f)
        assertEquals(4f, gap2, 1f)

        // Collapse & expand Refraction Controls card
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("glass_refraction_controls_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider").assertDoesNotExist()

        composeTestRule.onNodeWithTag("glass_refraction_controls_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("glass_refraction_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()

        // Collapse & expand Blur Sensitivity card
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_slider").assertDoesNotExist()

        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()

        // Collapse & expand Transparency card
        composeTestRule.onNodeWithTag("frosted_glass_transparency_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_glass_transparency_slider").assertDoesNotExist()

        composeTestRule.onNodeWithTag("frosted_glass_transparency_header")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("frosted_glass_transparency_slider")
            .performScrollTo()
            .assertIsDisplayed()

        // Switch to Frosted Glass mode -> verify 4dp spacing between Blur Sensitivity and Transparency
        composeTestRule.onNodeWithTag("glass_mode_frosted")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("glass_controls_grouped_stack", useUnmergedTree = true).performScrollTo()
        composeTestRule.waitForIdle()
        val frostedBlurBounds = composeTestRule.onNodeWithTag("frosted_glass_blur_sensitivity_card", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val frostedTransparencyBounds = composeTestRule.onNodeWithTag("frosted_glass_transparency_card", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertEquals(4f, (frostedTransparencyBounds.top - frostedBlurBounds.bottom).value, 1f)
    }

    @Test
    fun `LiveScreenRefractionBackdrop renders captured bitmap with AGSL refraction behind QS card region`() {
        val capturedBitmap = android.graphics.Bitmap.createBitmap(
            1080,
            2400,
            android.graphics.Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(android.graphics.Color.rgb(42, 96, 168))
        }
        val qsBounds = com.example.ui.overlay.QsCardBlurBounds(
            left = 24f,
            top = 80f,
            right = 1056f,
            bottom = 860f,
            topCornerRadiusPx = 48f,
            bottomCornerRadiusPx = 48f
        )

        try {
            composeTestRule.setContent {
                QuickPanelTheme(darkTheme = true) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.overlay.LiveScreenRefractionBackdrop(
                            screenBitmap = capturedBitmap,
                            enableBlur = true,
                            enableRefraction = true,
                            refractionSensitivity = 0.65f,
                            blurRadiusDp = 24f,
                            transparency = 0.40f,
                            isDark = true,
                            revealAlpha = 1f,
                            qsCardBounds = qsBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("live_screen_refraction_backdrop", useUnmergedTree = true)
                .assertIsDisplayed()
        } finally {
            capturedBitmap.recycle()
        }
    }
}
