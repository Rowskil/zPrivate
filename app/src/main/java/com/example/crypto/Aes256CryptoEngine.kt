package com.example.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedPacketFrame(
    val ivHex: String,
    val cipherTextHex: String,
    val authTagHex: String,
    val plainByteSize: Int,
    val encryptedByteSize: Int,
    val encryptionDurationMicros: Long,
    val keyFingerprintSha256: String,
    val verifiedDecryption: Boolean
)

/**
 * Real AES-256-GCM Cryptographic Engine using javax.crypto.Cipher ("AES/GCM/NoPadding")
 * with 256-bit keys, 96-bit IV nonces, and 128-bit GCM authentication tags.
 */
class Aes256CryptoEngine {
    private val secureRandom = SecureRandom()
    @Volatile
    private var activeSessionKey: SecretKey = generateAes256Key()
    @Volatile
    private var keyRotationCount: Int = 1
    @Volatile
    private var totalEncryptedPackets: Long = 0L
    @Volatile
    private var totalEncryptedBytes: Long = 0L

    private fun generateAes256Key(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    @Synchronized
    fun rotateSessionKey(): String {
        activeSessionKey = generateAes256Key()
        keyRotationCount += 1
        return getActiveKeyFingerprint()
    }

    fun getKeyRotationCount(): Int = keyRotationCount

    fun getTotalEncryptedPackets(): Long = totalEncryptedPackets

    fun getTotalEncryptedBytes(): Long = totalEncryptedBytes

    fun getActiveKeyFingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(activeSessionKey.encoded)
        return hash.take(12).joinToString(":") { "%02X".format(it) }
    }

    /**
     * Encrypts raw packet bytes using AES-256-GCM (96-bit IV + 128-bit Auth Tag)
     * and immediately verifies integrity via authenticated decryption.
     */
    fun encryptAndVerifyFrame(payloadBytes: ByteArray, associatedData: ByteArray = "zPrivate-AES256-GCM-v1".toByteArray()): EncryptedPacketFrame {
        val startNanos = System.nanoTime()
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        encryptCipher.init(Cipher.ENCRYPT_MODE, activeSessionKey, gcmSpec)
        encryptCipher.updateAAD(associatedData)
        val cipherBytesWithTag = encryptCipher.doFinal(payloadBytes)

        // Verify authenticated decryption (AEAD GCM tag check)
        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, activeSessionKey, gcmSpec)
        decryptCipher.updateAAD(associatedData)
        val decryptedBytes = decryptCipher.doFinal(cipherBytesWithTag)
        val elapsedMicros = ((System.nanoTime() - startNanos) / 1_000L).coerceAtLeast(1L)

        val isVerified = payloadBytes.contentEquals(decryptedBytes)
        totalEncryptedPackets += 1
        totalEncryptedBytes += cipherBytesWithTag.size

        // In GCM with 128-bit tag, the last 16 bytes are the authentication tag
        val tagStartIndex = (cipherBytesWithTag.size - 16).coerceAtLeast(0)
        val cipherBody = cipherBytesWithTag.copyOfRange(0, tagStartIndex)
        val authTag = cipherBytesWithTag.copyOfRange(tagStartIndex, cipherBytesWithTag.size)

        return EncryptedPacketFrame(
            ivHex = iv.toHex(),
            cipherTextHex = cipherBody.toHex(maxBytes = 24),
            authTagHex = authTag.toHex(),
            plainByteSize = payloadBytes.size,
            encryptedByteSize = cipherBytesWithTag.size + iv.size,
            encryptionDurationMicros = elapsedMicros,
            keyFingerprintSha256 = getActiveKeyFingerprint(),
            verifiedDecryption = isVerified
        )
    }

    /**
     * Raw byte buffer encryption used by the local VPN tunnel packet loop.
     */
    fun encryptRawPacket(packet: ByteArray, length: Int): ByteArray {
        val slice = packet.copyOfRange(0, length)
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, activeSessionKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(slice)
        totalEncryptedPackets += 1
        totalEncryptedBytes += encrypted.size
        return iv + encrypted
    }

    private fun ByteArray.toHex(maxBytes: Int = this.size): String {
        val count = minOf(this.size, maxBytes)
        val hex = this.take(count).joinToString("") { "%02x".format(it) }
        return if (this.size > maxBytes) "$hex…" else hex
    }
}
