Java.perform(function () {
    console.log("[+] Dex dump hook loaded");

    const DexClassLoader = Java.use("dalvik.system.DexClassLoader");
    const File = Java.use("java.io.File");
    const FileInputStream = Java.use("java.io.FileInputStream");
    const FileOutputStream = Java.use("java.io.FileOutputStream");

    let lastDexPath = null;

    DexClassLoader.$init.overload(
        "java.lang.String",
        "java.lang.String",
        "java.lang.String",
        "java.lang.ClassLoader"
    ).implementation = function (dexPath, optimizedDir, libPath, parent) {
        console.log("[+] DexClassLoader called");
        console.log("    dexPath = " + dexPath);
        console.log("    optimizedDir = " + optimizedDir);

        lastDexPath = dexPath;

        return this.$init(dexPath, optimizedDir, libPath, parent);
    };

    File.delete.implementation = function () {
        const path = this.getAbsolutePath();

        console.log("[+] File.delete called: " + path);

        if (path.indexOf("payload.dex") >= 0 || path === lastDexPath) {
            const dumpPath = "/data/data/com.example.ptrace_practice/files/dumped_payload.dex";

            console.log("[+] target dex delete detected");
            console.log("[+] copying before delete...");
            copyFile(path, dumpPath);
            console.log("[+] copied to: " + dumpPath);
        }

        return this.delete();
    };

    function copyFile(srcPath, dstPath) {
        const input = FileInputStream.$new(srcPath);
        const output = FileOutputStream.$new(dstPath);

        const buffer = Java.array("byte", new Array(4096).fill(0));

        let len;
        while ((len = input.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }

        input.close();
        output.close();
    }
});
