package com.example.ptrace_practice

import android.content.Context
import dalvik.system.InMemoryDexClassLoader
import java.nio.ByteBuffer

object InMemoryDexPractice {

    private const val KEY = 0x37

    fun run(context: Context): String {
        val result = StringBuilder()

        result.append("[InMemoryDexClassLoader Practice]\n\n")

        val encryptedBytes = context.assets.open("payload.enc").use {
            it.readBytes()
        }

        result.append("payload.enc loaded from assets\n")
        result.append("encrypted size = ${encryptedBytes.size} bytes\n")

        val decryptedBytes = ByteArray(encryptedBytes.size)

        for (i in encryptedBytes.indices) {
            decryptedBytes[i] = (encryptedBytes[i].toInt() xor KEY).toByte()
        }

        result.append("payload decrypted in memory\n")
        result.append("No payload.dex file was written to disk\n\n")

        val dexBuffer = ByteBuffer.wrap(decryptedBytes)

        val loader = InMemoryDexClassLoader(
            dexBuffer,
            context.classLoader
        )

        result.append("InMemoryDexClassLoader created\n")

        val clazz = loader.loadClass("com.lab.payload.Entry")
        result.append("Class loaded: ${clazz.name}\n")

        val method = clazz.getDeclaredMethod("run")
        result.append("Method loaded: ${method.name}\n")

        val output = method.invoke(null) as String

        result.append("\nPayload result:\n")
        result.append(output)

        return result.toString()
    }
}