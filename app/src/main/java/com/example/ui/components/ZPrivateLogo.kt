package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Renders the official zPrivate vector logo directly without any surrounding box or square.
 */
@Composable
fun ZPrivateLogoVector(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_zprivate_logo),
        contentDescription = "Logo officiel zPrivate VPN",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
