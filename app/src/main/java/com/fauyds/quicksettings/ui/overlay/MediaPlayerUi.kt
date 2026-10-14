package com.fauyds.quicksettings.ui.overlay

import androidx.core.graphics.ColorUtils
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.fauyds.quicksettings.data.PanelPreferences
import com.fauyds.quicksettings.ui.components.QsProgressSlider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fauyds.quicksettings.R
import com.fauyds.quicksettings.model.MediaState

/**
 * Album Artwork Display Component with Kygo / Kids In Love asset fallback
 */
@Composable
fun AlbumArtworkView(
    mediaState: MediaState,
    size: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(cornerRadius)

    val dominantColor = remember(mediaState.artworkBitmap) {
        mediaState.artworkBitmap?.let {
            Palette.from(it).generate().dominantSwatch?.rgb?.let { color -> Color(color) }
        } ?: Color(0xFF2E3440) // Fallback
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(dominantColor),
        contentAlignment = Alignment.Center
    ) {
        if (mediaState.artworkBitmap != null) {
            Image(
                bitmap = mediaState.artworkBitmap.asImageBitmap(),
                contentDescription = "Album Artwork",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4C566A),
                                Color(0xFF2E3440)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mediaState.isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                    contentDescription = "Music Artwork",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(size * 0.48f)
                )
            }
        }
    }
}

/**
 * Android QS/QQS Adaptive Media Player Component:
 * - In QQS (Compact mode): Single horizontal row below header with small artwork, title/artist, and 3 controls [Prev, Play/Pause, Next].
 * - In QS (Expanded mode): Rich layout with larger artwork, track metadata, Material You progress slider, timestamps, and 4 controls [Prev, Play/Pause, Next, Favorite].
 */
