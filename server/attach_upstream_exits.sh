#!/bin/bash
# ==============================================================================
# zPrivate VPN • Option 3: Upstream International Exit Node Attacher
# Usage:
#   Place standard WireGuard exit configs (Mullvad, IVPN, ProtonVPN, etc.) in:
#   /etc/wireguard/upstreams/<country_code>.conf (e.g., us.conf, jp.conf, cm.conf)
#   Then run: sudo bash server/attach_upstream_exits.sh
#
# Mapping between zPrivate App Country Subnets and Routing Tables:
#   Index 0  (10.8.0.0/24  - UDP 51820) : FR (Paris / Gravelines Direct OVH)
#   Index 1  (10.8.1.0/24  - UDP 51821) : CM (Douala)       -> table 101 (cm.conf)
#   Index 2  (10.8.2.0/24  - UDP 51822) : CM (Yaoundé)      -> table 102 (cm.conf)
#   Index 3  (10.8.3.0/24  - UDP 51823) : CH (Zurich)       -> table 103 (ch.conf)
#   Index 4  (10.8.4.0/24  - UDP 51824) : DE (Francfort)    -> table 104 (de.conf)
#   Index 5  (10.8.5.0/24  - UDP 51825) : NL (Amsterdam)    -> table 105 (nl.conf)
#   Index 6  (10.8.6.0/24  - UDP 51826) : GB (Londres)      -> table 106 (gb.conf)
#   Index 7  (10.8.7.0/24  - UDP 51827) : IS (Reykjavik)    -> table 107 (is.conf)
#   Index 8  (10.8.8.0/24  - UDP 51828) : US (New York)     -> table 108 (us.conf)
#   Index 9  (10.8.9.0/24  - UDP 51829) : US (Silicon Val.) -> table 109 (us-west.conf)
#   Index 10 (10.8.10.0/24 - UDP 51830) : CA (Montréal)     -> table 110 (ca.conf)
#   Index 11 (10.8.11.0/24 - UDP 51831) : BR (São Paulo)    -> table 111 (br.conf)
#   Index 12 (10.8.12.0/24 - UDP 51832) : JP (Tokyo)        -> table 112 (jp.conf)
#   Index 13 (10.8.13.0/24 - UDP 51833) : SG (Singapour)    -> table 113 (sg.conf)
#   Index 14 (10.8.14.0/24 - UDP 51834) : KR (Séoul)        -> table 114 (kr.conf)
#   Index 15 (10.8.15.0/24 - UDP 51835) : AU (Sydney)       -> table 115 (au.conf)
#   Index 16 (10.8.16.0/24 - UDP 51836) : AE (Dubaï)        -> table 116 (ae.conf)
#   Index 17 (10.8.17.0/24 - UDP 51837) : ZA (Johannesbourg)-> table 117 (za.conf)
# ==============================================================================

set -euo pipefail

UPSTREAM_DIR="/etc/wireguard/upstreams"
mkdir -p "$UPSTREAM_DIR"

declare -A SUBNET_COUNTRY=(
    [1]="cm"
    [2]="cm"
    [3]="ch"
    [4]="de"
    [5]="nl"
    [6]="gb"
    [7]="is"
    [8]="us"
    [9]="us"
    [10]="ca"
    [11]="br"
    [12]="jp"
    [13]="sg"
    [14]="kr"
    [15]="au"
    [16]="ae"
    [17]="za"
)

echo "=========================================================="
echo "🌐 Attaching Multi-Hop International Exit Tunnels..."
echo "=========================================================="

for idx in "${!SUBNET_COUNTRY[@]}"; do
    cc="${SUBNET_COUNTRY[$idx]}"
    conf_file="$UPSTREAM_DIR/${cc}.conf"
    iface="wg-${cc}"
    table_id=$((100 + idx))
    subnet="10.8.${idx}.0/24"

    if [[ -f "$conf_file" ]]; then
        echo "✔ Found upstream config for [${cc^^}] -> Routing $subnet via $iface (Table $table_id)"

        # Ensure Table = off is set in the upstream config so it doesn't overwrite the VPS default route
        if ! grep -q "^Table" "$conf_file"; then
            sed -i '/\[Interface\]/a Table = off' "$conf_file"
        fi

        cp "$conf_file" "/etc/wireguard/${iface}.conf"
        chmod 600 "/etc/wireguard/${iface}.conf"

        # Bring up the upstream WireGuard interface if not already up
        if ! ip link show "$iface" >/dev/null 2>&1; then
            wg-quick up "$iface"
        fi

        # Add NAT Masquerading on the upstream interface
        iptables -t nat -C POSTROUTING -s "$subnet" -o "$iface" -j MASQUERADE 2>/dev/null || \
            iptables -t nat -A POSTROUTING -s "$subnet" -o "$iface" -j MASQUERADE
        iptables -C FORWARD -i wg0 -o "$iface" -j ACCEPT 2>/dev/null || \
            iptables -A FORWARD -i wg0 -o "$iface" -j ACCEPT
        iptables -C FORWARD -i "$iface" -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT 2>/dev/null || \
            iptables -A FORWARD -i "$iface" -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

        # Policy-Based Routing: send traffic from 10.8.<idx>.0/24 through wg-<cc>
        ip route replace default dev "$iface" table "$table_id"
        ip rule del from "$subnet" table "$table_id" 2>/dev/null || true
        ip rule add from "$subnet" table "$table_id" priority "$((1000 + idx))"
    else
        echo "ℹ No upstream config at $conf_file -> $subnet automatically uses OVH France Direct NAT"
    fi
done

echo "=========================================================="
echo "✅ Multi-Hop Policy Routing Synchronized!"
echo "=========================================================="
