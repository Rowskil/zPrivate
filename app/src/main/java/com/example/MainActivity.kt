package com.example

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppLanguage
import com.example.model.AppThemePreference
import com.example.ui.components.PhysicsMotion
import com.example.ui.screens.HomeShieldScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SplitTunnelingScreen
import com.example.ui.screens.TransparencySecurityScreen
import com.example.ui.screens.ZPrivateSplashScreen
import com.example.ui.theme.ZPrivateVpnTheme
import com.example.viewmodel.VpnViewModel

enum class ZPrivateDestination(
    val route: String,
    val labelFr: String,
    val labelEn: String,
    val labelEs: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    SHIELD("shield", "Accueil", "Home", "Inicio", Icons.Filled.Shield, Icons.Outlined.Shield),
    SERVERS("servers", "Serveurs", "Servers", "Servidores", Icons.Filled.Public, Icons.Outlined.Public),
    TUNNELS("tunnels", "Tunnels", "Tunnels", "Túneles", Icons.AutoMirrored.Filled.CallSplit, Icons.Outlined.CallSplit),
    SETTINGS("settings", "Réglages", "Settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings);

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
        AppLanguage.ES -> labelEs
    }
}

class MainActivity : ComponentActivity() {
    private val vpnViewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by vpnViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (uiState.themePreference) {
                AppThemePreference.DARK -> true
                AppThemePreference.LIGHT -> false
                AppThemePreference.SYSTEM -> systemDark
            }

            var showSplash by rememberSaveable { mutableStateOf(true) }

            ZPrivateVpnTheme(
                darkTheme = useDarkTheme,
                accentColor = uiState.accentColor
            ) {
                if (showSplash) {
                    ZPrivateSplashScreen(
                        themePreference = uiState.themePreference,
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    ZPrivateVpnApp(viewModel = vpnViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vpnViewModel.syncActiveVpnState()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        vpnViewModel.syncActiveVpnState()
    }
}

@Composable
fun ZPrivateVpnApp(
    viewModel: VpnViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var currentTab by rememberSaveable { mutableStateOf(ZPrivateDestination.SHIELD) }
    val lang = uiState.appLanguage
    val accent = MaterialTheme.colorScheme.primary

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.toggleOneClickConnection()
    }

    val requestOrToggleVpn: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val prepareIntent = runCatching { VpnService.prepare(context) }.getOrNull()
        if (prepareIntent != null) {
            runCatching {
                vpnPermissionLauncher.launch(prepareIntent)
            }.onFailure {
                viewModel.toggleOneClickConnection()
            }
        } else {
            viewModel.toggleOneClickConnection()
        }
    }

    BackHandler(enabled = currentTab != ZPrivateDestination.SHIELD) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        currentTab = ZPrivateDestination.SHIELD
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 640.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        ZPrivateDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val label = dest.localizedLabel(lang)
                            NavigationBarItem(
                                selected = selected,
                                alwaysShowLabel = true,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentTab = dest
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accent,
                                    selectedTextColor = accent,
                                    indicatorColor = accent.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        ZPrivateDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val label = dest.localizedLabel(lang)
                            NavigationRailItem(
                                selected = selected,
                                alwaysShowLabel = true,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentTab = dest
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("nav_rail_${dest.route}")
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.96f,
                            transformOrigin = TransformOrigin(0.5f, 0.85f),
                            animationSpec = tween(
                                durationMillis = PhysicsMotion.MICRO_DURATION_MS,
                                easing = PhysicsMotion.SnappyOutEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = PhysicsMotion.MICRO_DURATION_MS,
                                easing = PhysicsMotion.SnappyOutEasing
                            )
                        )).togetherWith(
                            fadeOut(
                                animationSpec = tween(
                                    durationMillis = 140,
                                    easing = PhysicsMotion.CrispExitEasing
                                )
                            )
                        )
                    },
                    label = "tabTransition"
                ) { destination ->
                    when (destination) {
                        ZPrivateDestination.SHIELD -> HomeShieldScreen(
                            uiState = uiState,
                            onToggleConnect = requestOrToggleVpn,
                            onOpenServers = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = ZPrivateDestination.SERVERS
                            },
                            onOpenSplitTunneling = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = ZPrivateDestination.TUNNELS
                            },
                            onOpenSettings = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = ZPrivateDestination.SETTINGS
                            },
                            onToggleKillSwitchDirectly = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleKillSwitchDirectly()
                            },
                            onCycleSplitTunnelMode = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.cycleSplitTunnelMode()
                            }
                        )

                        ZPrivateDestination.SERVERS -> ServersScreen(
                            uiState = uiState,
                            onSelectServer = { server ->
                                viewModel.selectServer(server)
                                currentTab = ZPrivateDestination.SHIELD
                            },
                            onSelectFastestSmart = {
                                viewModel.enableSmartFastestServer()
                                currentTab = ZPrivateDestination.SHIELD
                            },
                            onSelectRegion = { viewModel.setRegionFilter(it) },
                            onSearchQueryChange = { viewModel.setServerSearchQuery(it) }
                        )

                        ZPrivateDestination.TUNNELS -> SplitTunnelingScreen(
                            uiState = uiState,
                            onSelectSplitMode = { viewModel.setSplitTunnelMode(it) },
                            onToggleAppSelection = { viewModel.toggleAppSplitSelection(it) },
                            onSearchQueryChange = { viewModel.setAppSearchQuery(it) },
                            onSelectCategory = { viewModel.setAppCategoryFilter(it) }
                        )

                        ZPrivateDestination.SETTINGS -> TransparencySecurityScreen(
                            uiState = uiState,
                            onUpdateKillSwitch = { enabled, strict ->
                                viewModel.setKillSwitch(enabled, strict)
                            },
                            onSelectProtocol = { viewModel.setProtocol(it) },
                            onUpdateSecurityOptions = { dns, ipv6, multiHop ->
                                viewModel.setSecurityOptions(dns, ipv6, multiHop)
                            },
                            onUpdateAppearance = { themePref, accentColor ->
                                viewModel.setAppearance(themePref, accentColor)
                            },
                            onUpdateLanguage = { newLang ->
                                viewModel.setAppLanguage(newLang)
                            },
                            onCheckForUpdates = {
                                viewModel.checkForUpdates(silent = false)
                            }
                        )
                    }
                }
            }

            // In-app GitHub Update Modal Dialog
            val updateInfo = uiState.availableUpdate
            if (updateInfo != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.dismissUpdateDialog() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Mise à jour disponible • ${updateInfo.tagName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = updateInfo.releaseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.dismissUpdateDialog()
                                runCatching {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(updateInfo.downloadUrl))
                                    context.startActivity(browserIntent)
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Télécharger la mise à jour")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.dismissUpdateDialog() }
                        ) {
                            Text("Plus tard")
                        }
                    }
                )
            }
        }
    }
}
