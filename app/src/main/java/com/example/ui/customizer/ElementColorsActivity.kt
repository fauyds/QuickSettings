package com.example.ui.customizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PanelPreferences
import com.example.ui.theme.AppSettingsTheme

class ElementColorsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PanelPreferences.init(this)

        setContent {
            AppSettingsTheme {
                ElementColorsScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementColorsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    BackHandler { onBackClick() }

    val surfaceBgColorTertiary by PanelPreferences.surfaceBgColorTertiary.collectAsState()
    val panelContainerColorTertiary by PanelPreferences.panelContainerColorTertiary.collectAsState()
    val headerGradientColorTertiary by PanelPreferences.headerGradientColorTertiary.collectAsState()
    val qsTilesColorTertiary by PanelPreferences.qsTilesColorTertiary.collectAsState()
    val slidersColorTertiary by PanelPreferences.slidersColorTertiary.collectAsState()
    val miscElementsColorTertiary by PanelPreferences.miscElementsColorTertiary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Element Accent Colors",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("element_colors_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description Header Card
            SettingsCard(
                shape = CustomizeCardShapes.SingleCard
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Primary vs Tertiary Accents",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Customize individual UI elements to use either Primary (Switch OFF) or Tertiary (Switch ON) Monet accent colors.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Group of PreferenceCard Toggles
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
            ) {
                // 1. Surface background
                ElementColorPreferenceCard(
                    title = "1. Surface Background",
                    subtitle = "Behind panel containers and background layers",
                    isTertiary = surfaceBgColorTertiary,
                    onCheckedChange = { PanelPreferences.setSurfaceBgColorTertiary(context, it) },
                    shape = CustomizeCardShapes.TopCard,
                    testTag = "activity_surface_bg_tertiary_toggle"
                )

                // 2. Panel containers & notifications
                ElementColorPreferenceCard(
                    title = "2. Panel Containers & Notifications",
                    subtitle = "Main Quick Settings card panels, sections, and notification cards",
                    isTertiary = panelContainerColorTertiary,
                    onCheckedChange = { PanelPreferences.setPanelContainerColorTertiary(context, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_panel_container_tertiary_toggle"
                )

                // 3. Header Gradient
                ElementColorPreferenceCard(
                    title = "3. Header Gradient",
                    subtitle = "Top Monet gradient background accent layer",
                    isTertiary = headerGradientColorTertiary,
                    onCheckedChange = { PanelPreferences.setHeaderGradientColorTertiary(context, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_header_gradient_tertiary_toggle"
                )

                // 4. QS/QQS Tiles
                ElementColorPreferenceCard(
                    title = "4. QS/QQS Tiles",
                    subtitle = "Quick settings active tiles and icons",
                    isTertiary = qsTilesColorTertiary,
                    onCheckedChange = { PanelPreferences.setQsTilesColorTertiary(context, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_qs_tiles_tertiary_toggle"
                )

                // 5. Sliders/progress bars
                ElementColorPreferenceCard(
                    title = "5. Sliders / Progress Bars",
                    subtitle = "Brightness, volume sliders and progress tracks",
                    isTertiary = slidersColorTertiary,
                    onCheckedChange = { PanelPreferences.setSlidersColorTertiary(context, it) },
                    shape = CustomizeCardShapes.CenterCard,
                    testTag = "activity_sliders_tertiary_toggle"
                )

                // 6. Misc elements
                ElementColorPreferenceCard(
                    title = "6. Misc Elements",
                    subtitle = "Page indicators, footer buttons, and status icons",
                    isTertiary = miscElementsColorTertiary,
                    onCheckedChange = { PanelPreferences.setMiscElementsColorTertiary(context, it) },
                    shape = CustomizeCardShapes.BottomCard,
                    testTag = "activity_misc_elements_tertiary_toggle"
                )
            }
        }
    }
}

@Composable
private fun ElementColorPreferenceCard(
    title: String,
    subtitle: String,
    isTertiary: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String
) {
    SettingsCard(
        shape = shape,
        onClick = { onCheckedChange(!isTertiary) },
        modifier = Modifier.testTag("${testTag}_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isTertiary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Text(
                        text = if (isTertiary) "Tertiary Color Accent" else "Primary Color Accent",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTertiary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = isTertiary,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

