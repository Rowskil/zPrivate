package com.example

import com.example.crypto.Aes256CryptoEngine
import com.example.model.GlobalServersCatalog
import com.example.model.SplitTunnelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testAes256EncryptionAndIntegrity() {
        val engine = Aes256CryptoEngine()
        val plainText = "zPrivate-Secure-Payload-12345"
        val frame = engine.encryptAndVerifyFrame(plainText.toByteArray(Charsets.UTF_8))
        assertNotNull(frame)
        assertTrue(frame.verifiedDecryption)
        assertEquals(plainText.toByteArray(Charsets.UTF_8).size, frame.plainByteSize)
    }

    @Test
    fun testGlobalServersCatalogContainsCameroon() {
        val servers = GlobalServersCatalog.servers
        val cameroonServers = servers.filter { it.countryCode == "CM" }
        assertTrue("Cameroon servers must be present in catalog", cameroonServers.isNotEmpty())
        assertTrue("Must contain Douala", cameroonServers.any { it.city == "Douala" })
        assertTrue("Must contain Yaoundé", cameroonServers.any { it.city == "Yaoundé" })
    }

    @Test
    fun testSplitTunnelModes() {
        assertEquals(3, SplitTunnelMode.entries.size)
    }
}
