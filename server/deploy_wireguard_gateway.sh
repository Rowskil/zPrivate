#!/bin/bash
# ==============================================================================
# zPrivate VPN • WireGuard NAT Relay Server Deployment Script
# Target OS: Ubuntu 22.04 LTS / 24.04 LTS
# Protocol: WireGuard Kernel Module + iptables NAT Forwarding
# Cipher: ChaCha20-Poly1305 / AES-256-GCM Handshake
# ==============================================================================

set -euo pipefail

echo "=========================================================="
echo "🛡️  Deploying zPrivate WireGuard NAT Gateway..."
echo "=========================================================="

# 1. Update OS and Install WireGuard, iptables, qrencode
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y wireguard iptables iptables-persistent qrencode curl ufw

# 2. Detect Public IP and Primary Network Interface
MAIN_INTERFACE=$(ip -4 route ls | grep default | awk -F "dev " '{print $2}' | awk '{print $1}' | head -n 1)
PUBLIC_IPV4=$(curl -s4 https://api.ipify.org || curl -s4 https://ifconfig.me || ip -4 addr show "$MAIN_INTERFACE" | grep -oP '(?<=inet\s)\d+(\.\d+){3}' | head -n 1)

echo "Detected Interface: $MAIN_INTERFACE"
echo "Detected Public IP : $PUBLIC_IPV4"

# 3. Enable IPv4 and IPv6 Forwarding in Kernel
cat << 'EOF' > /etc/sysctl.d/99-zprivate-wireguard.conf
net.ipv4.ip_forward = 1
net.ipv6.conf.all.forwarding = 1
net.core.default_qdisc = fq
net.ipv4.tcp_congestion_control = bbr
EOF
sysctl --system

# 4. Generate WireGuard Cryptographic Server Keys
mkdir -p /etc/wireguard
chmod 700 /etc/wireguard
cd /etc/wireguard

SERVER_PRIVKEY=$(wg genkey)
SERVER_PUBKEY=$(echo "$SERVER_PRIVKEY" | wg pubkey)

# 5. Generate Initial Android Client Keypair
CLIENT_PRIVKEY=$(wg genkey)
CLIENT_PUBKEY=$(echo "$CLIENT_PRIVKEY" | wg pubkey)
CLIENT_PSK=$(wg genpsk)

# 6. Configure WireGuard Interface (wg0) with NAT Masquerading
cat << EOF > /etc/wireguard/wg0.conf
[Interface]
Address = 10.8.0.1/24, fd00:1:fd00:1::1/64
ListenPort = 51820
PrivateKey = $SERVER_PRIVKEY
SaveConfig = false

# PostUp NAT Rules for Full Internet Tunneling
PostUp = iptables -t nat -A POSTROUTING -s 10.8.0.0/24 -o $MAIN_INTERFACE -j MASQUERADE
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT
PostUp = iptables -A FORWARD -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT
PostUp = ip6tables -t nat -A POSTROUTING -s fd00:1:fd00:1::/64 -o $MAIN_INTERFACE -j MASQUERADE 2>/dev/null || true
PostUp = ip6tables -A FORWARD -i wg0 -j ACCEPT 2>/dev/null || true

# PostDown Cleanup Rules
PostDown = iptables -t nat -D POSTROUTING -s 10.8.0.0/24 -o $MAIN_INTERFACE -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT
PostDown = iptables -D FORWARD -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

# Peer 1: zPrivate Android Client
[Peer]
PublicKey = $CLIENT_PUBKEY
PresharedKey = $CLIENT_PSK
AllowedIPs = 10.8.0.104/32, fd00:1:fd00:1::104/128
EOF

chmod 600 /etc/wireguard/wg0.conf

# 7. Configure Firewall (UFW)
ufw allow 22/tcp
ufw allow 51820/udp
ufw --force enable || true

# 8. Start and Enable WireGuard Systemd Service
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0

# 9. Generate Client Configuration File (.conf)
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
echo "✅ zPrivate WireGuard NAT Gateway Successfully Installed!"
echo "Server Public IP : $PUBLIC_IPV4"
echo "Server WireGuard Port : 51820 (UDP)"
echo "Client Config Path    : /etc/wireguard/zprivate-client.conf"
echo "=========================================================="
qrencode -t ansiutf8 < /etc/wireguard/zprivate-client.conf || true
