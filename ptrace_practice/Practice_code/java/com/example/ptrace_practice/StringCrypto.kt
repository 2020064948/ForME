package com.example.ptrace_practice

object StringCrypto {

    private const val KEY: Int = 0x37

    private val encryptedServerUrl = intArrayOf(
        95, 67, 67, 71, 68, 13, 24, 24, 82, 79, 86, 90, 71, 91, 82, 25, 84, 88, 90, 24, 91, 88, 80, 94, 89
    )

    private val encryptedApiKey = intArrayOf(
        102, 103, 126, 104, 124, 114, 110, 104, 102, 101, 100, 99, 98
    )

    fun decrypt(data: IntArray): String {
        val builder = StringBuilder()

        for (value in data) {
            val decodedChar = (value xor KEY).toChar()
            builder.append(decodedChar)
        }

        return builder.toString()
    }

    fun getServerUrl(): String {
        return decrypt(encryptedServerUrl)
    }

    fun getApiKey(): String {
        return decrypt(encryptedApiKey)
    }

    fun check(): String {
        val serverUrl = getServerUrl()
        val apiKey = getApiKey()

        return """
            [String Encryption Practice]
            
            encryptedServerUrl = ${encryptedServerUrl.joinToString(", ")}
            decryptedServerUrl = $serverUrl
            
            encryptedApiKey = ${encryptedApiKey.joinToString(", ")}
            decryptedApiKey = $apiKey
        """.trimIndent()
    }
}