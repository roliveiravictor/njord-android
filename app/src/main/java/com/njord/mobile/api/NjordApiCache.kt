package com.njord.mobile.api

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

enum class ApiCacheKey(val fileName: String) {
    Home("home.json"),
    Activity("activity.json"),
    Live("live.json"),
    LiveBigBang("live-big-bang.json"),
    LiveWcr("live-wcr.json"),
    LiveHunch("live-hunch.json"),
    PerformanceAll("performance-all.json"),
    PerformanceBigBang("performance-big-bang.json"),
    PerformanceWcr("performance-wcr.json"),
    PerformanceHunch("performance-hunch.json"),
    Logs("logs.json"),
    Heartbeat("heartbeat.json"),
    HunchReport("hunch-report-latest.json"),
    Incidents("incidents.json")
}

object NjordApiCache {
    private const val CacheDirectoryName = "api-cache"
    private const val DefaultMaxAgeMillis = 60 * 1000L

    fun read(filesDir: File, key: ApiCacheKey): String? =
        runCatching {
            val file = fileFor(filesDir, key)
            if (!file.exists()) return@runCatching null
            file.readText()
        }.getOrNull()

    fun readFresh(filesDir: File, key: ApiCacheKey, maxAgeMillis: Long = DefaultMaxAgeMillis): String? =
        runCatching {
            val file = fileFor(filesDir, key)
            if (!file.exists()) return@runCatching null
            val ageMillis = System.currentTimeMillis() - file.lastModified()
            if (ageMillis > maxAgeMillis) {
                return@runCatching null
            }
            file.readText()
        }.getOrNull()

    fun write(filesDir: File, key: ApiCacheKey, body: String): Boolean =
        runCatching {
            val file = fileFor(filesDir, key)
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            tempFile.writeText(body)
            moveAtomically(tempFile, file)
            true
        }.getOrDefault(false)

    fun delete(filesDir: File, key: ApiCacheKey): Boolean =
        runCatching {
            val file = fileFor(filesDir, key)
            !file.exists() || file.delete()
        }.getOrDefault(false)

    fun fileFor(filesDir: File, key: ApiCacheKey): File =
        File(File(filesDir, CacheDirectoryName), key.fileName)

    private fun moveAtomically(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
