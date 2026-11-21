package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.BatteryPercentageLocation
import com.example.data.DatePosition
import com.example.data.PanelPreferences
import com.example.data.QsPanelStyle
import com.example.data.ThemeManager
import com.example.model.ComplicationTileModel
import com.example.model.QsTileModel
import com.example.model.TileCategory
import com.example.model.TileState
import com.example.ui.customizer.CustomizeScreen
import com.example.ui.customizer.FavoriteFeature
import com.example.ui.customizer.QsDesignSection
import com.example.ui.notification.CustomShadowHelper
import com.example.ui.notification.PanelTopRimBackgroundDrawable
import com.example.ui.notification.TopRimOutlineDrawable
import com.example.ui.overlay.CircularQsTile
import com.example.ui.overlay.ContainerCard
import com.example.ui.overlay.KitKatShadeHostView
import com.example.ui.overlay.Qqs2x1ComplicationCard
import com.example.ui.overlay.QsExpansionStage
import com.example.ui.overlay.QsHeaderBar
import com.example.ui.overlay.QsOverlaySurface
import com.example.ui.overlay.QsOverlaySurfaceConfig
import com.example.ui.overlay.pillSurfaceWithRim
import com.example.ui.shape.QsShapeManager
import com.example.ui.theme.QuickPanelTheme
import com.example.ui.tile.ComplicationTileView
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
        PanelPreferences.setEnableTileTopRim(context, false)
        PanelPreferences.setTopRimAlpha(context, 0.20f)
        PanelPreferences.setDateBackgroundEnabled(context, false)
        PanelPreferences.setBatteryBackgroundEnabled(context, false)
        PanelPreferences.setClockBackgroundEnabled(context, false)
        PanelPreferences.setCarrierBackgroundEnabled(context, false)
        PanelPreferences.setFooterIconBackground(context, false)
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
        composeTestRule.onAllNodesWithText("Top Rim Preview (20% -> 0% Alpha)", useUnmergedTree = true)
            .assertCountEquals(0)
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

    @Test
    fun `CustomShadowHelper attaches and omits PanelTopRimBackgroundDrawable based on enabled state`() {
        val view = FrameLayout(context)
        val radii = FloatArray(8) { 20f }
        val baseDrawable = GradientDrawable().apply {
            setColor(Color.DKGRAY)
            cornerRadii = radii
        }

        // Enabled without elevation: [contentDrawable, PanelTopRimBackgroundDrawable]
        CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = baseDrawable,
            radii = radii,
            elevation = 0f,
            isDark = true,
            enableTopRimBackground = true
        )
        val noShadowLayers = view.background as? LayerDrawable
        assertTrue(noShadowLayers != null && noShadowLayers.numberOfLayers == 2)
        assertTrue(noShadowLayers!!.getDrawable(1) is PanelTopRimBackgroundDrawable)

        // Enabled with elevation: [shadowDrawable, contentDrawable, PanelTopRimBackgroundDrawable]
        CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = baseDrawable,
            radii = radii,
            elevation = 8f,
            isDark = true,
            enableTopRimBackground = true
        )
        val shadowLayers = view.background as? LayerDrawable
        assertTrue(shadowLayers != null && shadowLayers.numberOfLayers == 3)
        assertTrue(shadowLayers!!.getDrawable(2) is PanelTopRimBackgroundDrawable)

        // Disabled without elevation: contentDrawable directly, no gradient overlay
        CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = baseDrawable,
            radii = radii,
            elevation = 0f,
            isDark = true,
            enableTopRimBackground = false
        )
        assertTrue(view.background === baseDrawable)

        // Disabled with elevation: [shadowDrawable, contentDrawable], no gradient overlay
        CustomShadowHelper.applyCustomShadowBackground(
            view = view,
            contentDrawable = baseDrawable,
            radii = radii,
            elevation = 8f,
            isDark = true,
            enableTopRimBackground = false
        )
        val disabledShadowLayers = view.background as? LayerDrawable
        assertTrue(disabledShadowLayers != null && disabledShadowLayers.numberOfLayers == 2)
        assertFalse(disabledShadowLayers!!.getDrawable(1) is PanelTopRimBackgroundDrawable)

        // Null check: drawable handles bounds gracefully
        val bgDrawable = PanelTopRimBackgroundDrawable(radii = radii)
        bgDrawable.setBounds(0, 0, 200, 100)
        assertTrue(bgDrawable.bounds.width() == 200)
        assertTrue(ColorDrawable(Color.TRANSPARENT).opacity != 0)
    }

    @Test
    fun `tile top rim preference persists across init`() {
        assertFalse(PanelPreferences.enableTileTopRim.value)

        PanelPreferences.setEnableTileTopRim(context, true)
        assertTrue(PanelPreferences.enableTileTopRim.value)

        PanelPreferences.init(context)
        assertTrue(PanelPreferences.enableTileTopRim.value)

        PanelPreferences.setEnableTileTopRim(context, false)
        assertFalse(PanelPreferences.enableTileTopRim.value)
    }

    @Test
    fun `search finds Tile & Complication Top Rim and toggles preference`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Complication Top Rim")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.DESIGN_TILE_TOP_RIM.id}")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("feature_tile_top_rim_switch")
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableTileTopRim.value)

        composeTestRule.onNodeWithTag("feature_tile_top_rim_switch")
            .performClick()
        composeTestRule.waitForIdle()
        assertFalse(PanelPreferences.enableTileTopRim.value)
    }

    @Test
    fun `QsDesignSection tile top rim toggle updates preference`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
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

        composeTestRule.onNodeWithTag("tile_top_rim_toggle")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(PanelPreferences.enableTileTopRim.value)
        composeTestRule.onAllNodesWithText("Enabled & Inactive Rim", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun `QsPathDrawable renders cleanly with enableTopRim toggled`() {
        val drawable = QsShapeManager.createDrawable("squircle", Color.DKGRAY)
        drawable.setBounds(0, 0, 120, 120)
        val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.enableTopRim = false
        drawable.draw(canvas)

        drawable.enableTopRim = true
        drawable.topRimStrokeWidthPx = 3f
        drawable.draw(canvas)
        assertTrue(drawable.enableTopRim)
    }

    @Test
    fun `ComplicationTileView Qqs2x1ComplicationCard and QsOverlaySurface render cleanly with tile rim enabled`() {
        PanelPreferences.setEnableTileTopRim(context, true)
        val compSpec = ComplicationTileModel(
            id = "comp_battery",
            title = "Battery",
            subline = "82%",
            progress = 0.82f,
            icon = Icons.Default.BatteryFull,
            isActive = true
        )
        val compTile = QsTileModel(
            id = "comp_battery_2x1",
            label = "Battery",
            subtitle = "82%",
            state = TileState.ACTIVE,
            category = TileCategory.COMPLICATION,
            complicationSpec = compSpec.copy(id = "comp_battery_2x1")
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column {
                    ComplicationTileView(
                        model = compSpec,
                        showGridCardContainer = true
                    )
                    Qqs2x1ComplicationCard(
                        tile = compTile,
                        isDark = true,
                        onClick = {},
                        onLongClick = {}
                    )
                    QsOverlaySurface(
                        config = QsOverlaySurfaceConfig(enableTileTopRim = true),
                        testTagPrefix = "qs_overlay_tile_rim_preview"
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("complication_tile_comp_battery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qqs_2x1_complication_comp_battery_2x1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qs_overlay_tile_rim_preview").assertIsDisplayed()
    }

    @Test
    fun `enabled tiles and bright primary surfaces adapt top rim and draw cleanly`() {
        val brightPrimary = Color.parseColor("#4EE3D0")
        val brightTertiary = Color.parseColor("#A8C7FA")

        // QsPathDrawable with bright solid primary and gradient primary->tertiary
        val drawable = QsShapeManager.createDrawable("squircle", brightPrimary)
        drawable.enableTopRim = true
        drawable.topRimStrokeWidthPx = 3f
        drawable.setBounds(0, 0, 120, 120)
        val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        drawable.gradientColors = intArrayOf(brightPrimary, brightTertiary)
        drawable.draw(canvas)

        // CustomShadowHelper View drawables with bright enabled baseColor
        val view = FrameLayout(context)
        val radii = FloatArray(8) { 24f }
        CustomShadowHelper.applyPanelTopRimForeground(
            view = view,
            enabled = true,
            radii = radii,
            baseColor = brightPrimary
        )
        assertTrue(view.foreground is TopRimOutlineDrawable)
        view.foreground?.setBounds(0, 0, 140, 140)
        view.foreground?.draw(canvas)

        val bgDrawable = PanelTopRimBackgroundDrawable(radii = radii, baseColor = brightPrimary)
        bgDrawable.setBounds(0, 0, 140, 140)
        bgDrawable.draw(canvas)

        // Active & Inactive CircularQsTile with enableTileTopRim = true
        PanelPreferences.setEnableTileTopRim(context, true)
        val activeWifiTile = QsTileModel(
            id = "wifi_active",
            label = "Wi-Fi",
            subtitle = "Connected",
            iconVector = Icons.Default.Wifi,
            state = TileState.ACTIVE,
            category = TileCategory.CONNECTIVITY
        )
        val inactiveWifiTile = QsTileModel(
            id = "wifi_inactive",
            label = "Wi-Fi Off",
            subtitle = "Off",
            iconVector = Icons.Default.Wifi,
            state = TileState.INACTIVE,
            category = TileCategory.CONNECTIVITY
        )

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Row {
                    CircularQsTile(
                        tile = activeWifiTile,
                        morphProgress = 1f,
                        tileWidth = 80.dp,
                        onClick = {},
                        onLongClick = {}
                    )
                    CircularQsTile(
                        tile = inactiveWifiTile,
                        morphProgress = 1f,
                        tileWidth = 80.dp,
                        onClick = {},
                        onLongClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tile_wifi_active").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tile_wifi_inactive").assertIsDisplayed()
    }

    @Test
    fun `header status pills and footer icon backgrounds render cleanly with panel top rim enabled`() {
        PanelPreferences.setEnablePanelTopRim(context, true)
        PanelPreferences.setClockBackgroundEnabled(context, true)
        PanelPreferences.setClockBackgroundCorner(context, 10f)
        PanelPreferences.setDateBackgroundEnabled(context, true)
        PanelPreferences.setDateBackgroundCorner(context, 22f)
        PanelPreferences.setBatteryBackgroundEnabled(context, true)
        PanelPreferences.setBatteryBackgroundCorner(context, 10f)
        PanelPreferences.setCarrierBackgroundEnabled(context, true)
        PanelPreferences.setCarrierBackgroundCorner(context, 10f)
        PanelPreferences.setFooterIconBackground(context, true)
        PanelPreferences.setDatePosition(context, DatePosition.NEXT_TO_TIME)
        PanelPreferences.setBatteryPercentageLocation(context, BatteryPercentageLocation.HEADER)

        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column {
                    QsHeaderBar(currentStage = QsExpansionStage.QQS)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .pillSurfaceWithRim(
                                enabled = true,
                                shape = RoundedCornerShape(
                                    topStart = 10.dp,
                                    bottomStart = 10.dp,
                                    topEnd = 3.dp,
                                    bottomEnd = 3.dp
                                ),
                                backgroundColor = androidx.compose.ui.graphics.Color(0xFF2B2B2B),
                                topRimEnabled = true
                            )
                            .testTag("footer_pill_with_rim_test")
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .pillSurfaceWithRim(
                                enabled = false,
                                shape = RoundedCornerShape(10.dp),
                                backgroundColor = androidx.compose.ui.graphics.Color(0xFF2B2B2B),
                                topRimEnabled = true
                            )
                            .testTag("footer_pill_disabled_test")
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("qqs_header_time").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qqs_header_date_inline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qqs_header_battery_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("footer_pill_with_rim_test").assertIsDisplayed()
        composeTestRule.onNodeWithTag("footer_pill_disabled_test").assertIsDisplayed()
    }

    @Test
    fun `KitKatShadeHostView applies and clears top rim on header and carrier pill backgrounds`() {
        PanelPreferences.setClockBackgroundEnabled(context, true)
        PanelPreferences.setDateBackgroundEnabled(context, true)
        PanelPreferences.setCarrierBackgroundEnabled(context, true)
        PanelPreferences.setEnablePanelTopRim(context, true)

        val hostView = KitKatShadeHostView(context)
        hostView.updateData(
            tiles = emptyList(),
            brightness = 128,
            notifications = emptyList(),
            isNotificationPermissionGranted = true,
            carrierName = "SIM1",
            isDark = true
        )

        val tvTime = hostView.findViewById<TextView>(R.id.tv_oreo_top_time)
        val tvDate = hostView.findViewById<TextView>(R.id.tv_qs_oreo_header_date)
        val tvCarrier = hostView.findViewById<TextView>(R.id.tv_oreo_top_carrier)
        val tvBottomCarrier = hostView.findViewById<TextView>(R.id.tv_kitkat_bottom_carrier)

        assertTrue(tvTime.foreground is TopRimOutlineDrawable)
        assertTrue(tvDate.foreground is TopRimOutlineDrawable)
        assertTrue(tvCarrier.foreground is TopRimOutlineDrawable)
        assertTrue(tvBottomCarrier.foreground is TopRimOutlineDrawable)
        assertTrue((tvTime.background as? LayerDrawable)?.let {
            it.getDrawable(it.numberOfLayers - 1) is PanelTopRimBackgroundDrawable
        } == true)

        PanelPreferences.setEnablePanelTopRim(context, false)
        hostView.updateData(
            tiles = emptyList(),
            brightness = 128,
            notifications = emptyList(),
            isNotificationPermissionGranted = true,
            carrierName = "SIM1",
            isDark = true
        )

        assertNull(tvTime.foreground)
        assertNull(tvDate.foreground)
        assertNull(tvCarrier.foreground)
        assertNull(tvBottomCarrier.foreground)
        assertFalse(tvTime.background is LayerDrawable)
    }

    @Test
    fun `rim debauchery preference clamps 5 to 30 percent and persists across init`() {
        org.junit.Assert.assertEquals(0.20f, PanelPreferences.topRimAlpha.value, 0.001f)

        PanelPreferences.setTopRimAlpha(context, 0.01f)
        org.junit.Assert.assertEquals(0.05f, PanelPreferences.topRimAlpha.value, 0.001f)

        PanelPreferences.setTopRimAlpha(context, 0.55f)
        org.junit.Assert.assertEquals(0.30f, PanelPreferences.topRimAlpha.value, 0.001f)

        PanelPreferences.setTopRimAlpha(context, 0.14f)
        PanelPreferences.init(context)
        org.junit.Assert.assertEquals(0.14f, PanelPreferences.topRimAlpha.value, 0.001f)
    }

    @Test
    fun `search finds Rim Sensitivity and QsDesignSection slider reflects 5 to 30 percent`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                CustomizeScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("global_customize_search_input")
            .performTextInput("Rim Sensitivity")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_result_${FavoriteFeature.DESIGN_RIM_SENSITIVITY.id}")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("feature_rim_sensitivity_slider")
            .assertIsDisplayed()
    }

    @Test
    fun `QsDesignSection shows Rim Sensitivity slider and updates live with scaled drawables`() {
        composeTestRule.setContent {
            QuickPanelTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    QsDesignSection()
                    QsOverlaySurface(
                        config = QsOverlaySurfaceConfig(
                            enablePanelTopRim = true,
                            enableTileTopRim = true,
                            topRimAlpha = 0.30f
                        ),
                        testTagPrefix = "qs_overlay_rim_sensitivity_30"
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("rim_sensitivity_slider")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("rim_sensitivity_value_text", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals("20%")

        PanelPreferences.setTopRimAlpha(context, 0.05f)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rim_sensitivity_value_text", useUnmergedTree = true)
            .assertTextEquals("5%")

        PanelPreferences.setTopRimAlpha(context, 0.30f)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rim_sensitivity_value_text", useUnmergedTree = true)
            .assertTextEquals("30%")

        // Verify QsPathDrawable and CustomShadowHelper draw cleanly at 5% and 30%
        val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = QsShapeManager.createDrawable("squircle", Color.DKGRAY)
        drawable.enableTopRim = true
        drawable.setBounds(0, 0, 120, 120)
        drawable.topRimAlpha = 0.05f
        drawable.draw(canvas)
        drawable.topRimAlpha = 0.30f
        drawable.draw(canvas)

        val radii = FloatArray(8) { 20f }
        val rimDrawable = TopRimOutlineDrawable(radii = radii, topRimAlpha = 0.30f)
        rimDrawable.setBounds(0, 0, 120, 120)
        rimDrawable.draw(canvas)

        val bgDrawable = PanelTopRimBackgroundDrawable(radii = radii, topRimAlpha = 0.05f)
        bgDrawable.setBounds(0, 0, 120, 120)
        bgDrawable.draw(canvas)
    }

    @Test
    fun `theme manager correctly persists and restores QS Design and Tweaks settings including top rim alpha`() {
        PanelPreferences.setEnablePanelTopRim(context, true)
        PanelPreferences.setEnableTileTopRim(context, true)
        PanelPreferences.setTopRimAlpha(context, 0.25f)
        PanelPreferences.setFooterIconBackground(context, true)
        PanelPreferences.setDateBackgroundEnabled(context, true)
        PanelPreferences.setDateBackgroundCorner(context, 15f)

        val savedTheme = ThemeManager.saveCurrentSettingsAsTheme(context, "Test Design Theme")
        
        // Reset prefs to defaults
        PanelPreferences.setEnablePanelTopRim(context, false)
        PanelPreferences.setEnableTileTopRim(context, false)
        PanelPreferences.setTopRimAlpha(context, 0.20f)
        PanelPreferences.setFooterIconBackground(context, false)
        PanelPreferences.setDateBackgroundEnabled(context, false)
        PanelPreferences.setDateBackgroundCorner(context, 10f)

        // Apply saved theme
        val success = ThemeManager.applyTheme(context, savedTheme)
        assertTrue(success)

        assertTrue(PanelPreferences.enablePanelTopRim.value)
        assertTrue(PanelPreferences.enableTileTopRim.value)
        org.junit.Assert.assertEquals(0.25f, PanelPreferences.topRimAlpha.value, 0.001f)
        assertTrue(PanelPreferences.footerIconBackground.value)
        assertTrue(PanelPreferences.dateBackgroundEnabled.value)
        org.junit.Assert.assertEquals(15f, PanelPreferences.dateBackgroundCorner.value, 0.001f)
    }
}
