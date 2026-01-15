@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)


package com.maxrave.simpmusic.ui.screen

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.utils.connectArtists
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.expect.ui.drawBackdropCustomShape
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.extension.toResizedBitmap
import com.maxrave.simpmusic.expect.toggleMiniPlayer
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.ExplicitBadge
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.PlayPauseButton
import com.maxrave.simpmusic.ui.component.PlayerControlLayout
import com.maxrave.simpmusic.ui.theme.transparent
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.holder
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.seconds

private const val TAG = "MiniPlayer"

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MiniPlayer(
    modifier: Modifier,
    backdrop: PlatformBackdrop,
    sharedViewModel: SharedViewModel = koinInject(),
    onClose: () -> Unit,
    onClick: () -> Unit,
) {
    val isLiquidGlassEnabled by sharedViewModel.getEnableLiquidGlass().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val timelineState by sharedViewModel.timeline.collectAsStateWithLifecycle()

    val layer = rememberGraphicsLayer()
    val luminanceAnimation = remember { Animatable(0f) }

    val textColor by animateColorAsState(
        targetValue = if (luminanceAnimation.value > 0.6f) Color.Black else Color.White,
        label = "MiniPlayerTextColor",
        animationSpec = tween(500),
    )

    LaunchedEffect(layer, isLiquidGlassEnabled) {
        val buffer = IntArray(25)
        while (isActive && isLiquidGlassEnabled == DataStoreManager.TRUE) {
            try {
                withContext(Dispatchers.Main) {
                    val imageBitmap = layer.toImageBitmap()
                    val thumbnail = imageBitmap.toResizedBitmap(5, 5)
                    thumbnail.readPixels(buffer)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error getting pixels from layer: ${e.localizedMessage}")
            }
            val averageLuminance =
                (0 until 25).sumOf { index ->
                    val color = buffer.get(index)
                    val r = (color shr 16 and 0xFF) / 255f
                    val g = (color shr 8 and 0xFF) / 255f
                    val b = (color and 0xFF) / 255f
                    0.2126 * r + 0.7152 * g + 0.0722 * b
                } / 25
            luminanceAnimation.animateTo(
                averageLuminance.coerceAtMost(0.8).toFloat(),
                tween(500),
            )
            delay(1.seconds)
        }
    }

    val (songEntity, setSongEntity) =
        remember {
            mutableStateOf<SongEntity?>(null)
        }
    val (liked, setLiked) =
        remember {
            mutableStateOf(false)
        }
    val (isPlaying, setIsPlaying) =
        remember {
            mutableStateOf(false)
        }
    val (progress, setProgress) =
        remember {
            mutableFloatStateOf(0f)
        }
    val (isCrossfading, setIsCrossfading) =
        remember {
            mutableStateOf(false)
        }

    val coroutineScope = rememberCoroutineScope()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "",
    )

    // Palette state
    val paletteState = rememberPaletteState()
    val background =
        remember {
            Animatable(Color.DarkGray)
        }

    val offsetX = remember { Animatable(initialValue = 0f) }
    val offsetY = remember { Animatable(0f) }

    var loading by rememberSaveable {
        mutableStateOf(true)
    }

    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(bitmap, songEntity) {
        val bm = bitmap
        if (bm != null) {
            paletteState.generate(bm)
        }
    }

    LaunchedEffect(paletteState.palette) {
        paletteState.palette?.let {
            background.animateTo(it.getColorFromPalette())
        }
    }

    LaunchedEffect(key1 = true) {
        val job1 =
            launch {
                sharedViewModel.nowPlayingState.collect { item ->
                    if (item != null) {
                        setSongEntity(item.songEntity)
                    }
                }
            }
        val job2 =
            launch {
                sharedViewModel.controllerState.collectLatest { state ->
                    setLiked(state.isLiked)
                    setIsPlaying(state.isPlaying)
                    setIsCrossfading(state.isCrossfading)
                }
            }
        val job4 =
            launch {
                sharedViewModel.timeline.collect { timeline ->
                    loading = timeline.loading
                    val prog =
                        if (timeline.total > 0L && timeline.current >= 0L) {
                            timeline.current.toFloat() / timeline.total
                        } else {
                            0f
                        }
                    setProgress(prog)
                }
            }
        job1.join()
        job2.join()
        job4.join()
    }

    if (getPlatform() == Platform.Android) {
        val expressiveShape = RoundedCornerShape(24.dp)
        androidx.compose.material3.Surface(
            shape = if (isLiquidGlassEnabled == DataStoreManager.TRUE) CircleShape else expressiveShape,
            color = if (isLiquidGlassEnabled == DataStoreManager.TRUE) transparent else background.value,
            tonalElevation = 4.dp,
            modifier =
                modifier
                    .then(
                        if (isLiquidGlassEnabled == DataStoreManager.TRUE) {
                            Modifier.drawBackdropCustomShape(backdrop, layer, luminanceAnimation.value, RoundedCornerShape(16.dp))
                        } else {
                            Modifier
                        },
                    ).then(
                        Modifier
                            .clipToBounds()
                            .offset { IntOffset(0, offsetY.value.roundToInt()) }
                            .clip(expressiveShape)
                            .clickable(
                                onClick = onClick,
                            ).pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                    },
                                    onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
                                        if (offsetY.value + dragAmount > 0) {
                                            coroutineScope.launch {
                                                change.consume()
                                                offsetY.animateTo(offsetY.value + 2 * dragAmount)
                                                Logger.w("MiniPlayer", "Dragged ${offsetY.value}")
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            offsetY.animateTo(0f)
                                        }
                                    },
                                    onDragEnd = {
                                        Logger.w("MiniPlayer", "Drag Ended")
                                        coroutineScope.launch {
                                            if (offsetY.value > 70) {
                                                onClose()
                                            }
                                            offsetY.animateTo(0f)
                                        }
                                    },
                                )
                            },
                    ),
        ) {
            Box(modifier = Modifier.fillMaxHeight()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(modifier = Modifier.weight(1F)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                            },
                                            onHorizontalDrag = {
                                                change: PointerInputChange,
                                                dragAmount: Float,
                                                ->
                                                coroutineScope.launch {
                                                    change.consume()
                                                    offsetX.animateTo(offsetX.value + dragAmount * 2)
                                                    Logger.w("MiniPlayer", "Dragged ${offsetX.value}")
                                                }
                                            },
                                            onDragCancel = {
                                                Logger.w("MiniPlayer", "Drag Cancelled")
                                                coroutineScope.launch {
                                                    if (offsetX.value > 200) {
                                                        sharedViewModel.onUIEvent(UIEvent.Previous)
                                                    } else if (offsetX.value < -120) {
                                                        sharedViewModel.onUIEvent(UIEvent.Next)
                                                    }
                                                    offsetX.animateTo(0f)
                                                }
                                            },
                                            onDragEnd = {
                                                Logger.w("MiniPlayer", "Drag Ended")
                                                coroutineScope.launch {
                                                    if (offsetX.value > 200) {
                                                        sharedViewModel.onUIEvent(UIEvent.Previous)
                                                    } else if (offsetX.value < -120) {
                                                        sharedViewModel.onUIEvent(UIEvent.Next)
                                                    }
                                                    offsetX.animateTo(0f)
                                                }
                                            },
                                        )
                                    },
                        ) {
                            AsyncImage(
                                model =
                                    ImageRequest
                                        .Builder(LocalPlatformContext.current)
                                        .data(songEntity?.thumbnails)
                                        .crossfade(550)
                                        .build(),
                                placeholder = painterResource(Res.drawable.holder),
                                error = painterResource(Res.drawable.holder),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                onSuccess = {
                                    bitmap =
                                        it.result.image.toImageBitmap()
                                },
                                modifier =
                                    Modifier
                                        .size(48.dp)
                                        .align(Alignment.CenterVertically)
                                        .clip(
                                            RoundedCornerShape(8.dp),
                                        ),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            AnimatedContent(
                                targetState = songEntity,
                                modifier = Modifier.weight(1F).fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart,
                                transitionSpec = {
                                    // Compare the incoming number with the previous number.
                                    if (targetState != initialState) {
                                        // If the target number is larger, it slides up and fades in
                                        // while the initial (smaller) number slides up and fades out.
                                        (
                                            slideInHorizontally { width ->
                                                width
                                            } + fadeIn()
                                        ).togetherWith(
                                            slideOutHorizontally { width -> +width } + fadeOut(),
                                        )
                                    } else {
                                        // If the target number is smaller, it slides down and fades in
                                        // while the initial number slides down and fades out.
                                        (
                                            slideInHorizontally { width ->
                                                +width
                                            } + fadeIn()
                                        ).togetherWith(
                                            slideOutHorizontally { width -> width } + fadeOut(),
                                        )
                                    }.using(
                                        // Disable clipping since the faded slide-in/out should
                                        // be displayed out of bounds.
                                        SizeTransform(clip = false),
                                    )
                                },
                            ) { target ->
                                if (target != null) {
                                    Column(
                                        Modifier
                                            .wrapContentHeight()
                                            .align(Alignment.CenterVertically),
                                    ) {
                                        Text(
                                            text = (songEntity?.title ?: "").toString(),
                                            style = typo().labelSmall,
                                            color = textColor,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(
                                                        align = Alignment.CenterVertically,
                                                    ).basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        animationMode = MarqueeAnimationMode.Immediately,
                                                    ).focusable(),
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.animation.AnimatedVisibility(visible = songEntity?.isExplicit == true) {
                                                ExplicitBadge(
                                                    modifier =
                                                        Modifier
                                                            .size(20.dp)
                                                            .padding(end = 4.dp)
                                                            .weight(1f),
                                                )
                                            }
                                            Text(
                                                text = (songEntity?.artistName?.connectArtists() ?: ""),
                                                style = typo().bodySmall,
                                                maxLines = 1,
                                                color = textColor,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(
                                                            align = Alignment.CenterVertically,
                                                        ).basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Floating Controls Container
                    androidx.compose.material3.Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            HeartCheckBox(checked = liked, size = 28) {
                                sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Crossfade(targetState = loading, label = "") {
                                if (it) {
                                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = textColor,
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                } else {
                                    PlayPauseButton(isPlaying = isPlaying, modifier = Modifier.size(44.dp)) {
                                        sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.BottomCenter),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Buffering layer
                        LinearWavyProgressIndicator(
                            progress = { if (timelineState.loading) 0f else timelineState.bufferedPercent.toFloat() / 100 },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.4f),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                        // Actual progress layer
                        LinearWavyProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            trackColor = Color.Transparent,
                        )
                    }

                }
            }
        }
    } else {
        val expressiveShape = RoundedCornerShape(24.dp)
        var isSliding by rememberSaveable {
            mutableStateOf(false)
        }
        var sliderValue by rememberSaveable {
            mutableFloatStateOf(0f)
        }
        LaunchedEffect(key1 = timelineState, key2 = isSliding) {
            if (!isSliding) {
                sliderValue =
                    if (timelineState.total > 0L) {
                        timelineState.current.toFloat() * 100 / timelineState.total.toFloat()
                    } else {
                        0f
                    }
            }
        }
        Surface(
            onClick = onClick,
            shape = expressiveShape,
            color = background.value,
            tonalElevation = 6.dp,
            modifier = modifier
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Part 1: Info (Album Art, Title, Artist)
                Box(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(songEntity?.thumbnails)
                                .crossfade(550)
                                .build(),
                            placeholder = painterResource(Res.drawable.holder),
                            error = painterResource(Res.drawable.holder),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            onSuccess = { bitmap = it.result.image.toImageBitmap() },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = (songEntity?.title ?: "").toString(),
                                style = typo().titleMedium,
                                color = textColor,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth().basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    animationMode = MarqueeAnimationMode.Immediately,
                                ).focusable(),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (songEntity?.isExplicit == true) {
                                    ExplicitBadge(Modifier.size(18.dp).padding(end = 4.dp))
                                }
                                Text(
                                    text = (songEntity?.artistName?.connectArtists() ?: ""),
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    color = textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth().basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        animationMode = MarqueeAnimationMode.Immediately,
                                    ).focusable(),
                                )
                            }
                        }
                    }
                }

                // Part 2: Floating Controls & Progress
                Box(modifier = Modifier.weight(1.3f), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.width(440.dp).padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.12f),
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            PlayerControlLayout(
                                controllerState,
                                isSmallSize = true,
                            ) {
                                sharedViewModel.onUIEvent(it)
                            }
                        }
                        
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatDuration((timelineState.total * (sliderValue / 100f)).roundToLong()),
                                style = typo().labelSmall,
                                color = textColor,
                                textAlign = TextAlign.Left,
                                modifier = Modifier.width(45.dp),
                            )
                            
                            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        // Buffering layer
                                        LinearWavyProgressIndicator(
                                            progress = { if (timelineState.loading) 0f else timelineState.bufferedPercent.toFloat() / 100 },
                                            modifier = Modifier.fillMaxSize(),
                                            color = textColor.copy(alpha = 0.3f),
                                            trackColor = textColor.copy(alpha = 0.1f),
                                        )
                                        // Actual progress layer
                                        LinearWavyProgressIndicator(
                                            progress = { if (timelineState.total > 0) timelineState.current.toFloat() / timelineState.total.toFloat() else 0f },
                                            modifier = Modifier.fillMaxSize(),
                                            color = textColor,
                                            trackColor = Color.Transparent,
                                        )
                                    }

                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                    Slider(
                                        value = sliderValue,
                                        onValueChange = { isSliding = true; sliderValue = it },
                                        onValueChangeFinished = {
                                            isSliding = false
                                            sharedViewModel.onUIEvent(UIEvent.UpdateProgress(sliderValue))
                                        },
                                        valueRange = 0f..100f,
                                        track = { sliderState ->
                                            SliderDefaults.Track(
                                                modifier = Modifier.height(6.dp),
                                                sliderState = sliderState,
                                                colors = SliderDefaults.colors(
                                                    activeTrackColor = Color.Transparent,
                                                    inactiveTrackColor = Color.Transparent,
                                                ),
                                                thumbTrackGapSize = 0.dp,
                                                drawStopIndicator = null,
                                            )
                                        },
                                        thumb = {
                                            SliderDefaults.Thumb(
                                                interactionSource = remember { MutableInteractionSource() },
                                                modifier = Modifier.size(14.dp),
                                                thumbSize = DpSize(14.dp, 14.dp),
                                                colors = SliderDefaults.colors(thumbColor = textColor),
                                            )
                                        },
                                    )
                                }
                            }
                            
                            Text(
                                text = formatDuration(timelineState.total),
                                style = typo().labelSmall,
                                color = textColor,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.width(45.dp),
                            )
                        }
                    }
                }

                // Part 3: Secondary Actions & Volume
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HeartCheckBox(checked = liked, size = 30) {
                            sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                        }
                        
                        if (getPlatform() == Platform.Desktop) {
                            IconButton(onClick = { toggleMiniPlayer() }) {
                                Icon(Icons.Outlined.OpenInNew, "Mini Player", tint = textColor, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Volume
                        var isVolumeSliding by rememberSaveable { mutableStateOf(false) }
                        var volumeValue by rememberSaveable { mutableFloatStateOf(0f) }
                        LaunchedEffect(key1 = controllerState, key2 = isVolumeSliding) {
                            if (!isVolumeSliding) { volumeValue = controllerState.volume }
                        }
                        
                        IconButton(onClick = {
                            val newVolume = if (controllerState.volume > 0f) 0f else 1f
                            sharedViewModel.onUIEvent(UIEvent.UpdateVolume(newVolume))
                        }) {
                            Icon(
                                imageVector = if (controllerState.volume > 0f) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                contentDescription = "Volume",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                            Slider(
                                value = volumeValue,
                                onValueChange = { isVolumeSliding = true; volumeValue = it },
                                onValueChangeFinished = {
                                    isVolumeSliding = false
                                    sharedViewModel.onUIEvent(UIEvent.UpdateVolume(volumeValue.coerceIn(0f, 1f)))
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.width(80.dp),
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        modifier = Modifier.height(4.dp),
                                        sliderState = sliderState,
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = textColor,
                                            inactiveTrackColor = textColor.copy(alpha = 0.2f),
                                        ),
                                        thumbTrackGapSize = 0.dp,
                                        drawStopIndicator = null,
                                    )
                                },
                                thumb = {
                                    SliderDefaults.Thumb(
                                        interactionSource = remember { MutableInteractionSource() },
                                        modifier = Modifier.size(10.dp),
                                        thumbSize = DpSize(10.dp, 10.dp),
                                        colors = SliderDefaults.colors(thumbColor = textColor),
                                    )
                                },
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(onClick = { onClose() }) {
                            Icon(Icons.Rounded.Close, "Close", tint = textColor, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}