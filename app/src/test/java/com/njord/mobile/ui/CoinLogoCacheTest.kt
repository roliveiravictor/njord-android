package com.njord.mobile.ui

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CoinLogoCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun writeThenRead_returnsSameBytes() {
        val cacheDir = temporaryFolder.newFolder("cache")
        val bytes = byteArrayOf(1, 2, 3, 4)

        assertTrue(CoinLogoCache.write(cacheDir, "BTC", bytes))

        assertArrayEquals(bytes, CoinLogoCache.read(cacheDir, "BTC"))
    }

    @Test
    fun fileFor_sanitizesSymbolIntoCoinLogoDirectory() {
        val cacheDir = temporaryFolder.newFolder("cache")
        val file = CoinLogoCache.fileFor(cacheDir, "BtC / UsD")

        assertEquals("btc___usd.img", file.name)
        assertEquals("coin-logos", file.parentFile?.name)
        assertEquals(cacheDir, file.parentFile?.parentFile)
    }

    @Test
    fun delete_removesExistingCachedLogo() {
        val cacheDir = temporaryFolder.newFolder("cache")
        val bytes = byteArrayOf(5, 6, 7)
        CoinLogoCache.write(cacheDir, "ETH", bytes)

        assertTrue(CoinLogoCache.delete(cacheDir, "ETH"))

        assertFalse(CoinLogoCache.fileFor(cacheDir, "ETH").exists())
    }

    @Test
    fun read_missingFileReturnsNull() {
        val cacheDir = temporaryFolder.newFolder("cache")

        assertNull(CoinLogoCache.read(cacheDir, "SOL"))
    }
}
