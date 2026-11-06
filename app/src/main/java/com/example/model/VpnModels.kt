package com.example.model

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    KILL_SWITCH_LOCKED,
    RECONNECTING
}

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    FR("fr", "Français", "🇫🇷"),
    EN("en", "English", "🇬🇧"),
    ES("es", "Español", "🇪🇸")
}

enum class AppThemePreference(val labelFr: String, val labelEn: String, val labelEs: String) {
    DARK("Sombre", "Dark", "Oscuro"),
    LIGHT("Clair", "Light", "Claro"),
    SYSTEM("Système", "System", "Sistema");

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
        AppLanguage.ES -> labelEs
    }
}

/**
 * 3 Accent Colors + 1 Neutral Color for the "Thèmes" section in Settings.
 */
enum class AppAccentColor(
    val labelFr: String,
    val labelEn: String,
    val labelEs: String,
    val darkHex: Long,
    val lightHex: Long,
    val isNeutral: Boolean = false
) {
    OCEAN_BLUE(
        labelFr = "Bleu zPrivate",
        labelEn = "zPrivate Blue",
        labelEs = "Azul zPrivate",
        darkHex = 0xFF3AA6DD,
        lightHex = 0xFF0284C7,
        isNeutral = false
    ),
    EMERALD_CYBER(
        labelFr = "Émeraude",
        labelEn = "Emerald",
        labelEs = "Esmeralda",
        darkHex = 0xFF10B981,
        lightHex = 0xFF059669,
        isNeutral = false
    ),
    ROYAL_INDIGO(
        labelFr = "Indigo Royal",
        labelEn = "Royal Indigo",
        labelEs = "Índigo Real",
        darkHex = 0xFF6366F1,
        lightHex = 0xFF4F46E5,
        isNeutral = false
    ),
    NEUTRAL_SLATE(
        labelFr = "Neutre Ardoise",
        labelEn = "Neutral Slate",
        labelEs = "Neutro Pizarra",
        darkHex = 0xFF94A3B8,
        lightHex = 0xFF475569,
        isNeutral = true
    );

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
        AppLanguage.ES -> labelEs
    }
}

enum class ServerRegion(
    val labelFr: String,
    val labelEn: String,
    val labelEs: String
) {
    ALL("Tous", "All", "Todos"),
    MEA("Afrique & Moyen-Orient", "Africa & Middle East", "África y Oriente Medio"),
    EUROPE("Europe", "Europe", "Europa"),
    AMERICAS("Amériques", "Americas", "Américas"),
    ASIA_PACIFIC("Asie-Pacifique", "Asia-Pacific", "Asia-Pacífico");

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> labelFr
        AppLanguage.EN -> labelEn
        AppLanguage.ES -> labelEs
    }
}

enum class SplitTunnelMode(
    val titleFr: String,
    val titleEn: String,
    val titleEs: String,
    val subtitleFr: String,
    val subtitleEn: String,
    val subtitleEs: String
) {
    ALL_APPS(
        titleFr = "Toutes les applications protégées",
        titleEn = "All applications protected",
        titleEs = "Todas las aplicaciones protegidas",
        subtitleFr = "100% du trafic passe par le tunnel AES-256-GCM",
        subtitleEn = "100% of traffic is routed through the AES-256-GCM tunnel",
        subtitleEs = "El 100% del tráfico pasa por el túnel AES-256-GCM"
    ),
    BYPASS_SELECTED(
        titleFr = "Exclure les applications cochées (Bypass)",
        titleEn = "Bypass selected applications",
        titleEs = "Excluir aplicaciones seleccionadas (Bypass)",
        subtitleFr = "Les applications sélectionnées utilisent la connexion directe",
        subtitleEn = "Selected apps use direct connection outside the VPN",
        subtitleEs = "Las aplicaciones seleccionadas usan la conexión directa"
    ),
    ONLY_SELECTED(
        titleFr = "Chiffrer uniquement les applications cochées",
        titleEn = "Encrypt only selected applications",
        titleEs = "Cifrar solo las aplicaciones seleccionadas",
        subtitleFr = "Seules les applications sélectionnées passent par zPrivate VPN",
        subtitleEn = "Only selected apps are routed through zPrivate VPN",
        subtitleEs = "Solo las aplicaciones seleccionadas pasan por zPrivate VPN"
    );

    fun localizedTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
        AppLanguage.ES -> titleEs
    }

    fun localizedSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> subtitleFr
        AppLanguage.EN -> subtitleEn
        AppLanguage.ES -> subtitleEs
    }
}

