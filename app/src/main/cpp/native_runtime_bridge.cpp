#include <jni.h>

#include <string>

#include "llama.h"

namespace {

    constexpr const char* BRIDGE_CLASS_NAME =
            "com/example/phishingawareness/generation/runtime/"
            "NativeRuntimeBridge";

    constexpr const char* MODEL_LOAD_SUCCESS =
            "OK|MODEL_LOADED_AND_RELEASED";

    constexpr const char* MODEL_PATH_NULL =
            "ERROR|MODEL_PATH_NULL";

    constexpr const char* MODEL_PATH_EMPTY =
            "ERROR|MODEL_PATH_EMPTY";

    constexpr const char* MODEL_LOAD_FAILED =
            "ERROR|MODEL_LOAD_FAILED";

    jstring to_jstring(
            JNIEnv* environment,
            const char* value
    ) {
        return environment->NewStringUTF(
                value
        );
    }

    jstring native_version(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        return to_jstring(
                environment,
                "phishingawareness-native-3-gguf-load"
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

    jstring native_load_model_probe(
            JNIEnv* environment,
            jobject /* instance */,
            jstring modelPath
    ) {
        if (modelPath == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_PATH_NULL
            );
        }

        const char* modelPathChars =
                environment->GetStringUTFChars(
                        modelPath,
                        nullptr
                );

        if (modelPathChars == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        const std::string modelPathValue(
                modelPathChars
        );

        environment->ReleaseStringUTFChars(
                modelPath,
                modelPathChars
        );

        if (modelPathValue.empty()) {
            return to_jstring(
                    environment,
                    MODEL_PATH_EMPTY
            );
        }

        llama_model_params modelParams =
                llama_model_default_params();

        llama_model* model =
                llama_model_load_from_file(
                        modelPathValue.c_str(),
                        modelParams
                );

        if (model == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        llama_model_free(
                model
        );

        return to_jstring(
                environment,
                MODEL_LOAD_SUCCESS
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
            },
            {
                    const_cast<char*>("loadModelProbe"),
                    const_cast<char*>(
                            "(Ljava/lang/String;)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_load_model_probe
                    )
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

    llama_backend_init();

    jclass bridgeClass =
            environment->FindClass(
                    BRIDGE_CLASS_NAME
            );

    if (bridgeClass == nullptr) {
        llama_backend_free();
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
        llama_backend_free();
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
JNI_OnUnload(
        JavaVM* /* javaVirtualMachine */,
        void* /* reserved */
) {
    llama_backend_free();
}