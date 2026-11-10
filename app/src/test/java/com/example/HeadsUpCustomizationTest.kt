package com.example

import com.example.data.HeadsUpAction
import com.example.service.HeadsUpSnoozeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HeadsUpCustomizationTest {

    @Before
    fun setup() {
        HeadsUpSnoozeManager.clearSnooze()
    }

    @Test
    fun testHeadsUpActionFromId() {
        assertEquals(HeadsUpAction.DISMISS, HeadsUpAction.fromId("dismiss"))
        assertEquals(HeadsUpAction.HIDE_BANNER, HeadsUpAction.fromId("hide_banner"))
        assertEquals(HeadsUpAction.OPEN_SHADE, HeadsUpAction.fromId("open_shade"))
        assertEquals(HeadsUpAction.OPEN_NOTIFICATION, HeadsUpAction.fromId("open_notification"))
        assertEquals(HeadsUpAction.OPEN_APP, HeadsUpAction.fromId("open_app"))
        assertEquals(HeadsUpAction.EXPAND_NOTIFICATION, HeadsUpAction.fromId("expand_notification"))
        assertEquals(HeadsUpAction.SNOOZE_APP, HeadsUpAction.fromId("snooze_app"))
        assertEquals(HeadsUpAction.SNOOZE_ALL, HeadsUpAction.fromId("snooze_all"))
        assertEquals(HeadsUpAction.NONE, HeadsUpAction.fromId("none"))
        assertEquals(HeadsUpAction.NONE, HeadsUpAction.fromId("unknown_xyz"))
    }

    @Test
    fun testIsSnoozeAction() {
        assertTrue(HeadsUpAction.SNOOZE_APP.isSnoozeAction)
        assertTrue(HeadsUpAction.SNOOZE_ALL.isSnoozeAction)
        assertFalse(HeadsUpAction.DISMISS.isSnoozeAction)
        assertFalse(HeadsUpAction.OPEN_NOTIFICATION.isSnoozeAction)
        assertFalse(HeadsUpAction.OPEN_SHADE.isSnoozeAction)
    }

    @Test
    fun testAppLevelSnooze() {
        val pkg = "com.whatsapp"
        assertFalse(HeadsUpSnoozeManager.isSnoozed(pkg))

        HeadsUpSnoozeManager.snoozePackage(pkg, 15)
        assertTrue(HeadsUpSnoozeManager.isSnoozed(pkg))
        assertFalse(HeadsUpSnoozeManager.isSnoozed("com.telegram"))

        // Calls and alarms MUST bypass snooze
        assertFalse(HeadsUpSnoozeManager.isSnoozed(pkg, isCall = true))
        assertFalse(HeadsUpSnoozeManager.isSnoozed(pkg, isAlarm = true))

        HeadsUpSnoozeManager.clearSnooze(pkg)
        assertFalse(HeadsUpSnoozeManager.isSnoozed(pkg))
    }

    @Test
    fun testGlobalSnooze() {
        assertFalse(HeadsUpSnoozeManager.isGlobalSnoozed())

        HeadsUpSnoozeManager.snoozeAll(30)
        assertTrue(HeadsUpSnoozeManager.isGlobalSnoozed())
        assertTrue(HeadsUpSnoozeManager.isSnoozed("com.twitter"))
        assertTrue(HeadsUpSnoozeManager.isSnoozed("com.slack"))

        // Calls and alarms MUST bypass global snooze
        assertFalse(HeadsUpSnoozeManager.isSnoozed("com.google.android.dialer", isCall = true))
        assertFalse(HeadsUpSnoozeManager.isSnoozed("com.google.android.deskclock", isAlarm = true))

        HeadsUpSnoozeManager.clearSnooze()
        assertFalse(HeadsUpSnoozeManager.isGlobalSnoozed())
        assertFalse(HeadsUpSnoozeManager.isSnoozed("com.twitter"))
    }
}
