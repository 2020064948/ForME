Java.perform(function () {

    const Build = Java.use("android.os.Build");

    Build.HARDWARE.value = "qcom";
    Build.PRODUCT.value = "SM-S918N";
    Build.MODEL.value = "SM-S918N";
    Build.MANUFACTURER.value = "samsung";
    Build.BRAND.value = "samsung";
    Build.DEVICE.value = "dm3q";
    Build.BOARD.value = "kalama";
    Build.TAGS.value = "release-keys";
    Build.FINGERPRINT.value =
        "samsung/dm3qxxx/dm3q:14/UP1A.231005.007/S918NKSU2BXB1:user/release-keys";

    console.log("[+] Build.* spoofed");
});
