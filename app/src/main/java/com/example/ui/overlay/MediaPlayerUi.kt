package com.example.ui.overlay

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.toArgb
import android.graphics.Bitmap
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.PanelPreferences
import com.example.data.QsSliderTarget
import com.example.ui.components.QsProgressSlider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawOutline
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
import com.example.R
import com.example.model.MediaState

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
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    val fallbackArtwork = remember {
        try {
            android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.example.R.drawable.album_art_kygo_1790704645442
            )
        } catch (e: Exception) {
            null
        }
    }
    val effectiveArtwork = mediaState.artworkBitmap ?: fallbackArtwork

    val dominantColor = remember(effectiveArtwork) {
        effectiveArtwork?.let {
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
        if (effectiveArtwork != null) {
            Image(
                bitmap = effectiveArtwork.asImageBitmap(),
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
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    val playPauseColor = if (isDark) Color.White else Color.Black.copy(alpha = 0.8f)
    val progressBarStyle by PanelPreferences.progressBarStyle.collectAsState()

    val context = LocalContext.current
    val fallbackArtwork = remember {
        try {
            android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.example.R.drawable.album_art_kygo_1790704645442
            )
        } catch (e: Exception) {
            null
        }
    }
    val artworkBitmap = currentHeaderState.artworkBitmap ?: fallbackArtwork
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

    val enablePanelShadows by PanelPreferences.enablePanelShadows.collectAsState()
    val panelShadowElevation by PanelPreferences.panelShadowElevation.collectAsState()
    val panelShadowOffsetY by PanelPreferences.panelShadowOffsetY.collectAsState()
    val panelShadowAlpha by PanelPreferences.panelShadowAlpha.collectAsState()

    val shadowModifier = if (enablePanelShadows && panelShadowElevation > 0f) {
        Modifier
            .drawBehind {
                val shadowRadius = panelShadowElevation.dp.toPx()
                val offsetY = panelShadowOffsetY.dp.toPx()
                if (shadowRadius > 0f) {
                    drawIntoCanvas { canvas ->
                        val paint = androidx.compose.ui.graphics.Paint()
                        val frameworkPaint = paint.asFrameworkPaint()
                        frameworkPaint.color = android.graphics.Color.argb((panelShadowAlpha * 255).toInt(), 0, 0, 0)
                        frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                            shadowRadius,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                        canvas.save()
                        canvas.translate(0f, offsetY)
                        canvas.drawOutline(
                            outline = cardShape.createOutline(size, layoutDirection, this),
                            paint = paint
                        )
                        canvas.restore()
                    }
                }
            }
            .graphicsLayer {
                shadowElevation = panelShadowElevation.dp.toPx()
                this.shape = cardShape
                clip = false
                ambientShadowColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = panelShadowAlpha)
                spotShadowColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = panelShadowAlpha)
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(shadowModifier)
    ) {
        Surface(
            shape = cardShape,
            color = cardColor,
            modifier = Modifier
                .fillMaxWidth()
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
                            sliderTarget = QsSliderTarget.MEDIA,
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
                                            if (cardMedia.isFavorite) com.example.R.drawable.ic_heart_filled else com.example.R.drawable.ic_heart_outline
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

/**
 * Android Q Beta 2 Style Music Player Notification Card (Placed inside Notifications Container)
 */
@Composable
fun AndroidQMediaNotificationCard(
    mediaState: MediaState,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    onPlayPauseClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onRepeatClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isSeekBarVisible by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var localProgress by remember(mediaState.progress) { mutableFloatStateOf(mediaState.progress) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    // Dynamic background and precise accents extracted from album art using Palette
    val fallbackArtwork = remember {
        try {
            android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.example.R.drawable.album_art_kygo_1790704645442
            )
        } catch (e: Exception) {
            null
        }
    }
    val artwork = mediaState.artworkBitmap ?: fallbackArtwork
    val palette = remember(artwork) {
        artwork?.let {
            Palette.from(it)
                .maximumColorCount(32)
                .clearFilters()
                .generate()
        }
    }
    val extractedColors = remember(artwork, palette) {
        extractAndroidQNotificationColors(artwork, palette)
    }
    val cardBgColor = extractedColors.backgroundColor
    val accentColor = extractedColors.accentColor
    val subTextColor = remember(accentColor) {
        accentColor.copy(alpha = 0.80f)
    }

    // Dynamic App Source & Icon
    val displayAppName = remember(mediaState.appName, mediaState.packageName) {
        if (mediaState.appName.isNotBlank() && mediaState.appName != "MUSER") {
            mediaState.appName
        } else if (mediaState.packageName.isNotBlank()) {
            try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(mediaState.packageName, 0)
                pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                "Music Player"
            }
        } else {
            "Music Player"
        }
    }

    val appIconBitmap = remember(mediaState.packageName) {
        if (mediaState.packageName.isNotBlank()) {
            try {
                val pm = context.packageManager
                val drawable = pm.getApplicationIcon(mediaState.packageName)
                val bmp = Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(48),
                    drawable.intrinsicHeight.coerceAtLeast(48),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val displayTitle = mediaState.title.ifBlank { "Track Title" }
    val displayArtist = mediaState.artist.ifBlank { "" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onCardClick() }
            .testTag("android_q_media_notification_card"),
        shape = shape,
        color = cardBgColor,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Album art covering the WHOLE right side, without any extra paddings
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(130.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    if (artwork != null) {
                        Image(
                            bitmap = artwork.asImageBitmap(),
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            cardBgColor,
                                            cardBgColor.copy(alpha = 0.8f),
                                            cardBgColor.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = accentColor.copy(alpha = 0.35f),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Top Bar: App Badge + Name + Hide/Show Seekbar Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap.asImageBitmap(),
                                contentDescription = displayAppName,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = cardBgColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Text(
                            text = displayAppName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Arrow toggles seekbar visibility
                    IconButton(
                        onClick = { isSeekBarVisible = !isSeekBarVisible },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("android_q_media_toggle_seekbar_btn")
                    ) {
                        Icon(
                            imageVector = if (isSeekBarVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isSeekBarVisible) "Hide Seekbar" else "Show Seekbar",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 2. Song Title & Artist
                Column(
                    modifier = Modifier.fillMaxWidth(0.68f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = displayTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (displayArtist.isNotBlank()) {
                        Text(
                            text = displayArtist,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Playback Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(0.72f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamically shown Shuffle button
                    if (mediaState.supportsShuffle) {
                        IconButton(
                            onClick = onShuffleClick,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("android_q_media_shuffle_btn")
                        ) {
                            val isShuffleActive = mediaState.shuffleMode != 0 && mediaState.shuffleMode != -1
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleActive) accentColor else subTextColor.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Previous Button
                    IconButton(
                        onClick = onPreviousClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("android_q_media_prev_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Play / Pause Button
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("android_q_media_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onNextClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("android_q_media_next_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Dynamically shown Repeat button
                    if (mediaState.supportsRepeat) {
                        IconButton(
                            onClick = onRepeatClick,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("android_q_media_repeat_btn")
                        ) {
                            val isRepeatActive = mediaState.repeatMode != 0 && mediaState.repeatMode != -1
                            Icon(
                                imageVector = if (mediaState.repeatMode == 1) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (isRepeatActive) accentColor else subTextColor.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Dynamically shown Like button
                    if (mediaState.supportsFavorite) {
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("android_q_media_like_btn")
                        ) {
                            Icon(
                                imageVector = if (mediaState.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (mediaState.isFavorite) "Liked" else "Like",
                                tint = if (mediaState.isFavorite) accentColor else subTextColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 4. Collapsible Progress Slider & Timestamps (Full Width on top of artwork)
                AnimatedVisibility(
                    visible = isSeekBarVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        val displayProgress = if (isDraggingSlider) localProgress else mediaState.progress.coerceIn(0f, 1f)

                        QsProgressSlider(
                            value = displayProgress,
                            onValueChange = {
                                localProgress = it
                                onSeek(it)
                            },
                            sliderTarget = QsSliderTarget.MEDIA,
                            isPlaying = mediaState.isPlaying,
                            activeColor = accentColor,
                            inactiveColor = accentColor.copy(alpha = 0.25f),
                            thumbColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("android_q_media_progress_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currMs = if (isDraggingSlider) (displayProgress * mediaState.totalDurationMs).toLong() else mediaState.currentPositionMs
                            Text(
                                text = MediaState.formatDuration(currMs),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Text(
                                text = if (mediaState.totalDurationMs > 0) mediaState.formattedTotalTime else "--:--",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MediaNotificationColors(
    val backgroundColor: Color,
    val accentColor: Color
)

/**
 * Extracts precise background and accent colors for the Android Q media notification design.
 *
 * Rules:
 * 1. Background color (backgroundColor):
 *    - Accurately captures artwork canvas / dominant tone.
 *    - Black canvas (e.g. black with red artwork) -> pure black / dark canvas.
 *    - White canvas (e.g. white with blue artwork) -> pure white / light canvas.
 *    - Preserves gradient blend with artwork left edge.
 * 2. Accent color (accentColor for labels, buttons, seekbar):
 *    - When artwork has a single colorful accent (e.g. red on black, or blue on white), extracts that accent.
 *    - When artwork has multiple distinct colorful hues (e.g. Kygo with cyan & yellow), extracts the second most colorful color.
 *    - Enforces contrast: bright & vivid on dark backgrounds, deep & saturated on light backgrounds.
 */
fun extractAndroidQNotificationColors(
    bitmap: Bitmap?,
    palette: Palette?
): MediaNotificationColors {
    val cardBgColor = extractNotificationBackgroundColor(bitmap, palette)
    val bgArgb = cardBgColor.toArgb()
    val bgLuminance = ColorUtils.calculateLuminance(bgArgb)
    val isDarkBg = bgLuminance < 0.5f

    val accentColor = extractNotificationAccentColor(palette, isDarkBg, bgArgb)

    return MediaNotificationColors(
        backgroundColor = cardBgColor,
        accentColor = accentColor
    )
}

fun extractNotificationBackgroundColor(bitmap: Bitmap?, palette: Palette?): Color {
    var edgeLuminanceAvg: Float? = null
    var edgeSampleCount = 0
    var edgeLumSum = 0.0

    if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
        try {
            val swBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            if (swBitmap != null) {
                val stepY = maxOf(1, swBitmap.height / 8)
                for (y in 0 until swBitmap.height step stepY) {
                    val pixel = swBitmap.getPixel(0, y)
                    val alpha = (pixel ushr 24) and 0xFF
                    if (alpha >= 50) {
                        edgeLumSum += ColorUtils.calculateLuminance(pixel)
                        edgeSampleCount++
                    }
                }
                val cornerTopRight = swBitmap.getPixel(swBitmap.width - 1, 0)
                val cornerBottomRight = swBitmap.getPixel(swBitmap.width - 1, swBitmap.height - 1)
                val alphaTR = (cornerTopRight ushr 24) and 0xFF
                val alphaBR = (cornerBottomRight ushr 24) and 0xFF
                if (alphaTR >= 50) {
                    edgeLumSum += ColorUtils.calculateLuminance(cornerTopRight)
                    edgeSampleCount++
                }
                if (alphaBR >= 50) {
                    edgeLumSum += ColorUtils.calculateLuminance(cornerBottomRight)
                    edgeSampleCount++
                }

                if (edgeSampleCount > 0) {
                    edgeLuminanceAvg = (edgeLumSum / edgeSampleCount).toFloat()
                }
            }
        } catch (_: Exception) {
            // Safe fallback if bitmap sampling fails
        }
    }

    val dominantSwatch = palette?.dominantSwatch

    // If edge sampling clearly indicates black canvas (luminance < 0.08)
    if (edgeLuminanceAvg != null && edgeLuminanceAvg < 0.08f) {
        return Color(0xFF000000)
    }
    // If edge sampling clearly indicates white canvas (luminance > 0.88)
    if (edgeLuminanceAvg != null && edgeLuminanceAvg > 0.88f) {
        return Color(0xFFFFFFFF)
    }

    if (dominantSwatch != null) {
        val domLum = ColorUtils.calculateLuminance(dominantSwatch.rgb).toFloat()
        // Pure black or near-black dominant canvas
        if (domLum < 0.04f) {
            val r = android.graphics.Color.red(dominantSwatch.rgb)
            val g = android.graphics.Color.green(dominantSwatch.rgb)
            val b = android.graphics.Color.blue(dominantSwatch.rgb)
            if (r <= 14 && g <= 14 && b <= 14) {
                return Color(0xFF000000)
            }
            return Color(dominantSwatch.rgb)
        }
        // Pure white or near-white dominant canvas
        if (domLum > 0.88f) {
            val r = android.graphics.Color.red(dominantSwatch.rgb)
            val g = android.graphics.Color.green(dominantSwatch.rgb)
            val b = android.graphics.Color.blue(dominantSwatch.rgb)
            if (r >= 242 && g >= 242 && b >= 242) {
                return Color(0xFFFFFFFF)
            }
            return Color(dominantSwatch.rgb)
        }

        // Mid-tones: check edge average if available
        if (edgeLuminanceAvg != null) {
            if (edgeLuminanceAvg < 0.12f) return Color(0xFF000000)
            if (edgeLuminanceAvg > 0.82f) return Color(0xFFFFFFFF)
        }

        val domHsl = dominantSwatch.hsl
        return if (domHsl[2] < 0.22f) {
            Color(dominantSwatch.rgb)
        } else if (domHsl[2] > 0.78f) {
            Color(dominantSwatch.rgb)
        } else {
            palette.darkMutedSwatch?.rgb?.let { Color(it) }
                ?: palette.mutedSwatch?.rgb?.let { Color(it) }
                ?: Color(dominantSwatch.rgb)
        }
    }

    return Color(0xFF121212)
}

fun extractNotificationAccentColor(
    palette: Palette?,
    isDarkBg: Boolean,
    bgArgb: Int
): Color {
    if (palette == null) {
        return if (isDarkBg) Color(0xFFE5B15D) else Color(0xFF1A56DB)
    }

    val swatches = palette.swatches
    if (swatches.isEmpty()) {
        val fallbackInt = if (isDarkBg) {
            palette.vibrantSwatch?.rgb ?: palette.lightVibrantSwatch?.rgb ?: 0xFFE5B15D.toInt()
        } else {
            palette.darkVibrantSwatch?.rgb ?: palette.vibrantSwatch?.rgb ?: 0xFF1A56DB.toInt()
        }
        return Color(fallbackInt)
    }

    // Filter swatches that have noticeable saturation and some contrast with the background
    val colorfulCandidates = swatches.filter { swatch ->
        val s = swatch.hsl[1]
        val contrast = ColorUtils.calculateContrast(swatch.rgb, bgArgb)
        s >= 0.15f && contrast >= 1.5
    }.sortedByDescending { it.hsl[1] } // highest saturation first

    if (colorfulCandidates.isEmpty()) {
        // Monochrome fallback
        return if (isDarkBg) Color(0xFFF2F2F2) else Color(0xFF1C1C1E)
    }

    val mostColorful = colorfulCandidates.first()

    // Check for a second distinct colorful hue (>= 25 degrees hue difference)
    val distinctHueCandidates = colorfulCandidates.filter { swatch ->
        val h1 = mostColorful.hsl[0]
        val h2 = swatch.hsl[0]
        val diff = kotlin.math.abs(h1 - h2)
        val hueDistance = kotlin.math.min(diff, 360f - diff)
        hueDistance >= 25f
    }

    // If multiple distinct colorful hues exist (like Kygo), choose the second distinct colorful hue.
    // Otherwise (like black+red or white+blue where all colorful pixels share the same hue family),
    // choose that primary colorful hue!
    val chosenSwatch = if (distinctHueCandidates.isNotEmpty()) {
        distinctHueCandidates.first()
    } else {
        mostColorful
    }

    // Ensure legibility and contrast against the card background:
    // Dark bg -> bright, vivid accent (lightness >= 0.50)
    // Light bg -> deep, saturated accent (lightness <= 0.42)
    val hsl = chosenSwatch.hsl.clone()
    if (isDarkBg) {
        if (hsl[2] < 0.48f) {
            hsl[2] = 0.52f
        }
        if (hsl[1] < 0.65f) {
            hsl[1] = 0.75f
        }
    } else {
        if (hsl[2] > 0.44f) {
            hsl[2] = 0.40f
        }
        if (hsl[1] < 0.65f) {
            hsl[1] = 0.75f
        }
    }

    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * Extracts the second most colorful (vibrant and distinct) color from the Palette.
 * Maintained as a compatibility helper.
 */
fun extractSecondMostColorfulColor(palette: Palette?): Color {
    return extractNotificationAccentColor(palette, isDarkBg = true, bgArgb = android.graphics.Color.BLACK)
}
