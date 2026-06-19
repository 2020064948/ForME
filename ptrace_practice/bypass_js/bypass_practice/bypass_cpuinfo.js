const libc = Process.getModuleByName("libc.so");

const fakeCpuinfoPath = "/data/local/tmp/fake_cpuinfo";

function safeReadCString(ptr) {
    try {
        if (ptr.isNull()) {
            return "";
        }
        return ptr.readUtf8String();
    } catch (e) {
        return "";
    }
}

function hookOpenLike(name, pathArgIndex) {
    let addr = null;

    try {
        addr = libc.getExportByName(name);
    } catch (e) {
        console.log("[-] " + name + " not found");
        return;
    }

    Interceptor.attach(addr, {
        onEnter(args) {
            const path = safeReadCString(args[pathArgIndex]);

            if (path.indexOf("/proc/cpuinfo") >= 0) {
                console.log("[+] redirect " + name + ": " + path);
                args[pathArgIndex] = Memory.allocUtf8String(fakeCpuinfoPath);
            }
        }
    });

    console.log("[+] hooked " + name);
}

hookOpenLike("open", 0);
hookOpenLike("open64", 0);
hookOpenLike("openat", 1);
