#include <jni.h>

#include <string>

#include "llama.h"

namespace {

    constexpr const char* BRIDGE_CLASS_NAME =
            "com/example/phishingawareness/generation/runtime/"
            "NativeRuntimeBridge";

    jstring native_version(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        return environment->NewStringUTF(
                "phishingawareness-native-2-llama-b9947"
        );
    }

    jboolean native_llama_supports_mmap(
            JNIEnv* /* environment */,
            jobject /* instance */
    ) {
        return ::llama_supports_mmap()
               ? JNI_TRUE
               : JNI_FALSE;
    }

    jlong native_llama_max_devices(
            JNIEnv* /* environment */,
            jobject /* instance */
    ) {
        return static_cast<jlong>(
                ::llama_max_devices()
        );
    }

    JNINativeMethod NATIVE_METHODS[] = {
            {
                    const_cast<char*>("nativeVersion"),
                    const_cast<char*>("()Ljava/lang/String;"),
                    reinterpret_cast<void*>(native_version)
            },
            {
                    const_cast<char*>("llamaSupportsMmap"),
                    const_cast<char*>("()Z"),
                    reinterpret_cast<void*>(native_llama_supports_mmap)
            },
            {
                    const_cast<char*>("llamaMaxDevices"),
                    const_cast<char*>("()J"),
                    reinterpret_cast<void*>(native_llama_max_devices)
            }
    };

}  // namespace

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(
        JavaVM* javaVirtualMachine,
        void* /* reserved */
) {
    JNIEnv* environment = nullptr;

    const jint environmentResult =
            javaVirtualMachine->GetEnv(
                    reinterpret_cast<void**>(&environment),
                    JNI_VERSION_1_6
            );

    if (
            environmentResult != JNI_OK ||
            environment == nullptr
            ) {
        return JNI_ERR;
    }

    jclass bridgeClass =
            environment->FindClass(
                    BRIDGE_CLASS_NAME
            );

    if (bridgeClass == nullptr) {
        return JNI_ERR;
    }

    const jint registrationResult =
            environment->RegisterNatives(
                    bridgeClass,
                    NATIVE_METHODS,
                    static_cast<jint>(
                            sizeof(NATIVE_METHODS) /
                            sizeof(NATIVE_METHODS[0])
                    )
            );

    environment->DeleteLocalRef(
            bridgeClass
    );

    if (registrationResult != JNI_OK) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}