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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalTextStyle

@Composable
fun CustomizeScreen(modifier: Modifier = Modifier) {
    // null means user is on the Category Buttons Hub screen
    var selectedCategory by remember { mutableStateOf<CustomizeCategory?>(null) }

    // Intercept back button when inside a category to return to the category buttons hub
    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(com.example.ui.theme.getSettingsScreenBackgroundColor(com.example.ui.theme.LocalIsDarkTheme.current))
        ) {
        if (selectedCategory == null) {
            // ==========================================
            // SCREEN 1: CATEGORY BUTTONS HUB (NO TABS)
            // ==========================================
             val pinnedIds by com.example.data.PanelPreferences.pinnedFeatures.collectAsState()
            var globalSearchQuery by remember { mutableStateOf("") }
            val allCategories = listOf(
                CustomizeCategory.HEADS_UP,
                CustomizeCategory.HEADS_UP_DESIGN,
                CustomizeCategory.BASIC,
                CustomizeCategory.QS_DESIGN,
                CustomizeCategory.QS_TWEAKS,
                CustomizeCategory.THEMES,
                CustomizeCategory.MISCELLANEOUS,
                CustomizeCategory.ABOUT
            )
            val pinnedCategories = remember(pinnedIds) {
                allCategories.filter { pinnedIds.contains("cat_${it.name.lowercase()}") }
            }
            val unpinnedCategories = remember(pinnedIds) {
                allCategories.filter { !pinnedIds.contains("cat_${it.name.lowercase()}") }
            }

            val matchingFeatures = remember(globalSearchQuery) {
                if (globalSearchQuery.isBlank()) emptyList()
                else FavoriteFeature.entries.filter { feat ->
                    feat.title.contains(globalSearchQuery, ignoreCase = true) ||
                        feat.description.contains(globalSearchQuery, ignoreCase = true) ||
                        feat.category.title.contains(globalSearchQuery, ignoreCase = true)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
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
                        text = "Select a category to customize Quick Settings (Hold any category or option to pin it)",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Global Search Bar across all settings and features
                androidx.compose.material3.OutlinedTextField(
                    value = globalSearchQuery,
                    onValueChange = { globalSearchQuery = it },
                    placeholder = { Text("Search all toggles & features...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (globalSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { globalSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        errorBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("global_customize_search_input")
                )

                if (globalSearchQuery.isNotBlank()) {
                    Text(
                        text = "SEARCH RESULTS (${matchingFeatures.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 4.dp)
                    )
                    if (matchingFeatures.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching toggles or features found",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        matchingFeatures.forEachIndexed { index, feat ->
                            val cardShape = CustomizeCardShapes.getShapeForIndex(index, matchingFeatures.size)
                            GenericPinnedFeatureCard(
                                feature = feat,
                                shape = cardShape,
                                modifier = Modifier.testTag("search_result_${feat.id}")
                            )
                        }
                    }
                } else {
                    if (pinnedCategories.isNotEmpty()) {
                        Text(
                            text = "PINNED CATEGORIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
                        )
                        pinnedCategories.forEachIndexed { index, category ->
                            val cardShape = CustomizeCardShapes.getShapeForIndex(index, pinnedCategories.size)
                            CategoryButton(
                                category = category,
                                itemCount = 1,
                                shape = cardShape,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }

                    if (unpinnedCategories.isNotEmpty()) {
                        if (pinnedCategories.isNotEmpty()) {
                            Text(
                                text = "ALL CATEGORIES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
                            )
                        }
                        unpinnedCategories.forEachIndexed { index, category ->
                            val cardShape = CustomizeCardShapes.getShapeForIndex(index, unpinnedCategories.size)
                            CategoryButton(
                                category = category,
                                itemCount = 1,
                                shape = cardShape,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }

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
                    .background(com.example.ui.theme.getSettingsScreenBackgroundColor(com.example.ui.theme.LocalIsDarkTheme.current))
            ) {
                // Top Row: Back button and collapsed title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
                    // Circle Back Button in corner
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.getSettingsCardBackgroundColor(isDark))
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
                verticalArrangement = if (activeCategory == CustomizeCategory.BASIC) Arrangement.Center else Arrangement.spacedBy(CustomizeCardShapes.CardSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (activeCategory) {
                    CustomizeCategory.HEADS_UP -> HeadsUpSection()
                    CustomizeCategory.HEADS_UP_DESIGN -> HeadsUpDesignSection()
                    CustomizeCategory.BASIC -> BasicSettingsSection()
                    CustomizeCategory.QS_DESIGN -> QsDesignSection()
                    CustomizeCategory.QS_TWEAKS -> QsTweaksSection()
                    CustomizeCategory.THEMES -> ThemesSection()
                    CustomizeCategory.MISCELLANEOUS -> MiscellaneousSection()
                    CustomizeCategory.ABOUT -> AboutSection()
                    CustomizeCategory.DEBUG -> MiscellaneousSection()
                    CustomizeCategory.TRIGGER -> MiscellaneousSection()
                    CustomizeCategory.GESTURES -> MiscellaneousSection()
                    CustomizeCategory.PERFORMANCE -> MiscellaneousSection()
                }

                if (activeCategory != CustomizeCategory.BASIC) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
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
    val categoryId = "cat_${category.name.lowercase()}"
    SettingsCard(
        shape = shape,
        onClick = onClick,
        featureId = categoryId,
        featureTitle = category.title,
        modifier = modifier.testTag("category_btn_${category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon in circle on the left
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
