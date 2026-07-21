#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_phishingawareness_generation_runtime_NativeRuntimeBridge_nativeVersion(
        JNIEnv* environment,
        jobject /* instance */
) {
    return environment->NewStringUTF(
            "phishingawareness-native-1"
    );
}
