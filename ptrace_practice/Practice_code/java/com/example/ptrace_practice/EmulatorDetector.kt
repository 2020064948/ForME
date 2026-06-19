package com.example.ptrace_practice

import android.os.Build
import java.io.File

object EmulatorDetector {

    fun check(): String {
        val result = StringBuilder()
        var suspiciousCount = 0

        result.append("[Emulator Detection]\n\n")

        val cpuInfo = readCpuInfo()
        result.append("1. /proc/cpuinfo check\n")

        val cpuKeywords = listOf("goldfish", "ranchu", "qemu", "hypervisor", "intel", "amd")
        val cpuHit = cpuKeywords.any { cpuInfo.contains(it, ignoreCase = true) }

        if (cpuHit) {
            suspiciousCount++
            result.append("Suspicious CPU info detected.\n")
        } else {
            result.append("CPU info looks normal.\n")
        }

        result.append("\n2. Build.* check\n")

        val buildValues = listOf(
            "FINGERPRINT" to Build.FINGERPRINT,
            "MODEL" to Build.MODEL,
            "MANUFACTURER" to Build.MANUFACTURER,
            "BRAND" to Build.BRAND,
            "DEVICE" to Build.DEVICE,
            "PRODUCT" to Build.PRODUCT,
            "HARDWARE" to Build.HARDWARE,
            "BOARD" to Build.BOARD,
            "TAGS" to Build.TAGS
        )

        val buildKeywords = listOf(
            "generic", "sdk", "emulator", "goldfish", "ranchu",
            "x86", "x86_64", "google_sdk", "aosp", "test-keys", "dev-keys"
        )

        for ((name, value) in buildValues) {
            result.append("$name = $value\n")

            if (buildKeywords.any { value.contains(it, ignoreCase = true) }) {
                suspiciousCount++
                result.append("  -> suspicious\n")
            }
        }

        result.append("\n3. ro.kernel.qemu check\n")

        val qemuValue = getSystemProperty("ro.kernel.qemu")
        result.append("ro.kernel.qemu = $qemuValue\n")

        if (qemuValue == "1") {
            suspiciousCount++
            result.append("  -> QEMU based emulator detected.\n")
        } else {
            result.append("  -> ro.kernel.qemu not detected.\n")
        }

        result.append("\n4. Emulator file check\n")

        val emulatorFiles = listOf(
            "/dev/qemu_pipe",
            "/dev/qemu_trace",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )

        for (path in emulatorFiles) {
            val exists = File(path).exists()
            result.append("$path exists = $exists\n")

            if (exists) {
                suspiciousCount++
            }
        }

        result.append("\n[Final Result]\n")
        result.append("Suspicious score = $suspiciousCount\n")

        result.append(
            when {
                suspiciousCount >= 3 -> "에뮬레이터 환경으로 강하게 의심됩니다."
                suspiciousCount >= 1 -> "에뮬레이터 가능성이 있습니다."
                else -> "일반 실제 기기 환경으로 보입니다."
            }
        )

        return result.toString()
    }

    private fun readCpuInfo(): String {
        return try {
            File("/proc/cpuinfo").readText()
        } catch (e: Exception) {
            "failed to read /proc/cpuinfo"
        }
    }

    private fun getSystemProperty(name: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java)
            getMethod.invoke(null, name) as String
        } catch (e: Exception) {
            "unavailable"
        }
    }
}