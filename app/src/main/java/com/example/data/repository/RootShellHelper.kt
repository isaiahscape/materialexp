package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

data class RootResult(
    val isSuccess: Boolean,
    val output: String,
    val error: String
)

object RootShellHelper {

    private val SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/sd/xbin/su",
        "/system/usr/we-need-root/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/data/adb/ksu/bin/su",
        "/data/adb/magisk/magisk"
    )

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val hasSuBinary = SU_PATHS.any { File(it).exists() }
        if (hasSuBinary) return@withContext true

        val result = executeSuCommand("id")
        result.isSuccess && result.output.contains("uid=0")
    }

    suspend fun checkMagiskOrKernelSuInstalled(): String = withContext(Dispatchers.IO) {
        if (File("/data/adb/ksu").exists()) return@withContext "KernelSU Detected"
        if (File("/data/adb/magisk").exists()) return@withContext "Magisk Installed"
        
        val result = executeSuCommand("magisk -v")
        if (result.isSuccess && result.output.isNotBlank()) {
            return@withContext "Magisk (${result.output.trim()})"
        }

        val ksuResult = executeSuCommand("ksu --version")
        if (ksuResult.isSuccess && ksuResult.output.isNotBlank()) {
            return@withContext "KernelSU (${ksuResult.output.trim()})"
        }

        if (isRootAvailable()) "Generic Root (SU)" else "Not Rooted"
    }

    suspend fun executeSuCommand(command: String): RootResult = withContext(Dispatchers.IO) {
        var process: Process? = null
        var os: DataOutputStream? = null
        var stdoutReader: BufferedReader? = null
        var stderrReader: BufferedReader? = null

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdout = StringBuilder()
            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                stdout.append(line).append("\n")
            }

            val stderr = StringBuilder()
            while (stderrReader.readLine().also { line = it } != null) {
                stderr.append(line).append("\n")
            }

            val exitVal = process.waitFor()
            RootResult(
                isSuccess = exitVal == 0,
                output = stdout.toString().trim(),
                error = stderr.toString().trim()
            )
        } catch (e: Exception) {
            RootResult(
                isSuccess = false,
                output = "",
                error = e.localizedMessage ?: "Failed to execute root command"
            )
        } finally {
            runCatching { os?.close() }
            runCatching { stdoutReader?.close() }
            runCatching { stderrReader?.close() }
            runCatching { process?.destroy() }
        }
    }

    suspend fun readRootFile(path: String): String = withContext(Dispatchers.IO) {
        val result = executeSuCommand("cat '$path'")
        if (result.isSuccess) result.output else "Error reading root file: ${result.error}"
    }

    suspend fun writeRootFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val escaped = content.replace("'", "'\\''")
        val result = executeSuCommand("echo '$escaped' > '$path'")
        result.isSuccess
    }

    suspend fun listRootDirectory(path: String): List<String> = withContext(Dispatchers.IO) {
        val result = executeSuCommand("ls -1a '$path'")
        if (result.isSuccess) {
            result.output.lines().filter { it.isNotBlank() && it != "." && it != ".." }
        } else {
            emptyList()
        }
    }
}
