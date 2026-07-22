#include <jni.h>

#include <mutex>
#include <string>

#include "llama.h"

namespace {

    constexpr const char* BRIDGE_CLASS_NAME =
            "com/example/phishingawareness/generation/runtime/"
            "NativeRuntimeBridge";

    constexpr const char* MODEL_LOAD_SUCCESS =
            "OK|MODEL_LOADED";

    constexpr const char* MODEL_PROBE_SUCCESS =
            "OK|MODEL_LOADED_AND_RELEASED";

    constexpr const char* MODEL_UNLOAD_SUCCESS =
            "OK|MODEL_UNLOADED";

    constexpr const char* MODEL_PATH_NULL =
            "ERROR|MODEL_PATH_NULL";

    constexpr const char* MODEL_PATH_EMPTY =
            "ERROR|MODEL_PATH_EMPTY";

    constexpr const char* MODEL_LOAD_FAILED =
            "ERROR|MODEL_LOAD_FAILED";

    constexpr const char* MODEL_ALREADY_LOADED =
            "ERROR|MODEL_ALREADY_LOADED";

    constexpr const char* MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";
    constexpr const char* CONTEXT_CREATE_SUCCESS =
            "OK|CONTEXT_CREATED";

    constexpr const char* CONTEXT_FREE_SUCCESS =
            "OK|CONTEXT_FREED";

    constexpr const char* CONTEXT_SIZE_INVALID =
            "ERROR|CONTEXT_SIZE_INVALID";

    constexpr const char* CONTEXT_MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";

    constexpr const char* CONTEXT_ALREADY_CREATED =
            "ERROR|CONTEXT_ALREADY_CREATED";

    constexpr const char* CONTEXT_CREATE_FAILED =
            "ERROR|CONTEXT_CREATE_FAILED";

    constexpr const char* CONTEXT_NOT_CREATED =
            "ERROR|CONTEXT_NOT_CREATED";

    constexpr const char* CONTEXT_MUST_BE_FREED =
            "ERROR|CONTEXT_MUST_BE_FREED";

    llama_model* loadedModel = nullptr;
    llama_context* inferenceContext = nullptr;
    std::mutex modelMutex;

    jstring to_jstring(
            JNIEnv* environment,
            const char* value
    ) {
        return environment->NewStringUTF(
                value
        );
    }

    bool read_model_path(
            JNIEnv* environment,
            jstring modelPath,
            std::string& destination
    ) {
        if (modelPath == nullptr) {
            return false;
        }

        const char* modelPathChars =
                environment->GetStringUTFChars(
                        modelPath,
                        nullptr
                );

        if (modelPathChars == nullptr) {
            return false;
        }

        destination.assign(
                modelPathChars
        );

        environment->ReleaseStringUTFChars(
                modelPath,
                modelPathChars
        );

        return true;
    }

    jstring native_version(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        return to_jstring(
                environment,
                "phishingawareness-native-5-inference-context"
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

        std::string modelPathValue;

        if (
                !read_model_path(
                        environment,
                        modelPath,
                        modelPathValue
                )
                ) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        if (modelPathValue.empty()) {
            return to_jstring(
                    environment,
                    MODEL_PATH_EMPTY
            );
        }

        llama_model_params modelParams =
                llama_model_default_params();

        llama_model* probeModel =
                llama_model_load_from_file(
                        modelPathValue.c_str(),
                        modelParams
                );

        if (probeModel == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        llama_model_free(
                probeModel
        );

        return to_jstring(
                environment,
                MODEL_PROBE_SUCCESS
        );
    }

    jstring native_load_model(
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

        std::string modelPathValue;

        if (
                !read_model_path(
                        environment,
                        modelPath,
                        modelPathValue
                )
                ) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        if (modelPathValue.empty()) {
            return to_jstring(
                    environment,
                    MODEL_PATH_EMPTY
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel != nullptr) {
            return to_jstring(
                    environment,
                    MODEL_ALREADY_LOADED
            );
        }

        llama_model_params modelParams =
                llama_model_default_params();

        loadedModel =
                llama_model_load_from_file(
                        modelPathValue.c_str(),
                        modelParams
                );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_LOAD_FAILED
            );
        }

        return to_jstring(
                environment,
                MODEL_LOAD_SUCCESS
        );
    }

    jboolean native_is_model_loaded(
            JNIEnv* /* environment */,
            jobject /* instance */
    ) {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        return loadedModel != nullptr
               ? JNI_TRUE
               : JNI_FALSE;
    }

    jstring native_unload_model(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (inferenceContext != nullptr) {
            return to_jstring(
                    environment,
                    CONTEXT_MUST_BE_FREED
            );
        }

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    MODEL_NOT_LOADED
            );
        }

