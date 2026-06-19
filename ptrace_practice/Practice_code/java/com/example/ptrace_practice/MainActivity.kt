package com.example.ptrace_practice

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Debug
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.ptrace_practice.BuildConfig
import android.os.Build

class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("ptrace_native")
        }
    }

    external fun checkTracerPid(): Int
    external fun checkPtrace(): Int
    external fun checkHiddenPtrace(): Int

    external fun testSigIll(): Int
    external fun testSigSegv(): Int
    external fun testSigTrap(): Int

    external fun checkFridaPort(): String
    external fun checkFridaMaps(): String
    external fun checkFridaThreads(): String
    external fun checkFridaSegments(): String

    private fun readCpuInfo(): String {
        return try {
            java.io.File("/proc/cpuinfo").readText()
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
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultText = TextView(this)
        resultText.textSize = 18f
        resultText.text = "Anti-debug practice\n"

        //Buttons
        val tracerButton = Button(this)
        tracerButton.text = "Check TracerPid"

        val ptraceButton = Button(this)
        ptraceButton.text = "Check ptrace"

        val hiddenPtraceButton = Button(this)
        hiddenPtraceButton.text = "Check hidden ptrace"

        val sigIllButton = Button(this)
        sigIllButton.text = "Test SIGILL"

        val sigSegvButton = Button(this)
        sigSegvButton.text = "Test SIGSEGV"

        val sigTrapButton = Button(this)
        sigTrapButton.text = "Test SIGTRAP"

        val androidDebugButton = Button(this)
        androidDebugButton.text = "Check Android Debug Flags"

        val fridaPortButton = Button(this)
        fridaPortButton.text = "Check Frida Port"

        val fridaMapsButton = Button(this)
        fridaMapsButton.text = "Check Frida Maps"

        val fridaThreadButton = Button(this)
        fridaThreadButton.text = "Check Frida Threads"

        val fridaSegmentButton = Button(this)
        fridaSegmentButton.text = "Check Frida Segments"

        val emulatorButton = Button(this)
        emulatorButton.text = "Check Emulator"

        val rootButton = Button(this)
        rootButton.text = "Check Root"

        val stringCryptoButton = Button(this)
        stringCryptoButton.text = "Check String Crypto"

        val dexLoaderButton = Button(this)
        dexLoaderButton.text = "Run Encrypted Dex Loader"

        val inMemoryDexButton = Button(this)
        inMemoryDexButton.text = "Run InMemoryDexClassLoader"

        //Listeners
        tracerButton.setOnClickListener {
            val tracerPid = checkTracerPid()
            resultText.text = if (tracerPid == 0) {
                "TracerPid = $tracerPid\n디버거가 attach되지 않았습니다."
            } else {
                "TracerPid = $tracerPid\n디버거가 attach된 것으로 의심됩니다."
            }
        }

        ptraceButton.setOnClickListener {
            val result = checkPtrace()
            resultText.text = if (result == 0) {
                "ptrace result = $result\nptrace 성공: 디버거가 먼저 attach된 상태는 아닌 것으로 보입니다."
            } else {
                "ptrace result = $result\nptrace 실패: 디버거 attach 또는 정책 차단 가능성이 있습니다."
            }
        }

        hiddenPtraceButton.setOnClickListener {
            val result = checkHiddenPtrace()
            resultText.text = if (result == 0) {
                "hidden ptrace result = $result\nhidden ptrace 성공: 디버거가 먼저 attach된 상태는 아닌 것으로 보입니다."
            } else {
                "hidden ptrace result = $result\nhidden ptrace 실패: 디버거 attach 또는 정책 차단 가능성이 있습니다."
            }
        }

        sigIllButton.setOnClickListener {
            val result = testSigIll()
            resultText.text = if (result == 1) {
                "SIGILL handler 호출 성공\n정상 실행 환경으로 보입니다."
            } else {
                "SIGILL handler 호출 실패\n디버거 또는 분석 도구가 시그널을 가로챘을 가능성이 있습니다."
            }
        }

        sigSegvButton.setOnClickListener {
            val result = testSigSegv()
            resultText.text = if (result == 1) {
                "SIGSEGV handler 호출 성공\n정상 실행 환경으로 보입니다."
            } else {
                "SIGSEGV handler 호출 실패\n디버거 또는 분석 도구가 시그널을 가로챘을 가능성이 있습니다."
            }
        }

        sigTrapButton.setOnClickListener {
            val result = testSigTrap()
            resultText.text = if (result == 1) {
                "SIGTRAP handler 호출 성공\n정상 실행 환경으로 보입니다."
            } else {
                "SIGTRAP handler 호출 실패\n디버거 또는 분석 도구가 시그널을 가로챘을 가능성이 있습니다."
            }
        }

        androidDebugButton.setOnClickListener {
            val isDebuggerConnected = Debug.isDebuggerConnected()

            val isDebuggable =
                (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

            val isBuildConfigDebug = BuildConfig.DEBUG

            val message = StringBuilder()

            message.append("Debug.isDebuggerConnected() = ")
            message.append(isDebuggerConnected)
            message.append("\n")

            message.append("ApplicationInfo.FLAG_DEBUGGABLE = ")
            message.append(isDebuggable)
            message.append("\n")

            message.append("BuildConfig.DEBUG = ")
            message.append(isBuildConfigDebug)
            message.append("\n\n")

            if (isDebuggerConnected) {
                message.append("Java/JDWP 디버거가 현재 연결된 것으로 보입니다.\n")
            } else {
                message.append("현재 Java/JDWP 디버거 연결은 감지되지 않았습니다.\n")
            }

            if (isDebuggable) {
                message.append("이 APK는 debuggable=true 상태입니다.\n")
            } else {
                message.append("이 APK는 debuggable=false 상태입니다.\n")
            }

            if (isBuildConfigDebug) {
                message.append("현재 빌드는 Debug build입니다.")
            } else {
                message.append("현재 빌드는 Release build입니다.")
            }

            resultText.text = message.toString()
        }

        fridaPortButton.setOnClickListener {
            resultText.text = checkFridaPort()
        }

        fridaMapsButton.setOnClickListener {
            resultText.text = checkFridaMaps()
        }

        fridaThreadButton.setOnClickListener {
            resultText.text = checkFridaThreads()
        }

        fridaSegmentButton.setOnClickListener {
            resultText.text = checkFridaSegments()
        }

        emulatorButton.setOnClickListener {
            resultText.text = EmulatorDetector.check()
        }

        rootButton.setOnClickListener {
            resultText.text = RootDetector.check()
        }

        stringCryptoButton.setOnClickListener {
            resultText.text = StringCrypto.check()
        }

        dexLoaderButton.setOnClickListener {
            resultText.text = DexLoaderPractice.run(this)
        }

        inMemoryDexButton.setOnClickListener {
            resultText.text = InMemoryDexPractice.run(this)
        }


        //layouts
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 80, 40, 40)

        layout.addView(resultText)
        layout.addView(tracerButton)
        layout.addView(ptraceButton)
        layout.addView(hiddenPtraceButton)
        layout.addView(sigIllButton)
        layout.addView(sigSegvButton)
        layout.addView(sigTrapButton)
        layout.addView(androidDebugButton)
        layout.addView(fridaPortButton)
        layout.addView(fridaMapsButton)
        layout.addView(fridaThreadButton)
        layout.addView(fridaSegmentButton)
        layout.addView(emulatorButton)
        layout.addView(rootButton)
        layout.addView(stringCryptoButton)
        layout.addView(dexLoaderButton)
        layout.addView(inMemoryDexButton)

        val scrollView = android.widget.ScrollView(this)
        scrollView.addView(layout)

        setContentView(scrollView)
    }
}