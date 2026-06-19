Java.perform(function () {
    console.log("[+] Generic string/decryption discovery hook loaded");

    // 1. byte[] -> String 생성 감시
    const StringCls = Java.use("java.lang.String");

    StringCls.$init.overload("[B").implementation = function (bytes) {
        const ret = this.$init(bytes);
        const value = this.toString();

        if (isInteresting(value)) {
            console.log("[String(byte[])] " + value);
            printStack();
        }

        return ret;
    };

    StringCls.$init.overload("[B", "java.lang.String").implementation = function (bytes, charset) {
        const ret = this.$init(bytes, charset);
        const value = this.toString();

        if (isInteresting(value)) {
            console.log("[String(byte[], charset)] " + value);
            console.log("charset = " + charset);
            printStack();
        }

        return ret;
    };

    // 2. Base64 decode 감시
    try {
        const Base64 = Java.use("android.util.Base64");

        Base64.decode.overload("java.lang.String", "int").implementation = function (input, flags) {
            console.log("[Base64.decode] input = " + input);

            const out = this.decode(input, flags);

            try {
                const decoded = StringCls.$new(out);
                if (isInteresting(decoded.toString())) {
                    console.log("[Base64.decode] decoded = " + decoded);
                    printStack();
                }
            } catch (e) {}

            return out;
        };
    } catch (e) {
        console.log("[-] Base64 hook failed: " + e);
    }

    // 3. javax.crypto.Cipher.doFinal 감시
    try {
        const Cipher = Java.use("javax.crypto.Cipher");

        Cipher.doFinal.overload("[B").implementation = function (input) {
            const out = this.doFinal(input);

            try {
                const decoded = StringCls.$new(out);
                if (isInteresting(decoded.toString())) {
                    console.log("[Cipher.doFinal] output = " + decoded);
                    printStack();
                }
            } catch (e) {}

            return out;
        };
    } catch (e) {
        console.log("[-] Cipher hook failed: " + e);
    }

    // 4. ClassLoader.loadClass 감시: 의심 클래스 찾기
    const ClassLoader = Java.use("java.lang.ClassLoader");

    ClassLoader.loadClass.overload("java.lang.String").implementation = function (name) {
        const ret = this.loadClass(name);

        if (
            name.toLowerCase().includes("crypt") ||
            name.toLowerCase().includes("sec") ||
            name.toLowerCase().includes("util") ||
            name.toLowerCase().includes("string") ||
            name.toLowerCase().includes("payload")
        ) {
            console.log("[loadClass] suspicious class = " + name);
        }

        return ret;
    };

    function isInteresting(s) {
        if (s === null || s === undefined) return false;
        if (s.length < 4) return false;

        const lower = s.toLowerCase();

        return (
            lower.includes("http") ||
            lower.includes("api") ||
            lower.includes("key") ||
            lower.includes("token") ||
            lower.includes("secret") ||
            lower.includes("login") ||
            lower.includes("payload") ||
            lower.includes(".dex") ||
            lower.includes(".so") ||
            lower.includes("com.")
        );
    }

    function printStack() {
        try {
            const Exception = Java.use("java.lang.Exception");
            const Log = Java.use("android.util.Log");
            console.log(Log.getStackTraceString(Exception.$new()));
        } catch (e) {
            console.log("[-] stack failed: " + e);
        }
    }
});
