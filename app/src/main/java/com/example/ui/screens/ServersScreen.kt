package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.GlobalServersCatalog
import com.example.model.ServerRegion
import com.example.model.VpnServer
import com.example.ui.components.tactilePressScale
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.KillSwitchAmber
import com.example.ui.theme.ProtectedEmerald
import com.example.viewmodel.VpnUiState

@Composable
fun ServersScreen(
    uiState: VpnUiState,
    onSelectServer: (VpnServer) -> Unit,
    onSelectFastestSmart: () -> Unit,
    onSelectRegion: (ServerRegion) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.appLanguage
    val accent = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current

    // Track expanded countries with multiple cities
    var expandedCountryCodes by remember {
        mutableStateOf(setOf<String>())
    }

    val countryGroups = remember(uiState.servers, uiState.selectedRegion, uiState.serverSearchQuery) {
        val q = uiState.serverSearchQuery.trim().lowercase()
        GlobalServersCatalog.getCountryGroups().filter { group ->
            val matchesRegion = uiState.selectedRegion == ServerRegion.ALL || group.region == uiState.selectedRegion
            val matchesSearch = q.isEmpty() ||
                group.country.lowercase().contains(q) ||
                group.countryCode.lowercase().contains(q) ||
                group.servers.any { it.city.lowercase().contains(q) }
            matchesRegion && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header Card (Removed "Cameroun" and its flag from parentheses in the subtitle, removed Ping button)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(134.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_vpn_mesh_hero_1793894887242),
                        contentDescription = "Réseau mondial",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xF2060B14),
                                        Color(0xCC060B14),
                                        Color(0x77060B14)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accent.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "${uiState.servers.size} CLUSTERS • 40 GBPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Réseau Mondial zPrivate"
                                    AppLanguage.EN -> "zPrivate Global Network"
                                    AppLanguage.ES -> "Red Mundial zPrivate"
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Europe, Afrique, Amériques & Asie"
                                    AppLanguage.EN -> "Europe, Africa, Americas & Asia"
                                    AppLanguage.ES -> "Europa, África, Américas y Asia"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // Smart Fastest Server Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .tactilePressScale()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectFastestSmart()
                    }
                    .testTag("smart_fastest_server_button"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.smartFastestRouting) {
                        accent.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = if (uiState.smartFastestRouting) 1.5.dp else 1.dp,
                    color = if (uiState.smartFastestRouting) accent else MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Connexion la plus rapide (Auto)"
                                    AppLanguage.EN -> "Fastest Connection (Auto)"
                                    AppLanguage.ES -> "Conexión más rápida (Auto)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Sélection automatique du meilleur serveur"
                                    AppLanguage.EN -> "Automatic selection of the best server"
                                    AppLanguage.ES -> "Selección automática del mejor servidor"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (uiState.smartFastestRouting) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Actif",
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Search & Region Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                OutlinedTextField(
                    value = uiState.serverSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            when (lang) {
                                AppLanguage.FR -> "Rechercher un pays ou une ville…"
                                AppLanguage.EN -> "Search a country or city…"
                                AppLanguage.ES -> "Buscar un país o ciudad…"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = accent
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServerRegion.entries.forEach { region ->
                        val selected = uiState.selectedRegion == region
                        FilterChip(
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectRegion(region)
                            },
                            label = { Text(region.localizedLabel(lang)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.2f),
                                selectedLabelColor = accent
                            ),
                            modifier = Modifier.testTag("region_chip_${region.name}")
                        )
                    }
                }
            }
        }

        // Country Cards with Submenu for multiple cities/states (e.g. Cameroun: Douala / Yaoundé; USA: New York / Silicon Valley)
        items(countryGroups, key = { it.countryCode }) { group ->
            val hasMultipleCities = group.servers.size > 1
            val isCountrySelected = group.servers.any { it.id == uiState.selectedServer.id }
            val isExpanded = expandedCountryCodes.contains(group.countryCode) ||
                (uiState.serverSearchQuery.isNotBlank() && hasMultipleCities)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .tactilePressScale(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCountrySelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = if (isCountrySelected) 1.5.dp else 1.dp,
                    color = if (isCountrySelected) ProtectedEmerald else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Main Country Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hasMultipleCities) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    expandedCountryCodes = if (expandedCountryCodes.contains(group.countryCode)) {
                                        expandedCountryCodes - group.countryCode
                                    } else {
                                        expandedCountryCodes + group.countryCode
                                    }
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectServer(group.servers.first())
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = group.flagEmoji, fontSize = 23.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = group.country,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (hasMultipleCities) {
                                        "${group.servers.size} localisations (${group.servers.joinToString { it.city }})"
                                    } else {
                                        group.servers.first().city
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!hasMultipleCities) {
                                val singleServer = group.servers.first()
                                Surface(
                                    shape = CircleShape,
                                    color = ProtectedEmerald.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "${singleServer.measuredLatencyMs} ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMono,
                                        color = ProtectedEmerald,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = accent.copy(alpha = 0.14f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${group.servers.size} villes",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Submenu of cities/states for countries with multiple locations
                    AnimatedVisibility(visible = hasMultipleCities && isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            group.servers.forEach { srv ->
                                val isCitySelected = srv.id == uiState.selectedServer.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCitySelected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isCitySelected) ProtectedEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSelectServer(srv)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = srv.city,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = if (isCitySelected) FontWeight.Bold else FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCitySelected) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "• Connecté",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ProtectedEmerald
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = ProtectedEmerald.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = "${srv.measuredLatencyMs} ms",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = JetBrainsMono,
                                                color = ProtectedEmerald,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
