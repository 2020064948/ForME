#include <jni.h>
#include <android/log.h>

#include <sys/ptrace.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <signal.h>
#include <setjmp.h>
#include <unistd.h>

#define LOG_TAG "PTRACE_NATIVE"

#define LOGI(...) \
__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define LOGW(...) \
__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkTracerPid(
        JNIEnv *env,
        jobject thiz) {

    FILE *fp = fopen("/proc/self/status", "r");

    if (fp == nullptr) {
        LOGW("failed to open /proc/self/status");
        return -1;
    }

    char line[256];
    int tracerPid = 0;

    while (fgets(line, sizeof(line), fp)) {

        if (strncmp(line, "TracerPid:", 10) == 0) {

            tracerPid = atoi(line + 10);
            break;
        }
    }

    fclose(fp);

    LOGI("TracerPid = %d", tracerPid);

    return tracerPid;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkPtrace(
        JNIEnv *env,
        jobject thiz) {

    errno = 0;

    long result =
            ptrace(PTRACE_TRACEME,
                    0,
                    nullptr,
                    nullptr);

    if (result == -1) {

        LOGW("ptrace failed errno = %d", errno);

        return -1;
    }

    LOGI("ptrace success");

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkHiddenPtrace(
        JNIEnv *env,
        jobject thiz) {

    void *handle =
            dlopen("libc.so", RTLD_NOW);

    if (handle == nullptr) {

        LOGW("dlopen failed");

        return -2;
    }

    typedef long (*ptrace_func_t)(
            int,
            pid_t,
            void *,
            void *);

    ptrace_func_t hidden_ptrace =
            (ptrace_func_t)dlsym(
                    handle,
                    "ptrace");

    if (hidden_ptrace == nullptr) {

        LOGW("dlsym failed");

        dlclose(handle);

        return -3;
    }

    errno = 0;

    long result =
            hidden_ptrace(
                    PTRACE_TRACEME,
                    0,
                    nullptr,
                    nullptr);

    if (result == -1) {

        LOGW("hidden ptrace failed errno = %d",
             errno);

        dlclose(handle);

        return -1;
    }

    LOGI("hidden ptrace success");

    dlclose(handle);

    return 0;
}

static sigjmp_buf jump_buffer;
static volatile sig_atomic_t signal_handled = 0;

static void signal_handler(int signo) {
    signal_handled = 1;
    siglongjmp(jump_buffer, 1);
}

static int run_signal_test(int signo) {
    signal_handled = 0;

    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));

    sa.sa_handler = signal_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;

    struct sigaction old_sa;
    memset(&old_sa, 0, sizeof(old_sa));

    if (sigaction(signo, &sa, &old_sa) != 0) {
        LOGW("sigaction failed for signal %d", signo);
        return -1;
    }

    if (sigsetjmp(jump_buffer, 1) == 0) {
        raise(signo);
    }

    sigaction(signo, &old_sa, nullptr);

    if (signal_handled == 1) {
        LOGI("signal %d handler was called", signo);
        return 1;
    } else {
        LOGW("signal %d handler was not called", signo);
        return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_testSigIll(
        JNIEnv *env,
        jobject thiz) {
    return run_signal_test(SIGILL);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_testSigSegv(
        JNIEnv *env,
        jobject thiz) {
    return run_signal_test(SIGSEGV);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ptrace_1practice_MainActivity_testSigTrap(
        JNIEnv *env,
        jobject thiz) {
    return run_signal_test(SIGTRAP);
}

//4. Frida port, maps, thread, segment detection
#include <dirent.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

static bool contains_keyword(const char *text) {
    if (text == nullptr) return false;

    const char *keywords[] = {
            "frida",
            "gum-js-loop",
            "gmain",
            "gdbus",
            "linjector",
            "re.frida",
            "frida-agent",
            "frida-gadget",
            "gadget"
    };

    for (const char *keyword : keywords) {
        if (strstr(text, keyword) != nullptr) {
            return true;
        }
    }

    return false;
}

static jstring make_jstring(JNIEnv *env, const char *msg) {
    return env->NewStringUTF(msg);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkFridaPort(
        JNIEnv *env,
        jobject thiz) {

    int ports[] = {27042, 27043};
    char result[1024];
    memset(result, 0, sizeof(result));

    strcat(result, "[Frida Port Scan]\n");

    bool detected = false;

    for (int port : ports) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock < 0) {
            continue;
        }

        struct sockaddr_in addr;
        memset(&addr, 0, sizeof(addr));

        addr.sin_family = AF_INET;
        addr.sin_port = htons(port);
        inet_pton(AF_INET, "127.0.0.1", &addr.sin_addr);

        int conn = connect(sock, (struct sockaddr *)&addr, sizeof(addr));

        char line[128];
        if (conn == 0) {
            detected = true;
            snprintf(line, sizeof(line),
                     "Port %d is OPEN. Frida server may be running.\n", port);
        } else {
            snprintf(line, sizeof(line),
                     "Port %d is closed.\n", port);
        }

        strcat(result, line);
        close(sock);
    }

    if (detected) {
        strcat(result, "\nResult: Frida port detected.");
    } else {
        strcat(result, "\nResult: Frida port not detected.");
    }

    return make_jstring(env, result);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkFridaMaps(
        JNIEnv *env,
        jobject thiz) {

    FILE *fp = fopen("/proc/self/maps", "r");

    if (fp == nullptr) {
        return make_jstring(env, "Failed to open /proc/self/maps");
    }

    char result[4096];
    memset(result, 0, sizeof(result));

    strcat(result, "[Frida Maps Scan]\n");

    char line[512];
    bool detected = false;

    while (fgets(line, sizeof(line), fp)) {
        if (contains_keyword(line)) {
            detected = true;
            strcat(result, "Suspicious maps entry:\n");
            strncat(result, line, sizeof(result) - strlen(result) - 1);
            strcat(result, "\n");

            if (strlen(result) > sizeof(result) - 700) {
                strcat(result, "... output truncated ...\n");
                break;
            }
        }
    }

    fclose(fp);

    if (detected) {
        strcat(result, "\nResult: Frida-related maps entry detected.");
    } else {
        strcat(result, "\nResult: Frida-related maps entry not detected.");
    }

    return make_jstring(env, result);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkFridaThreads(
        JNIEnv *env,
        jobject thiz) {

    DIR *dir = opendir("/proc/self/task");

    if (dir == nullptr) {
        return make_jstring(env, "Failed to open /proc/self/task");
    }

    char result[4096];
    memset(result, 0, sizeof(result));

    strcat(result, "[Frida Thread Scan]\n");

    struct dirent *entry;
    bool detected = false;

    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_name[0] == '.') {
            continue;
        }

        char path[256];
        snprintf(path, sizeof(path),
                 "/proc/self/task/%s/comm",
                 entry->d_name);

        FILE *fp = fopen(path, "r");
        if (fp == nullptr) {
            continue;
        }

        char threadName[256];
        memset(threadName, 0, sizeof(threadName));

        if (fgets(threadName, sizeof(threadName), fp)) {
            if (contains_keyword(threadName)) {
                detected = true;

                char line[512];
                snprintf(line, sizeof(line),
                         "Suspicious thread TID=%s name=%s",
                         entry->d_name,
                         threadName);

                strncat(result, line, sizeof(result) - strlen(result) - 1);
            }
        }

        fclose(fp);
    }

    closedir(dir);

    if (detected) {
        strcat(result, "\nResult: Frida-related thread detected.");
    } else {
        strcat(result, "\nResult: Frida-related thread not detected.");
    }

    return make_jstring(env, result);
}

static bool memory_contains_signature(uintptr_t start, uintptr_t end) {
    const char *signatures[] = {
            "frida",
            "FRIDA",
            "gum-js-loop",
            "GumScript",
            "frida-agent",
            "frida-gadget",
            "Gadget"
    };

    size_t regionSize = end - start;

    // 너무 큰 영역은 앞부분만 스캔
    const size_t maxScanSize = 4 * 1024 * 1024;
    if (regionSize > maxScanSize) {
        regionSize = maxScanSize;
    }

    const char *mem = reinterpret_cast<const char *>(start);

    for (size_t i = 0; i < regionSize; i++) {
        for (const char *sig : signatures) {
            size_t sigLen = strlen(sig);

            if (i + sigLen < regionSize) {
                if (memcmp(mem + i, sig, sigLen) == 0) {
                    return true;
                }
            }
        }
    }

    return false;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_ptrace_1practice_MainActivity_checkFridaSegments(
        JNIEnv *env,
        jobject thiz) {

    FILE *fp = fopen("/proc/self/maps", "r");

    if (fp == nullptr) {
        return make_jstring(env, "Failed to open /proc/self/maps");
    }

    char result[4096];
    memset(result, 0, sizeof(result));

    strcat(result, "[Frida Segment Signature Scan]\n");

    char line[512];
    bool detected = false;
    int scanned = 0;

    while (fgets(line, sizeof(line), fp)) {
        uintptr_t start = 0;
        uintptr_t end = 0;
        char perm[8];

        memset(perm, 0, sizeof(perm));

        if (sscanf(line, "%lx-%lx %7s", &start, &end, perm) != 3) {
            continue;
        }

        bool readable = perm[0] == 'r';
        bool executable = perm[2] == 'x';

        if (!readable || !executable) {
            continue;
        }

        scanned++;

        if (memory_contains_signature(start, end)) {
            detected = true;

            strcat(result, "Suspicious executable segment:\n");
            strncat(result, line, sizeof(result) - strlen(result) - 1);
            strcat(result, "\n");

            break;
        }

        if (scanned >= 80) {
            break;
        }
    }

    fclose(fp);

    char summary[256];
    snprintf(summary, sizeof(summary),
             "\nScanned executable readable segments: %d\n", scanned);
    strcat(result, summary);

    if (detected) {
        strcat(result, "Result: Frida-like signature detected in memory segment.");
    } else {
        strcat(result, "Result: Frida-like signature not detected in executable segments.");
    }

    return make_jstring(env, result);
}