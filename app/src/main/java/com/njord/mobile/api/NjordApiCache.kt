package com.njord.mobile.api

import java.io.File

enum class ApiCacheKey(val fileName: String) {
    Home("home.json"),
    Activity("activity.json"),
    Live("live.json"),
    PortfolioAll("portfolio-all.json"),
    PortfolioBigBang("portfolio-big-bang.json"),
    PortfolioWcr("portfolio-wcr.json"),
    PortfolioHunch("portfolio-hunch.json"),
    Logs("logs.json"),
    Heartbeat("heartbeat.json"),
    HunchReport("hunch-report-latest.json")
}

object NjordApiCache {
    private const val CacheDirectoryName = "api-cache"

    fun read(filesDir: File, key: ApiCacheKey): String? =
        runCatching {
            val file = fileFor(filesDir, key)
            if (!file.exists()) return@runCatching null
            file.readText()
        }.getOrNull()

    fun write(filesDir: File, key: ApiCacheKey, body: String): Boolean =
        runCatching {
            val file = fileFor(filesDir, key)
            file.parentFile?.mkdirs()
            file.writeText(body)
            true
        }.getOrDefault(false)

    fun delete(filesDir: File, key: ApiCacheKey): Boolean =
        runCatching {
            val file = fileFor(filesDir, key)
            !file.exists() || file.delete()
        }.getOrDefault(false)

    fun fileFor(filesDir: File, key: ApiCacheKey): File =
        File(File(filesDir, CacheDirectoryName), key.fileName)
}
