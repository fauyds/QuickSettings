package com.fauyds.quicksettings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fauyds.quicksettings.model.PrivilegeMode
import com.fauyds.quicksettings.model.TileCategory
import com.fauyds.quicksettings.model.TileState
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
        com.fauyds.quicksettings.data.PanelPreferences.init(context)

        com.fauyds.quicksettings.data.PanelPreferences.setScreenRecordResolution(context, com.fauyds.quicksettings.data.ScreenRecordResolution.RES_1080P)
        assertEquals(com.fauyds.quicksettings.data.ScreenRecordResolution.RES_1080P, com.fauyds.quicksettings.data.PanelPreferences.screenRecordResolution.value)

        com.fauyds.quicksettings.data.PanelPreferences.setScreenRecordAudioSource(context, com.fauyds.quicksettings.data.ScreenRecordAudioSource.INTERNAL_AND_MIC)
        assertEquals(com.fauyds.quicksettings.data.ScreenRecordAudioSource.INTERNAL_AND_MIC, com.fauyds.quicksettings.data.PanelPreferences.screenRecordAudioSource.value)

        com.fauyds.quicksettings.data.PanelPreferences.setScreenRecordFps(context, com.fauyds.quicksettings.data.ScreenRecordFps.FPS_30)
        assertEquals(com.fauyds.quicksettings.data.ScreenRecordFps.FPS_30, com.fauyds.quicksettings.data.PanelPreferences.screenRecordFps.value)

        com.fauyds.quicksettings.data.PanelPreferences.setScreenRecordShowTouches(context, true)
        assertEquals(true, com.fauyds.quicksettings.data.PanelPreferences.screenRecordShowTouches.value)

        com.fauyds.quicksettings.data.PanelPreferences.setPanelReplacementDelayMs(context, 350)
        assertEquals(350, com.fauyds.quicksettings.data.PanelPreferences.panelReplacementDelayMs.value)
    }

    @Test
    fun `verify app status card shapes dimensions`() {
        assertEquals(16.0f, com.fauyds.quicksettings.ui.status.AppStatusCardShapes.OuterCorner.value, 0.001f)
        assertEquals(5.0f, com.fauyds.quicksettings.ui.status.AppStatusCardShapes.InsideCorner.value, 0.001f)
        assertEquals(4.0f, com.fauyds.quicksettings.ui.status.AppStatusCardShapes.CardSpacing.value, 0.001f)
    }

    @Test
    fun `verify progress bar style preferences and reversions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.data.PanelPreferences.init(context)

        // Switch to STRAIGHT style
        com.fauyds.quicksettings.data.PanelPreferences.setProgressBarStyle(context, com.fauyds.quicksettings.data.ProgressBarStyle.STRAIGHT)
        assertEquals(com.fauyds.quicksettings.data.ProgressBarStyle.STRAIGHT, com.fauyds.quicksettings.data.PanelPreferences.progressBarStyle.value)
        assertEquals(false, com.fauyds.quicksettings.data.PanelPreferences.wavyProgressBars.value)

        // Switch to WAVY style
        com.fauyds.quicksettings.data.PanelPreferences.setProgressBarStyle(context, com.fauyds.quicksettings.data.ProgressBarStyle.WAVY)
        assertEquals(com.fauyds.quicksettings.data.ProgressBarStyle.WAVY, com.fauyds.quicksettings.data.PanelPreferences.progressBarStyle.value)
        assertEquals(true, com.fauyds.quicksettings.data.PanelPreferences.wavyProgressBars.value)

        // Revert back to previous/DEFAULT style
        com.fauyds.quicksettings.data.PanelPreferences.setProgressBarStyle(context, com.fauyds.quicksettings.data.ProgressBarStyle.DEFAULT)
        assertEquals(com.fauyds.quicksettings.data.ProgressBarStyle.DEFAULT, com.fauyds.quicksettings.data.PanelPreferences.progressBarStyle.value)
        assertEquals(false, com.fauyds.quicksettings.data.PanelPreferences.wavyProgressBars.value)
    }

    @Test
    fun `verify app settings theme mode toggle and panel isolation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.data.PanelPreferences.init(context)

        // Set App Settings Theme to LIGHT
        com.fauyds.quicksettings.data.PanelPreferences.setThemeMode(context, com.fauyds.quicksettings.data.ThemeMode.LIGHT)
        assertEquals(com.fauyds.quicksettings.data.ThemeMode.LIGHT, com.fauyds.quicksettings.data.PanelPreferences.themeMode.value)

        // Set App Settings Theme to DARK
        com.fauyds.quicksettings.data.PanelPreferences.setThemeMode(context, com.fauyds.quicksettings.data.ThemeMode.DARK)
        assertEquals(com.fauyds.quicksettings.data.ThemeMode.DARK, com.fauyds.quicksettings.data.PanelPreferences.themeMode.value)

        // Set App Settings Theme to SYSTEM
        com.fauyds.quicksettings.data.PanelPreferences.setThemeMode(context, com.fauyds.quicksettings.data.ThemeMode.SYSTEM)
        assertEquals(com.fauyds.quicksettings.data.ThemeMode.SYSTEM, com.fauyds.quicksettings.data.PanelPreferences.themeMode.value)
    }

    @Test
    fun `verify all built-in QS shapes have XML drawable and valid paths`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.ui.shape.QsShapeManager.init(context)

        val shapes = com.fauyds.quicksettings.ui.shape.QsShapeManager.allShapes.value
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
            val drawable = com.fauyds.quicksettings.ui.shape.QsShapeManager.createDrawable(shape.id, android.graphics.Color.BLUE)
            assertNotNull(drawable)
            drawable.setBounds(0, 0, 100, 100)

            val composeShape = com.fauyds.quicksettings.ui.shape.QsShapeManager.getComposeShape(shape.id)
            assertNotNull(composeShape)
        }
    }

    @Test
    fun `verify importing and deleting custom XML drawable shape`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.ui.shape.QsShapeManager.init(context)

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
        val result = com.fauyds.quicksettings.ui.shape.QsShapeManager.importShapeFromXml(context, starXml, "Star")
        org.junit.Assert.assertTrue(result.isSuccess)
        val importedStar = result.getOrNull()!!
        assertEquals("Star", importedStar.label)
        org.junit.Assert.assertTrue(importedStar.isCustom)

        // 2. Select it as enabled tile shape
        com.fauyds.quicksettings.data.PanelPreferences.setEnabledTileShapeId(context, importedStar.id)
        assertEquals(importedStar.id, com.fauyds.quicksettings.data.PanelPreferences.enabledTileShapeId.value)

        // 3. Test deleting the custom shape
        val deleted = com.fauyds.quicksettings.ui.shape.QsShapeManager.deleteImportedShape(context, importedStar.id)
        org.junit.Assert.assertTrue(deleted)

        // Revert enabled tile shape to circle
        com.fauyds.quicksettings.data.PanelPreferences.setEnabledTileShapeId(context, "circle")
        assertEquals("circle", com.fauyds.quicksettings.data.PanelPreferences.enabledTileShapeId.value)
    }

    @Test
    fun `verify importing and deleting custom shape directly from path data string`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.ui.shape.QsShapeManager.init(context)

        val heartPath = "M 50,88 C 20,68 4,46 4,28 C 4,12 16,4 30,4 C 40,4 47,10 50,18 C 53,10 60,4 70,4 C 84,4 96,12 96,28 C 96,46 80,68 50,88 Z"

        // 1. Import shape by directly pasting path data string
        val result = com.fauyds.quicksettings.ui.shape.QsShapeManager.importShapeFromPathData(
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
        val drawable = com.fauyds.quicksettings.ui.shape.QsShapeManager.createDrawable(importedHeart.id)
        assertNotNull(drawable)
        val composeShape = com.fauyds.quicksettings.ui.shape.QsShapeManager.getComposeShape(importedHeart.id)
        assertNotNull(composeShape)

        // 3. Verify auto-detection of XML vs path data
        val parsedFromPath = com.fauyds.quicksettings.ui.shape.QsShapeManager.parseShapeInput(heartPath, "Heart Auto")
        assertEquals("Heart Auto", parsedFromPath.label)
        assertEquals(heartPath, parsedFromPath.pathData)

        val xmlSnippet = "<vector android:viewportWidth=\"100\" android:viewportHeight=\"100\"><path android:pathData=\"$heartPath\" /></vector>"
        val parsedFromXml = com.fauyds.quicksettings.ui.shape.QsShapeManager.parseShapeInput(xmlSnippet, "Heart Xml")
        assertEquals("Heart Xml", parsedFromXml.label)
        assertEquals(heartPath, parsedFromXml.pathData)

        // 4. Clean up
        val deleted = com.fauyds.quicksettings.ui.shape.QsShapeManager.deleteImportedShape(context, importedHeart.id)
        org.junit.Assert.assertTrue(deleted)
    }

    @Test
    fun `verify tile outline toggle preference and drawable stroke`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.fauyds.quicksettings.data.PanelPreferences.init(context)

        // Initial state should be false
        com.fauyds.quicksettings.data.PanelPreferences.setTileOutlineEnabled(context, false)
        assertEquals(false, com.fauyds.quicksettings.data.PanelPreferences.tileOutlineEnabled.value)

        // Enable 2dp outline
        com.fauyds.quicksettings.data.PanelPreferences.setTileOutlineEnabled(context, true)
        assertEquals(true, com.fauyds.quicksettings.data.PanelPreferences.tileOutlineEnabled.value)

        // Verify stroke properties on QsPathDrawable
        val drawable = com.fauyds.quicksettings.ui.shape.QsShapeManager.createDrawable("circle")
        drawable.setColor(android.graphics.Color.TRANSPARENT)
        drawable.strokeWidth = 1.2f * 2.5f // 1.2dp in px
        drawable.strokeColor = android.graphics.Color.RED
        assertEquals(3.0f, drawable.strokeWidth, 0.001f)
        assertEquals(android.graphics.Color.RED, drawable.strokeColor)

        // Disable outline
        com.fauyds.quicksettings.data.PanelPreferences.setTileOutlineEnabled(context, false)
        assertEquals(false, com.fauyds.quicksettings.data.PanelPreferences.tileOutlineEnabled.value)
    }
}
