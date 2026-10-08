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
}
