package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.SplitTunnelMode
import com.example.ui.components.tactilePressScale
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.KillSwitchAmber
import com.example.ui.theme.ProtectedEmerald
import com.example.viewmodel.VpnUiState

@Composable
fun SplitTunnelingScreen(
    uiState: VpnUiState,
    onSelectSplitMode: (SplitTunnelMode) -> Unit,
    onToggleAppSelection: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.appLanguage
    val accent = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current
    val categories = listOf(
        "Toutes",
        "Paiements",
        "Banques",
        "Streaming",
        "Navigateurs",
        "Messagerie",
        "Navigation",
        "Système",
        "Installées"
    )
    val filteredApps = uiState.appRoutingList.filter { app ->
        val matchesCategory = uiState.selectedAppCategory == "Toutes" || app.category == uiState.selectedAppCategory
        val q = uiState.appSearchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
            app.appName.lowercase().contains(q) ||
            app.packageName.lowercase().contains(q)
        matchesCategory && matchesSearch
    }

    val selectedCount = uiState.appRoutingList.count { it.isSelectedForSplit }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Card: Renamed to "Tunnels"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.16f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Tunnels"
                                    AppLanguage.EN -> "Tunnels"
                                    AppLanguage.ES -> "Túneles"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Gérez le routage par application (PayPal, banques, streaming) entre le tunnel AES-256 et le réseau direct."
                                    AppLanguage.EN -> "Manage per-app routing (PayPal, banking, streaming) between the AES-256 tunnel and direct network."
                                    AppLanguage.ES -> "Gestiona el enrutamiento por aplicación (PayPal, bancos, streaming) entre el túnel AES-256 y la red directa."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SplitTunnelMode.entries.forEach { mode ->
                        val isSelected = uiState.splitTunnelMode == mode
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.background,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .tactilePressScale()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectSplitMode(mode)
                                }
                                .testTag("split_mode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectSplitMode(mode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = accent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = mode.localizedTitle(lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = mode.localizedSubtitle(lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search + Category Filters
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "Applications (${filteredApps.size})"
                            AppLanguage.EN -> "Applications (${filteredApps.size})"
                            AppLanguage.ES -> "Aplicaciones (${filteredApps.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$selectedCount sélectionnées",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.appSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            when (lang) {
                                AppLanguage.FR -> "Rechercher PayPal, banque, navigateur…"
                                AppLanguage.EN -> "Search PayPal, banking, browser…"
                                AppLanguage.ES -> "Buscar PayPal, banco, navegador…"
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
                        .testTag("split_app_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = uiState.selectedAppCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.2f),
                                selectedLabelColor = accent
                            )
                        )
                    }
                }
            }
        }

        // Application Routing Cards
        items(filteredApps, key = { it.packageName }) { app ->
            val isProtectedByVpn = when (uiState.splitTunnelMode) {
                SplitTunnelMode.ALL_APPS -> true
                SplitTunnelMode.BYPASS_SELECTED -> !app.isSelectedForSplit
                SplitTunnelMode.ONLY_SELECTED -> app.isSelectedForSplit
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .tactilePressScale()
                    .clickable {
                        if (uiState.splitTunnelMode != SplitTunnelMode.ALL_APPS) {
                            onToggleAppSelection(app.packageName)
                        }
                    }
                    .testTag("split_app_item_${app.packageName}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isProtectedByVpn) {
                        ProtectedEmerald.copy(alpha = 0.45f)
                    } else {
                        KillSwitchAmber.copy(alpha = 0.55f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isProtectedByVpn) ProtectedEmerald.copy(alpha = 0.16f)
                            else KillSwitchAmber.copy(alpha = 0.16f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isProtectedByVpn) Icons.Default.Lock else Icons.Default.Public,
                                    contentDescription = null,
                                    tint = if (isProtectedByVpn) ProtectedEmerald else KillSwitchAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = app.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMono,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isProtectedByVpn) {
                                    when (lang) {
                                        AppLanguage.FR -> "Tunnel Chiffré AES-256"
                                        AppLanguage.EN -> "Encrypted AES-256 Tunnel"
                                        AppLanguage.ES -> "Túnel Cifrado AES-256"
                                    }
                                } else {
                                    when (lang) {
                                        AppLanguage.FR -> "Direct (Hors VPN)"
                                        AppLanguage.EN -> "Direct (Bypass VPN)"
                                        AppLanguage.ES -> "Directo (Fuera de VPN)"
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isProtectedByVpn) ProtectedEmerald else KillSwitchAmber
                            )
                        }
                    }

                    Switch(
                        checked = app.isSelectedForSplit,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleAppSelection(app.packageName)
                        },
                        enabled = uiState.splitTunnelMode != SplitTunnelMode.ALL_APPS,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accent
                        ),
                        modifier = Modifier.testTag("split_switch_${app.packageName}")
                    )
                }
            }
        }
    }
}
