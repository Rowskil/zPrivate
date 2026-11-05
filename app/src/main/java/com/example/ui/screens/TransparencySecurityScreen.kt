package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.AppAccentColor
import com.example.model.AppLanguage
import com.example.model.AppThemePreference
import com.example.model.VpnProtocol
import com.example.ui.components.ZPrivateLogoVector
import com.example.ui.components.tactilePressScale
import com.example.ui.theme.KillSwitchAmber
import com.example.ui.theme.ProtectedEmerald
import com.example.viewmodel.VpnUiState

@Composable
fun TransparencySecurityScreen(
    uiState: VpnUiState,
    onUpdateKillSwitch: (enabled: Boolean, strictLockdown: Boolean) -> Unit,
    onSelectProtocol: (VpnProtocol) -> Unit,
    onUpdateSecurityOptions: (dnsLeak: Boolean, ipv6Leak: Boolean, multiHop: Boolean) -> Unit,
    onUpdateAppearance: (AppThemePreference, AppAccentColor) -> Unit,
    onUpdateLanguage: (AppLanguage) -> Unit,
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = uiState.appLanguage
    val accent = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current
    var languageMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 0. Header with Logo & Language Dropdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ZPrivateLogoVector(size = 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.FR -> "Réglages"
                                        AppLanguage.EN -> "Settings"
                                        AppLanguage.ES -> "Ajustes"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "zPrivate VPN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Compact Language Dropdown Selector
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .tactilePressScale()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        languageMenuExpanded = true
                                    }
                                    .testTag("language_dropdown_trigger")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${lang.flag} ${lang.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                AppLanguage.entries.forEach { languageOption ->
                                    val isSelected = languageOption == lang
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = languageOption.flag)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = languageOption.displayName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = accent
                                                )
                                            }
                                        },
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onUpdateLanguage(languageOption)
                                            languageMenuExpanded = false
                                        },
                                        modifier = Modifier.testTag("language_option_${languageOption.name}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Section "Thèmes" (Pure Clean Dark Mode & Palette)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.16f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Thèmes"
                                    AppLanguage.EN -> "Themes"
                                    AppLanguage.ES -> "Temas"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Affichage et couleurs de l'application"
                                    AppLanguage.EN -> "App display and accent colors"
                                    AppLanguage.ES -> "Visualización y colores de la aplicación"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "MODE D'AFFICHAGE"
                            AppLanguage.EN -> "DISPLAY MODE"
                            AppLanguage.ES -> "MODO DE PANTALLA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemePreference.entries.forEach { pref ->
                            val isSelected = uiState.themePreference == pref
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) accent.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.background,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePressScale()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onUpdateAppearance(pref, uiState.accentColor)
                                    }
                                    .testTag("theme_pref_${pref.name}")
                            ) {
                                Text(
                                    text = pref.localizedLabel(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "COULEURS D'ACCENTUATION"
                            AppLanguage.EN -> "ACCENT COLORS"
                            AppLanguage.ES -> "COLORES DE ACENTO"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppAccentColor.entries.forEach { colorOption ->
                            val isSelected = uiState.accentColor == colorOption
                            val swatchColor = Color(colorOption.darkHex)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) swatchColor.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.background,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) swatchColor else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePressScale()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onUpdateAppearance(uiState.themePreference, colorOption)
                                    }
                                    .testTag("accent_color_${colorOption.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF041320),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = colorOption.localizedLabel(lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Production Security Parameters (Kill Switch, Lockdown, DNS, IPv6, Multi-Hop)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (uiState.killSwitchEnabled) ProtectedEmerald.copy(alpha = 0.55f)
                    else KillSwitchAmber.copy(alpha = 0.55f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ProtectedEmerald.copy(alpha = 0.16f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = ProtectedEmerald
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.FR -> "Sécurité & Kill Switch"
                                        AppLanguage.EN -> "Security & Kill Switch"
                                        AppLanguage.ES -> "Seguridad y Kill Switch"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.FR -> "Protection continue du tunnel"
                                        AppLanguage.EN -> "Continuous tunnel protection"
                                        AppLanguage.ES -> "Protección continua del túnel"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.killSwitchEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUpdateKillSwitch(it, uiState.strictLockdownEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ProtectedEmerald
                            ),
                            modifier = Modifier.testTag("security_killswitch_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityToggleRow(
                        title = when (lang) {
                            AppLanguage.FR -> "Verrouillage Strict (Always-On)"
                            AppLanguage.EN -> "Strict Lockdown (Always-On)"
                            AppLanguage.ES -> "Bloqueo Estricto (Always-On)"
                        },
                        subtitle = when (lang) {
                            AppLanguage.FR -> "Bloque tout paquet hors du tunnel chiffré"
                            AppLanguage.EN -> "Blocks all packets outside the encrypted tunnel"
                            AppLanguage.ES -> "Bloquea todo paquete fuera del túnel cifrado"
                        },
                        checked = uiState.strictLockdownEnabled,
                        accentColor = accent,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUpdateKillSwitch(uiState.killSwitchEnabled, it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityToggleRow(
                        title = when (lang) {
                            AppLanguage.FR -> "DNS Chiffré Privé (DoH / DoT)"
                            AppLanguage.EN -> "Private Encrypted DNS (DoH / DoT)"
                            AppLanguage.ES -> "DNS Cifrado Privado (DoH / DoT)"
                        },
                        subtitle = when (lang) {
                            AppLanguage.FR -> "Résolution DNS sans journalisation via Cloudflare"
                            AppLanguage.EN -> "Zero-log DNS resolution via Cloudflare"
                            AppLanguage.ES -> "Resolución DNS sin registros vía Cloudflare"
                        },
                        checked = uiState.dnsLeakProtection,
                        accentColor = accent,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUpdateSecurityOptions(it, uiState.ipv6LeakProtection, uiState.multiHopEnabled)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityToggleRow(
                        title = when (lang) {
                            AppLanguage.FR -> "Protection Anti-Fuite IPv6 & WebRTC"
                            AppLanguage.EN -> "IPv6 & WebRTC Leak Protection"
                            AppLanguage.ES -> "Protección contra Fugas IPv6 y WebRTC"
                        },
                        subtitle = when (lang) {
                            AppLanguage.FR -> "Empêche l'exposition de l'IP réelle"
                            AppLanguage.EN -> "Prevents exposure of real IP"
                            AppLanguage.ES -> "Evita la exposición de la IP real"
                        },
                        checked = uiState.ipv6LeakProtection,
                        accentColor = accent,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUpdateSecurityOptions(uiState.dnsLeakProtection, it, uiState.multiHopEnabled)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecurityToggleRow(
                        title = when (lang) {
                            AppLanguage.FR -> "Double VPN (Multi-Hop via Suisse 🇨🇭)"
                            AppLanguage.EN -> "Double VPN (Multi-Hop via Switzerland 🇨🇭)"
                            AppLanguage.ES -> "Doble VPN (Multi-Hop vía Suiza 🇨🇭)"
                        },
                        subtitle = when (lang) {
                            AppLanguage.FR -> "Double chiffrement AES-256 en cascade"
                            AppLanguage.EN -> "Cascaded dual AES-256 encryption"
                            AppLanguage.ES -> "Doble cifrado AES-256 en cascada"
                        },
                        checked = uiState.multiHopEnabled,
                        accentColor = accent,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUpdateSecurityOptions(uiState.dnsLeakProtection, uiState.ipv6LeakProtection, it)
                        }
                    )
                }
            }
        }

        // 3. Protocole de Tunnelisation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "Protocole de Tunnelisation"
                            AppLanguage.EN -> "Tunneling Protocol"
                            AppLanguage.ES -> "Protocolo de Tunelización"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VpnProtocol.entries.forEach { proto ->
                        val isSelected = uiState.protocol == proto
                        Surface(
                            shape = RoundedCornerShape(14.dp),
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
                                    onSelectProtocol(proto)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectProtocol(proto)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = accent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = proto.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = proto.localizedDescription(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Section Passerelle Réelle & Déploiement Relais NAT (WireGuard + iptables)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ProtectedEmerald.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ProtectedEmerald.copy(alpha = 0.16f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = ProtectedEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Passerelle Relais NAT Dédiée",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Serveur VPS WireGuard • Masquage IP Public",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ProtectedEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ProtectedEmerald.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "NAT Actif",
                                style = MaterialTheme.typography.labelSmall,
                                color = ProtectedEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pour changer l'IP publique vue par les sites distants, 100% du trafic (0.0.0.0/0) est capté dans l'app et redirigé vers la passerelle Linux Ubuntu avec iptables POSTROUTING MASQUERADE.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "server/deploy_wireguard_gateway.sh",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = com.example.ui.theme.JetBrainsMono,
                                color = accent
                            )
                        }
                    }
                }
            }
        }

        // 5. Section Mises à jour GitHub & Distribution
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Mises à jour de l'application"
                                    AppLanguage.EN -> "Application Updates"
                                    AppLanguage.ES -> "Actualizaciones de la aplicación"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.FR -> "Dépôt GitHub officiel : releases et correctifs"
                                    AppLanguage.EN -> "Official GitHub repository: releases & patches"
                                    AppLanguage.ES -> "Repositorio GitHub oficial: versiones y parches"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .tactilePressScale()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCheckForUpdates()
                                }
                                .testTag("btn_check_github_update")
                        ) {
                            Text(
                                text = if (uiState.isCheckingForUpdate) "Vérification…" else "Vérifier",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    if (uiState.updateCheckMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.updateCheckMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Signature officielle de l'application (Feedback 004 #6)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ZPrivateLogoVector(size = 32.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "zPrivate VPN • v1.0.0 (Build 1)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Conçu par Anthony zaMer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "WireGuard® • AES-256-GCM",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SecurityToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}
