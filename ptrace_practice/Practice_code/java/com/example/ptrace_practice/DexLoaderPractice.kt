package com.example.ptrace_practice

import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File

object DexLoaderPractice {

    private const val KEY = 0x37

    fun run(context: Context): String {
        val result = StringBuilder()

        result.append("[Encrypted Dex + DexClassLoader Practice]\n\n")

        val encryptedBytes = context.assets.open("payload.enc").use {
            it.readBytes()
        }

        result.append("payload.enc loaded from assets\n")
        result.append("encrypted size = ${encryptedBytes.size} bytes\n")

        val decryptedBytes = ByteArray(encryptedBytes.size)

        for (i in encryptedBytes.indices) {
            decryptedBytes[i] = (encryptedBytes[i].toInt() xor KEY).toByte()
        }

        val payloadDex = File(context.filesDir, "payload.dex")

        payloadDex.writeBytes(decryptedBytes)

        result.append("payload.dex decrypted to:\n")
        result.append(payloadDex.absolutePath)
        result.append("\n")

        val optimizedDir = File(context.codeCacheDir, "dex_opt")
        if (!optimizedDir.exists()) {
            optimizedDir.mkdirs()
        }

        val loader = DexClassLoader(
            payloadDex.absolutePath,
            optimizedDir.absolutePath,
            null,
            context.classLoader
        )

        result.append("DexClassLoader created\n")

        val clazz = loader.loadClass("com.lab.payload.Entry")
        result.append("Class loaded: ${clazz.name}\n")

        val method = clazz.getDeclaredMethod("run")
        result.append("Method loaded: ${method.name}\n")

        val output = method.invoke(null) as String

        result.append("\nPayload result:\n")
        result.append(output)
        result.append("\n\n")

        val deleted = payloadDex.delete()

        result.append("payload.dex deleted after load = $deleted\n")
        result.append("payload.dex exists after delete = ${payloadDex.exists()}\n")

        return result.toString()
    }
}