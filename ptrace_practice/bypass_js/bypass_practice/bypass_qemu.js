Java.perform(function () {

    const SystemProperties =
        Java.use("android.os.SystemProperties");

    SystemProperties.get
        .overload("java.lang.String")
        .implementation = function (key) {

        if (key === "ro.kernel.qemu") {
            console.log("[+] bypass qemu property");
            return "0";
        }

        return this.get(key);
    };
});
