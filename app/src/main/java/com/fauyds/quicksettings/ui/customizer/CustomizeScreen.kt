package com.fauyds.quicksettings.ui.customizer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomizeScreen(modifier: Modifier = Modifier) {
    // null means user is on the Category Buttons Hub screen
    var selectedCategory by remember { mutableStateOf<CustomizeCategory?>(null) }

    // Intercept back button when inside a category to return to the category buttons hub
    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.fauyds.quicksettings.ui.theme.getSettingsScreenBackgroundColor(com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current))
    ) {
        if (selectedCategory == null) {
            // ==========================================
            // SCREEN 1: CATEGORY BUTTONS HUB (NO TABS)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing) // 4dp padding between cards
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 6.dp, start = 4.dp, end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CUSTOMIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select a category to customize Quick Settings",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 8 Category Cards with 4dp spacing & 5dp inside corners
                CategoryButton(
                    category = CustomizeCategory.HEADS_UP,
                    itemCount = 1,
                    shape = CustomizeCardShapes.TopCard,
                    onClick = { selectedCategory = CustomizeCategory.HEADS_UP }
                )

                CategoryButton(
                    category = CustomizeCategory.BASIC,
                    itemCount = 1,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.BASIC }
                )

                CategoryButton(
                    category = CustomizeCategory.QS_DESIGN,
                    itemCount = 7,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.QS_DESIGN }
                )

                CategoryButton(
                    category = CustomizeCategory.QS_TWEAKS,
                    itemCount = 5,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.QS_TWEAKS }
                )

                CategoryButton(
                    category = CustomizeCategory.DEBUG,
                    itemCount = 1,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.DEBUG }
                )

                CategoryButton(
                    category = CustomizeCategory.TRIGGER,
                    itemCount = 5,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.TRIGGER }
                )

                CategoryButton(
                    category = CustomizeCategory.GESTURES,
                    itemCount = 1,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.GESTURES }
                )

                CategoryButton(
                    category = CustomizeCategory.PERFORMANCE,
                    itemCount = 3,
                    shape = CustomizeCardShapes.CenterCard,
                    onClick = { selectedCategory = CustomizeCategory.PERFORMANCE }
                )

                CategoryButton(
                    category = CustomizeCategory.ABOUT,
                    itemCount = 1,
                    shape = CustomizeCardShapes.BottomCard,
                    onClick = { selectedCategory = CustomizeCategory.ABOUT }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        } else {
            // ==========================================
            // SCREEN 2: DEDICATED CATEGORY VIEW (NO TABS)
            // ==========================================
            val activeCategory = selectedCategory!!
            val categoryScrollState = rememberScrollState()
            val isScrolledToTop = categoryScrollState.value == 0

            // Top Header: Back Button and Dynamic Title
            // When scrolled down, title appears in the top app bar row beside the back button (like Android Settings)
            // When scrolled to the very top, header expands with large title below the back button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.fauyds.quicksettings.ui.theme.getSettingsScreenBackgroundColor(com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current))
            ) {
                // Top Row: Back button and collapsed title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isDark = com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current
                    // Circle Back Button in corner
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark))
                            .clickable(onClick = { selectedCategory = null })
                            .testTag("back_to_categories_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Collapsed title shown only when scrolled down (not at very top)
                    AnimatedVisibility(
                        visible = !isScrolledToTop,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = activeCategory.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Expanded Section: Shown only when scrolled to the very top
                AnimatedVisibility(
                    visible = isScrolledToTop,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        // Large Category Title (as seen in Android Settings)
                        Text(
                            text = activeCategory.title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 38.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Scrollable Content for Active Category Only
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(categoryScrollState),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing) // 4dp padding
            ) {
                when (activeCategory) {
                    CustomizeCategory.HEADS_UP -> HeadsUpSection()
                    CustomizeCategory.BASIC -> BasicSettingsSection()
                    CustomizeCategory.QS_DESIGN -> QsDesignSection()
                    CustomizeCategory.QS_TWEAKS -> QsTweaksSection()
                    CustomizeCategory.DEBUG -> DebugSection()
                    CustomizeCategory.TRIGGER -> TriggerSection()
                    CustomizeCategory.GESTURES -> GesturesSection()
                    CustomizeCategory.PERFORMANCE -> PerformanceSection()
                    CustomizeCategory.ABOUT -> AboutSection()
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Category Button Card displayed on the main Customize screen
 * (Icons in circles on left, inside corners 5dp, 4dp spacing, no item indicator on right)
 */
@Composable
fun CategoryButton(
    category: CustomizeCategory,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CustomizeCardShapes.SingleCard
) {
    val isDark = com.fauyds.quicksettings.ui.theme.LocalIsDarkTheme.current
    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = com.fauyds.quicksettings.ui.theme.getSettingsCardBackgroundColor(isDark)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_btn_${category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon in circle on the left reverted back
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Category Title & Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.description,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron Arrow on the right
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
