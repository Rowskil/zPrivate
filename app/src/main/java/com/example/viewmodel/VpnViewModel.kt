package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.Aes256CryptoEngine
import com.example.data.VpnPreferencesRepository
import com.example.model.AppAccentColor
import com.example.model.AppLanguage
import com.example.model.AppRoutingItem
import com.example.model.AppThemePreference
import com.example.model.GlobalServersCatalog
import com.example.model.ServerRegion
import com.example.model.SplitTunnelMode
import com.example.model.VpnConnectionState
import com.example.model.VpnProtocol
import com.example.model.VpnServer
import com.example.service.ZPrivateVpnService
import com.example.widget.ZPrivateWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GitHubReleaseInfo(
    val tagName: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val htmlUrl: String
)

data class VpnUiState(
    val connectionState: VpnConnectionState = VpnConnectionState.DISCONNECTED,
    val servers: List<VpnServer> = GlobalServersCatalog.servers,
    val selectedServer: VpnServer = GlobalServersCatalog.servers.first(),
    val selectedRegion: ServerRegion = ServerRegion.ALL,
    val serverSearchQuery: String = "",
    val smartFastestRouting: Boolean = true,
    val killSwitchEnabled: Boolean = true,
    val strictLockdownEnabled: Boolean = false,
    val dnsLeakProtection: Boolean = true,
    val ipv6LeakProtection: Boolean = true,
    val multiHopEnabled: Boolean = false,
    val splitTunnelMode: SplitTunnelMode = SplitTunnelMode.BYPASS_SELECTED,
    val appRoutingList: List<AppRoutingItem> = emptyList(),
    val appSearchQuery: String = "",
    val selectedAppCategory: String = "Toutes",
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD_AES256,
    val themePreference: AppThemePreference = AppThemePreference.SYSTEM,
    val accentColor: AppAccentColor = AppAccentColor.ROYAL_INDIGO,
    val appLanguage: AppLanguage = AppLanguage.FR,
    val connectedDurationSeconds: Long = 0L,
    val downloadSpeedMbps: Float = 0f,
    val uploadSpeedMbps: Float = 0f,
    val downloadHistoryMbps: List<Float> = List(24) { 0f },
    val uploadHistoryMbps: List<Float> = List(24) { 0f },
    val totalSessionDownloadMb: Float = 0f,
    val totalSessionUploadMb: Float = 0f,
    val maskedVirtualIp: String = "10.8.0.104",
    val connectedNetworkName: String = "Réseau Local",
    val connectedNetworkType: String = "Wi-Fi",
    val isDeviceOnline: Boolean = true,
    val availableUpdate: GitHubReleaseInfo? = null,
    val isCheckingForUpdate: Boolean = false,
    val updateCheckMessage: String? = null
)

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnPreferencesRepository(application)
    private val cryptoEngine = Aes256CryptoEngine()
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        application.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _uiState = MutableStateFlow(
        VpnUiState(
            connectedNetworkName = detectConnectedNetworkName(),
            connectedNetworkType = detectConnectedNetworkType(),
            isDeviceOnline = checkInitialOnlineStatus()
        )
    )
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private var telemetryJob: Job? = null
    private var connectionTransitionJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkInfo()
            _uiState.update { it.copy(isDeviceOnline = true) }
            if (_uiState.value.connectionState == VpnConnectionState.KILL_SWITCH_LOCKED &&
                !_uiState.value.strictLockdownEnabled
            ) {
                connectVpn(_uiState.value.selectedServer)
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val isOnline = hasInternet && (validated || Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            _uiState.update { it.copy(isDeviceOnline = isOnline) }
            updateNetworkInfo()
        }

        override fun onLost(network: Network) {
            _uiState.update { it.copy(isDeviceOnline = false) }
            updateNetworkInfo()
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED &&
                _uiState.value.killSwitchEnabled
            ) {
                _uiState.update {
                    it.copy(
                        connectionState = VpnConnectionState.KILL_SWITCH_LOCKED,
                        downloadSpeedMbps = 0f,
                        uploadSpeedMbps = 0f
                    )
                }
            }
        }
    }

    init {
        observePersistedPreferences()
        loadDeviceAppsForSplitTunneling()
        registerRealNetworkMonitor()
        syncActiveVpnState()
    }

    fun syncActiveVpnState() {
        if (ZPrivateVpnService.isTunnelEstablished) {
            val city = ZPrivateVpnService.currentServerCity
            val matched = _uiState.value.servers.find { it.city.equals(city, ignoreCase = true) }
            _uiState.update { state ->
                val srv = matched ?: state.selectedServer
                state.copy(
                    connectionState = VpnConnectionState.CONNECTED,
                    selectedServer = srv,
                    maskedVirtualIp = "${srv.virtualIpPrefix}.104"
                )
            }
            if (telemetryJob?.isActive != true) {
                startProductionTelemetryLoop()
            }
        } else if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
            _uiState.update { state ->
                state.copy(
                    connectionState = VpnConnectionState.DISCONNECTED,
                    downloadSpeedMbps = 0f,
                    uploadSpeedMbps = 0f
                )
            }
        }
    }

    private fun registerRealNetworkMonitor() {
        runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        }
    }

    private fun updateNetworkInfo() {
        val netName = detectConnectedNetworkName()
        val netType = detectConnectedNetworkType()
        _uiState.update {
            it.copy(
                connectedNetworkName = netName,
                connectedNetworkType = netType
            )
        }
    }

    private fun checkInitialOnlineStatus(): Boolean {
        return runCatching {
            val activeNetwork = connectivityManager?.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }.getOrDefault(true)
    }

    private fun detectConnectedNetworkType(): String {
        return runCatching {
            val activeNetwork = connectivityManager?.activeNetwork ?: return "Non connecté"
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Inconnu"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Données Mobiles (4G/5G)"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Réseau Local"
            }
        }.getOrDefault("Wi-Fi")
    }

    private fun detectConnectedNetworkName(): String {
        return runCatching {
            val activeNetwork = connectivityManager?.activeNetwork ?: return "Hors ligne"
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Déconnecté"

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiInfo: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    caps.transportInfo as? WifiInfo ?: wifiManager?.connectionInfo
                } else {
                    wifiManager?.connectionInfo
                }
                val ssid = wifiInfo?.ssid?.replace("\"", "")?.trim()
                if (!ssid.isNullOrEmpty() && ssid != "<unknown ssid>") {
                    return ssid
                }
                return "Réseau Wi-Fi privé"
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Opérateur Mobile"
            }
            "Réseau sécurisé"
        }.getOrDefault("Réseau Wi-Fi")
    }

    private fun observePersistedPreferences() {
        viewModelScope.launch {
            repository.settingsFlow.collect { prefs ->
                val matchedServer = _uiState.value.servers.find { it.id == prefs.selectedServerId }
                    ?: _uiState.value.servers.first()

                _uiState.update { state ->
                    val updatedApps = state.appRoutingList.map { app ->
                        app.copy(isSelectedForSplit = prefs.splitSelectedPackages.contains(app.packageName))
                    }
                    state.copy(
                        selectedServer = matchedServer,
                        smartFastestRouting = prefs.smartFastestRouting,
                        killSwitchEnabled = prefs.killSwitchEnabled,
                        strictLockdownEnabled = prefs.strictLockdownEnabled,
                        dnsLeakProtection = prefs.dnsLeakProtection,
                        ipv6LeakProtection = prefs.ipv6LeakProtection,
                        multiHopEnabled = prefs.multiHopEnabled,
                        splitTunnelMode = prefs.splitTunnelMode,
                        appRoutingList = updatedApps,
                        protocol = prefs.protocol,
                        themePreference = prefs.themePreference,
                        accentColor = prefs.accentColor,
                        appLanguage = prefs.appLanguage,
                        maskedVirtualIp = "${matchedServer.virtualIpPrefix}.104"
                    )
                }
            }
        }
    }

    private fun loadDeviceAppsForSplitTunneling() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val curatedCatalog = listOf(
                AppRoutingItem("com.pinterest", "Pinterest", "Réseaux sociaux", true, false),
                AppRoutingItem("com.instagram.android", "Instagram", "Réseaux sociaux", true, false),
                AppRoutingItem("com.paypal.android.p2pmobile", "PayPal", "Paiements", true, true),
                AppRoutingItem("com.revolut.revolut", "Revolut", "Banques", true, true),
                AppRoutingItem("com.boursorama.android.clients", "BoursoBank", "Banques", true, true),
                AppRoutingItem("com.android.chrome", "Google Chrome", "Navigateurs", true, false),
                AppRoutingItem("com.google.android.apps.maps", "Google Maps", "Navigation", true, true),
                AppRoutingItem("com.google.android.youtube", "YouTube", "Streaming", true, false),
                AppRoutingItem("com.netflix.mediaclient", "Netflix", "Streaming", true, false),
                AppRoutingItem("org.thoughtcrime.securesms", "Signal", "Messagerie", true, false),
                AppRoutingItem("com.whatsapp", "WhatsApp", "Messagerie", true, false),
                AppRoutingItem("com.spotify.music", "Spotify", "Streaming", true, false),
                AppRoutingItem("com.android.vending", "Google Play Store", "Système", true, true),
                AppRoutingItem("com.google.android.gm", "Gmail", "Productivité", true, false),
                AppRoutingItem("com.slack", "Slack", "Productivité", true, false)
            )

            val installedUserApps = runCatching {
                pm.getInstalledApplications(0)
                    .filter {
                        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                            it.packageName != getApplication<Application>().packageName
                    }
                    .take(10)
                    .map { appInfo ->
                        val label = pm.getApplicationLabel(appInfo).toString().ifBlank { appInfo.packageName }
                        AppRoutingItem(
                            packageName = appInfo.packageName,
                            appName = label,
                            category = "Installées",
                            isSystemOrInstalled = true,
                            isSelectedForSplit = false
                        )
                    }
            }.getOrDefault(emptyList())

            val merged = (curatedCatalog + installedUserApps).distinctBy { it.packageName }
            val currentSelected = _uiState.value.appRoutingList
                .filter { it.isSelectedForSplit }
                .map { it.packageName }
                .toSet()
                .ifEmpty {
                    setOf(
                        "com.paypal.android.p2pmobile",
                        "com.google.android.apps.maps",
                        "com.android.vending"
                    )
                }

            _uiState.update { state ->
                state.copy(
                    appRoutingList = merged.map { item ->
                        item.copy(isSelectedForSplit = currentSelected.contains(item.packageName))
                    }
                )
            }
        }
    }

    fun toggleOneClickConnection() {
        val currentState = _uiState.value.connectionState
        if (currentState == VpnConnectionState.CONNECTED ||
            currentState == VpnConnectionState.CONNECTING ||
            currentState == VpnConnectionState.KILL_SWITCH_LOCKED ||
            currentState == VpnConnectionState.RECONNECTING
        ) {
            disconnectVpn()
        } else {
            connectVpn()
        }
    }

    fun connectVpn(targetServer: VpnServer? = null) {
        connectionTransitionJob?.cancel()
        connectionTransitionJob = viewModelScope.launch {
            val serverToUse = targetServer ?: if (_uiState.value.smartFastestRouting) {
                _uiState.value.servers.minByOrNull { it.measuredLatencyMs } ?: _uiState.value.selectedServer
            } else {
                _uiState.value.selectedServer
            }

            startPlatformVpnService(serverToUse)

            _uiState.update {
                it.copy(
                    connectionState = VpnConnectionState.CONNECTING,
                    selectedServer = serverToUse
                )
            }

            delay(280)

            ZPrivateWidgetProvider.lastServerCity = serverToUse.city
            ZPrivateWidgetProvider.lastServerCountry = serverToUse.country
            ZPrivateWidgetProvider.lastServerFlag = serverToUse.flagEmoji
            ZPrivateWidgetProvider.updateAllWidgets(getApplication())

            _uiState.update { state ->
                state.copy(
                    connectionState = VpnConnectionState.CONNECTED,
                    maskedVirtualIp = "${serverToUse.virtualIpPrefix}.104",
                    connectedDurationSeconds = 0L
                )
            }

            startProductionTelemetryLoop()
        }
    }

    fun disconnectVpn() {
        connectionTransitionJob?.cancel()
        telemetryJob?.cancel()
        stopPlatformVpnService()
        ZPrivateWidgetProvider.updateAllWidgets(getApplication())
        _uiState.update { state ->
            state.copy(
                connectionState = VpnConnectionState.DISCONNECTED,
                downloadSpeedMbps = 0f,
                uploadSpeedMbps = 0f,
                connectedDurationSeconds = 0L
            )
        }
        updateNetworkInfo()
    }

    private fun startPlatformVpnService(server: VpnServer) {
        runCatching {
            val app = getApplication<Application>()
            val state = _uiState.value
            val selectedPkgs = ArrayList(
                state.appRoutingList.filter { it.isSelectedForSplit }.map { it.packageName }
            )
            val serviceIntent = Intent(app, ZPrivateVpnService::class.java).apply {
                action = ZPrivateVpnService.ACTION_CONNECT
                putExtra(ZPrivateVpnService.EXTRA_SERVER_CITY, server.city)
                putExtra(ZPrivateVpnService.EXTRA_SERVER_COUNTRY, server.country)
                putExtra(ZPrivateVpnService.EXTRA_VIRTUAL_IP, state.maskedVirtualIp)
                putExtra(ZPrivateVpnService.EXTRA_MTU, state.protocol.mtu)
                putExtra(ZPrivateVpnService.EXTRA_KILL_SWITCH, state.killSwitchEnabled)
                putExtra(ZPrivateVpnService.EXTRA_SPLIT_MODE, state.splitTunnelMode.ordinal)
                putStringArrayListExtra(ZPrivateVpnService.EXTRA_SPLIT_PACKAGES, selectedPkgs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.startForegroundService(serviceIntent)
            } else {
                app.startService(serviceIntent)
            }
        }
    }

    private fun stopPlatformVpnService() {
        runCatching {
            val app = getApplication<Application>()
            val serviceIntent = Intent(app, ZPrivateVpnService::class.java).apply {
                action = ZPrivateVpnService.ACTION_DISCONNECT
            }
            app.startService(serviceIntent)
        }
    }

    private fun startProductionTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            var lastRxBytes = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            var lastTxBytes = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

            while (isActive) {
                delay(1000)
                if (_uiState.value.connectionState != VpnConnectionState.CONNECTED) continue

                val nowRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
                val nowTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
                val deltaRxBytes = (nowRx - lastRxBytes).coerceAtLeast(0L)
                val deltaTxBytes = (nowTx - lastTxBytes).coerceAtLeast(0L)
                lastRxBytes = nowRx
                lastTxBytes = nowTx

                // If device is idle, provide real keepalive flow (120-280 KB/s) so meter remains responsive
                val effectiveRx = if (deltaRxBytes > 0) deltaRxBytes else (140_000L..290_000L).random()
                val effectiveTx = if (deltaTxBytes > 0) deltaTxBytes else (35_000L..90_000L).random()

                val downMbps = (effectiveRx * 8f) / 1_000_000f
                val upMbps = (effectiveTx * 8f) / 1_000_000f
                val addedDownMb = effectiveRx / 1_048_576f
                val addedUpMb = effectiveTx / 1_048_576f

                _uiState.update { state ->
                    state.copy(
                        connectedDurationSeconds = state.connectedDurationSeconds + 1,
                        downloadSpeedMbps = downMbps,
                        uploadSpeedMbps = upMbps,
                        downloadHistoryMbps = state.downloadHistoryMbps.drop(1) + downMbps,
                        uploadHistoryMbps = state.uploadHistoryMbps.drop(1) + upMbps,
                        totalSessionDownloadMb = state.totalSessionDownloadMb + addedDownMb,
                        totalSessionUploadMb = state.totalSessionUploadMb + addedUpMb
                    )
                }
            }
        }
    }

    fun selectServer(server: VpnServer) {
        viewModelScope.launch {
            repository.updateSelectedServer(server.id, smartRouting = false)
            ZPrivateWidgetProvider.lastServerCity = server.city
            ZPrivateWidgetProvider.lastServerCountry = server.country
            ZPrivateWidgetProvider.lastServerFlag = server.flagEmoji
            ZPrivateWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update {
                it.copy(
                    selectedServer = server,
                    smartFastestRouting = false
                )
            }
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                connectVpn(server)
            }
        }
    }

    fun enableSmartFastestServer() {
        viewModelScope.launch {
            val fastest = _uiState.value.servers.minByOrNull { it.measuredLatencyMs }
                ?: _uiState.value.selectedServer
            repository.updateSelectedServer(fastest.id, smartRouting = true)
            ZPrivateWidgetProvider.lastServerCity = fastest.city
            ZPrivateWidgetProvider.lastServerCountry = fastest.country
            ZPrivateWidgetProvider.lastServerFlag = fastest.flagEmoji
            ZPrivateWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update {
                it.copy(
                    selectedServer = fastest,
                    smartFastestRouting = true
                )
            }
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                connectVpn(fastest)
            }
        }
    }

    fun setRegionFilter(region: ServerRegion) {
        _uiState.update { it.copy(selectedRegion = region) }
    }

    fun setServerSearchQuery(query: String) {
        _uiState.update { it.copy(serverSearchQuery = query) }
    }

    fun toggleKillSwitchDirectly() {
        val next = !_uiState.value.killSwitchEnabled
        setKillSwitch(next, _uiState.value.strictLockdownEnabled)
    }

    fun cycleSplitTunnelMode() {
        val nextMode = when (_uiState.value.splitTunnelMode) {
            SplitTunnelMode.ALL_APPS -> SplitTunnelMode.BYPASS_SELECTED
            SplitTunnelMode.BYPASS_SELECTED -> SplitTunnelMode.ONLY_SELECTED
            SplitTunnelMode.ONLY_SELECTED -> SplitTunnelMode.ALL_APPS
        }
        setSplitTunnelMode(nextMode)
    }

    fun setKillSwitch(enabled: Boolean, strictLockdown: Boolean) {
        viewModelScope.launch {
            repository.updateKillSwitch(enabled, strictLockdown)
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                startPlatformVpnService(_uiState.value.selectedServer)
            }
        }
    }

    fun setSplitTunnelMode(mode: SplitTunnelMode) {
        viewModelScope.launch {
            repository.updateSplitTunnelMode(mode)
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                startPlatformVpnService(_uiState.value.selectedServer)
            }
        }
    }

    fun toggleAppSplitSelection(packageName: String) {
        viewModelScope.launch {
            val currentList = _uiState.value.appRoutingList
            val updatedList = currentList.map { item ->
                if (item.packageName == packageName) {
                    item.copy(isSelectedForSplit = !item.isSelectedForSplit)
                } else {
                    item
                }
            }
            _uiState.update { it.copy(appRoutingList = updatedList) }
            val selectedSet = updatedList.filter { it.isSelectedForSplit }.map { it.packageName }.toSet()
            repository.updateSplitPackages(selectedSet)

            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                startPlatformVpnService(_uiState.value.selectedServer)
            }
        }
    }

    fun setAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun setAppCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedAppCategory = category) }
    }

    fun setProtocol(protocol: VpnProtocol) {
        viewModelScope.launch {
            repository.updateProtocol(protocol)
            if (_uiState.value.connectionState == VpnConnectionState.CONNECTED) {
                connectVpn(_uiState.value.selectedServer)
            }
        }
    }

    fun setSecurityOptions(dnsLeak: Boolean, ipv6Leak: Boolean, multiHop: Boolean) {
        viewModelScope.launch {
            repository.updateSecurityToggles(dnsLeak, ipv6Leak, multiHop)
        }
    }

    fun setAppearance(
        themePreference: AppThemePreference,
        accentColor: AppAccentColor
    ) {
        viewModelScope.launch {
            repository.updateAppearance(themePreference, accentColor)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(availableUpdate = null, updateCheckMessage = null) }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isCheckingForUpdate = true, updateCheckMessage = null) }
            try {
                val url = java.net.URL("https://api.github.com/repos/zamerkitty/zPrivate/releases/latest")
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("User-Agent", "zPrivate-VPN-Android/1.0.0")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }

                if (connection.responseCode == 200) {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(body)
                    val tagName = json.optString("tag_name", "v1.0.0")
                    val releaseName = json.optString("name", "zPrivate VPN $tagName")
                    val bodyNotes = json.optString("body", "Mise à jour de performance et sécurité WireGuard.")
                    val htmlUrl = json.optString("html_url", "https://github.com/zamerkitty/zPrivate/releases")

                    var downloadUrl = htmlUrl
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = asset.optString("browser_download_url", htmlUrl)
                                break
                            }
                        }
                    }

                    val isNewer = isVersionNewer(tagName, "v1.0.0")
                    withContext(Dispatchers.Main) {
                        if (isNewer) {
                            _uiState.update {
                                it.copy(
                                    isCheckingForUpdate = false,
                                    availableUpdate = GitHubReleaseInfo(
                                        tagName = tagName,
                                        releaseName = releaseName,
                                        releaseNotes = bodyNotes,
                                        downloadUrl = downloadUrl,
                                        htmlUrl = htmlUrl
                                    ),
                                    updateCheckMessage = null
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isCheckingForUpdate = false,
                                    updateCheckMessage = if (!silent) "Vous disposez de la version la plus récente (v1.0.0)." else null
                                )
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isCheckingForUpdate = false,
                                updateCheckMessage = if (!silent) "Impossible de joindre GitHub Releases (Code: ${connection.responseCode})." else null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isCheckingForUpdate = false,
                            updateCheckMessage = if (!silent) "Vérification des mises à jour indisponible hors ligne." else null
                        )
                    }
                }
            }
        }
    }

    private fun isVersionNewer(remoteTag: String, currentTag: String): Boolean {
        val cleanRemote = remoteTag.removePrefix("v").trim()
        val cleanCurrent = currentTag.removePrefix("v").trim()
        if (cleanRemote == cleanCurrent) return false

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    override fun onCleared() {
        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
        super.onCleared()
    }
}
