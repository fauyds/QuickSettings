package com.example.ui.customizer

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
import androidx.compose.material3.ButtonDefaults
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
            .background(MaterialTheme.colorScheme.background)
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CUSTOMIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select a category to customize Quick Settings",
                        fontSize = 13.sp,
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
                    shape = CustomizeCardShapes.BottomCard,
                    onClick = { selectedCategory = CustomizeCategory.PERFORMANCE }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        } else {
            // ==========================================
            // SCREEN 2: DEDICATED CATEGORY VIEW (NO TABS)
            // ==========================================
            val activeCategory = selectedCategory!!

            // Clean Top Bar with Back Button & Category Title (No tabs bar!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back Button to Categories Hub
                FilledTonalButton(
                    onClick = { selectedCategory = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("back_to_categories_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Categories",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active Category Title
                Text(
                    text = activeCategory.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            // Scrollable Content for Active Category Only
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing) // 4dp padding
            ) {
                CategorySectionHeader(
                    category = activeCategory,
                    itemCount = when (activeCategory) {
                        CustomizeCategory.HEADS_UP -> 1
                        CustomizeCategory.BASIC -> 1
                        CustomizeCategory.QS_DESIGN -> 7
                        CustomizeCategory.QS_TWEAKS -> 5
                        CustomizeCategory.DEBUG -> 1
                        CustomizeCategory.TRIGGER -> 5
                        CustomizeCategory.GESTURES -> 1
                        CustomizeCategory.PERFORMANCE -> 3
                    }
                )

                when (activeCategory) {
                    CustomizeCategory.HEADS_UP -> HeadsUpSection()
                    CustomizeCategory.BASIC -> BasicSettingsSection()
                    CustomizeCategory.QS_DESIGN -> QsDesignSection()
                    CustomizeCategory.QS_TWEAKS -> QsTweaksSection()
                    CustomizeCategory.DEBUG -> DebugSection()
                    CustomizeCategory.TRIGGER -> TriggerSection()
                    CustomizeCategory.GESTURES -> GesturesSection()
                    CustomizeCategory.PERFORMANCE -> PerformanceSection()
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
    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Category Title & Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.description,
                    fontSize = 12.sp,
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
