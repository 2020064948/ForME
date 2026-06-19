package com.example.ptrace_practice

import android.os.Build
import java.io.File

object RootDetector {

    fun check(): String {
        val result = StringBuilder()
        var suspiciousScore = 0

        result.append("[Root Detection]\n\n")

        // ------------------------------------------------
        // 1. su binary detection
        // ------------------------------------------------
        result.append("1. su binary check\n")

        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/su/bin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/data/local/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su"
        )

        var suDetected = false

        for (path in suPaths) {
            val exists = File(path).exists()

            result.append("$path exists = $exists\n")

            if (exists) {
                suDetected = true
            }
        }

        if (suDetected) {
            suspiciousScore++
            result.append("-> su binary detected\n")
        }

        result.append("\n")

        // ------------------------------------------------
        // 2. Magisk detection
        // ------------------------------------------------
        result.append("2. Magisk check\n")

        val magiskPaths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/cache/.disable_magisk",
            "/init.magisk.rc"
        )

        var magiskDetected = false

        for (path in magiskPaths) {
            val exists = File(path).exists()

            result.append("$path exists = $exists\n")

            if (exists) {
                magiskDetected = true
            }
        }

        if (magiskDetected) {
            suspiciousScore++
            result.append("-> Magisk traces detected\n")
        }

        result.append("\n")

        // ------------------------------------------------
        // 3. test-keys detection
        // ------------------------------------------------
        result.append("3. test-keys check\n")

        val tags = Build.TAGS ?: ""

        result.append("Build.TAGS = $tags\n")

        if (tags.contains("test-keys")) {
            suspiciousScore++
            result.append("-> test-keys detected\n")
        } else {
            result.append("-> release-keys\n")
        }

        result.append("\n")

        // ------------------------------------------------
        // 4. writable path detection
        // ------------------------------------------------
        result.append("4. writable system path check\n")

        val writablePaths = listOf(
            "/system",
            "/system/bin",
            "/vendor",
            "/vendor/bin"
        )

        var writableDetected = false

        for (path in writablePaths) {
            val file = File(path)

            val canWrite = file.canWrite()

            result.append("$path writable = $canWrite\n")

            if (canWrite) {
                writableDetected = true
            }
        }

        if (writableDetected) {
            suspiciousScore++
            result.append("-> writable protected path detected\n")
        }

        result.append("\n")

        // ------------------------------------------------
        // Final Result
        // ------------------------------------------------
        result.append("[Final Result]\n")
        result.append("Suspicious score = $suspiciousScore\n")

        result.append(
            when {
                suspiciousScore >= 3 ->
                    "루팅 환경으로 강하게 의심됩니다."

                suspiciousScore >= 1 ->
                    "루팅 가능성이 있습니다."

                else ->
                    "일반 비루팅 환경으로 보입니다."
            }
        )

        return result.toString()
    }
}