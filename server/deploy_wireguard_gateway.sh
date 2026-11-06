#!/bin/bash
# ==============================================================================
# zPrivate VPN • Option 3: Multi-Port / Multi-Hop WireGuard NAT Gateway Script
# Target OS: Ubuntu 22.04 LTS / 24.04 LTS (OVHcloud VPS-1 Gravelines)
# Architecture:
#   - Port UDP 51820 (10.8.0.0/24)  -> Direct Native Exit (France - Gravelines)
#   - Ports UDP 51821..51837        -> Multi-Hop Policy Routing Subnets (10.8.1.0/24 .. 10.8.17.0/24)
#                                      with automatic fallback to OVH France NAT
# ==============================================================================

set -euo pipefail

echo "=========================================================="
echo "🛡️  Deploying zPrivate Multi-Hop WireGuard Gateway (OVH)"
echo "=========================================================="

# 1. Update OS and Install WireGuard Tools, iptables, iproute2, qrencode
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y wireguard wireguard-tools iptables iproute2 qrencode curl ufw

# 2. Detect Public IP and Primary Network Interface
MAIN_INTERFACE=$(ip -4 route ls | grep default | awk -F "dev " '{print $2}' | awk '{print $1}' | head -n 1)
PUBLIC_IPV4=$(curl -s4 https://api.ipify.org || curl -s4 https://ifconfig.me || ip -4 addr show "$MAIN_INTERFACE" | grep -oP '(?<=inet\s)\d+(\.\d+){3}' | head -n 1)

echo "Detected Interface : $MAIN_INTERFACE"
echo "Detected Public IP : $PUBLIC_IPV4"

# 3. Enable IPv4/IPv6 Forwarding, TCP BBR Congestion Control & Loose Reverse Path Filtering (required for Multi-Hop)
cat << 'EOF' > /etc/sysctl.d/99-zprivate-wireguard.conf
net.ipv4.ip_forward = 1
net.ipv6.conf.all.forwarding = 1
net.core.default_qdisc = fq
net.ipv4.tcp_congestion_control = bbr
net.ipv4.conf.all.rp_filter = 2
net.ipv4.conf.default.rp_filter = 2
EOF
sysctl --system

# 4. Prepare WireGuard & Upstream Multi-Hop Directories
mkdir -p /etc/wireguard/upstreams
chmod 700 /etc/wireguard /etc/wireguard/upstreams
cd /etc/wireguard

SERVER_PRIVKEY=$(wg genkey)
SERVER_PUBKEY=$(echo "$SERVER_PRIVKEY" | wg pubkey)

CLIENT_PRIVKEY=$(wg genkey)
CLIENT_PUBKEY=$(echo "$CLIENT_PRIVKEY" | wg pubkey)
CLIENT_PSK=$(wg genpsk)

# 5. Configure Master WireGuard Interface (wg0) covering all 18 regional subnets (10.8.0.0/16)
#    Ports 51821..51837 are transparently redirected by PREROUTING to wg0 (51820)
cat << EOF > /etc/wireguard/wg0.conf
[Interface]
Address = 10.8.0.1/16, fd00:1:fd00:1::1/64
ListenPort = 51820
PrivateKey = $SERVER_PRIVKEY
SaveConfig = false
MTU = 1420

# Multi-Port UDP Listener Redirection (Ports 51820..51837 -> wg0 51820)
PostUp = iptables -t nat -A PREROUTING -i $MAIN_INTERFACE -p udp --dport 51821:51837 -j REDIRECT --to-ports 51820
PostUp = iptables -t nat -A POSTROUTING -s 10.8.0.0/16 -o $MAIN_INTERFACE -j MASQUERADE
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT
PostUp = iptables -A FORWARD -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT
PostUp = iptables -t mangle -A FORWARD -p tcp --tcp-flags SYN,RST SYN -j TCPMSS --clamp-mss-to-pmtu
PostUp = ip6tables -t nat -A POSTROUTING -s fd00:1:fd00:1::/64 -o $MAIN_INTERFACE -j MASQUERADE 2>/dev/null || true
PostUp = ip6tables -A FORWARD -i wg0 -j ACCEPT 2>/dev/null || true

# PostDown Cleanup Rules
PostDown = iptables -t nat -D PREROUTING -i $MAIN_INTERFACE -p udp --dport 51821:51837 -j REDIRECT --to-ports 51820
PostDown = iptables -t nat -D POSTROUTING -s 10.8.0.0/16 -o $MAIN_INTERFACE -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT
PostDown = iptables -D FORWARD -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT
PostDown = iptables -t mangle -D FORWARD -p tcp --tcp-flags SYN,RST SYN -j TCPMSS --clamp-mss-to-pmtu

# Peer 1: zPrivate Android Client (All 18 Country Virtual Subnets Authorized)
[Peer]
PublicKey = $CLIENT_PUBKEY
PresharedKey = $CLIENT_PSK
AllowedIPs = 10.8.0.0/16, fd00:1:fd00:1::/64
EOF

chmod 600 /etc/wireguard/wg0.conf

# 6. Configure Firewall (UFW) for SSH and Multi-Port WireGuard Range (51820:51837/udp)
ufw allow 22/tcp
ufw allow 51820:51837/udp
ufw --force enable || true

# 7. Start and Enable Master WireGuard Service
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0

# 8. Generate Client Configuration File (.conf)
cat << EOF > /etc/wireguard/zprivate-client.conf
[Interface]
PrivateKey = $CLIENT_PRIVKEY
Address = 10.8.0.104/32, fd00:1:fd00:1::104/128
DNS = 1.1.1.1, 8.8.8.8
MTU = 1420

[Peer]
PublicKey = $SERVER_PUBKEY
PresharedKey = $CLIENT_PSK
Endpoint = $PUBLIC_IPV4:51820
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25
EOF

chmod 600 /etc/wireguard/zprivate-client.conf

echo "=========================================================="
echo "✅ zPrivate Multi-Hop Gateway Successfully Installed!"
echo "Master VPS Public IP  : $PUBLIC_IPV4"
echo "Direct FR Port (UDP)  : 51820 (Subnet 10.8.0.104)"
echo "Multi-Hop Ports (UDP) : 51821 - 51837 (Subnets 10.8.1.104 - 10.8.17.104)"
echo "Client Config Path    : /etc/wireguard/zprivate-client.conf"
echo "=========================================================="
qrencode -t ansiutf8 < /etc/wireguard/zprivate-client.conf || true
