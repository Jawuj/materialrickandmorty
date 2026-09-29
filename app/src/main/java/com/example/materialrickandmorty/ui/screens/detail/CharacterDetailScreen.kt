package com.example.materialrickandmorty.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.materialrickandmorty.data.model.Character
import com.example.materialrickandmorty.data.model.Episode
import com.example.materialrickandmorty.ui.screens.pick.InfoChip
import com.example.materialrickandmorty.ui.screens.pick.StatusChip
import com.example.materialrickandmorty.ui.theme.CardContainer
import com.example.materialrickandmorty.ui.theme.CardText
import com.example.materialrickandmorty.ui.theme.ChipBackground
import com.example.materialrickandmorty.ui.theme.DarkBackground
import com.example.materialrickandmorty.ui.theme.HeaderTitleText
import com.example.materialrickandmorty.ui.theme.IconPurple
import com.example.materialrickandmorty.ui.theme.StatusAlive
import com.example.materialrickandmorty.ui.theme.StatusDead
import com.example.materialrickandmorty.ui.theme.StatusUnknown
import com.example.materialrickandmorty.ui.viewmodel.RickAndMortyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    viewModel: RickAndMortyViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    LaunchedEffect(characterId) {
        viewModel.loadCharacterDetail(characterId)
    }

    val character = detailState.character
    val isFavorite = character != null && favoriteIds.contains(character.id)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground
    ) { innerPadding ->
        if (detailState.isLoading && character == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CardContainer)
            }
        } else if (detailState.error != null && character == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = detailState.error ?: "Error loading character",
                    color = HeaderTitleText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (character != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top Navigation and Action Bar (Back, Favorite, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardContainer)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconPurple
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(character) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CardContainer)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = IconPurple
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareText = "Rick & Morty Character: ${character.name}\n" +
                                        "Species: ${character.species}\n" +
                                        "Status: ${character.status}\n" +
                                        "Origin: ${character.origin.name}\n" +
                                        "Image: ${character.image}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share character"))
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CardContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = IconPurple
                            )
                        }
                    }
                }

                // Character Image Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardContainer)
                ) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = character.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.1f)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Character Details Card 1
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = IconPurple
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Attributes Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (character.species.isNotBlank()) {
                                InfoChip(text = "🧬 ${character.species}")
                            }

                            val statusColor = when (character.status.lowercase()) {
                                "alive" -> StatusAlive
                                "dead" -> StatusDead
                                else -> StatusUnknown
                            }
                            StatusChip(statusText = character.status, dotColor = statusColor)

                            if (character.gender.isNotBlank()) {
                                InfoChip(text = "♂ ${character.gender}")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Origin & Last Known Ubication
                        if (character.origin.name.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = IconPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Origin:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CardText
                                    )
                                    Text(
                                        text = character.origin.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = IconPurple
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (character.location.name.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = IconPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Last Know Ubication:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CardText
                                    )
                                    Text(
                                        text = character.location.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = IconPurple
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Episodes Section Card 2
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Episodes (${character.episode.size})",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = IconPurple
                            )
                            Icon(
                                imageVector = Icons.Filled.Movie,
                                contentDescription = null,
                                tint = IconPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (detailState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = IconPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else if (detailState.episodes.isNotEmpty()) {
                            detailState.episodes.forEachIndexed { index, episode ->
                                EpisodeItemRow(episode = episode)
                                if (index < detailState.episodes.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = ChipBackground
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Appears in ${character.episode.size} episodes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CardText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeItemRow(
    episode: Episode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${episode.episode} - ${episode.name}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = IconPurple
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Release: ${episode.airDate}",
            style = MaterialTheme.typography.bodySmall,
            color = CardText
        )
    }
}
