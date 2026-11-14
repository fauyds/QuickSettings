package com.example.ui.customizer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationCategoryManager
import com.example.model.NotificationCategory
import com.example.model.NotificationCategoryIcon
import com.example.ui.theme.ExpressiveCardShapes

private data class CategoryPresetTemplate(
    val name: String,
    val icon: NotificationCategoryIcon,
    val keywords: List<String>
)

private val StarterCategoryTemplates = listOf(
    CategoryPresetTemplate("Messages", NotificationCategoryIcon.CHAT, listOf("message", "replied", "sent")),
    CategoryPresetTemplate("Work", NotificationCategoryIcon.WORK, listOf("meeting", "calendar", "slack", "teams")),
    CategoryPresetTemplate("Social", NotificationCategoryIcon.GROUP, listOf("liked", "commented", "mentioned")),
    CategoryPresetTemplate("Finance", NotificationCategoryIcon.WALLET, listOf("payment", "transaction", "otp", "bank")),
    CategoryPresetTemplate("Shopping", NotificationCategoryIcon.SHOPPING, listOf("order", "shipped", "delivered")),
    CategoryPresetTemplate("Media", NotificationCategoryIcon.MOVIE, listOf("playing", "episode", "podcast"))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationCategoriesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        NotificationCategoryManager.init(context)
    }

    val isSortingEnabled by NotificationCategoryManager.isSortingEnabled.collectAsState()
    val categories by NotificationCategoryManager.categories.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<NotificationCategory?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val screenBg = com.example.ui.theme.getSettingsScreenBackgroundColor(isDark)

    if (showCreateDialog || editingCategory != null) {
        NotificationCategoryEditorDialog(
            initialCategory = editingCategory,
            onDismiss = {
                showCreateDialog = false
                editingCategory = null
            },
            onSave = { name, iconId, pkgs, keywords, expandedDefault ->
                val target = editingCategory
                if (target == null) {
                    NotificationCategoryManager.addCategory(
                        context = context,
                        name = name,
                        iconId = iconId,
                        packageNames = pkgs,
                        keywords = keywords,
                        isExpandedByDefault = expandedDefault
                    )
                    Toast.makeText(context, "Created category '$name'", Toast.LENGTH_SHORT).show()
                } else {
                    NotificationCategoryManager.updateCategory(
                        context = context,
                        updated = target.copy(
                            name = name,
                            iconId = iconId,
                            packageNames = pkgs,
                            keywords = keywords,
                            isExpandedByDefault = expandedDefault
                        )
                    )
                    Toast.makeText(context, "Updated category '$name'", Toast.LENGTH_SHORT).show()
                }
                showCreateDialog = false
                editingCategory = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("notification_categories_screen"),
        verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "NOTIFICATION CATEGORIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                color = colorScheme.primary
            )

            Surface(
                color = if (isSortingEnabled && categories.isNotEmpty()) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Text(
                    text = when {
                        !isSortingEnabled -> "Sorting Off"
                        categories.isEmpty() -> "No Categories"
                        else -> "${categories.size} Active"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSortingEnabled && categories.isNotEmpty()) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        // Master Control & New Category Card
        SettingsCard(
            shape = ExpressiveCardShapes.SingleCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Category Grouping",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Group shade notifications into collapsible sections with custom icons.",
                                fontSize = 11.5.sp,
                                lineHeight = 14.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_add_category")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Enable Sorting Toggle inside card (uses appBackground)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(screenBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sort Notifications by Category",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Unmatched notifications appear under \"Other\"",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSortingEnabled,
                        onCheckedChange = { NotificationCategoryManager.setSortingEnabled(context, it) },
                        modifier = Modifier.testTag("switch_category_sorting_enabled")
                    )
                }

                if (categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                NotificationCategoryManager.setAllCategoriesExpanded(context, true)
                                Toast.makeText(context, "Expanded all categories in shade", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                                contentColor = colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_expand_all_categories")
                        ) {
                            Icon(Icons.Default.UnfoldMore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expand All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                NotificationCategoryManager.setAllCategoriesExpanded(context, false)
                                Toast.makeText(context, "Collapsed all categories in shade", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                                contentColor = colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_collapse_all_categories")
                        ) {
                            Icon(Icons.Default.UnfoldLess, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Collapse All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Categories Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CUSTOM CATEGORIES (${categories.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colorScheme.primary
            )
            if (categories.size > 1) {
                Text(
                    text = "Top to bottom priority",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        if (categories.isEmpty()) {
            SettingsCard(
                shape = ExpressiveCardShapes.SingleCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "No Custom Categories Yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Create your own categories with custom icons, assign apps or keywords, and collapse noisy sections in the shade.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = CircleShape,
                        modifier = Modifier.testTag("btn_empty_create_category")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Category", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "OR QUICK-ADD A STARTER CATEGORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = colorScheme.primary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StarterCategoryTemplates.forEach { preset ->
                            Surface(
                                shape = CircleShape,
                                color = screenBg,
                                modifier = Modifier.clickable {
                                    NotificationCategoryManager.addCategory(
                                        context = context,
                                        name = preset.name,
                                        iconId = preset.icon.id,
                                        keywords = preset.keywords,
                                        isExpandedByDefault = true
                                    )
                                    Toast.makeText(context, "Added '${preset.name}' category", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = preset.icon.imageVector,
                                        contentDescription = preset.name,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = preset.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            categories.forEachIndexed { index, category ->
                val cardShape = when {
                    categories.size == 1 -> ExpressiveCardShapes.SingleCard
                    index == 0 -> ExpressiveCardShapes.TopCard
                    index == categories.lastIndex -> ExpressiveCardShapes.BottomCard
                    else -> ExpressiveCardShapes.CenterCard
                }
                val iconEntry = category.resolveIcon()
                val summaryParts = buildList {
                    if (category.packageNames.isNotEmpty()) {
                        val n = category.packageNames.size
                        add("$n ${if (n == 1) "app" else "apps"}")
                    }
                    if (category.keywords.isNotEmpty()) {
                        val k = category.keywords.size
                        add("$k ${if (k == 1) "keyword" else "keywords"}")
                    }
                    if (isEmpty()) {
                        add("No apps or keywords yet")
                    }
                    add(if (category.isExpandedByDefault) "Expanded" else "Collapsed")
                }

                SettingsCard(
                    shape = cardShape,
                    onClick = { editingCategory = category },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_card_${category.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconEntry.imageVector,
                                    contentDescription = category.name,
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = summaryParts.joinToString(" • "),
                                    fontSize = 11.5.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (categories.size > 1) {
                                IconButton(
                                    onClick = {
                                        NotificationCategoryManager.moveCategory(context, index, index - 1)
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.testTag("btn_move_up_${category.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move up",
                                        tint = if (index > 0) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        NotificationCategoryManager.moveCategory(context, index, index + 1)
                                    },
                                    enabled = index < categories.lastIndex,
                                    modifier = Modifier.testTag("btn_move_down_${category.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move down",
                                        tint = if (index < categories.lastIndex) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { editingCategory = category },
                                modifier = Modifier.testTag("btn_edit_category_${category.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit ${category.name}",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    NotificationCategoryManager.deleteCategory(context, category.id)
                                    Toast.makeText(context, "Deleted '${category.name}'", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("btn_delete_category_${category.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete ${category.name}",
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
