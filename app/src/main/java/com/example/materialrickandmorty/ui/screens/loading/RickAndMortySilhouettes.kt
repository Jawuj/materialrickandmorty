package com.example.materialrickandmorty.ui.screens.loading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.materialrickandmorty.ui.theme.DarkBackground

@Composable
fun RickAndMortySilhouettes(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8B63C7),
    eyeColor: Color = DarkBackground
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val width = size.width
        val height = size.height

        // Rick Spiky Silhouette
        val rickPath = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.55f)
            lineTo(width * 0.05f, height * 0.42f)
            lineTo(width * 0.08f, height * 0.48f)
            lineTo(width * 0.12f, height * 0.30f)
            lineTo(width * 0.16f, height * 0.40f)
            lineTo(width * 0.22f, height * 0.20f)
            lineTo(width * 0.26f, height * 0.35f)
            lineTo(width * 0.32f, height * 0.18f)
            lineTo(width * 0.36f, height * 0.38f)
            lineTo(width * 0.40f, height * 0.28f)
            lineTo(width * 0.44f, height * 0.45f)
            cubicTo(
                width * 0.46f, height * 0.55f,
                width * 0.42f, height * 0.70f,
                width * 0.40f, height * 0.85f
            )
            lineTo(width * 0.42f, height)
            close()
        }
        drawPath(path = rickPath, color = color, style = Fill)

        // Rick unibrow/eye cutouts
        drawCircle(
            color = eyeColor,
            radius = width * 0.025f,
            center = Offset(width * 0.18f, height * 0.52f)
        )
        drawCircle(
            color = eyeColor,
            radius = width * 0.025f,
            center = Offset(width * 0.28f, height * 0.52f)
        )

        // Morty Silhouette
        val mortyPath = Path().apply {
            moveTo(width * 0.25f, height)
            cubicTo(
                width * 0.28f, height * 0.82f,
                width * 0.32f, height * 0.65f,
                width * 0.35f, height * 0.58f
            )
            cubicTo(
                width * 0.33f, height * 0.32f,
                width * 0.75f, height * 0.32f,
                width * 0.78f, height * 0.58f
            )
            cubicTo(
                width * 0.80f, height * 0.65f,
                width * 0.88f, height * 0.82f,
                width * 0.95f, height
            )
            close()
        }
        drawPath(path = mortyPath, color = color, style = Fill)

        // Morty eyes
        val mortyEye1Center = Offset(width * 0.48f, height * 0.64f)
        val mortyEye2Center = Offset(width * 0.68f, height * 0.64f)
        val eyeRadius = width * 0.08f

        drawCircle(color = eyeColor, radius = eyeRadius, center = mortyEye1Center)
        drawCircle(color = eyeColor, radius = eyeRadius, center = mortyEye2Center)

        // Eyebrows
        val browPath1 = Path().apply {
            moveTo(width * 0.40f, height * 0.52f)
            lineTo(width * 0.54f, height * 0.55f)
        }
        val browPath2 = Path().apply {
            moveTo(width * 0.76f, height * 0.52f)
            lineTo(width * 0.62f, height * 0.55f)
        }
        drawPath(browPath1, color = eyeColor, style = Stroke(width = 8f))
        drawPath(browPath2, color = eyeColor, style = Stroke(width = 8f))

        // Sad mouth arc
        val mouthPath = Path().apply {
            moveTo(width * 0.52f, height * 0.78f)
            cubicTo(
                width * 0.55f, height * 0.74f,
                width * 0.61f, height * 0.74f,
                width * 0.64f, height * 0.78f
            )
        }
        drawPath(mouthPath, color = eyeColor, style = Stroke(width = 8f))
    }
}
