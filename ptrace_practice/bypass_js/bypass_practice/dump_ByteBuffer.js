Java.perform(function () {
    const IMDCL = Java.use("dalvik.system.InMemoryDexClassLoader");

    IMDCL.$init.overload("java.nio.ByteBuffer", "java.lang.ClassLoader")
        .implementation = function (buffer, parent) {
            console.log("[+] InMemoryDexClassLoader called");
            console.log("[+] buffer = " + buffer);

            return this.$init(buffer, parent);
        };
});

Java.perform(function () {
    const IMDCL = Java.use("dalvik.system.InMemoryDexClassLoader");

    IMDCL.$init.overload("java.nio.ByteBuffer", "java.lang.ClassLoader")
        .implementation = function (buffer, parent) {
            console.log("[+] InMemoryDexClassLoader called");

            const duplicate = buffer.duplicate();
            duplicate.position(0);

            const size = duplicate.remaining();
            const byteArray = Java.array("byte", new Array(size).fill(0));

            duplicate.get(byteArray);

            const outPath = "/data/data/com.example.ptrace_practice/files/dumped_inmemory.dex";
            const FileOutputStream = Java.use("java.io.FileOutputStream");
            const fos = FileOutputStream.$new(outPath);

            fos.write(byteArray);
            fos.close();

            console.log("[+] dumped dex to: " + outPath);
            console.log("[+] size = " + size);

            return this.$init(buffer, parent);
        };
});
