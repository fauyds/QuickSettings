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
}


