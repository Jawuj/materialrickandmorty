package com.example.materialrickandmorty.ui.screens.pick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.ui.theme.CardContainer
import com.example.materialrickandmorty.ui.theme.CardText
import com.example.materialrickandmorty.ui.theme.IconPurple

@Composable
fun EpisodeCard(
    episode: Episode,
    isFavorite: Boolean = false,
    isWatched: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null,
    onWatchedToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${episode.episode} - ${episode.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IconPurple,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onWatchedToggle != null) {
                        IconButton(onClick = onWatchedToggle, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isWatched) Icons.Filled.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Watched",
                                tint = if (isWatched) IconPurple else CardText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (onFavoriteToggle != null) {
                        IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = IconPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = IconPurple,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = CardText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Release: ${episode.airDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = null,
                    tint = CardText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Characters in the episode: ${episode.characters.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardText
                )
            }
        }
    }
}