enum class VpnProtocol(
    val displayName: String,
    val cipherBadge: String,
    val descriptionFr: String,
    val descriptionEn: String,
    val descriptionEs: String,
    val mtu: Int
) {
    WIREGUARD_AES256(
        displayName = "zGuard (WireGuard + AES-256-GCM)",
        cipherBadge = "AES-256-GCM / SHA-256",
        descriptionFr = "Haute vitesse, latence minimale et négociation instantanée inspirée de VPN by Google.",
        descriptionEn = "High speed, minimal latency and instant handshake inspired by VPN by Google.",
        descriptionEs = "Alta velocidad, latencia mínima y negociación instantánea inspirada en VPN by Google.",
        mtu = 1420
    ),
    OPENVPN_GCM(
        displayName = "OpenVPN DCO (TCP/UDP 443)",
        cipherBadge = "AES-256-GCM / TLS 1.3",
        descriptionFr = "Contournement des pare-feux restrictifs via le port HTTPS 443.",
        descriptionEn = "Bypasses restrictive firewalls over HTTPS port 443.",
        descriptionEs = "Evita cortafuegos restrictivos mediante el puerto HTTPS 443.",
        mtu = 1400
    ),
    IKEV2_IPSEC(
        displayName = "IKEv2 / IPsec MOBIKE",
        cipherBadge = "AES-256-GCM / ECDH P-384",
        descriptionFr = "Basculement fluide entre Wi-Fi et 5G sans perte de session.",
        descriptionEn = "Seamless handoff between Wi-Fi and 5G without dropping sessions.",
        descriptionEs = "Cambio fluido entre Wi-Fi y 5G sin pérdida de sesión.",
        mtu = 1380
    );

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> descriptionFr
        AppLanguage.EN -> descriptionEn
        AppLanguage.ES -> descriptionEs
    }
}

data class VpnServer(
    val id: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val region: ServerRegion,
    val virtualIpPrefix: String,
    val endpointHost: String,
    val relayPort: Int = 51820,
    val tunnelSubnetIndex: Int = 0,
    val baseLatencyMs: Int,
    val measuredLatencyMs: Int = baseLatencyMs,
    val loadPercent: Int,
    val isMultiHopReady: Boolean = true,
    val bandwidthGbps: Int = 10
)

data class CountryServerGroup(
    val countryCode: String,
    val country: String,
    val flagEmoji: String,
    val region: ServerRegion,
    val servers: List<VpnServer>
)

data class AppRoutingItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val isSystemOrInstalled: Boolean = true,
    val isSelectedForSplit: Boolean = false
)

object GlobalServersCatalog {
    const val OVH_MASTER_IPV4 = "213.32.22.5"
    const val OVH_MASTER_IPV6 = "2001:41d0:305:2100::ada5"
    const val OVH_MASTER_HOSTNAME = "vps-e379a1a0.vps.ovh.net"
    const val OVH_SERVER_PUBLIC_KEY = "FYAMLKeeWfVk+p5sLFXKlZvHWdWa8DQDskqgepNiTWE="
    const val OVH_CLIENT_PUBLIC_KEY = "kHDyi7lLEXzJhFhqoBrrSvCI7qLIUMPLKbzxpSZdKmA="