        llama_model_free(
                loadedModel
        );

        loadedModel = nullptr;

        return to_jstring(
                environment,
                MODEL_UNLOAD_SUCCESS
        );
    }

    jstring native_create_context(
            JNIEnv* environment,
            jobject /* instance */,
            jint contextSize
    ) {
        if (contextSize <= 0) {
            return to_jstring(
                    environment,
                    CONTEXT_SIZE_INVALID
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    CONTEXT_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext != nullptr) {
            return to_jstring(
                    environment,
                    CONTEXT_ALREADY_CREATED
            );
        }

        llama_context_params contextParams =
                llama_context_default_params();

        contextParams.n_ctx =
                static_cast<uint32_t>(
                        contextSize
                );

        contextParams.n_batch = 256;
        contextParams.n_ubatch = 128;
        contextParams.n_seq_max = 1;
        contextParams.n_threads = 4;
        contextParams.n_threads_batch = 4;

        inferenceContext =
                llama_init_from_model(
                        loadedModel,
                        contextParams
                );

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    CONTEXT_CREATE_FAILED
            );
        }

        return to_jstring(
                environment,
                CONTEXT_CREATE_SUCCESS
        );
    }

    jboolean native_is_context_ready(
            JNIEnv* /* environment */,
            jobject /* instance */
    ) {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        return inferenceContext != nullptr
               ? JNI_TRUE
               : JNI_FALSE;
    }

    jlong native_context_size(
            JNIEnv* /* environment */,
            jobject /* instance */
    ) {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (inferenceContext == nullptr) {
            return static_cast<jlong>(
                    0
            );
        }

        return static_cast<jlong>(
                llama_n_ctx(
                        inferenceContext
                )
        );
    }

    jstring native_free_context(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    CONTEXT_NOT_CREATED
            );
        }

        llama_free(
                inferenceContext
        );

        inferenceContext = nullptr;

        return to_jstring(
                environment,
                CONTEXT_FREE_SUCCESS
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
                    reinterpret_cast<void*>(
                            native_llama_supports_mmap
                    )
            },
            {
                    const_cast<char*>("llamaMaxDevices"),
                    const_cast<char*>("()J"),
                    reinterpret_cast<void*>(
                            native_llama_max_devices
                    )
            },
            {
                    const_cast<char*>("loadModelProbe"),
                    const_cast<char*>(
                            "(Ljava/lang/String;)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_load_model_probe
                    )
            },
            {
                    const_cast<char*>("loadModel"),
                    const_cast<char*>(
                            "(Ljava/lang/String;)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_load_model
                    )
            },
            {
                    const_cast<char*>("isModelLoaded"),
                    const_cast<char*>("()Z"),
                    reinterpret_cast<void*>(
                            native_is_model_loaded
                    )
            },
            {
                    const_cast<char*>("unloadModel"),
                    const_cast<char*>(
                            "()Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_unload_model
                    )
            },

            {
                const_cast<char*>("createContext"),
                        const_cast<char*>("(I)Ljava/lang/String;"),
                        reinterpret_cast<void*>(
                                native_create_context
                        )
            },
            {
                const_cast<char*>("isContextReady"),
                        const_cast<char*>("()Z"),
                        reinterpret_cast<void*>(
                                native_is_context_ready
                        )
            },
            {
                const_cast<char*>("contextSize"),
                        const_cast<char*>("()J"),
                        reinterpret_cast<void*>(
                                native_context_size
                        )
            },
            {
                const_cast<char*>("freeContext"),
                        const_cast<char*>(
                                "()Ljava/lang/String;"
                        ),
                        reinterpret_cast<void*>(
                                native_free_context
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
    {
        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (inferenceContext != nullptr) {
            llama_free(
                    inferenceContext
            );

            inferenceContext = nullptr;
        }

        if (loadedModel != nullptr) {
            llama_model_free(
                    loadedModel
            );

            loadedModel = nullptr;
        }
    }

    llama_backend_free();
}