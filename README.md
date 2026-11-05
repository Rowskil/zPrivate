# 🛡️ zPrivate VPN (v1.0.0)

> Application VPN sécurisée 100% fonctionnelle avec protocole **WireGuard®** haute vitesse et chiffrement **AES-256-GCM**.
> Conçue selon les standards **Google Material Design 3** et les principes d'interaction tactile haute précision (ressorts fluides, retours haptiques, architecture zéro-reflow).

---

## ✨ Points Clés & Fonctionnalités

- **Connexion en 1 Clic :** Activation instantanée avec négociation de tunnel WireGuard ultra-rapide et indicateur de bouclier dynamique.
- **Chiffrement AES-256-GCM :** Protection cryptographique complète de chaque paquet réseau entrant et sortant.
- **Réseau Mondial de Serveurs :** Présence en Europe, Amérique du Nord, Amérique du Sud, Asie-Pacifique, Moyen-Orient et **Afrique (Douala et Yaoundé, Cameroun 🇨🇲)**.
- **Tunnels Fractionnés (Split Tunneling) :** Gestion fine par application (avec support préconfiguré de PayPal, Pinterest, Instagram, Google Maps, etc.).
- **Kill Switch Automatique & Lockdown Strict :** Coupe immédiatement tout le trafic réseau non chiffré en cas d'instabilité ou de coupure du tunnel.
- **Widget Android Interactif Adaptatif (3x2, 2x1, 4x2) :** Bascule ON/OFF directe en 1 clic, switch de serveur (◀ / ▶) et télémétrie de débit en temps réel sur l'écran d'accueil du smartphone.
- **Détection Automatique d'Absence de Connexion :** Bannière d'alerte animée informant l'utilisateur en cas de déconnexion réseau physique.
- **Système de Thèmes & Couleurs :**
  - Thème système par défaut (`AppThemePreference.SYSTEM`).
  - Couleur d'accentuation par défaut : **Indigo Royal** (`#6366F1`), complétée par Bleu zPrivate, Émeraude et Neutre Ardoise.
- **Mises à Jour In-App via GitHub Releases :** Vérification automatique et manuelle depuis l'application avec téléchargement direct du nouvel APK.

---

## 🚀 GitHub Actions & CI/CD

Le dépôt intègre deux flux automatisés dans `.github/workflows/` :

1. **`build-android-apk.yml` :**
   - Se déclenche automatiquement à chaque `push` sur la branche principale ou lors de la publication d'un tag de version (`v*`).
   - Compile l'application via Gradle (`assembleRelease` et `assembleDebug`).
   - Met à disposition l'artefact APK dans les Actions GitHub et publie automatiquement une Release GitHub avec l'APK téléchargeable.

2. **`build-ios.yml` :**
   - Exécuté sur les machines `macos-14` avec Xcode pour valider et compiler les composants réseau iOS (`NEPacketTunnelProvider`).

---

## 📦 Système de Mise à Jour Automatique

À chaque nouveau commit incrémentant le tag de version sur GitHub (ex: `v1.0.1`) :
1. L'Action GitHub génère automatiquement la release avec les fichiers APK.
2. L'utilisateur reçoit une alerte dans l'application zPrivate lui proposant de mettre à jour directement l'application.
3. Le menu **Réglages** permet également de forcer la vérification via le bouton **"Vérifier"**.

---

## 🌐 Déploiement de la Passerelle Serveur Réelle (Relais NAT WireGuard)

Pour que l'IP publique vue par les sites distants corresponde au pays du serveur choisi, déployez le script fourni dans le dossier `server/` sur votre VPS Ubuntu (ex: OVH, Hetzner, DigitalOcean, AWS) :

```bash
# 1. Rendre le script exécutable
chmod +x server/deploy_wireguard_gateway.sh

# 2. Lancer le déploiement automatisé
sudo ./server/deploy_wireguard_gateway.sh
```

Ce script configure :
- Le module noyau WireGuard (`wg-quick@wg0`).
- L'activation de l'IP Forwarding (`sysctl net.ipv4.ip_forward=1`).
- La règle de translation d'adresses réseau :
  `iptables -t nat -A POSTROUTING -s 10.8.0.0/24 -o <eth0> -j MASQUERADE`.
- La génération des clés cryptographiques serveur et client Android/iOS.
- Une stack Docker Compose alternative (`server/docker-compose.yml`) pour déploiement en conteneur.

---

## 🛠️ Signature Officielle

- **Nom :** zPrivate VPN
- **Version :** v1.0.0 (Build 1)
- **Conçu par :** Anthony zaMer
- **Protocole :** WireGuard® • AES-256-GCM

