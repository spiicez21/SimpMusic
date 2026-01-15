@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.CookieShape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import simpmusic.composeapp.generated.resources.*

@Composable
fun RippleIconButton(
    resId: DrawableResource,
    modifier: Modifier = Modifier,
    fillMaxSize: Boolean = false,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painterResource(resId),
            null,
            tint = tint,
            modifier = if (fillMaxSize) Modifier.fillMaxSize().padding(4.dp) else Modifier,
        )
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CookieShape,
        color = Color.White.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    if (!isPlaying) {
                        Res.drawable.baseline_play_arrow_24
                    } else {
                        Res.drawable.baseline_pause_24
                    }
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}