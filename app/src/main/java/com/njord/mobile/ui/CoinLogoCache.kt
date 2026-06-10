package com.njord.mobile.ui

import java.io.File

internal object CoinLogoCache {
    private const val CacheDirectoryName = "coin-logos"

    fun read(cacheDir: File, symbol: String): ByteArray? =
        runCatching {
            val file = fileFor(cacheDir, symbol)
            if (!file.isFile) return@runCatching null
            file.readBytes()
        }.getOrNull()

    fun write(cacheDir: File, symbol: String, bytes: ByteArray): Boolean =
        runCatching {
            val file = fileFor(cacheDir, symbol)
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            tempFile.writeBytes(bytes)
            if (tempFile.renameTo(file)) {
                true
            } else {
                tempFile.delete()
                false
            }
        }.getOrDefault(false)

    fun delete(cacheDir: File, symbol: String): Boolean =
        runCatching {
            val file = fileFor(cacheDir, symbol)
            !file.exists() || file.delete()
        }.getOrDefault(false)

    fun fileFor(cacheDir: File, symbol: String): File {
        val safeSymbol = symbol
            .lowercase()
            .replace(Regex("[^a-z0-9._-]"), "_")
            .ifBlank { "unknown" }

        return File(File(cacheDir, CacheDirectoryName), "$safeSymbol.img")
    }
}
