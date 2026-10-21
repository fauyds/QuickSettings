package com.example.ui.customizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val telegramUrl = "https://t.me/allphonecustomization"

    val pinnedIds by PinnedCardsManager.getPinnedCardIds(CustomizeCategory.ABOUT, context).collectAsState()

    val appHeaderCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("about_app_header_card", CustomizeCategory.ABOUT) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("about_app_header_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "QuickSettings Logo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "QuickSettings",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Version 1.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    Text(
                        text = "QuickSettings is a modern, highly customizable control center replacement for Android. Personalize tile shapes, custom status bar triggers, brightness sliders, notification overlays, and gesture controls with fluid Material 3 design.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }
        }
    }

    val developerCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("about_developer_card", CustomizeCategory.ABOUT) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("about_developer_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Developer",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Developer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = "Maxim Tarna",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = "Creator & Lead Developer",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }
    }

    val telegramCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("about_telegram_card", CustomizeCategory.ABOUT) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("about_telegram_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF29B6F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Telegram Channel",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Telegram Channel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(
                                text = "t.me/allphonecustomization",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }

                    Text(
                        text = "Join our Telegram community for custom themes, setup guides, exclusive updates, and feedback!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                runCatching { uriHandler.openUri(telegramUrl) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_telegram_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0288D1),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Telegram",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Join Channel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Telegram Channel", telegramUrl)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("copy_telegram_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    val featuresCard = @Composable { shape: androidx.compose.ui.graphics.Shape ->
        PinnableCardWrapper("about_features_card", CustomizeCategory.ABOUT) {
            SettingsCard(
                shape = shape,
                modifier = Modifier.testTag("about_features_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Key Features",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )

                    val features = listOf(
                        "Material 3 custom tile shapes, squircles, & custom path SVGs",
                        "Customizable status bar trigger strip & gesture interception",
                        "Wavy or standard Material 3 brightness sliders",
                        "Heads-Up notification banner overlays & notification manager",
                        "Root & Shizuku privilege engine for deep system tile integration"
                    )

                    features.forEach { feature ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = feature,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(CustomizeCardShapes.CardSpacing)
    ) {
        // 1. Pinned Cards Section at the very top of About
        val pinnedCards = mutableListOf<@Composable (androidx.compose.ui.graphics.Shape) -> Unit>()
        if (pinnedIds.contains("about_app_header_card")) pinnedCards.add(appHeaderCard)
        if (pinnedIds.contains("about_developer_card")) pinnedCards.add(developerCard)
        if (pinnedIds.contains("about_telegram_card")) pinnedCards.add(telegramCard)
        if (pinnedIds.contains("about_features_card")) pinnedCards.add(featuresCard)

        if (pinnedCards.isNotEmpty()) {
            ListSubheader(
                text = "Pinned",
                icon = androidx.compose.material.icons.Icons.Default.PushPin
            )
            pinnedCards.forEachIndexed { index, card ->
                card(CustomizeCardShapes.getShapeForIndex(index, pinnedCards.size))
            }
        }

        // 2. Unpinned App Information
        if (!pinnedIds.contains("about_app_header_card")) {
            CollapsibleSubheaderSection(
                title = "App Information",
                icon = Icons.Default.Info
            ) {
                appHeaderCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 3. Unpinned Developer & Creator
        if (!pinnedIds.contains("about_developer_card")) {
            CollapsibleSubheaderSection(
                title = "Developer & Creator",
                icon = Icons.Default.Person
            ) {
                developerCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 4. Unpinned Community & Support
        if (!pinnedIds.contains("about_telegram_card")) {
            CollapsibleSubheaderSection(
                title = "Community & Support",
                icon = Icons.Default.Send
            ) {
                telegramCard(CustomizeCardShapes.SingleCard)
            }
        }

        // 5. Unpinned Features Overview
        if (!pinnedIds.contains("about_features_card")) {
            CollapsibleSubheaderSection(
                title = "Features Overview",
                icon = Icons.Default.Star
            ) {
                featuresCard(CustomizeCardShapes.SingleCard)
            }
        }
    }
}


