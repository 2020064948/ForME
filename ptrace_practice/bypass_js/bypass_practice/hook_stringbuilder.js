Java.perform(function () {
    console.log("[+] Safe StringBuilder hook loaded");

    const StringBuilder = Java.use("java.lang.StringBuilder");

    let inside = false;

    StringBuilder.toString.implementation = function () {
        const result = this.toString();

        if (!inside && isInteresting(result)) {
            inside = true;

            try {
                console.log("[StringBuilder.toString] " + result);
                printStack();
            } finally {
                inside = false;
            }
        }

        return result;
    };

    function isInteresting(s) {
        if (s === null || s === undefined) return false;
        if (s.length < 4) return false;

        const lower = s.toLowerCase();

        return (
            lower.includes("http") ||
            lower.includes("api") ||
            lower.includes("key") ||
            lower.includes("login") ||
            lower.includes("token") ||
            lower.includes("secret") ||
            lower.includes("payload")
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
