package com.njord.mobile.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NjordApiCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun writeThenRead_returnsOriginalJson() {
        val filesDir = temporaryFolder.newFolder("files")
        val body = """{"strategies":[],"heartbeat":{"healthy":0,"total":0,"late_count":0}}"""

        assertTrue(NjordApiCache.write(filesDir, ApiCacheKey.Home, body))

        assertEquals(body, NjordApiCache.read(filesDir, ApiCacheKey.Home))
        assertFalse(NjordApiCache.fileFor(filesDir, ApiCacheKey.Home).resolveSibling("home.json.tmp").exists())
    }

    @Test
    fun readFresh_recentFile_returnsOriginalJson() {
        val filesDir = temporaryFolder.newFolder("files")
        val body = """{"cycles":[]}"""

        assertTrue(NjordApiCache.write(filesDir, ApiCacheKey.Activity, body))

        assertEquals(body, NjordApiCache.readFresh(filesDir, ApiCacheKey.Activity))
    }

    @Test
    fun readFresh_staleFileReturnsNullAndKeepsCache() {
        val filesDir = temporaryFolder.newFolder("files")
        val body = """{"cycles":[]}"""

        assertTrue(NjordApiCache.write(filesDir, ApiCacheKey.Activity, body))
        val file = NjordApiCache.fileFor(filesDir, ApiCacheKey.Activity)
        assertTrue(file.setLastModified(System.currentTimeMillis() - 61 * 1000L))

        assertNull(NjordApiCache.readFresh(filesDir, ApiCacheKey.Activity))
        assertTrue(file.exists())
        assertEquals(body, NjordApiCache.read(filesDir, ApiCacheKey.Activity))
    }

    @Test
    fun fileFor_usesApiCacheDirectoryAndStableFileName() {
        val filesDir = temporaryFolder.newFolder("files")
        val file = NjordApiCache.fileFor(filesDir, ApiCacheKey.HunchReport)

        assertEquals("hunch-report-latest.json", file.name)
        assertEquals("api-cache", file.parentFile?.name)
        assertEquals(filesDir, file.parentFile?.parentFile)
    }

    @Test
    fun fileFor_live_usesDedicatedCacheFileName() {
        val filesDir = temporaryFolder.newFolder("files")
        val file = NjordApiCache.fileFor(filesDir, ApiCacheKey.Live)

        assertEquals("live.json", file.name)
        assertEquals("api-cache", file.parentFile?.name)
        assertEquals(filesDir, file.parentFile?.parentFile)
    }

    @Test
    fun delete_removesCachedEndpointFile() {
        val filesDir = temporaryFolder.newFolder("files")
        NjordApiCache.write(filesDir, ApiCacheKey.Logs, """{"entries":[]}""")

        assertTrue(NjordApiCache.delete(filesDir, ApiCacheKey.Logs))

        assertFalse(NjordApiCache.fileFor(filesDir, ApiCacheKey.Logs).exists())
    }

    @Test
    fun read_missingFile_returnsNull() {
        val filesDir = temporaryFolder.newFolder("files")

        assertNull(NjordApiCache.read(filesDir, ApiCacheKey.Activity))
    }

    @Test
    fun read_whenCachePathIsDirectory_returnsNull() {
        val filesDir = temporaryFolder.newFolder("files")
        NjordApiCache.fileFor(filesDir, ApiCacheKey.Heartbeat).mkdirs()

        assertNull(NjordApiCache.read(filesDir, ApiCacheKey.Heartbeat))
    }
}
