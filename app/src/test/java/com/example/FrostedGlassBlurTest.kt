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

        composeTestRule.onNodeWithTag("notification_card_blur_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableNotificationBlur.value)
        assertFalse(PanelPreferences.enableContainerBlur.value)

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
    @Config(sdk = [32])
    fun `RuntimeShader refraction cleanly reports unsupported below API 33`() {
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
}