@Composable
fun AdaptiveMediaPlayerCard(
    mediaState: MediaState,
    isExpanded: Boolean,
    sessions: List<MediaState> = emptyList(),
    selectedIndex: Int = 0,
    onSelectSession: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0xFF353A3B),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onPlayPauseClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onRepeatClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onOutputDeviceClick: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(cornerRadius)
    val activeSessions = if (sessions.isNotEmpty()) sessions else listOf(mediaState)
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = selectedIndex.coerceIn(0 until activeSessions.size),
        pageCount = { activeSessions.size }
    )

    LaunchedEffect(selectedIndex) {
        if (activeSessions.size > 1 && selectedIndex in activeSessions.indices && pagerState.currentPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (activeSessions.size > 1 && pagerState.currentPage in activeSessions.indices && pagerState.currentPage != selectedIndex) {
            onSelectSession(pagerState.currentPage)
        }
    }

    val currentHeaderState = activeSessions.getOrElse(pagerState.currentPage.coerceIn(0 until activeSessions.size)) { mediaState }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val playPauseColor = if (isDark) Color.White else Color.Black.copy(alpha = 0.8f)
    val progressBarStyle by PanelPreferences.progressBarStyle.collectAsState()

    val artworkBitmap = currentHeaderState.artworkBitmap
    val artworkColor = remember(artworkBitmap, isDark) {
        artworkBitmap?.let {
            Palette.from(it).generate().dominantSwatch?.rgb?.let { colorInt ->
                if (!isDark) {
                    // Light mode: Lighten all by 85%
                    Color(ColorUtils.blendARGB(colorInt, AndroidColor.WHITE, 0.85f))
                } else {
                    // Dark mode: Darken light colors (>0.5) by 70%
                    val luminance = ColorUtils.calculateLuminance(colorInt)
                    if (luminance > 0.5) {
                        Color(ColorUtils.blendARGB(colorInt, AndroidColor.BLACK, 0.7f))
                    } else {
                        Color(colorInt)
                    }
                }
            }
        }
    }

    val cardColor = artworkColor ?: backgroundColor

    Surface(
        shape = cardShape,
        color = cardColor,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
            .clip(cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCardClick
            )
            .testTag(if (isExpanded) "expanded_media_card" else "compact_media_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = if (isExpanded) 4.dp else 4.dp,
                    bottom = if (isExpanded) 6.dp else 5.dp
                )
        ) {
            // 1. Top Header Row: Music Note + App Name & Output Switcher Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App identity (Music note icon + MUSER)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music App",
                        tint = contentColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(if (isExpanded) 15.dp else 14.dp)
                    )
                    Text(
                        text = currentHeaderState.appName.uppercase(),
                        fontSize = if (isExpanded) 12.sp else 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = contentColor.copy(alpha = 0.9f)
                    )
                }

                // Output Switcher Pill ("Phone speaker")
                Surface(
                    onClick = onOutputDeviceClick,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.testTag("media_output_switcher")
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isExpanded) 8.dp else 7.dp,
                            vertical = if (isExpanded) 2.5.dp else 2.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (currentHeaderState.isHeadphonesOrBluetooth) Icons.Outlined.Headphones else Icons.Outlined.PhoneAndroid,
                            contentDescription = "Audio Output",
                            tint = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(if (isExpanded) 12.dp else 11.dp)
                        )
                        Text(
                            text = currentHeaderState.outputDevice,
                            fontSize = if (isExpanded) 11.sp else 10.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isExpanded) 1.dp else 2.dp))

            // 2. Horizontal Pager with horizontal drag capture & smooth snapping
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                        }
                    },
                userScrollEnabled = activeSessions.size > 1,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
            ) { pageIdx ->
                val cardMedia = activeSessions.getOrElse(pageIdx) { mediaState }

                if (!isExpanded) {
                    // COMPACT / QQS MODE: Single Horizontal Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AlbumArtworkView(
                            mediaState = cardMedia,
                            size = 48.dp,
                            cornerRadius = 10.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cardMedia.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = cardMedia.artist,
                                fontSize = 12.5.sp,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onPreviousClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("compact_media_prev")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = contentColor.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = onPlayPauseClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("compact_media_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (cardMedia.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (cardMedia.isPlaying) "Pause" else "Play",
                                    tint = playPauseColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = onNextClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("compact_media_skip_next")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Skip Next",
                                    tint = contentColor.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                } else {
                    // EXPANDED / FULL QS MODE
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AlbumArtworkView(
                                mediaState = cardMedia,
                                size = 52.dp,
                                cornerRadius = 10.dp
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = cardMedia.title,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = cardMedia.artist,
                                    fontSize = 12.5.sp,
                                    color = contentColor.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val inactiveTrackColor = contentColor.copy(alpha = 0.22f)
                        QsProgressSlider(
                            value = cardMedia.progress.coerceIn(0f, 1f),
                            onValueChange = onSeek,
                            style = progressBarStyle,
                            isPlaying = cardMedia.isPlaying,
                            activeColor = accentColor,
                            inactiveColor = inactiveTrackColor,
                            thumbColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("media_progress_slider")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cardMedia.formattedCurrentTime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentColor.copy(alpha = 0.75f)
                            )
                            Text(
                                text = cardMedia.formattedTotalTime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentColor.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (cardMedia.supportsShuffle) {
                                val isShuffleActive = cardMedia.shuffleMode != 0 && cardMedia.shuffleMode != -1
                                IconButton(
                                    onClick = onShuffleClick,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("media_control_shuffle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = if (isShuffleActive) accentColor else contentColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onPreviousClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("media_control_previous")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    tint = contentColor.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = onPlayPauseClick,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("media_control_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (cardMedia.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (cardMedia.isPlaying) "Pause" else "Play",
                                    tint = playPauseColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = onNextClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("media_control_next")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = contentColor.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            if (cardMedia.supportsRepeat) {
                                val isRepeatOne = cardMedia.repeatMode == 1
                                val isRepeatActive = cardMedia.repeatMode != 0 && cardMedia.repeatMode != -1
                                IconButton(
                                    onClick = onRepeatClick,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("media_control_repeat")
                                ) {
                                    Icon(
                                        imageVector = if (isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                        contentDescription = "Repeat",
                                        tint = if (isRepeatActive) accentColor else contentColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            if (cardMedia.supportsFavorite) {
                                IconButton(
                                    onClick = onFavoriteClick,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("media_control_favorite")
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(
                                            if (cardMedia.isFavorite) com.fauyds.quicksettings.R.drawable.ic_heart_filled else com.fauyds.quicksettings.R.drawable.ic_heart_outline
                                        ),
                                        contentDescription = if (cardMedia.isFavorite) "Liked" else "Like",
                                        tint = if (cardMedia.isFavorite) accentColor else contentColor.copy(alpha = 0.9f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Repositioned Session Indicator Dots to Bottom Layout (Below controls)
            if (activeSessions.size > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeSessions.forEachIndexed { idx, _ ->
                            val isSelected = idx == pagerState.currentPage
                            val chipWidth = if (isSelected) 18.dp else 6.dp
                            val chipColor = if (isSelected) accentColor else contentColor.copy(alpha = 0.35f)

                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(chipWidth)
                                    .clip(CircleShape)
                                    .background(chipColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(idx)
                                        }
                                        onSelectSession(idx)
                                    }
                                    .testTag("media_session_dot_$idx")
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Expanded Media Card wrapper for backwards-compatibility and direct usage.
 */
@Composable
fun ExpandedMediaCard(
    mediaState: MediaState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color(0xFF353A3B),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onPlayPauseClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onRepeatClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onOutputDeviceClick: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    AdaptiveMediaPlayerCard(
        mediaState = mediaState,
        isExpanded = true,
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor,
        accentColor = accentColor,
        contentColor = contentColor,
        onPlayPauseClick = onPlayPauseClick,
        onPreviousClick = onPreviousClick,
        onNextClick = onNextClick,
        onFavoriteClick = onFavoriteClick,
        onRepeatClick = onRepeatClick,
        onShuffleClick = onShuffleClick,
        onSeek = onSeek,
        onOutputDeviceClick = onOutputDeviceClick,
        onCardClick = onCardClick
    )
}

/**
 * Standard Compact Media Card wrapper.
 */
@Composable
fun CompactMediaTile(
    mediaState: MediaState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0xFF353A3B),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onPlayPauseClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onTileClick: () -> Unit = {}
) {
    AdaptiveMediaPlayerCard(
        mediaState = mediaState,
        isExpanded = false,
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor,
        accentColor = accentColor,
        contentColor = contentColor,
        onPlayPauseClick = onPlayPauseClick,
        onPreviousClick = onPreviousClick,
        onNextClick = onNextClick,
        onCardClick = onTileClick
    )
}
