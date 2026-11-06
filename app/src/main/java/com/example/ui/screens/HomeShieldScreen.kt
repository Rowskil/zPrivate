package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.SplitTunnelMode
import com.example.model.VpnConnectionState
import com.example.ui.components.PhysicsMotion
import com.example.ui.components.ZPrivateLogoVector
import com.example.ui.components.tactilePressScale
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.KillSwitchAmber
import com.example.ui.theme.KillSwitchCrimson
import com.example.ui.theme.ProtectedEmerald
import com.example.viewmodel.VpnUiState
import java.util.Locale

@Composable
fun HomeShieldScreen(
    uiState: VpnUiState,
    onToggleConnect: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSplitTunneling: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleKillSwitchDirectly: () -> Unit,
    onCycleSplitTunnelMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.appLanguage
    val cardCorner = 24.dp
    val accent = MaterialTheme.colorScheme.primary

    val statusColor by animateColorAsState(
        targetValue = when (uiState.connectionState) {
            VpnConnectionState.CONNECTED -> ProtectedEmerald
            VpnConnectionState.CONNECTING, VpnConnectionState.RECONNECTING -> accent
            VpnConnectionState.KILL_SWITCH_LOCKED -> KillSwitchCrimson
            VpnConnectionState.DISCONNECTED -> accent
        },
        animationSpec = tween(PhysicsMotion.MICRO_DURATION_MS, easing = PhysicsMotion.SnappyOutEasing),
        label = "statusColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar: Clean Brand Logo & Name (Removed non-dynamic subtitle and Wi-Fi pill badge as requested in A.1 & A.5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZPrivateLogoVector(size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "zPrivate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Offline Connectivity Banner (Feedback 004 #5)
        androidx.compose.animation.AnimatedVisibility(
            visible = !uiState.isDeviceOnline,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = KillSwitchAmber.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, KillSwitchAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = KillSwitchAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "Aucune connexion Internet • En attente de réseau"
                            AppLanguage.EN -> "No Internet connection • Waiting for network"
                            AppLanguage.ES -> "Sin conexión a Internet • Esperando red"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = KillSwitchAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Central 1-Click Shield Orb
        OneClickShieldOrb(
            connectionState = uiState.connectionState,
            durationSeconds = uiState.connectedDurationSeconds,
            serverFlag = uiState.selectedServer.flagEmoji,
            serverCity = uiState.selectedServer.city,
            lang = lang,
            accentColor = accent,
            onClick = onToggleConnect
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Active Server Card (Ping aligned horizontally at the top right as requested in A.2)
        ActiveServerCard(
            uiState = uiState,
            lang = lang,
            accentColor = accent,
            cardCorner = cardCorner,
            onClick = onOpenServers
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Live Speed & Encrypted Throughput Card (Placed ABOVE the Virtual IP Card)
        LiveSpeedTelemetryCard(
            uiState = uiState,
            lang = lang,
            accentColor = accent,
            cardCorner = cardCorner
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Redesigned Protection & Network Card (Ergonomic layout displaying device network name as requested in A.3)
        NetworkTransparencyCard(
            uiState = uiState,
            lang = lang,
            statusColor = statusColor,
            cardCorner = cardCorner
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Actionable Quick Cards: Kill Switch & Tunnels with Equal Dimensions (as requested in A.4)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Interactive Kill Switch (Direct Switch)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp)
                    .tactilePressScale()
                    .testTag("home_killswitch_quick_card"),
                shape = RoundedCornerShape(cardCorner),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (uiState.killSwitchEnabled) ProtectedEmerald.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = (if (uiState.killSwitchEnabled) ProtectedEmerald else KillSwitchAmber).copy(alpha = 0.18f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (uiState.killSwitchEnabled) ProtectedEmerald else KillSwitchAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Switch(
                            checked = uiState.killSwitchEnabled,
                            onCheckedChange = { onToggleKillSwitchDirectly() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ProtectedEmerald
                            ),
                            modifier = Modifier.testTag("home_killswitch_switch")
                        )
                    }

                    Column {
                        Text(
                            text = "Kill Switch",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.killSwitchEnabled) ProtectedEmerald else KillSwitchAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.killSwitchEnabled) {
                                    when (lang) {
                                        AppLanguage.FR -> "Actif (Auto-blocage)"
                                        AppLanguage.EN -> "Active (Auto-block)"
                                        AppLanguage.ES -> "Activo (Auto-bloqueo)"
                                    }
                                } else {
                                    when (lang) {
                                        AppLanguage.FR -> "Désactivé"
                                        AppLanguage.EN -> "Disabled"
                                        AppLanguage.ES -> "Desactivado"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.killSwitchEnabled) ProtectedEmerald else KillSwitchAmber,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Card 2: Interactive Tunnels (Split Tunneling mode switcher)
            val selectedSplitCount = uiState.appRoutingList.count { it.isSelectedForSplit }
            val modeLabel = when (uiState.splitTunnelMode) {
                SplitTunnelMode.ALL_APPS -> when (lang) {
                    AppLanguage.FR -> "Toutes"
                    AppLanguage.EN -> "All"
                    AppLanguage.ES -> "Todas"
                }
                SplitTunnelMode.BYPASS_SELECTED -> when (lang) {
                    AppLanguage.FR -> "Bypass"
                    AppLanguage.EN -> "Bypass"
                    AppLanguage.ES -> "Bypass"
                }
                SplitTunnelMode.ONLY_SELECTED -> when (lang) {
                    AppLanguage.FR -> "Sélectif"
                    AppLanguage.EN -> "Selective"
                    AppLanguage.ES -> "Selectivo"
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp)
                    .tactilePressScale()
                    .clickable { onOpenSplitTunneling() }
                    .testTag("home_splittunnel_quick_card"),
                shape = RoundedCornerShape(cardCorner),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accent.copy(alpha = 0.18f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Direct mode cycling chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accent.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onCycleSplitTunnelMode() }
                        ) {
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = when (lang) {
                                AppLanguage.FR -> "Tunnels"
                                AppLanguage.EN -> "Tunnels"
                                AppLanguage.ES -> "Túneles"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (uiState.splitTunnelMode) {
                                SplitTunnelMode.ALL_APPS -> when (lang) {
                                    AppLanguage.FR -> "Toutes les applications"
                                    AppLanguage.EN -> "All applications"
                                    AppLanguage.ES -> "Todas las aplicaciones"
                                }
                                SplitTunnelMode.BYPASS_SELECTED -> "$selectedSplitCount apps exclues (PayPal…)"
                                SplitTunnelMode.ONLY_SELECTED -> "$selectedSplitCount apps protégées"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun OneClickShieldOrb(
    connectionState: VpnConnectionState,
    durationSeconds: Long,
    serverFlag: String,
    serverCity: String,
    lang: AppLanguage,
    accentColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shieldPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val orbColor by animateColorAsState(
        targetValue = when (connectionState) {
            VpnConnectionState.CONNECTED -> ProtectedEmerald
            VpnConnectionState.CONNECTING, VpnConnectionState.RECONNECTING -> accentColor
            VpnConnectionState.KILL_SWITCH_LOCKED -> KillSwitchCrimson
            VpnConnectionState.DISCONNECTED -> accentColor
        },
        animationSpec = tween(PhysicsMotion.PANEL_DURATION_MS, easing = PhysicsMotion.SnappyOutEasing),
        label = "orbColor"
    )

    val glowFraction by animateFloatAsState(
        targetValue = if (connectionState == VpnConnectionState.CONNECTED) 1f else 0.45f,
        animationSpec = tween(PhysicsMotion.PANEL_DURATION_MS, easing = PhysicsMotion.SnappyOutEasing),
        label = "glowFraction"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(214.dp)
                .tactilePressScale(pressedScale = 0.93f, hoveredScale = 1.02f)
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .testTag("one_click_vpn_button")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.minDimension / 2f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orbColor.copy(alpha = 0.36f * glowFraction),
                            orbColor.copy(alpha = 0.08f * glowFraction),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxR * pulseRadius
                    ),
                    radius = maxR
                )

                drawCircle(
                    color = orbColor.copy(alpha = 0.38f),
                    radius = maxR * 0.85f,
                    style = Stroke(width = 2.dp.toPx())
                )

                drawArc(
                    color = accentColor.copy(alpha = 0.85f),
                    startAngle = ringRotation,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(center.x - maxR * 0.85f, center.y - maxR * 0.85f),
                    size = androidx.compose.ui.geometry.Size(maxR * 1.7f, maxR * 1.7f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    color = orbColor,
                    startAngle = ringRotation + 180f,
                    sweepAngle = 95f,
                    useCenter = false,
                    topLeft = Offset(center.x - maxR * 0.85f, center.y - maxR * 0.85f),
                    size = androidx.compose.ui.geometry.Size(maxR * 1.7f, maxR * 1.7f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Surface(
                modifier = Modifier.size(164.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.5.dp, orbColor),
                shadowElevation = 14.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    orbColor.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        ZPrivateLogoVector(size = 50.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (connectionState) {
                                VpnConnectionState.CONNECTED -> when (lang) {
                                    AppLanguage.FR -> "PROTÉGÉ"
                                    AppLanguage.EN -> "PROTECTED"
                                    AppLanguage.ES -> "PROTEGIDO"
                                }
                                VpnConnectionState.CONNECTING -> "CONNEXION…"
                                VpnConnectionState.RECONNECTING -> "RECONNEXION…"
                                VpnConnectionState.KILL_SWITCH_LOCKED -> "KILL SWITCH"
                                VpnConnectionState.DISCONNECTED -> when (lang) {
                                    AppLanguage.FR -> "CONNECTER"
                                    AppLanguage.EN -> "CONNECT"
                                    AppLanguage.ES -> "CONECTAR"
                                }
                            },
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (connectionState == VpnConnectionState.CONNECTED) {
                                formatDuration(durationSeconds)
                            } else {
                                "$serverFlag $serverCity"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMono,
                            color = orbColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (connectionState) {
                VpnConnectionState.CONNECTED -> when (lang) {
                    AppLanguage.FR -> "Tunnel sécurisé actif via $serverCity"
                    AppLanguage.EN -> "Secure tunnel active via $serverCity"
                    AppLanguage.ES -> "Túnel seguro activo vía $serverCity"
                }
                VpnConnectionState.KILL_SWITCH_LOCKED -> when (lang) {
                    AppLanguage.FR -> "Kill Switch actif : trafic protégé"
                    AppLanguage.EN -> "Kill Switch active: traffic protected"
                    AppLanguage.ES -> "Kill Switch activo: tráfico protegido"
                }
                VpnConnectionState.CONNECTING, VpnConnectionState.RECONNECTING -> when (lang) {
                    AppLanguage.FR -> "Établissement du tunnel chiffré…"
                    AppLanguage.EN -> "Establishing encrypted tunnel…"
                    AppLanguage.ES -> "Estableciendo túnel cifrado…"
                }
                VpnConnectionState.DISCONNECTED -> when (lang) {
                    AppLanguage.FR -> "Touchez le bouclier pour activer la protection"
                    AppLanguage.EN -> "Tap the shield to activate protection"
                    AppLanguage.ES -> "Toca el escudo para activar la protección"
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActiveServerCard(
    uiState: VpnUiState,
    lang: AppLanguage,
    accentColor: Color,
    cardCorner: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val srv = uiState.selectedServer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .tactilePressScale()
            .clickable(onClick = onClick)
            .testTag("active_server_card"),
        shape = RoundedCornerShape(cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Category label on Left, Perfectly Aligned Horizontal Ping on Right (A.2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.FR -> "SERVEUR SÉLECTIONNÉ"
                        AppLanguage.EN -> "SELECTED SERVER"
                        AppLanguage.ES -> "SERVIDOR SELECCIONADO"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Clean Horizontal Ping Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ProtectedEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ProtectedEmerald.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ProtectedEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${srv.measuredLatencyMs} ms",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = ProtectedEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Server Details Row
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = srv.flagEmoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${srv.city}, ${srv.country}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (uiState.smartFastestRouting) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "Auto",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.FR -> "Touchez pour changer de serveur"
                                AppLanguage.EN -> "Tap to change server"
                                AppLanguage.ES -> "Toca para cambiar de servidor"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accentColor
                )
            }
        }
    }
}

@Composable
private fun LiveSpeedTelemetryCard(
    uiState: VpnUiState,
    lang: AppLanguage,
    accentColor: Color,
    cardCorner: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "Débit Réseau Actuel"
                            AppLanguage.EN -> "Current Network Speed"
                            AppLanguage.ES -> "Velocidad de Red Actual"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = ProtectedEmerald.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (uiState.connectionState == VpnConnectionState.CONNECTED) "100% Chiffré" else "Direct",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.connectionState == VpnConnectionState.CONNECTED) ProtectedEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "TÉLÉCHARGEMENT"
                            AppLanguage.EN -> "DOWNLOAD"
                            AppLanguage.ES -> "DESCARGA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", uiState.downloadSpeedMbps),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = ProtectedEmerald
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "Session : %.1f MB", uiState.totalSessionDownloadMb),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "ENVOI"
                            AppLanguage.EN -> "UPLOAD"
                            AppLanguage.ES -> "SUBIDA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", uiState.uploadSpeedMbps),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "Session : %.1f MB", uiState.totalSessionUploadMb),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                val points = uiState.downloadHistoryMbps
                if (points.size >= 2) {
                    val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(0.5f)
                    val stepX = size.width / (points.size - 1)
                    val linePath = Path()
                    val fillPath = Path()

                    points.forEachIndexed { idx, value ->
                        val x = idx * stepX
                        val y = size.height - ((value / maxVal) * (size.height * 0.82f))
                        if (idx == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(size.width, size.height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    drawPath(
                        path = linePath,
                        color = accentColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkTransparencyCard(
    uiState: VpnUiState,
    lang: AppLanguage,
    statusColor: Color,
    cardCorner: androidx.compose.ui.unit.Dp
) {
    val isProtected = uiState.connectionState == VpnConnectionState.CONNECTED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Icon + Title + Protection Badge (A.3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProtected) Icons.Default.Lock else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "Statut de Protection"
                            AppLanguage.EN -> "Protection Status"
                            AppLanguage.ES -> "Estado de Protección"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isProtected) "IP Masquée" else "Non Protégé",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(14.dp))

            // Two clean balanced columns: Device Network & Virtual Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "RÉSEAU"
                            AppLanguage.EN -> "NETWORK"
                            AppLanguage.ES -> "RED"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.connectedNetworkType.contains("Wi-Fi", ignoreCase = true)) {
                                Icons.Default.Wifi
                            } else {
                                Icons.Default.CellTower
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.connectedNetworkName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.FR -> "ADRESSE VIRTUELLE"
                            AppLanguage.EN -> "VIRTUAL ADDRESS"
                            AppLanguage.ES -> "DIRECCIÓN VIRTUAL"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isProtected) uiState.maskedVirtualIp else "Non assignée",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = if (isProtected) ProtectedEmerald else KillSwitchAmber,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hrs = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
