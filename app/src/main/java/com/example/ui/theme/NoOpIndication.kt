@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.PanelPreferences
import com.example.data.ThemeMode

val LocalIsDarkTheme = staticCompositionLocalOf { true }

object NoOpIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : androidx.compose.ui.Modifier.Node() {}
    }

    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = DarkMonetPrimary,
    onPrimary = DarkMonetOnPrimary,
    primaryContainer = DarkMonetPrimaryContainer,
    onPrimaryContainer = DarkMonetOnPrimaryContainer,
    secondary = MonetSecondary,
    onSecondary = MonetOnSecondary,
    secondaryContainer = MonetSecondaryContainer,
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = MonetTertiary,
    onTertiary = MonetOnTertiary,
    tertiaryContainer = MonetTertiaryContainer,
    onTertiaryContainer = MonetOnTertiaryContainer,
    background = DarkUiBackground,
    onBackground = Color(0xFFEDF2F7),
    surface = DarkUiSurface,
    onSurface = Color(0xFFEDF2F7),
    surfaceVariant = DarkUiSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAD4E0),
    surfaceContainerHigh = DarkUiSurfaceContainerHigh,
    outline = Color(0xFF4A5568)
)

private val ExpressiveLightColorScheme = lightColorScheme(
    primary = LightMonetPrimary,
    onPrimary = LightMonetOnPrimary,
    primaryContainer = LightMonetPrimaryContainer,
    onPrimaryContainer = LightMonetOnPrimaryContainer,
    secondary = Color(0xFF006684),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBEE9FF),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = Color(0xFF984061),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E001D),
    background = LightUiBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightUiSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightUiSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerHigh = LightUiSurfaceContainerHigh,
    outline = Color(0xFF94A3B8)
)

@Composable
fun QuickPanelTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val userThemeMode by PanelPreferences.themeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()

    val isDark = darkTheme ?: when (userThemeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isDark) {
                base.copy(
                    background = DarkUiBackground,
                    surface = base.surface,
                    surfaceVariant = base.surfaceVariant
                )
            } else {
                base.copy(
                    background = LightUiBackground,
                    surface = base.surface,
                    surfaceVariant = base.surfaceVariant
                )
            }
        }
        isDark -> ExpressiveDarkColorScheme
        else -> ExpressiveLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalIsDarkTheme provides isDark,
            LocalIndication provides NoOpIndication,
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            androidx.compose.material3.LocalRippleConfiguration provides null
        ) {
            content()
        }
    }
}

// Keep backward compatibility name alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    QuickPanelTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