    val servers = listOf(
        VpnServer(
            id = "fr-par-01",
            city = "Gravelines / Paris",
            country = "France",
            countryCode = "FR",
            flagEmoji = "🇫🇷",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "213.32.22",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51820,
            tunnelSubnetIndex = 0,
            baseLatencyMs = 11,
            loadPercent = 12,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "cm-dla-01",
            city = "Douala",
            country = "Cameroun",
            countryCode = "CM",
            flagEmoji = "🇨🇲",
            region = ServerRegion.MEA,
            virtualIpPrefix = "154.72.168",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51821,
            tunnelSubnetIndex = 1,
            baseLatencyMs = 34,
            loadPercent = 16,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "cm-yao-01",
            city = "Yaoundé",
            country = "Cameroun",
            countryCode = "CM",
            flagEmoji = "🇨🇲",
            region = ServerRegion.MEA,
            virtualIpPrefix = "154.72.172",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51822,
            tunnelSubnetIndex = 2,
            baseLatencyMs = 39,
            loadPercent = 14,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "ch-zur-01",
            city = "Zurich",
            country = "Suisse",
            countryCode = "CH",
            flagEmoji = "🇨🇭",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "194.42.110",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51823,
            tunnelSubnetIndex = 3,
            baseLatencyMs = 16,
            loadPercent = 18,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "de-fra-01",
            city = "Francfort",
            country = "Allemagne",
            countryCode = "DE",
            flagEmoji = "🇩🇪",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "89.163.220",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51824,
            tunnelSubnetIndex = 4,
            baseLatencyMs = 18,
            loadPercent = 31,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "nl-ams-01",
            city = "Amsterdam",
            country = "Pays-Bas",
            countryCode = "NL",
            flagEmoji = "🇳🇱",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "146.70.88",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51825,
            tunnelSubnetIndex = 5,
            baseLatencyMs = 19,
            loadPercent = 29,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "gb-lon-01",
            city = "Londres",
            country = "Royaume-Uni",
            countryCode = "GB",
            flagEmoji = "🇬🇧",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "212.102.51",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51826,
            tunnelSubnetIndex = 6,
            baseLatencyMs = 21,
            loadPercent = 35,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "is-rek-01",
            city = "Reykjavik",
            country = "Islande",
            countryCode = "IS",
            flagEmoji = "🇮🇸",
            region = ServerRegion.EUROPE,
            virtualIpPrefix = "37.235.49",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51827,
            tunnelSubnetIndex = 7,
            baseLatencyMs = 38,
            loadPercent = 12,
            bandwidthGbps = 10
        ),
        VpnServer(
            id = "us-nyc-01",
            city = "New York",
            country = "États-Unis",
            countryCode = "US",
            flagEmoji = "🇺🇸",
            region = ServerRegion.AMERICAS,
            virtualIpPrefix = "64.44.102",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51828,
            tunnelSubnetIndex = 8,
            baseLatencyMs = 68,
            loadPercent = 41,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "us-sjc-01",
            city = "Silicon Valley",
            country = "États-Unis",
            countryCode = "US",
            flagEmoji = "🇺🇸",
            region = ServerRegion.AMERICAS,
            virtualIpPrefix = "104.28.14",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51829,
            tunnelSubnetIndex = 9,
            baseLatencyMs = 118,
            loadPercent = 33,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "ca-mtl-01",
            city = "Montréal",
            country = "Canada",
            countryCode = "CA",
            flagEmoji = "🇨🇦",
            region = ServerRegion.AMERICAS,
            virtualIpPrefix = "192.99.180",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51830,
            tunnelSubnetIndex = 10,
            baseLatencyMs = 74,
            loadPercent = 22,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "br-gru-01",
            city = "São Paulo",
            country = "Brésil",
            countryCode = "BR",
            flagEmoji = "🇧🇷",
            region = ServerRegion.AMERICAS,
            virtualIpPrefix = "177.54.144",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51831,
            tunnelSubnetIndex = 11,
            baseLatencyMs = 136,
            loadPercent = 27,
            bandwidthGbps = 10
        ),
        VpnServer(
            id = "jp-tyo-01",
            city = "Tokyo",
            country = "Japon",
            countryCode = "JP",
            flagEmoji = "🇯🇵",
            region = ServerRegion.ASIA_PACIFIC,
            virtualIpPrefix = "103.114.190",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51832,
            tunnelSubnetIndex = 12,
            baseLatencyMs = 152,
            loadPercent = 30,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "sg-sin-01",
            city = "Singapour",
            country = "Singapour",
            countryCode = "SG",
            flagEmoji = "🇸🇬",
            region = ServerRegion.ASIA_PACIFIC,
            virtualIpPrefix = "138.199.21",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51833,
            tunnelSubnetIndex = 13,
            baseLatencyMs = 144,
            loadPercent = 26,
            bandwidthGbps = 40
        ),
        VpnServer(
            id = "kr-sel-01",
            city = "Séoul",
            country = "Corée du Sud",
            countryCode = "KR",
            flagEmoji = "🇰🇷",
            region = ServerRegion.ASIA_PACIFIC,
            virtualIpPrefix = "121.78.90",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51834,
            tunnelSubnetIndex = 14,
            baseLatencyMs = 159,
            loadPercent = 19,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "au-syd-01",
            city = "Sydney",
            country = "Australie",
            countryCode = "AU",
            flagEmoji = "🇦🇺",
            region = ServerRegion.ASIA_PACIFIC,
            virtualIpPrefix = "45.121.209",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51835,
            tunnelSubnetIndex = 15,
            baseLatencyMs = 178,
            loadPercent = 21,
            bandwidthGbps = 20
        ),
        VpnServer(
            id = "ae-dxb-01",
            city = "Dubaï",
            country = "Émirats Arabes Unis",
            countryCode = "AE",
            flagEmoji = "🇦🇪",
            region = ServerRegion.MEA,
            virtualIpPrefix = "185.193.36",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51836,
            tunnelSubnetIndex = 16,
            baseLatencyMs = 89,
            loadPercent = 25,
            bandwidthGbps = 10
        ),
        VpnServer(
            id = "za-jnb-01",
            city = "Johannesbourg",
            country = "Afrique du Sud",
            countryCode = "ZA",
            flagEmoji = "🇿🇦",
            region = ServerRegion.MEA,
            virtualIpPrefix = "196.240.54",
            endpointHost = OVH_MASTER_IPV4,
            relayPort = 51837,
            tunnelSubnetIndex = 17,
            baseLatencyMs = 148,
            loadPercent = 15,
            bandwidthGbps = 10
        )
    )

    fun getCountryGroups(): List<CountryServerGroup> {
        return servers.groupBy { it.countryCode }.map { (_, srvList) ->
            val first = srvList.first()
            CountryServerGroup(
                countryCode = first.countryCode,
                country = first.country,
                flagEmoji = first.flagEmoji,
                region = first.region,
                servers = srvList.sortedBy { it.measuredLatencyMs }
            )
        }
    }
}
