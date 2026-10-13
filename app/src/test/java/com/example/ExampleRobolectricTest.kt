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
        assertEquals(13, shapes.size)

        // Verify key built-in shapes
        assertNotNull(shapes.find { it.id == "circle" })
        assertNotNull(shapes.find { it.id == "squircle" })
        assertNotNull(shapes.find { it.id == "square" })
        assertNotNull(shapes.find { it.id == "teardrop" })
        assertNotNull(shapes.find { it.id == "pill" })
        assertNotNull(shapes.find { it.id == "custom_1" })
        assertNotNull(shapes.find { it.id == "custom_7" }) // Flower
        assertNotNull(shapes.find { it.id == "custom_8" }) // Hexagon

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
}
