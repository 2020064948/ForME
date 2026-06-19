Java.perform(function () {
    const File = Java.use("java.io.File");

    File.exists.implementation = function () {
        const path = this.getAbsolutePath();

        if (
            path === "/dev/qemu_pipe" ||
            path === "/dev/qemu_trace" ||
            path === "/sys/qemu_trace" ||
            path === "/system/bin/qemu-props" ||
            path === "/system/lib/libc_malloc_debug_qemu.so"
        ) {
            console.log("[+] hide emulator file: " + path);
            return false;
        }

        return this.exists();
    };
});
