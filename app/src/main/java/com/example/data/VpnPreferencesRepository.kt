package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppAccentColor
import com.example.model.AppLanguage
import com.example.model.AppThemePreference
import com.example.model.SplitTunnelMode
import com.example.model.VpnProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zprivate_vpn_settings")

data class PersistedVpnSettings(
    val selectedServerId: String = "fr-par-01",
    val smartFastestRouting: Boolean = true,
    val killSwitchEnabled: Boolean = true,
    val strictLockdownEnabled: Boolean = false,
    val dnsLeakProtection: Boolean = true,
    val ipv6LeakProtection: Boolean = true,
    val multiHopEnabled: Boolean = true,
    val splitTunnelMode: SplitTunnelMode = SplitTunnelMode.BYPASS_SELECTED,
    val splitSelectedPackages: Set<String> = setOf(
        "com.paypal.android.p2pmobile",
        "com.google.android.apps.maps",
        "com.android.vending"
    ),
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD_AES256,
    val themePreference: AppThemePreference = AppThemePreference.SYSTEM,
    val accentColor: AppAccentColor = AppAccentColor.ROYAL_INDIGO,
    val appLanguage: AppLanguage = AppLanguage.FR,
    val masterGatewayIp: String = "213.32.22.5"
)

class VpnPreferencesRepository(private val context: Context) {

    private object Keys {
        val SELECTED_SERVER_ID = stringPreferencesKey("selected_server_id")
        val SMART_FASTEST_ROUTING = booleanPreferencesKey("smart_fastest_routing")
        val KILL_SWITCH_ENABLED = booleanPreferencesKey("kill_switch_enabled")
        val STRICT_LOCKDOWN = booleanPreferencesKey("strict_lockdown")
        val DNS_LEAK_PROTECTION = booleanPreferencesKey("dns_leak_protection")
        val IPV6_LEAK_PROTECTION = booleanPreferencesKey("ipv6_leak_protection")
        val MULTI_HOP = booleanPreferencesKey("multi_hop")
        val SPLIT_MODE = stringPreferencesKey("split_tunnel_mode")
        val SPLIT_PACKAGES = stringSetPreferencesKey("split_selected_packages")
        val VPN_PROTOCOL = stringPreferencesKey("vpn_protocol")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val MASTER_GATEWAY_IP = stringPreferencesKey("master_gateway_ip")
    }

    val settingsFlow: Flow<PersistedVpnSettings> = context.dataStore.data.map { prefs ->
        PersistedVpnSettings(
            selectedServerId = prefs[Keys.SELECTED_SERVER_ID] ?: "fr-par-01",
            smartFastestRouting = prefs[Keys.SMART_FASTEST_ROUTING] ?: true,
            killSwitchEnabled = prefs[Keys.KILL_SWITCH_ENABLED] ?: true,
            strictLockdownEnabled = prefs[Keys.STRICT_LOCKDOWN] ?: false,
            dnsLeakProtection = prefs[Keys.DNS_LEAK_PROTECTION] ?: true,
            ipv6LeakProtection = prefs[Keys.IPV6_LEAK_PROTECTION] ?: true,
            multiHopEnabled = prefs[Keys.MULTI_HOP] ?: true,
            splitTunnelMode = prefs[Keys.SPLIT_MODE]?.let {
                runCatching { SplitTunnelMode.valueOf(it) }.getOrNull()
            } ?: SplitTunnelMode.BYPASS_SELECTED,
            splitSelectedPackages = prefs[Keys.SPLIT_PACKAGES] ?: setOf(
                "com.paypal.android.p2pmobile",
                "com.google.android.apps.maps",
                "com.android.vending"
            ),
            protocol = prefs[Keys.VPN_PROTOCOL]?.let {
                runCatching { VpnProtocol.valueOf(it) }.getOrNull()
            } ?: VpnProtocol.WIREGUARD_AES256,
            themePreference = prefs[Keys.THEME_PREFERENCE]?.let {
                runCatching { AppThemePreference.valueOf(it) }.getOrNull()
            } ?: AppThemePreference.SYSTEM,
            accentColor = prefs[Keys.ACCENT_COLOR]?.let {
                runCatching { AppAccentColor.valueOf(it) }.getOrNull()
            } ?: AppAccentColor.ROYAL_INDIGO,
            appLanguage = prefs[Keys.APP_LANGUAGE]?.let {
                runCatching { AppLanguage.valueOf(it) }.getOrNull()
            } ?: AppLanguage.FR,
            masterGatewayIp = prefs[Keys.MASTER_GATEWAY_IP]?.ifBlank { "213.32.22.5" } ?: "213.32.22.5"
        )
    }

    suspend fun updateSelectedServer(serverId: String, smartRouting: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_SERVER_ID] = serverId
            prefs[Keys.SMART_FASTEST_ROUTING] = smartRouting
        }
    }

    suspend fun updateKillSwitch(enabled: Boolean, strictLockdown: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.KILL_SWITCH_ENABLED] = enabled
            prefs[Keys.STRICT_LOCKDOWN] = strictLockdown
        }
    }

    suspend fun updateSplitTunnelMode(mode: SplitTunnelMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPLIT_MODE] = mode.name
        }
    }

    suspend fun updateSplitPackages(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPLIT_PACKAGES] = packages
        }
    }

    suspend fun updateProtocol(protocol: VpnProtocol) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VPN_PROTOCOL] = protocol.name
        }
    }

    suspend fun updateSecurityToggles(dnsLeak: Boolean, ipv6Leak: Boolean, multiHop: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DNS_LEAK_PROTECTION] = dnsLeak
            prefs[Keys.IPV6_LEAK_PROTECTION] = ipv6Leak
            prefs[Keys.MULTI_HOP] = multiHop
        }
    }

    suspend fun updateAppearance(
        themePreference: AppThemePreference,
        accentColor: AppAccentColor
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_PREFERENCE] = themePreference.name
            prefs[Keys.ACCENT_COLOR] = accentColor.name
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = language.name
        }
    }

    suspend fun updateMasterGatewayIp(ip: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MASTER_GATEWAY_IP] = ip.trim()
        }
    }
}
