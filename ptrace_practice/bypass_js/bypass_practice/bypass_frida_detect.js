Java.perform(function () {
    const MainActivity = Java.use("com.example.ptrace_practice.MainActivity");

    MainActivity.checkFridaPort.implementation = function () {
        return "[Frida Port Scan]\nResult: Frida port not detected.";
    };

    MainActivity.checkFridaMaps.implementation = function () {
        return "[Frida Maps Scan]\nResult: Frida-related maps entry not detected.";
    };

    MainActivity.checkFridaThreads.implementation = function () {
        return "[Frida Thread Scan]\nResult: Frida-related thread not detected.";
    };

    MainActivity.checkFridaSegments.implementation = function () {
        return "[Frida Segment Signature Scan]\nResult: Frida-like signature not detected.";
    };
});
