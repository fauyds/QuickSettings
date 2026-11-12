package com.example.ui.customizer

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private enum class ColorPickerTab { PRESETS, CUSTOM }

// Preset color swatches matching standard Android/Material palette
private val PRESET_COLORS = listOf(
    0xFFF44336.toInt(), // Red
    0xFFE91E63.toInt(), // Pink
    0xFF9C27B0.toInt(), // Purple
    0xFF673AB7.toInt(), // Deep Purple
    0xFF3F51B5.toInt(), // Indigo
    0xFF2196F3.toInt(), // Blue
    0xFF03A9F4.toInt(), // Light Blue
    0xFF00BCD4.toInt(), // Cyan
    0xFF009688.toInt(), // Teal
    0xFF4CAF50.toInt(), // Green
    0xFF8BC34A.toInt(), // Light Green
    0xFFCDDC39.toInt(), // Lime
    0xFFFFEB3B.toInt(), // Yellow
    0xFFFFC107.toInt(), // Amber
    0xFFFF9800.toInt(), // Orange
    0xFFFF5722.toInt(), // Deep Orange
    0xFF795548.toInt(), // Brown
    0xFF9E9E9E.toInt(), // Grey
    0xFF607D8B.toInt()  // Blue Grey
)

/**
 * Two-Tab Material Color Picker (Presets & Custom HSV/Chroma/Tone sliders).
 * - Tab 1: Clean swatch grid without outlines or hex badge. Selected swatch has no white outline.
 * - Tab 2: Wide color preview banner without '+' icon, smaller labels, Hue/Chroma/Tone sliders with numeric input.
 */
