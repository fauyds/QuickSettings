package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PrivilegeMode
import com.example.model.TileCategory
import com.example.model.TileState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.graphics.toArgb

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickPanel", appName)
    }

    @Test
    fun `verify privilege modes and tile states`() {
        assertEquals(3, PrivilegeMode.values().size)
        assertEquals(3, TileState.values().size)
        assertNotNull(TileCategory.CONNECTIVITY)
        assertNotNull(TileCategory.HARDWARE)
        assertNotNull(TileCategory.AUDIO_DISPLAY)
        assertNotNull(TileCategory.SYSTEM)
        assertNotNull(TileCategory.EXTERNAL)
    }

    @Test
    fun `verify screen recording preferences initialization and setters`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        com.example.data.PanelPreferences.setScreenRecordResolution(context, com.example.data.ScreenRecordResolution.RES_1080P)
        assertEquals(com.example.data.ScreenRecordResolution.RES_1080P, com.example.data.PanelPreferences.screenRecordResolution.value)

        com.example.data.PanelPreferences.setScreenRecordAudioSource(context, com.example.data.ScreenRecordAudioSource.INTERNAL_AND_MIC)
        assertEquals(com.example.data.ScreenRecordAudioSource.INTERNAL_AND_MIC, com.example.data.PanelPreferences.screenRecordAudioSource.value)

        com.example.data.PanelPreferences.setScreenRecordFps(context, com.example.data.ScreenRecordFps.FPS_30)
        assertEquals(com.example.data.ScreenRecordFps.FPS_30, com.example.data.PanelPreferences.screenRecordFps.value)

        com.example.data.PanelPreferences.setScreenRecordShowTouches(context, true)
        assertEquals(true, com.example.data.PanelPreferences.screenRecordShowTouches.value)

        com.example.data.PanelPreferences.setPanelReplacementDelayMs(context, 350)
        assertEquals(350, com.example.data.PanelPreferences.panelReplacementDelayMs.value)
    }

    @Test
    fun `verify app status card shapes dimensions`() {
        assertEquals(16.0f, com.example.ui.status.AppStatusCardShapes.OuterCorner.value, 0.001f)
        assertEquals(5.0f, com.example.ui.status.AppStatusCardShapes.InsideCorner.value, 0.001f)
        assertEquals(4.0f, com.example.ui.status.AppStatusCardShapes.CardSpacing.value, 0.001f)
    }

    @Test
    fun `verify progress bar style preferences and reversions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        // Switch to STRAIGHT style
        com.example.data.PanelPreferences.setProgressBarStyle(context, com.example.data.ProgressBarStyle.STRAIGHT)
        assertEquals(com.example.data.ProgressBarStyle.STRAIGHT, com.example.data.PanelPreferences.progressBarStyle.value)
        assertEquals(false, com.example.data.PanelPreferences.wavyProgressBars.value)

        // Switch to WAVY style
        com.example.data.PanelPreferences.setProgressBarStyle(context, com.example.data.ProgressBarStyle.WAVY)
        assertEquals(com.example.data.ProgressBarStyle.WAVY, com.example.data.PanelPreferences.progressBarStyle.value)
        assertEquals(true, com.example.data.PanelPreferences.wavyProgressBars.value)

        // Revert back to previous/DEFAULT style
        com.example.data.PanelPreferences.setProgressBarStyle(context, com.example.data.ProgressBarStyle.DEFAULT)
        assertEquals(com.example.data.ProgressBarStyle.DEFAULT, com.example.data.PanelPreferences.progressBarStyle.value)
        assertEquals(false, com.example.data.PanelPreferences.wavyProgressBars.value)
    }

    @Test
    fun `verify app settings theme mode toggle and panel isolation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        // Set App Settings Theme to LIGHT
        com.example.data.PanelPreferences.setThemeMode(context, com.example.data.ThemeMode.LIGHT)
        assertEquals(com.example.data.ThemeMode.LIGHT, com.example.data.PanelPreferences.themeMode.value)

        // Set App Settings Theme to DARK
        com.example.data.PanelPreferences.setThemeMode(context, com.example.data.ThemeMode.DARK)
        assertEquals(com.example.data.ThemeMode.DARK, com.example.data.PanelPreferences.themeMode.value)

        // Set App Settings Theme to SYSTEM
        com.example.data.PanelPreferences.setThemeMode(context, com.example.data.ThemeMode.SYSTEM)
        assertEquals(com.example.data.ThemeMode.SYSTEM, com.example.data.PanelPreferences.themeMode.value)
    }

    @Test
    fun `verify all built-in QS shapes have XML drawable and valid paths`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ui.shape.QsShapeManager.init(context)

        val shapes = com.example.ui.shape.QsShapeManager.allShapes.value
        assertEquals(11, shapes.size)

        // Verify key built-in shapes
        assertNotNull(shapes.find { it.id == "circle" })
        assertNotNull(shapes.find { it.id == "squircle" })
        assertNotNull(shapes.find { it.id == "square" })
        assertNotNull(shapes.find { it.id == "teardrop" })
        val pillShape = shapes.find { it.id == "pill" }
        assertNotNull(pillShape)
        assertEquals("12dp Square", pillShape!!.label)
        assertNotNull(shapes.find { it.id == "custom_1" }) // Leaf
        assertNotNull(shapes.find { it.id == "custom_3" }) // Arch
        assertNotNull(shapes.find { it.id == "custom_8" }) // Hexagon
        assertNotNull(shapes.find { it.id == "cloud" }) // Cloud
        assertNotNull(shapes.find { it.id == "octagon" }) // Octagon
        assertNotNull(shapes.find { it.id == "stamp" }) // Stamp

        // Verify removed shapes are no longer present
        org.junit.Assert.assertNull(shapes.find { it.id == "custom_2" }) // Inverted Leaf
        org.junit.Assert.assertNull(shapes.find { it.id == "custom_4" }) // Inverted Arch
        org.junit.Assert.assertNull(shapes.find { it.id == "custom_5" }) // Shield
        org.junit.Assert.assertNull(shapes.find { it.id == "custom_6" }) // Pebble
        org.junit.Assert.assertNull(shapes.find { it.id == "custom_7" }) // Flower

        // Verify each built-in shape can produce a drawable and compose shape
        shapes.forEach { shape ->
            val drawable = com.example.ui.shape.QsShapeManager.createDrawable(shape.id, android.graphics.Color.BLUE)
            assertNotNull(drawable)
            drawable.setBounds(0, 0, 100, 100)

            val composeShape = com.example.ui.shape.QsShapeManager.getComposeShape(shape.id)
            assertNotNull(composeShape)
        }
    }

    @Test
    fun `verify importing and deleting custom XML drawable shape`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ui.shape.QsShapeManager.init(context)

        val starXml = """
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="100dp"
                android:height="100dp"
                android:viewportWidth="100"
                android:viewportHeight="100">
                <path
                    android:fillColor="#FFFFFFFF"
                    android:pathData="M 50,2 L 64.5,33.5 L 98.5,38 L 73.5,62 L 79.5,96 L 50,79.5 L 20.5,96 L 26.5,62 L 1.5,38 L 35.5,33.5 Z" />
            </vector>
        """.trimIndent()

        // 1. Import Star shape
        val result = com.example.ui.shape.QsShapeManager.importShapeFromXml(context, starXml, "Star")
        org.junit.Assert.assertTrue(result.isSuccess)
        val importedStar = result.getOrNull()!!
        assertEquals("Star", importedStar.label)
        org.junit.Assert.assertTrue(importedStar.isCustom)

        // 2. Select it as enabled tile shape
        com.example.data.PanelPreferences.setEnabledTileShapeId(context, importedStar.id)
        assertEquals(importedStar.id, com.example.data.PanelPreferences.enabledTileShapeId.value)

        // 3. Test deleting the custom shape
        val deleted = com.example.ui.shape.QsShapeManager.deleteImportedShape(context, importedStar.id)
        org.junit.Assert.assertTrue(deleted)

        // Revert enabled tile shape to circle
        com.example.data.PanelPreferences.setEnabledTileShapeId(context, "circle")
        assertEquals("circle", com.example.data.PanelPreferences.enabledTileShapeId.value)
    }

    @Test
    fun `verify importing and deleting custom shape directly from path data string`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ui.shape.QsShapeManager.init(context)

        val heartPath = "M 50,88 C 20,68 4,46 4,28 C 4,12 16,4 30,4 C 40,4 47,10 50,18 C 53,10 60,4 70,4 C 84,4 96,12 96,28 C 96,46 80,68 50,88 Z"

        // 1. Import shape by directly pasting path data string
        val result = com.example.ui.shape.QsShapeManager.importShapeFromPathData(
            context = context,
            pathData = heartPath,
            customLabel = "Heart",
            viewportWidth = 100f,
            viewportHeight = 100f
        )
        org.junit.Assert.assertTrue(result.isSuccess)
        val importedHeart = result.getOrNull()!!
        assertEquals("Heart", importedHeart.label)
        assertEquals(heartPath, importedHeart.pathData)
        org.junit.Assert.assertTrue(importedHeart.isCustom)
        assertNotNull(importedHeart.rawXml)

        // 2. Verify drawable and compose shape can be rendered from imported path
        val drawable = com.example.ui.shape.QsShapeManager.createDrawable(importedHeart.id)
        assertNotNull(drawable)
        val composeShape = com.example.ui.shape.QsShapeManager.getComposeShape(importedHeart.id)
        assertNotNull(composeShape)

        // 3. Verify auto-detection of XML vs path data
        val parsedFromPath = com.example.ui.shape.QsShapeManager.parseShapeInput(heartPath, "Heart Auto")
        assertEquals("Heart Auto", parsedFromPath.label)
        assertEquals(heartPath, parsedFromPath.pathData)

        val xmlSnippet = "<vector android:viewportWidth=\"100\" android:viewportHeight=\"100\"><path android:pathData=\"$heartPath\" /></vector>"
        val parsedFromXml = com.example.ui.shape.QsShapeManager.parseShapeInput(xmlSnippet, "Heart Xml")
        assertEquals("Heart Xml", parsedFromXml.label)
        assertEquals(heartPath, parsedFromXml.pathData)

        // 4. Clean up
        val deleted = com.example.ui.shape.QsShapeManager.deleteImportedShape(context, importedHeart.id)
        org.junit.Assert.assertTrue(deleted)
    }

    @Test
    fun `verify tile outline toggle preference and drawable stroke`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        // Initial state should be false
        com.example.data.PanelPreferences.setTileOutlineEnabled(context, false)
        assertEquals(false, com.example.data.PanelPreferences.tileOutlineEnabled.value)

        // Enable 2dp outline
        com.example.data.PanelPreferences.setTileOutlineEnabled(context, true)
        assertEquals(true, com.example.data.PanelPreferences.tileOutlineEnabled.value)

        // Verify stroke properties on QsPathDrawable
        val drawable = com.example.ui.shape.QsShapeManager.createDrawable("circle")
        drawable.setColor(android.graphics.Color.TRANSPARENT)
        drawable.strokeWidth = 1.2f * 2.5f // 1.2dp in px
        drawable.strokeColor = android.graphics.Color.RED
        assertEquals(3.0f, drawable.strokeWidth, 0.001f)
        assertEquals(android.graphics.Color.RED, drawable.strokeColor)

        // Disable outline
        com.example.data.PanelPreferences.setTileOutlineEnabled(context, false)
        assertEquals(false, com.example.data.PanelPreferences.tileOutlineEnabled.value)
    }

    @Test
    fun `verify panel global corner enabled defaults to false`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Clear prefs to check true default initialization
        context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        com.example.data.PanelPreferences.init(context)

        // Default should be false
        assertEquals(false, com.example.data.PanelPreferences.panelGlobalCornerEnabled.value)
        assertEquals(20.0f, com.example.data.PanelPreferences.panelOuterCorner.value, 0.001f)
        assertEquals(5.0f, com.example.data.PanelPreferences.panelInnerCorner.value, 0.001f)

        // Enable global corner sync
        com.example.data.PanelPreferences.setPanelGlobalCornerEnabled(context, true)
        assertEquals(true, com.example.data.PanelPreferences.panelGlobalCornerEnabled.value)

        // When enabled, changing outer corner should sync inner corner
        com.example.data.PanelPreferences.setPanelOuterCorner(context, 16.0f)
        assertEquals(16.0f, com.example.data.PanelPreferences.panelOuterCorner.value, 0.001f)
        assertEquals(16.0f, com.example.data.PanelPreferences.panelInnerCorner.value, 0.001f)

        // Disable global corner again
        com.example.data.PanelPreferences.setPanelGlobalCornerEnabled(context, false)
        assertEquals(false, com.example.data.PanelPreferences.panelGlobalCornerEnabled.value)

        // Independent inner corner can now be set without mutating outer corner
        com.example.data.PanelPreferences.setPanelInnerCorner(context, 8.0f)
        assertEquals(16.0f, com.example.data.PanelPreferences.panelOuterCorner.value, 0.001f)
        assertEquals(8.0f, com.example.data.PanelPreferences.panelInnerCorner.value, 0.001f)
    }

    @Test
    fun `verify collapse expand subheaders preference toggle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        // Default should be false
        com.example.data.PanelPreferences.setCollapseExpandSubheaders(context, false)
        assertEquals(false, com.example.data.PanelPreferences.collapseExpandSubheaders.value)

        // Enable collapse/expand for list subheaders
        com.example.data.PanelPreferences.setCollapseExpandSubheaders(context, true)
        assertEquals(true, com.example.data.PanelPreferences.collapseExpandSubheaders.value)

        // Re-read preference from storage
        val prefs = context.getSharedPreferences("quickpanel_prefs", Context.MODE_PRIVATE)
        assertEquals(true, prefs.getBoolean("collapse_expand_subheaders", false))

        // Disable again
        com.example.data.PanelPreferences.setCollapseExpandSubheaders(context, false)
        assertEquals(false, com.example.data.PanelPreferences.collapseExpandSubheaders.value)
    }

    @Test
    fun `verify miscellaneous category and options integration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.data.PanelPreferences.init(context)

        // Verify MISCELLANEOUS category definition
        val misc = com.example.ui.customizer.CustomizeCategory.MISCELLANEOUS
        assertEquals("Miscellaneous", misc.title)
        assertNotNull(misc.icon)
        assertEquals("Performance, gestures, triggers & debug options", misc.description)

        // Verify Performance options
        com.example.data.PanelPreferences.setDisablePanelAnimations(context, true)
        assertEquals(true, com.example.data.PanelPreferences.disablePanelAnimations.value)

        // Verify Gestures options
        com.example.data.PanelPreferences.setEnableCustomVolumePanel(context, true)
        assertEquals(true, com.example.data.PanelPreferences.enableCustomVolumePanel.value)

        // Verify Trigger options
        com.example.data.PanelPreferences.setInvisibleStatusBarTrigger(context, true)
        assertEquals(true, com.example.data.PanelPreferences.invisibleStatusBarTrigger.value)
        com.example.data.PanelPreferences.setAutoSystemPanelClosing(context, true)
        assertEquals(true, com.example.data.PanelPreferences.autoSystemPanelClosing.value)
        com.example.data.PanelPreferences.setPanelReplacementDelayMs(context, 150)
        assertEquals(150, com.example.data.PanelPreferences.panelReplacementDelayMs.value)
        com.example.data.PanelPreferences.setOverrideSystemQs(context, true)
        assertEquals(true, com.example.data.PanelPreferences.overrideSystemQs.value)

        // Verify Debug options
        com.example.data.PanelPreferences.setForceShowMediaPreview(context, true)
        assertEquals(true, com.example.data.PanelPreferences.forceShowMediaPreview.value)
    }

    @Test
    fun `verify themes section exists and has no preset themes by default`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Clear prefs to check initial state
        context.getSharedPreferences("quickpanel_saved_themes_store", Context.MODE_PRIVATE).edit().clear().apply()
        com.example.data.ThemeManager.init(context)

        // 1. Verify THEMES category definition
        val themesCategory = com.example.ui.customizer.CustomizeCategory.THEMES
        assertEquals("Themes", themesCategory.title)
        assertNotNull(themesCategory.icon)
        assertEquals("Save and restore custom settings profiles", themesCategory.description)

        // 2. CRITICAL: Verify there are NO preset themes by default
        val initialThemes = com.example.data.ThemeManager.savedThemes.value
        assertEquals("Themes must NOT contain preset themes by default", 0, initialThemes.size)

        // 3. Save current settings as a new theme
        com.example.data.PanelPreferences.setDisablePanelAnimations(context, true)
        com.example.data.PanelPreferences.setInvisibleStatusBarTrigger(context, true)
        val savedTheme = com.example.data.ThemeManager.saveCurrentSettingsAsTheme(context, "Test Dark Minimalist")
        assertEquals("Test Dark Minimalist", savedTheme.name)
        assertEquals(1, com.example.data.ThemeManager.savedThemes.value.size)
        org.junit.Assert.assertTrue(savedTheme.settingsCount > 0)

        // Verify basic preview data extraction
        val preview = savedTheme.previewData
        assertNotNull(preview.themeMode)
        assertNotNull(preview.enabledTileShapeId)
        assertNotNull(preview.shapeLabel)
        org.junit.Assert.assertTrue(preview.panelOuterCorner > 0)

        // 4. Change settings and apply saved theme to verify restoration
        com.example.data.PanelPreferences.setDisablePanelAnimations(context, false)
        com.example.data.PanelPreferences.setInvisibleStatusBarTrigger(context, false)
        assertEquals(false, com.example.data.PanelPreferences.disablePanelAnimations.value)
        assertEquals(false, com.example.data.PanelPreferences.invisibleStatusBarTrigger.value)

        val applied = com.example.data.ThemeManager.applyTheme(context, savedTheme)
        assertEquals(true, applied)
        assertEquals(true, com.example.data.PanelPreferences.disablePanelAnimations.value)
        assertEquals(true, com.example.data.PanelPreferences.invisibleStatusBarTrigger.value)

        // 5. Rename theme
        com.example.data.ThemeManager.renameTheme(context, savedTheme.id, "Renamed Profile")
        assertEquals("Renamed Profile", com.example.data.ThemeManager.savedThemes.value[0].name)

        // 6. Export and import
        val json = com.example.data.ThemeManager.exportThemeToJson(com.example.data.ThemeManager.savedThemes.value[0])
        org.junit.Assert.assertTrue(json.contains("Renamed Profile"))

        // 7. Delete theme
        com.example.data.ThemeManager.deleteTheme(context, savedTheme.id)
        assertEquals(0, com.example.data.ThemeManager.savedThemes.value.size)
    }

    @Test
    fun testElementColorsLightAndDarkModeIsolationAndSync() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        com.example.data.PanelPreferences.init(context)

        // 1. Configure Light mode settings
        com.example.data.PanelPreferences.setSurfaceBgColorTertiary(context, isDark = false, enabled = true)
        com.example.data.PanelPreferences.setSurfaceBgHsb(context, isDark = false, hue = 45f, sat = 1.2f, valFactor = 0.8f)
        com.example.data.PanelPreferences.setInactiveTilesColorTertiary(context, isDark = false, enabled = true)
        com.example.data.PanelPreferences.setInactiveTilesHsb(context, isDark = false, hue = 30f, sat = 1.1f, valFactor = 0.9f)

        // 2. Configure Dark mode settings differently
        com.example.data.PanelPreferences.setSurfaceBgColorTertiary(context, isDark = true, enabled = false)
        com.example.data.PanelPreferences.setSurfaceBgHsb(context, isDark = true, hue = -90f, sat = 0.5f, valFactor = 1.3f)
        com.example.data.PanelPreferences.setInactiveTilesColorTertiary(context, isDark = true, enabled = false)
        com.example.data.PanelPreferences.setInactiveTilesHsb(context, isDark = true, hue = -60f, sat = 0.7f, valFactor = 1.2f)

        // Verify Light mode values
        assertEquals(true, com.example.data.PanelPreferences.getSurfaceBgColorTertiary(isDark = false).value)
        assertEquals(45f, com.example.data.PanelPreferences.getSurfaceBgHue(isDark = false).value, 0.001f)
        assertEquals(1.2f, com.example.data.PanelPreferences.getSurfaceBgSat(isDark = false).value, 0.001f)
        assertEquals(0.8f, com.example.data.PanelPreferences.getSurfaceBgVal(isDark = false).value, 0.001f)
        assertEquals(true, com.example.data.PanelPreferences.getInactiveTilesColorTertiary(isDark = false).value)
        assertEquals(30f, com.example.data.PanelPreferences.getInactiveTilesHue(isDark = false).value, 0.001f)

        // Verify Dark mode values remain isolated
        assertEquals(false, com.example.data.PanelPreferences.getSurfaceBgColorTertiary(isDark = true).value)
        assertEquals(-90f, com.example.data.PanelPreferences.getSurfaceBgHue(isDark = true).value, 0.001f)
        assertEquals(0.5f, com.example.data.PanelPreferences.getSurfaceBgSat(isDark = true).value, 0.001f)
        assertEquals(1.3f, com.example.data.PanelPreferences.getSurfaceBgVal(isDark = true).value, 0.001f)
        assertEquals(false, com.example.data.PanelPreferences.getInactiveTilesColorTertiary(isDark = true).value)
        assertEquals(-60f, com.example.data.PanelPreferences.getInactiveTilesHue(isDark = true).value, 0.001f)

        // 3. Test Sync from Dark to Light
        com.example.data.PanelPreferences.syncElementColors(context, fromDark = true)
        assertEquals(false, com.example.data.PanelPreferences.getSurfaceBgColorTertiary(isDark = false).value)
        assertEquals(-90f, com.example.data.PanelPreferences.getSurfaceBgHue(isDark = false).value, 0.001f)
        assertEquals(0.5f, com.example.data.PanelPreferences.getSurfaceBgSat(isDark = false).value, 0.001f)
        assertEquals(1.3f, com.example.data.PanelPreferences.getSurfaceBgVal(isDark = false).value, 0.001f)
        assertEquals(false, com.example.data.PanelPreferences.getInactiveTilesColorTertiary(isDark = false).value)
        assertEquals(-60f, com.example.data.PanelPreferences.getInactiveTilesHue(isDark = false).value, 0.001f)
    }

    @Test
    fun `test second most colorful color extraction from album art`() {
        val bmp = android.graphics.Bitmap.createBitmap(120, 120, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.rgb(10, 10, 15)) // dark background
        val cyanPixels = IntArray(30 * 30) { android.graphics.Color.rgb(0, 200, 255) }
        val yellowPixels = IntArray(30 * 30) { android.graphics.Color.rgb(255, 215, 0) }
        bmp.setPixels(cyanPixels, 0, 30, 10, 10, 30, 30)
        bmp.setPixels(yellowPixels, 0, 30, 50, 50, 30, 30)
        val palette = androidx.palette.graphics.Palette.from(bmp).maximumColorCount(32).clearFilters().generate()

        val secondColor = com.example.ui.overlay.extractSecondMostColorfulColor(palette)
        assertNotNull(secondColor)

        val colors = com.example.ui.overlay.extractAndroidQNotificationColors(bmp, palette)
        assertNotNull(colors.backgroundColor)
        assertNotNull(colors.accentColor)
        val bgLum = androidx.core.graphics.ColorUtils.calculateLuminance(colors.backgroundColor.toArgb())
        org.junit.Assert.assertTrue("Artwork background should be dark (lum < 0.15)", bgLum < 0.15)
        val contrast = androidx.core.graphics.ColorUtils.calculateContrast(
            colors.accentColor.toArgb(),
            colors.backgroundColor.toArgb()
        )
        org.junit.Assert.assertTrue("Artwork accent should contrast with background", contrast >= 2.0)
    }

    @Test
    fun `test black with red artwork extracts black background with red elements`() {
        val bmp = android.graphics.Bitmap.createBitmap(120, 120, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.BLACK)
        val pixels = IntArray(60 * 60) { android.graphics.Color.RED }
        bmp.setPixels(pixels, 0, 60, 30, 30, 60, 60)

        val palette = androidx.palette.graphics.Palette.from(bmp).maximumColorCount(32).clearFilters().generate()
        val colors = com.example.ui.overlay.extractAndroidQNotificationColors(bmp, palette)

        val bgArgb = colors.backgroundColor.toArgb()
        val bgLum = androidx.core.graphics.ColorUtils.calculateLuminance(bgArgb)
        assertEquals("Black with red artwork must use black background", 0.0, bgLum, 0.05)

        val accentArgb = colors.accentColor.toArgb()
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(accentArgb, hsl)
        val hue = hsl[0]
        val isRedHue = (hue <= 30f || hue >= 330f)
        org.junit.Assert.assertTrue("Accent must be in the red hue family (was $hue)", isRedHue)
        org.junit.Assert.assertTrue("Accent must be saturated (sat >= 0.65, was ${hsl[1]})", hsl[1] >= 0.65f)
        val contrast = androidx.core.graphics.ColorUtils.calculateContrast(accentArgb, bgArgb)
        org.junit.Assert.assertTrue("Red accent must contrast with black background (contrast >= 4.5, was $contrast)", contrast >= 4.5)
    }

    @Test
    fun `test white with blue artwork extracts white background with blue elements`() {
        val bmp = android.graphics.Bitmap.createBitmap(120, 120, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.WHITE)
        val pixels = IntArray(60 * 60) { android.graphics.Color.BLUE }
        bmp.setPixels(pixels, 0, 60, 30, 30, 60, 60)

        val palette = androidx.palette.graphics.Palette.from(bmp).maximumColorCount(32).clearFilters().generate()
        val colors = com.example.ui.overlay.extractAndroidQNotificationColors(bmp, palette)

        val bgArgb = colors.backgroundColor.toArgb()
        val bgLum = androidx.core.graphics.ColorUtils.calculateLuminance(bgArgb)
        assertEquals("White with blue artwork must use white background", 1.0, bgLum, 0.05)

        val accentArgb = colors.accentColor.toArgb()
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(accentArgb, hsl)
        val hue = hsl[0]
        val isBlueHue = (hue in 190f..260f)
        org.junit.Assert.assertTrue("Accent must be in the blue hue family (was $hue)", isBlueHue)
        org.junit.Assert.assertTrue("Accent must be saturated (sat >= 0.65, was ${hsl[1]})", hsl[1] >= 0.65f)
        val contrast = androidx.core.graphics.ColorUtils.calculateContrast(accentArgb, bgArgb)
        org.junit.Assert.assertTrue("Blue accent must contrast with white background (contrast >= 4.0, was $contrast)", contrast >= 4.0)
    }

    @Test
    fun `verify screen wake helper acquires and releases wake lock for incoming calls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.service.ScreenWakeHelper.wakeScreen(context, "TestCallWake", 5000L)
        // Verify release does not throw
        com.example.service.ScreenWakeHelper.releaseWakeLock()
    }

    @Test
    fun `verify notification tracking and dismissal by unique key with multiple media player cards`() {
        val dismissedKeys = mutableListOf<String>()
        val adapter = com.example.ui.notification.NotificationAdapter(
            onNotificationClick = {},
            onNotificationDismiss = { item -> dismissedKeys.add(item.key) },
            onActionClick = {},
            onClearAll = {}
        )

        // 2 active media player cards
        val media1 = com.example.model.MediaState(
            title = "Song 1",
            artist = "Artist 1",
            isPlaying = true,
            sessionIndex = 0,
            packageName = "com.music.one"
        )
        val media2 = com.example.model.MediaState(
            title = "Podcast 2",
            artist = "Host 2",
            isPlaying = false,
            sessionIndex = 1,
            packageName = "com.podcast.two"
        )
        adapter.mediaSessions = listOf(media1, media2)

        // 3 regular notifications
        val notif0 = com.example.model.NotificationItem(
            key = "key_notif_0",
            id = 100,
            packageName = "com.app.chat",
            appName = "Chat",
            title = "First Notification (Top)",
            text = "Hello",
            isClearable = true
        )
        val notif1 = com.example.model.NotificationItem(
            key = "key_notif_1",
            id = 101,
            packageName = "com.app.mail",
            appName = "Mail",
            title = "Second Notification (Middle)",
            text = "Update",
            isClearable = true
        )
        val notif2 = com.example.model.NotificationItem(
            key = "key_notif_2",
            id = 102,
            packageName = "com.app.calendar",
            appName = "Calendar",
            title = "Third Notification (Bottom-most)",
            text = "Meeting",
            isClearable = true
        )
        adapter.submitNotifications(listOf(notif0, notif1, notif2))

        // Total item count: 2 media cards + 3 notifications = 5
        assertEquals(5, adapter.itemCount)

        // Positions 0 and 1 must be Media Players and never return a NotificationItem
        assertEquals(0, adapter.getItemViewType(0)) // TYPE_MEDIA_PLAYER
        assertEquals(0, adapter.getItemViewType(1)) // TYPE_MEDIA_PLAYER
        org.junit.Assert.assertNull(adapter.getNotificationItem(0))
        org.junit.Assert.assertNull(adapter.getNotificationItem(1))

        // Positions 2, 3, 4 must be Notifications with strict key-to-item mapping
        assertEquals(1, adapter.getItemViewType(2)) // TYPE_NOTIFICATION
        assertEquals(1, adapter.getItemViewType(3)) // TYPE_NOTIFICATION
        assertEquals(1, adapter.getItemViewType(4)) // TYPE_NOTIFICATION

        // Verifying offset: Position 2 must map to notif0 (First/Top notification), NOT notif2 (Bottom-most)
        val topItem = adapter.getNotificationItem(2)
        assertNotNull(topItem)
        assertEquals("key_notif_0", topItem?.key)
        assertEquals("First Notification (Top)", topItem?.title)

        val middleItem = adapter.getNotificationItem(3)
        assertNotNull(middleItem)
        assertEquals("key_notif_1", middleItem?.key)

        val bottomItem = adapter.getNotificationItem(4)
        assertNotNull(bottomItem)
        assertEquals("key_notif_2", bottomItem?.key)
        assertEquals("Third Notification (Bottom-most)", bottomItem?.title)

        // Lookup by unique key
        val itemByKey = adapter.getNotificationItemByKey("key_notif_0")
        assertNotNull(itemByKey)
        assertEquals("First Notification (Top)", itemByKey?.title)

        // Verify dismissal of the top notification: must dismiss notif0 by key ("key_notif_0")
        topItem?.let {
            com.example.service.QuickPanelNotificationListenerService.dismissNotification(it.key)
        }
    }

    @Test
    fun `verify setup wizard completion persists across app launches and does not reopen`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Mark setup as completed
        com.example.data.PanelPreferences.setInitialSetupCompleted(app, true)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.initialSetupCompleted.value)

        // Instantiate MainViewModel, simulating an app launch
        val viewModel = com.example.ui.MainViewModel(app)
        // Must NOT reopen initial setup since it was already finished
        org.junit.Assert.assertFalse("Initial setup should NOT reopen if already completed", viewModel.showInitialSetup.value)

        // Manual open request from status screen works
        viewModel.openInitialSetup()
        org.junit.Assert.assertTrue(viewModel.showInitialSetup.value)

        // Completing setup dismisses it and preserves true
        viewModel.completeInitialSetup()
        org.junit.Assert.assertFalse(viewModel.showInitialSetup.value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.initialSetupCompleted.value)
    }

    @Test
    fun `verify media player card close button terminates playback and dismisses card`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.service.MediaSessionManagerHelper.init(app)

        val media = com.example.model.MediaState(
            title = "Test Song",
            artist = "Test Artist",
            isPlaying = true,
            sessionIndex = 0,
            packageName = "com.test.music"
        )

        // Verify stopAndDismissSession terminates playback and removes session
        com.example.service.MediaSessionManagerHelper.stopAndDismissSession(0)
        org.junit.Assert.assertNull(com.example.service.MediaSessionManagerHelper.mediaState.value)
    }

    @Test
    fun `verify root permission request persists and hides request button`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val engine = com.example.system.PrivilegeEngine(app)

        engine.requestRootPermission()
        org.junit.Assert.assertTrue(engine.isRootAvailable.value)

        // Calling refreshPermissions should preserve granted root state
        engine.refreshPermissions()
        org.junit.Assert.assertTrue("Root state should remain granted after refreshPermissions", engine.isRootAvailable.value)
    }

    @Test
    fun `verify wifi ssid and bluetooth connected device parsing helpers`() {
        org.junit.Assert.assertNull(com.example.system.NativeTileManager.cleanSsid("<unknown ssid>"))
        org.junit.Assert.assertNull(com.example.system.NativeTileManager.cleanSsid("\"\""))
        org.junit.Assert.assertNull(com.example.system.NativeTileManager.cleanSsid("0x"))
        assertEquals("HomeFiber_5G", com.example.system.NativeTileManager.cleanSsid("\"HomeFiber_5G\""))

        val cmdWifiOutput = """
            Wifi is enabled
            Wifi is connected to "Pixel_Hotspot_6GHz"
            WifiInfo: SSID: "Pixel_Hotspot_6GHz", BSSID: aa:bb:cc:dd:ee:ff
        """.trimIndent()
        assertEquals("Pixel_Hotspot_6GHz", com.example.system.NativeTileManager.parseWifiSsidFromShellOutput(cmdWifiOutput))

        val dumpsysWifiOutput = "mWifiInfo SSID: Office_Guest, BSSID: 11:22:33:44:55:66, MAC: 02:00:00:00:00:00"
        assertEquals("Office_Guest", com.example.system.NativeTileManager.parseWifiSsidFromShellOutput(dumpsysWifiOutput))

        val btDumpDirect = """
            Bonded devices:
              AA:BB:CC:DD:EE:01 [BR/EDR] Galaxy Buds2 Pro (Connected)
        """.trimIndent()
        assertEquals("Galaxy Buds2 Pro", com.example.system.NativeTileManager.parseConnectedBluetoothFromShellOutput(btDumpDirect))

        val btDumpStateMachine = """
            Bonded devices:
              11:22:33:44:55:66 [ DUAL ] Sony WH-1000XM5
              AA:BB:CC:DD:EE:FF [ LE ] Keychron K3
            A2dpService:
              mActiveDevice: 11:22:33:44:55:66
        """.trimIndent()
        assertEquals("Sony WH-1000XM5", com.example.system.NativeTileManager.parseConnectedBluetoothFromShellOutput(btDumpStateMachine))
    }

    @Test
    fun `verify wifi and bluetooth subtitles and Android 6 label renaming`() {
        for (style in com.example.data.QsPanelStyle.values()) {
            // Wi-Fi replaces "Connected" with Wi-Fi name when connected, "On" when disconnected, "Off" when disabled
            assertEquals(
                "MyWiFi_5G",
                com.example.system.TileRegistry.resolveWifiSubtitle(
                    wifiOn = true,
                    wifiConnected = true,
                    wifiSsidName = "\"MyWiFi_5G\"",
                    panelStyle = style
                )
            )
            assertEquals(
                "On",
                com.example.system.TileRegistry.resolveWifiSubtitle(
                    wifiOn = true,
                    wifiConnected = false,
                    wifiSsidName = null,
                    panelStyle = style
                )
            )
            assertEquals(
                "Off",
                com.example.system.TileRegistry.resolveWifiSubtitle(
                    wifiOn = false,
                    wifiConnected = false,
                    wifiSsidName = "MyWiFi_5G",
                    panelStyle = style
                )
            )

            // Bluetooth replaces "On" with connected Bluetooth device name, "On" when no device connected, "Off" when disabled
            assertEquals(
                "Pixel Buds Pro",
                com.example.system.TileRegistry.resolveBluetoothSubtitle(
                    btOn = true,
                    btDeviceName = "Pixel Buds Pro",
                    panelStyle = style
                )
            )
            assertEquals(
                "On",
                com.example.system.TileRegistry.resolveBluetoothSubtitle(
                    btOn = true,
                    btDeviceName = null,
                    panelStyle = style
                )
            )
            assertEquals(
                "Off",
                com.example.system.TileRegistry.resolveBluetoothSubtitle(
                    btOn = false,
                    btDeviceName = "Pixel Buds Pro",
                    panelStyle = style
                )
            )
        }

        // Android 6 (Marshmallow) label renaming (no subtitle, single label renamed when enabled)
        val wifiOffTile = com.example.model.QsTileModel(
            id = "wifi",
            label = "Internet",
            subtitle = "Off",
            state = TileState.INACTIVE,
            category = TileCategory.CONNECTIVITY
        )
        val wifiOnDisconnectedTile = wifiOffTile.copy(state = TileState.ACTIVE, subtitle = "On")
        val wifiOnConnectedTile = wifiOffTile.copy(state = TileState.ACTIVE, subtitle = "MyWiFi_5G")

        assertEquals("Internet", com.example.system.TileRegistry.resolveMarshmallowTileLabel(wifiOffTile, wifiSsidName = "MyWiFi_5G"))
        assertEquals("Wi-Fi", com.example.system.TileRegistry.resolveMarshmallowTileLabel(wifiOnDisconnectedTile, wifiSsidName = null))
        assertEquals("MyWiFi_5G", com.example.system.TileRegistry.resolveMarshmallowTileLabel(wifiOnConnectedTile, wifiSsidName = "\"MyWiFi_5G\""))

        val btOffTile = com.example.model.QsTileModel(
            id = "bluetooth",
            label = "Bluetooth",
            subtitle = "Off",
            state = TileState.INACTIVE,
            category = TileCategory.CONNECTIVITY
        )
        val btOnDisconnectedTile = btOffTile.copy(state = TileState.ACTIVE, subtitle = "On")
        val btOnConnectedTile = btOffTile.copy(state = TileState.ACTIVE, subtitle = "Pixel Buds Pro")

        assertEquals("Bluetooth", com.example.system.TileRegistry.resolveMarshmallowTileLabel(btOffTile, btDeviceName = "Pixel Buds Pro"))
        assertEquals("Bluetooth", com.example.system.TileRegistry.resolveMarshmallowTileLabel(btOnDisconnectedTile, btDeviceName = null))
        assertEquals("Pixel Buds Pro", com.example.system.TileRegistry.resolveMarshmallowTileLabel(btOnConnectedTile, btDeviceName = "Pixel Buds Pro"))
    }

    @Test
    fun `verify separate date battery clock carrier background toggles and corner sliders`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset defaults
        com.example.data.PanelPreferences.setDateBackgroundEnabled(app, false)
        com.example.data.PanelPreferences.setBatteryBackgroundEnabled(app, false)
        com.example.data.PanelPreferences.setClockBackgroundEnabled(app, false)
        com.example.data.PanelPreferences.setCarrierBackgroundEnabled(app, false)

        com.example.data.PanelPreferences.setDateBackgroundCorner(app, 10f)
        com.example.data.PanelPreferences.setBatteryBackgroundCorner(app, 10f)
        com.example.data.PanelPreferences.setClockBackgroundCorner(app, 10f)
        com.example.data.PanelPreferences.setCarrierBackgroundCorner(app, 10f)

        // Verify independent toggles
        com.example.data.PanelPreferences.setDateBackgroundEnabled(app, true)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.dateBackgroundEnabled.value)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.batteryBackgroundEnabled.value)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.clockBackgroundEnabled.value)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.carrierBackgroundEnabled.value)

        com.example.data.PanelPreferences.setBatteryBackgroundEnabled(app, true)
        com.example.data.PanelPreferences.setClockBackgroundEnabled(app, true)
        com.example.data.PanelPreferences.setCarrierBackgroundEnabled(app, true)

        // Verify distinct corner radii and clamping (0f..24f)
        com.example.data.PanelPreferences.setDateBackgroundCorner(app, 6f)
        com.example.data.PanelPreferences.setBatteryBackgroundCorner(app, 16f)
        com.example.data.PanelPreferences.setClockBackgroundCorner(app, -5f) // clamped to 0f
        com.example.data.PanelPreferences.setCarrierBackgroundCorner(app, 42f) // clamped to 24f

        // Re-initialize from SharedPreferences to verify persistence
        com.example.data.PanelPreferences.init(app)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.dateBackgroundEnabled.value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.batteryBackgroundEnabled.value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.clockBackgroundEnabled.value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.carrierBackgroundEnabled.value)
        assertEquals(6f, com.example.data.PanelPreferences.dateBackgroundCorner.value, 0.01f)
        assertEquals(16f, com.example.data.PanelPreferences.batteryBackgroundCorner.value, 0.01f)
        assertEquals(0f, com.example.data.PanelPreferences.clockBackgroundCorner.value, 0.01f)
        assertEquals(24f, com.example.data.PanelPreferences.carrierBackgroundCorner.value, 0.01f)
    }

    @Test
    fun `verify qs page indicator style enum mapping and persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        assertEquals(com.example.data.QsPageIndicatorStyle.DOTS, com.example.data.QsPageIndicatorStyle.fromId(null))
        assertEquals(com.example.data.QsPageIndicatorStyle.DOTS, com.example.data.QsPageIndicatorStyle.fromId("unknown"))

        for (style in com.example.data.QsPageIndicatorStyle.entries) {
            com.example.data.PanelPreferences.setQsPageIndicatorStyle(app, style)
            assertEquals(style, com.example.data.PanelPreferences.qsPageIndicatorStyle.value)

            com.example.data.PanelPreferences.init(app)
            assertEquals(style, com.example.data.PanelPreferences.qsPageIndicatorStyle.value)
        }

        // Verify all 8 page indicator dimension preferences (persistence + clamping)
        com.example.data.PanelPreferences.setPiActiveLineThickness(app, 6.5f)
        com.example.data.PanelPreferences.setPiInactiveLineThickness(app, 2.0f)
        com.example.data.PanelPreferences.setPiActiveLineWidth(app, 34f)
        com.example.data.PanelPreferences.setPiInactiveLineWidth(app, 14f)
        com.example.data.PanelPreferences.setPiActiveDotRadius(app, 5.5f)
        com.example.data.PanelPreferences.setPiInactiveDotRadius(app, 1.5f)
        com.example.data.PanelPreferences.setPiActiveSlidingThickness(app, 8.0f)
        com.example.data.PanelPreferences.setPiInactiveSlidingThickness(app, 99f) // clamped to 12f

        com.example.data.PanelPreferences.init(app)
        assertEquals(6.5f, com.example.data.PanelPreferences.piActiveLineThickness.value, 0.01f)
        assertEquals(2.0f, com.example.data.PanelPreferences.piInactiveLineThickness.value, 0.01f)
        assertEquals(34f, com.example.data.PanelPreferences.piActiveLineWidth.value, 0.01f)
        assertEquals(14f, com.example.data.PanelPreferences.piInactiveLineWidth.value, 0.01f)
        assertEquals(5.5f, com.example.data.PanelPreferences.piActiveDotRadius.value, 0.01f)
        assertEquals(1.5f, com.example.data.PanelPreferences.piInactiveDotRadius.value, 0.01f)
        assertEquals(8.0f, com.example.data.PanelPreferences.piActiveSlidingThickness.value, 0.01f)
        assertEquals(12.0f, com.example.data.PanelPreferences.piInactiveSlidingThickness.value, 0.01f)

        // Restore default
        com.example.data.PanelPreferences.setQsPageIndicatorStyle(app, com.example.data.QsPageIndicatorStyle.DOTS)
    }

    @Test
    fun `verify move collapse to qqs footer and carrier divider as line persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to defaults
        com.example.data.PanelPreferences.setMoveCollapseToQqsFooter(app, false)
        com.example.data.PanelPreferences.setCarrierDividerAsLine(app, false)

        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.moveCollapseToQqsFooter.value)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.carrierDividerAsLine.value)

        // Enable both and re-initialize from SharedPreferences
        com.example.data.PanelPreferences.setMoveCollapseToQqsFooter(app, true)
        com.example.data.PanelPreferences.setCarrierDividerAsLine(app, true)

        com.example.data.PanelPreferences.init(app)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.moveCollapseToQqsFooter.value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.carrierDividerAsLine.value)

        // Restore defaults
        com.example.data.PanelPreferences.setMoveCollapseToQqsFooter(app, false)
        com.example.data.PanelPreferences.setCarrierDividerAsLine(app, false)
    }

    @Test
    fun `verify qqs 2x1 complications row toggle and complication selection persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to defaults
        com.example.data.PanelPreferences.setEnableQqsComplicationsRow(app, false)
        com.example.data.PanelPreferences.setQqs2x1ComplicationIds(
            app,
            com.example.data.PanelPreferences.DEFAULT_QQS_2X1_COMPLICATION_IDS
        )

        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.enableQqsComplicationsRow.value)
        assertEquals(
            listOf("comp_battery", "comp_storage"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // Enable toggle and modify selected 2x1 complications
        com.example.data.PanelPreferences.setEnableQqsComplicationsRow(app, true)
        com.example.data.PanelPreferences.toggleQqs2x1ComplicationId(app, "comp_brightness")
        com.example.data.PanelPreferences.toggleQqs2x1ComplicationId(app, "comp_storage")

        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.enableQqsComplicationsRow.value)
        assertEquals(
            listOf("comp_battery", "comp_brightness"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // Toggling off when only 1 item remains must keep at least 1 complication selected
        com.example.data.PanelPreferences.toggleQqs2x1ComplicationId(app, "comp_brightness")
        com.example.data.PanelPreferences.toggleQqs2x1ComplicationId(app, "comp_battery")
        assertEquals(listOf("comp_battery"), com.example.data.PanelPreferences.qqs2x1ComplicationIds.value)

        // Re-initialize from SharedPreferences to verify persistence
        com.example.data.PanelPreferences.init(app)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.enableQqsComplicationsRow.value)
        assertEquals(listOf("comp_battery"), com.example.data.PanelPreferences.qqs2x1ComplicationIds.value)

        // Restore defaults
        com.example.data.PanelPreferences.setEnableQqsComplicationsRow(app, false)
        com.example.data.PanelPreferences.setQqs2x1ComplicationIds(
            app,
            com.example.data.PanelPreferences.DEFAULT_QQS_2X1_COMPLICATION_IDS
        )
    }

    @Test
    fun `verify qqs 2x1 complications move insert and remove operations`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to known list
        com.example.data.PanelPreferences.setQqs2x1ComplicationIds(
            app,
            listOf("comp_battery", "comp_storage")
        )

        // 1. Insert new complication at index 1
        com.example.data.PanelPreferences.insertQqs2x1ComplicationId(app, "comp_brightness", targetIndex = 1)
        assertEquals(
            listOf("comp_battery", "comp_brightness", "comp_storage"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // 2. Insert new complication at end (null targetIndex)
        com.example.data.PanelPreferences.insertQqs2x1ComplicationId(app, "comp_ram")
        assertEquals(
            listOf("comp_battery", "comp_brightness", "comp_storage", "comp_ram"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // 3. Move item from index 3 to index 0
        com.example.data.PanelPreferences.moveQqs2x1ComplicationId(app, fromIndex = 3, toIndex = 0)
        assertEquals(
            listOf("comp_ram", "comp_battery", "comp_brightness", "comp_storage"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // 4. Re-insert existing item at a different target index repositions it without duplicates
        com.example.data.PanelPreferences.insertQqs2x1ComplicationId(app, "comp_ram", targetIndex = 2)
        assertEquals(
            listOf("comp_battery", "comp_brightness", "comp_ram", "comp_storage"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // 5. Ignore invalid move indices
        com.example.data.PanelPreferences.moveQqs2x1ComplicationId(app, fromIndex = -1, toIndex = 1)
        com.example.data.PanelPreferences.moveQqs2x1ComplicationId(app, fromIndex = 0, toIndex = 99)
        assertEquals(
            listOf("comp_battery", "comp_brightness", "comp_ram", "comp_storage"),
            com.example.data.PanelPreferences.qqs2x1ComplicationIds.value
        )

        // 6. Remove down to 1 item; last item cannot be removed
        com.example.data.PanelPreferences.removeQqs2x1ComplicationId(app, "comp_battery")
        com.example.data.PanelPreferences.removeQqs2x1ComplicationId(app, "comp_brightness")
        com.example.data.PanelPreferences.removeQqs2x1ComplicationId(app, "comp_ram")
        assertEquals(listOf("comp_storage"), com.example.data.PanelPreferences.qqs2x1ComplicationIds.value)
        com.example.data.PanelPreferences.removeQqs2x1ComplicationId(app, "comp_storage")
        assertEquals(listOf("comp_storage"), com.example.data.PanelPreferences.qqs2x1ComplicationIds.value)

        // Restore defaults
        com.example.data.PanelPreferences.setQqs2x1ComplicationIds(
            app,
            com.example.data.PanelPreferences.DEFAULT_QQS_2X1_COMPLICATION_IDS
        )
    }

    @Test
    fun `verify qqs 2x1 horizontal line toggle persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to default false
        com.example.data.PanelPreferences.setQqs2x1HorizontalLine(app, false)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.qqs2x1HorizontalLine.value)

        // Enable horizontal line mode and verify re-init persistence
        com.example.data.PanelPreferences.setQqs2x1HorizontalLine(app, true)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.qqs2x1HorizontalLine.value)

        com.example.data.PanelPreferences.init(app)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.qqs2x1HorizontalLine.value)

        // Restore default
        com.example.data.PanelPreferences.setQqs2x1HorizontalLine(app, false)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.qqs2x1HorizontalLine.value)
    }

    @Test
    fun `verify qqs 2x1 perimeter progress toggle persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to default false
        com.example.data.PanelPreferences.setQqs2x1PerimeterProgress(app, false)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.qqs2x1PerimeterProgress.value)

        // Enable perimeter progress bar and verify re-init persistence
        com.example.data.PanelPreferences.setQqs2x1PerimeterProgress(app, true)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.qqs2x1PerimeterProgress.value)

        com.example.data.PanelPreferences.init(app)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.qqs2x1PerimeterProgress.value)

        // Verify independence from horizontal line toggle
        com.example.data.PanelPreferences.setQqs2x1HorizontalLine(app, false)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.qqs2x1PerimeterProgress.value)

        // Restore default
        com.example.data.PanelPreferences.setQqs2x1PerimeterProgress(app, false)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.qqs2x1PerimeterProgress.value)
    }

    @Test
    fun `verify qqs 2x1 perimeter progress hides icon arc and horizontal progress indicator`() {
        // Default: only icon arc visible
        val defaultMode = com.example.data.PanelPreferences.resolveQqs2x1ProgressDisplayMode(
            useHorizontalLine = false,
            usePerimeterProgress = false
        )
        org.junit.Assert.assertFalse(defaultMode.showPerimeterBar)
        org.junit.Assert.assertFalse(defaultMode.showHorizontalLine)
        org.junit.Assert.assertTrue(defaultMode.showIconArc)

        // Horizontal line only: icon arc hidden, horizontal line visible
        val lineOnlyMode = com.example.data.PanelPreferences.resolveQqs2x1ProgressDisplayMode(
            useHorizontalLine = true,
            usePerimeterProgress = false
        )
        org.junit.Assert.assertFalse(lineOnlyMode.showPerimeterBar)
        org.junit.Assert.assertTrue(lineOnlyMode.showHorizontalLine)
        org.junit.Assert.assertFalse(lineOnlyMode.showIconArc)

        // Perimeter progress enabled (with horizontal line false): icon arc and horizontal indicator hidden
        val perimeterDefaultMode = com.example.data.PanelPreferences.resolveQqs2x1ProgressDisplayMode(
            useHorizontalLine = false,
            usePerimeterProgress = true
        )
        org.junit.Assert.assertTrue(perimeterDefaultMode.showPerimeterBar)
        org.junit.Assert.assertFalse(perimeterDefaultMode.showHorizontalLine)
        org.junit.Assert.assertFalse(perimeterDefaultMode.showIconArc)

        // Perimeter progress enabled (even if horizontal line toggle is also on): icon arc and horizontal indicator hidden
        val perimeterWithLineMode = com.example.data.PanelPreferences.resolveQqs2x1ProgressDisplayMode(
            useHorizontalLine = true,
            usePerimeterProgress = true
        )
        org.junit.Assert.assertTrue(perimeterWithLineMode.showPerimeterBar)
        org.junit.Assert.assertFalse(perimeterWithLineMode.showHorizontalLine)
        org.junit.Assert.assertFalse(perimeterWithLineMode.showIconArc)
    }

    @Test
    fun `verify qqs 2x1 corner sliders and tile height slider persistence and clamping`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset to defaults and verify
        com.example.data.PanelPreferences.setQqs2x1ActiveCornerDp(app, 28.0f)
        com.example.data.PanelPreferences.setQqs2x1InactiveCornerDp(app, 12.0f)
        com.example.data.PanelPreferences.setQqs2x1TileHeightDp(app, 56)
        assertEquals(28.0f, com.example.data.PanelPreferences.qqs2x1ActiveCornerDp.value, 0.001f)
        assertEquals(12.0f, com.example.data.PanelPreferences.qqs2x1InactiveCornerDp.value, 0.001f)
        assertEquals(56, com.example.data.PanelPreferences.qqs2x1TileHeightDp.value)

        // Set custom values within range
        com.example.data.PanelPreferences.setQqs2x1ActiveCornerDp(app, 20.0f)
        com.example.data.PanelPreferences.setQqs2x1InactiveCornerDp(app, 6.0f)
        com.example.data.PanelPreferences.setQqs2x1TileHeightDp(app, 68)
        assertEquals(20.0f, com.example.data.PanelPreferences.qqs2x1ActiveCornerDp.value, 0.001f)
        assertEquals(6.0f, com.example.data.PanelPreferences.qqs2x1InactiveCornerDp.value, 0.001f)
        assertEquals(68, com.example.data.PanelPreferences.qqs2x1TileHeightDp.value)

        // Re-initialize from SharedPreferences and verify persistence
        com.example.data.PanelPreferences.init(app)
        assertEquals(20.0f, com.example.data.PanelPreferences.qqs2x1ActiveCornerDp.value, 0.001f)
        assertEquals(6.0f, com.example.data.PanelPreferences.qqs2x1InactiveCornerDp.value, 0.001f)
        assertEquals(68, com.example.data.PanelPreferences.qqs2x1TileHeightDp.value)

        // Verify clamping bounds
        com.example.data.PanelPreferences.setQqs2x1ActiveCornerDp(app, -10.0f)
        assertEquals(0.0f, com.example.data.PanelPreferences.qqs2x1ActiveCornerDp.value, 0.001f)
        com.example.data.PanelPreferences.setQqs2x1ActiveCornerDp(app, 99.0f)
        assertEquals(40.0f, com.example.data.PanelPreferences.qqs2x1ActiveCornerDp.value, 0.001f)

        com.example.data.PanelPreferences.setQqs2x1InactiveCornerDp(app, -5.0f)
        assertEquals(0.0f, com.example.data.PanelPreferences.qqs2x1InactiveCornerDp.value, 0.001f)
        com.example.data.PanelPreferences.setQqs2x1InactiveCornerDp(app, 80.0f)
        assertEquals(40.0f, com.example.data.PanelPreferences.qqs2x1InactiveCornerDp.value, 0.001f)

        com.example.data.PanelPreferences.setQqs2x1TileHeightDp(app, 12)
        assertEquals(40, com.example.data.PanelPreferences.qqs2x1TileHeightDp.value)
        com.example.data.PanelPreferences.setQqs2x1TileHeightDp(app, 150)
        assertEquals(84, com.example.data.PanelPreferences.qqs2x1TileHeightDp.value)

        // Restore defaults
        com.example.data.PanelPreferences.setQqs2x1ActiveCornerDp(app, 28.0f)
        com.example.data.PanelPreferences.setQqs2x1InactiveCornerDp(app, 12.0f)
        com.example.data.PanelPreferences.setQqs2x1TileHeightDp(app, 56)
    }

    @Test
    fun `verify active inactive complication arc and track element color preferences isolation persistence and sync`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        com.example.data.PanelPreferences.init(app)

        // Reset both Light and Dark modes to defaults
        for (isDark in listOf(false, true)) {
            com.example.data.PanelPreferences.setActiveCompArcMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setActiveCompArcHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setActiveCompArcUseCustom(app, isDark, false)
            com.example.data.PanelPreferences.setActiveCompArcCustomColor(app, isDark, 0xFFFFFFFF.toInt())

            com.example.data.PanelPreferences.setInactiveCompArcMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setInactiveCompArcHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setInactiveCompArcUseCustom(app, isDark, false)
            com.example.data.PanelPreferences.setInactiveCompArcCustomColor(app, isDark, 0xFF673AB7.toInt())

            com.example.data.PanelPreferences.setCompArcTrackMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setCompArcTrackHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setCompArcTrackUseCustom(app, isDark, false)
            com.example.data.PanelPreferences.setCompArcTrackCustomColor(app, isDark, 0xFF334155.toInt())
        }

        // Verify defaults
        assertEquals(com.example.data.ComplicationArcMonetMode.DEFAULT, com.example.data.PanelPreferences.getActiveCompArcMode(false).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.DEFAULT, com.example.data.PanelPreferences.getInactiveCompArcMode(true).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.DEFAULT, com.example.data.PanelPreferences.getCompArcTrackMode(false).value)

        // Modify Dark mode settings and verify Light mode remains isolated
        com.example.data.PanelPreferences.setActiveCompArcMode(app, true, com.example.data.ComplicationArcMonetMode.TERTIARY)
        com.example.data.PanelPreferences.setActiveCompArcHsb(app, true, 45f, 1.4f, 0.9f)
        com.example.data.PanelPreferences.setActiveCompArcUseCustom(app, true, true)
        com.example.data.PanelPreferences.setActiveCompArcCustomColor(app, true, 0xFF00E5FF.toInt())

        com.example.data.PanelPreferences.setInactiveCompArcMode(app, true, com.example.data.ComplicationArcMonetMode.SECONDARY)
        com.example.data.PanelPreferences.setInactiveCompArcHsb(app, true, -30f, 0.8f, 1.2f)
        com.example.data.PanelPreferences.setInactiveCompArcUseCustom(app, true, false)

        com.example.data.PanelPreferences.setCompArcTrackMode(app, true, com.example.data.ComplicationArcMonetMode.PRIMARY)
        com.example.data.PanelPreferences.setCompArcTrackUseCustom(app, true, true)
        com.example.data.PanelPreferences.setCompArcTrackCustomColor(app, true, 0xFF223344.toInt())

        assertEquals(com.example.data.ComplicationArcMonetMode.TERTIARY, com.example.data.PanelPreferences.getActiveCompArcMode(true).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.DEFAULT, com.example.data.PanelPreferences.getActiveCompArcMode(false).value)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.getActiveCompArcUseCustom(true).value)
        org.junit.Assert.assertFalse(com.example.data.PanelPreferences.getActiveCompArcUseCustom(false).value)
        assertEquals(0xFF00E5FF.toInt(), com.example.data.PanelPreferences.getActiveCompArcCustomColor(true).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.SECONDARY, com.example.data.PanelPreferences.getInactiveCompArcMode(true).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.DEFAULT, com.example.data.PanelPreferences.getInactiveCompArcMode(false).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.PRIMARY, com.example.data.PanelPreferences.getCompArcTrackMode(true).value)
        assertEquals(0xFF223344.toInt(), com.example.data.PanelPreferences.getCompArcTrackCustomColor(true).value)

        // Re-initialize from SharedPreferences and confirm persistence
        com.example.data.PanelPreferences.init(app)
        assertEquals(com.example.data.ComplicationArcMonetMode.TERTIARY, com.example.data.PanelPreferences.getActiveCompArcMode(true).value)
        assertEquals(45f, com.example.data.PanelPreferences.getActiveCompArcHue(true).value, 0.001f)
        assertEquals(1.4f, com.example.data.PanelPreferences.getActiveCompArcSat(true).value, 0.001f)
        assertEquals(0.9f, com.example.data.PanelPreferences.getActiveCompArcVal(true).value, 0.001f)
        assertEquals(0xFF00E5FF.toInt(), com.example.data.PanelPreferences.getActiveCompArcCustomColor(true).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.SECONDARY, com.example.data.PanelPreferences.getInactiveCompArcMode(true).value)
        assertEquals(-30f, com.example.data.PanelPreferences.getInactiveCompArcHue(true).value, 0.001f)
        assertEquals(com.example.data.ComplicationArcMonetMode.PRIMARY, com.example.data.PanelPreferences.getCompArcTrackMode(true).value)
        assertEquals(0xFF223344.toInt(), com.example.data.PanelPreferences.getCompArcTrackCustomColor(true).value)

        // Verify syncElementColors copies Dark -> Light cleanly
        com.example.data.PanelPreferences.syncElementColors(app, fromDark = true)
        assertEquals(com.example.data.ComplicationArcMonetMode.TERTIARY, com.example.data.PanelPreferences.getActiveCompArcMode(false).value)
        assertEquals(45f, com.example.data.PanelPreferences.getActiveCompArcHue(false).value, 0.001f)
        org.junit.Assert.assertTrue(com.example.data.PanelPreferences.getActiveCompArcUseCustom(false).value)
        assertEquals(0xFF00E5FF.toInt(), com.example.data.PanelPreferences.getActiveCompArcCustomColor(false).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.SECONDARY, com.example.data.PanelPreferences.getInactiveCompArcMode(false).value)
        assertEquals(com.example.data.ComplicationArcMonetMode.PRIMARY, com.example.data.PanelPreferences.getCompArcTrackMode(false).value)
        assertEquals(0xFF223344.toInt(), com.example.data.PanelPreferences.getCompArcTrackCustomColor(false).value)

        // Restore defaults
        for (isDark in listOf(false, true)) {
            com.example.data.PanelPreferences.setActiveCompArcMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setActiveCompArcHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setActiveCompArcUseCustom(app, isDark, false)
            com.example.data.PanelPreferences.setInactiveCompArcMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setInactiveCompArcHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setInactiveCompArcUseCustom(app, isDark, false)
            com.example.data.PanelPreferences.setCompArcTrackMode(app, isDark, com.example.data.ComplicationArcMonetMode.DEFAULT)
            com.example.data.PanelPreferences.setCompArcTrackHsb(app, isDark, 0f, 1f, 1f)
            com.example.data.PanelPreferences.setCompArcTrackUseCustom(app, isDark, false)
        }
    }
}




