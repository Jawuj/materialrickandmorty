package com.example.materialrickandmorty.ui.screens.loading

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialrickandmorty.R
import com.example.materialrickandmorty.ui.theme.CardContainer
import com.example.materialrickandmorty.ui.theme.DarkBackground
import com.example.materialrickandmorty.ui.theme.HeaderTitleText
import com.example.materialrickandmorty.ui.theme.IconPurple

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200)
        )
        onLoadingComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "MATERIAL\nRICK",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    lineHeight = 48.sp,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                ),
                color = HeaderTitleText
            )

            Spacer(modifier = Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .width(220.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = IconPurple,
                trackColor = CardContainer
            )

            Spacer(modifier = Modifier.weight(1.2f))
        }

        // Display rickandmorty.png at the bottom start as requested
        Image(
            painter = painterResource(id = R.drawable.rickandmorty),
            contentDescription = "Rick and Morty",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.75f),
            contentScale = ContentScale.FillWidth
        )
    }
}