@Composable
fun TwoTabColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ColorPickerTab.PRESETS) }
    val isDark = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    // Convert Int color to HSV components
    val hsv = remember(selectedColor) {
        FloatArray(3).also { AndroidColor.colorToHSV(selectedColor, it) }
    }
    var hue by remember(selectedColor) { mutableFloatStateOf(hsv[0]) }
    var sat by remember(selectedColor) { mutableFloatStateOf(hsv[1]) }
    var valFactor by remember(selectedColor) { mutableFloatStateOf(hsv[2]) }

    val hexString = remember(selectedColor) {
        String.format("#%06X", (0xFFFFFF and selectedColor))
    }

    var showHexDialog by remember { mutableStateOf(false) }
    var hexInputText by remember { mutableStateOf(hexString.removePrefix("#")) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = activeTab == ColorPickerTab.PRESETS,
                onClick = { activeTab = ColorPickerTab.PRESETS },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Presets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = activeTab == ColorPickerTab.CUSTOM,
                onClick = { activeTab = ColorPickerTab.CUSTOM },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Custom", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        // TAB 1: PRESETS (No outlines, no hex badge)
        if (activeTab == ColorPickerTab.PRESETS) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptInFlowGrid(
                    colors = PRESET_COLORS,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected
                )
            }
        }

        // TAB 2: CUSTOM (Banner without +, smaller labels)
        if (activeTab == ColorPickerTab.CUSTOM) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Wide Color Preview Banner (No "+" icon)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(selectedColor))
                        .clickable {
                            hexInputText = String.format("%06X", (0xFFFFFF and selectedColor))
                            showHexDialog = true
                        }
                )

                // Custom Color Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom color",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hexString.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            hexInputText = String.format("%06X", (0xFFFFFF and selectedColor))
                            showHexDialog = true
                        }
                    )
                }

                // 1. HUE SLIDER
                val currentHueInt = hue.roundToInt().coerceIn(0, 360)
                CustomHsvSliderRow(
                    label = "Hue",
                    value = hue,
                    valueRange = 0f..360f,
                    displayValue = currentHueInt,
                    trackBrush = Brush.horizontalGradient(
                        listOf(
                            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                    ),
                    onValueChange = { newHue ->
                        hue = newHue
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                        onColorSelected(newColor)
                    },
                    onValueTextEntered = { textVal ->
                        val parsed = textVal.toIntOrNull()?.coerceIn(0, 360)
                        if (parsed != null) {
                            hue = parsed.toFloat()
                            val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                            onColorSelected(newColor)
                        }
                    }
                )

                // 2. CHROMA / SATURATION SLIDER
                val currentChromaInt = (sat * 100f).roundToInt().coerceIn(0, 100)
                val desaturatedColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0f, valFactor)))
                val saturatedColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, valFactor)))
                CustomHsvSliderRow(
                    label = "Chroma",
                    value = sat * 100f,
                    valueRange = 0f..100f,
                    displayValue = currentChromaInt,
                    trackBrush = Brush.horizontalGradient(listOf(desaturatedColor, saturatedColor)),
                    onValueChange = { newChroma ->
                        sat = (newChroma / 100f).coerceIn(0f, 1f)
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                        onColorSelected(newColor)
                    },
                    onValueTextEntered = { textVal ->
                        val parsed = textVal.toIntOrNull()?.coerceIn(0, 100)
                        if (parsed != null) {
                            sat = parsed / 100f
                            val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                            onColorSelected(newColor)
                        }
                    }
                )

                // 3. TONE / BRIGHTNESS SLIDER
                val currentToneInt = (valFactor * 100f).roundToInt().coerceIn(0, 100)
                val darkToneColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, 0f)))
                val midToneColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, 0.5f)))
                val brightToneColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, 1.0f)))
                CustomHsvSliderRow(
                    label = "Tone",
                    value = valFactor * 100f,
                    valueRange = 0f..100f,
                    displayValue = currentToneInt,
                    trackBrush = Brush.horizontalGradient(listOf(darkToneColor, midToneColor, brightToneColor)),
                    onValueChange = { newTone ->
                        valFactor = (newTone / 100f).coerceIn(0f, 1f)
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                        onColorSelected(newColor)
                    },
                    onValueTextEntered = { textVal ->
                        val parsed = textVal.toIntOrNull()?.coerceIn(0, 100)
                        if (parsed != null) {
                            valFactor = parsed / 100f
                            val newColor = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valFactor))
                            onColorSelected(newColor)
                        }
                    }
                )
            }
        }
    }

    // Hex Code Editor Dialog
    if (showHexDialog) {
        AlertDialog(
            onDismissRequest = { showHexDialog = false },
            title = { Text("Enter Hex Color", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                        hexInputText = filtered
                    },
                    prefix = { Text("#", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        try {
                            val parsedColor = AndroidColor.parseColor("#$hexInputText")
                            onColorSelected(parsedColor)
                            showHexDialog = false
                        } catch (_: Exception) {}
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val parsedColor = AndroidColor.parseColor("#$hexInputText")
                            onColorSelected(parsedColor)
                            showHexDialog = false
                        } catch (_: Exception) {}
                    }
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHexDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Grid of circular swatches with selection ring (no white outline).
 */
@Composable
private fun OptInFlowGrid(
    colors: List<Int>,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val rows = colors.chunked(6)
        rows.forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowColors.forEach { colorInt ->
                    val isSelected = (colorInt == selectedColor)
                    SwatchCircle(
                        colorInt = colorInt,
                        isSelected = isSelected,
                        onClick = { onColorSelected(colorInt) }
                    )
                }
                if (rowColors.size < 6) {
                    repeat(6 - rowColors.size) {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SwatchCircle(
    colorInt: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Outer ring only (no inner white outline)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(3.dp, Color(colorInt), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(colorInt))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(colorInt))
            )
        }
    }
}

/**
 * Custom Slider Row with Gradient Track and smaller label and numeric input box.
 */
@Composable
private fun CustomHsvSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: Int,
    trackBrush: Brush,
    onValueChange: (Float) -> Unit,
    onValueTextEntered: (String) -> Unit
) {
    var textValue by remember(displayValue) { mutableStateOf(displayValue.toString()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smaller label
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Numeric Input Box
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.width(52.dp)
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(3)
                        textValue = digits
                        onValueTextEntered(digits)
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.height(34.dp)
                )
            }
        }

        // Custom Slider over Gradient Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Gradient Track Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(trackBrush)
            )

            // Slider
            Slider(
                value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFBD7E20),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
