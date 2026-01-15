@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maxrave.domain.mediaservice.handler.ControlState
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.ui.theme.CookieShape
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.viewModel.UIEvent

@Composable
fun PlayerControlLayout(
    controllerState: ControlState,
    isSmallSize: Boolean = false,
    onUIEvent: (UIEvent) -> Unit,
) {
    val height = if (isSmallSize) 48.dp else 96.dp
    val smallIcon = if (isSmallSize) 20.dp to 28.dp else 32.dp to 42.dp
    val mediumIcon = if (isSmallSize) 28.dp to 38.dp else 42.dp to 52.dp
    val bigIcon = if (isSmallSize) 38.dp to 48.dp else 72.dp to 96.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 20.dp),
    ) {
        // Shuffle
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { onUIEvent(UIEvent.Shuffle) },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(smallIcon.second).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = controllerState.isShuffle, label = "Shuffle Button") { isShuffle ->
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            tint = if (isShuffle) seed else Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(smallIcon.first),
                        )
                    }
                }
            }
        }
        
        // Previous
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { if (controllerState.isPreviousAvailable) onUIEvent(UIEvent.Previous) },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(mediumIcon.second).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        tint = if (controllerState.isPreviousAvailable) Color.White else Color.Gray,
                        contentDescription = "",
                        modifier = Modifier.size(mediumIcon.first),
                    )
                }
            }
        }

        // Play/Pause
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { onUIEvent(UIEvent.PlayPause) },
                shape = CookieShape,
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(bigIcon.second).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = controllerState.isPlaying) { isPlaying ->
                        Icon(
                            imageVector = if (!isPlaying) Icons.Rounded.PlayCircle else Icons.Rounded.PauseCircle,
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    }
                }
            }
        }

        // Next
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { if (controllerState.isNextAvailable) onUIEvent(UIEvent.Next) },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(mediumIcon.second).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        tint = if (controllerState.isNextAvailable) Color.White else Color.Gray,
                        contentDescription = "",
                        modifier = Modifier.size(mediumIcon.first),
                    )
                }
            }
        }

        // Repeat
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { onUIEvent(UIEvent.Repeat) },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(smallIcon.second).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Crossfade(targetState = controllerState.repeatState) { rs ->
                        when (rs) {
                            is RepeatState.None -> {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    tint = Color.White,
                                    contentDescription = "",
                                    modifier = Modifier.size(smallIcon.first),
                                )
                            }
                            RepeatState.All -> {
                                Icon(
                                    imageVector = Icons.Rounded.Repeat,
                                    tint = seed,
                                    contentDescription = "",
                                    modifier = Modifier.size(smallIcon.first),
                                )
                            }
                            RepeatState.One -> {
                                Icon(
                                    imageVector = Icons.Rounded.RepeatOne,
                                    tint = seed,
                                    contentDescription = "",
                                    modifier = Modifier.size(smallIcon.first),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}