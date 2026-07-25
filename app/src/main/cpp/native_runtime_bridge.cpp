#include <jni.h>

#include <iomanip>
#include <mutex>
#include <sstream>
#include <string>
#include <vector>
#include <cmath>
#include <android/log.h>
#include <chrono>
#include <cstdint>

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

    constexpr const char* CHAT_TEMPLATE_NOT_AVAILABLE =
            "ERROR|CHAT_TEMPLATE_NOT_AVAILABLE";

    constexpr const char* CHAT_TEMPLATE_APPLY_FAILED =
            "ERROR|CHAT_TEMPLATE_APPLY_FAILED";

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

    constexpr const char* TOKENIZE_PROMPT_NULL =
            "ERROR|PROMPT_NULL";

    constexpr const char* TOKENIZE_PROMPT_EMPTY =
            "ERROR|PROMPT_EMPTY";

    constexpr const char* TOKENIZE_MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";

    constexpr const char* TOKENIZE_CONTEXT_NOT_CREATED =
            "ERROR|CONTEXT_NOT_CREATED";

    constexpr const char* TOKENIZE_FAILED =
            "ERROR|TOKENIZATION_FAILED";

    constexpr const char* TOKENIZE_CONTEXT_EXCEEDED =
            "ERROR|CONTEXT_SIZE_EXCEEDED";

    constexpr const char* DECODE_PROMPT_NULL =
            "ERROR|PROMPT_NULL";

    constexpr const char* DECODE_PROMPT_EMPTY =
            "ERROR|PROMPT_EMPTY";

    constexpr const char* DECODE_MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";

    constexpr const char* DECODE_CONTEXT_NOT_CREATED =
            "ERROR|CONTEXT_NOT_CREATED";

    constexpr const char* DECODE_TOKENIZATION_FAILED =
            "ERROR|TOKENIZATION_FAILED";

    constexpr const char* DECODE_CONTEXT_EXCEEDED =
            "ERROR|CONTEXT_SIZE_EXCEEDED";

    constexpr const char* DECODE_FAILED =
            "ERROR|PROMPT_DECODE_FAILED";

    constexpr const char* FIRST_TOKEN_PROMPT_NULL =
            "ERROR|PROMPT_NULL";

    constexpr const char* FIRST_TOKEN_PROMPT_EMPTY =
            "ERROR|PROMPT_EMPTY";

    constexpr const char* FIRST_TOKEN_MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";

    constexpr const char* FIRST_TOKEN_CONTEXT_NOT_CREATED =
            "ERROR|CONTEXT_NOT_CREATED";

    constexpr const char* FIRST_TOKEN_TOKENIZATION_FAILED =
            "ERROR|TOKENIZATION_FAILED";

    constexpr const char* FIRST_TOKEN_CONTEXT_EXCEEDED =
            "ERROR|CONTEXT_SIZE_EXCEEDED";

    constexpr const char* FIRST_TOKEN_PROMPT_DECODE_FAILED =
            "ERROR|PROMPT_DECODE_FAILED";

    constexpr const char* FIRST_TOKEN_SAMPLER_FAILED =
            "ERROR|SAMPLER_CREATION_FAILED";

    constexpr const char* FIRST_TOKEN_PIECE_FAILED =
            "ERROR|TOKEN_PIECE_FAILED";

    constexpr const char* FIRST_TOKEN_DECODE_FAILED =
            "ERROR|FIRST_TOKEN_DECODE_FAILED";

    constexpr const char* GREEDY_SEQUENCE_PROMPT_NULL =
            "ERROR|PROMPT_NULL";

    constexpr const char* GREEDY_SEQUENCE_PROMPT_EMPTY =
            "ERROR|PROMPT_EMPTY";

    constexpr const char* GREEDY_SEQUENCE_INVALID_MAX_TOKENS =
            "ERROR|INVALID_MAX_GENERATED_TOKENS";

    constexpr const char* GREEDY_SEQUENCE_MODEL_NOT_LOADED =
            "ERROR|MODEL_NOT_LOADED";

    constexpr const char* GREEDY_SEQUENCE_CONTEXT_NOT_CREATED =
            "ERROR|CONTEXT_NOT_CREATED";

    constexpr const char* GREEDY_SEQUENCE_TOKENIZATION_FAILED =
            "ERROR|TOKENIZATION_FAILED";

    constexpr const char* GREEDY_SEQUENCE_CONTEXT_EXCEEDED =
            "ERROR|CONTEXT_SIZE_EXCEEDED";

    constexpr const char* GREEDY_SEQUENCE_PROMPT_DECODE_FAILED =
            "ERROR|PROMPT_DECODE_FAILED";

    constexpr const char* GREEDY_SEQUENCE_SAMPLER_FAILED =
            "ERROR|SAMPLER_CREATION_FAILED";

    constexpr const char* GREEDY_SEQUENCE_PIECE_FAILED =
            "ERROR|TOKEN_PIECE_FAILED";

    constexpr const char* GREEDY_SEQUENCE_TOKEN_DECODE_FAILED =
            "ERROR|GENERATED_TOKEN_DECODE_FAILED";

    constexpr int32_t MAX_GREEDY_PROBE_TOKENS = 8;
    constexpr int32_t MAX_CONFIGURED_GENERATION_TOKENS = 1200;

    constexpr const char* SAMPLING_INVALID_MAX_TOKENS =
            "ERROR|INVALID_MAX_GENERATED_TOKENS";

    constexpr const char* SAMPLING_INVALID_TEMPERATURE =
            "ERROR|INVALID_TEMPERATURE";

    constexpr const char* SAMPLING_INVALID_TOP_K =
            "ERROR|INVALID_TOP_K";

    constexpr const char* SAMPLING_INVALID_TOP_P =
            "ERROR|INVALID_TOP_P";

    constexpr const char* SAMPLING_INVALID_MIN_P =
            "ERROR|INVALID_MIN_P";

    constexpr const char* SAMPLING_INVALID_REPEAT_PENALTY =
            "ERROR|INVALID_REPEAT_PENALTY";

    constexpr const char* SAMPLING_CHAIN_CREATE_SUCCESS =
            "OK|SAMPLING_CHAIN_CREATED_AND_RELEASED";

    constexpr const char* SAMPLING_CHAIN_CREATE_FAILED =
            "ERROR|SAMPLING_CHAIN_CREATION_FAILED";

    constexpr const char* SYSTEM_INFO_UNAVAILABLE =
            "ERROR|SYSTEM_INFO_UNAVAILABLE";

    constexpr int32_t SAMPLING_PENALTY_LAST_N = 64;

    constexpr std::size_t SAMPLING_MIN_KEEP = 1;

    constexpr int32_t DEFAULT_GENERATION_THREADS = 4;

    constexpr int32_t DEFAULT_BATCH_THREADS = 4;

    constexpr const char* NATIVE_TIMING_LOG_TAG =
            "PhishingNativeTiming";

    using NativeSteadyClock =
            std::chrono::steady_clock;

    using NativeTimePoint =
            NativeSteadyClock::time_point;

    int64_t elapsed_milliseconds(
            const NativeTimePoint& startedAt,
            const NativeTimePoint& endedAt
    ) {
        return std::chrono::duration_cast<
                std::chrono::milliseconds
        >(
                endedAt - startedAt
        ).count();
    }

    void log_configured_generation_timings(
            const int32_t promptTokenCount,
            const int32_t requestedTokenCount,
            const std::size_t generatedTokenCount,
            const bool reachedEndOfGeneration,
            const int32_t generationThreads,
            const int32_t batchThreads,
            const int64_t templateMilliseconds,
            const int64_t tokenizationMilliseconds,
            const int64_t promptDecodeMilliseconds,
            const int64_t samplerCreationMilliseconds,
            const int64_t generationMilliseconds,
            const int64_t totalNativeMilliseconds
    ) {
        __android_log_print(
                ANDROID_LOG_INFO,
                NATIVE_TIMING_LOG_TAG,
                "NATIVE_TIMING|"
                "promptTokens=%d|"
                "requestedTokens=%d|"
                "generatedTokens=%zu|"
                "eog=%d|"
                "generationThreads=%d|"
                "batchThreads=%d|"
                "templateMs=%lld|"
                "tokenizationMs=%lld|"
                "promptDecodeMs=%lld|"
                "samplerCreationMs=%lld|"
                "generationMs=%lld|"
                "totalNativeMs=%lld",
                promptTokenCount,
                requestedTokenCount,
                generatedTokenCount,
                reachedEndOfGeneration ? 1 : 0,
                generationThreads,
                batchThreads,
                static_cast<long long>(
                        templateMilliseconds
                ),
                static_cast<long long>(
                        tokenizationMilliseconds
                ),
                static_cast<long long>(
                        promptDecodeMilliseconds
                ),
                static_cast<long long>(
                        samplerCreationMilliseconds
                ),
                static_cast<long long>(
                        generationMilliseconds
                ),
                static_cast<long long>(
                        totalNativeMilliseconds
                )
        );
    }

    llama_model* loadedModel = nullptr;
    llama_context* inferenceContext = nullptr;
    std::mutex modelMutex;

    std::string bytes_to_hex(
            const char* data,
            const std::size_t size
    ) {
        std::ostringstream stream;

        stream << std::hex
               << std::setfill('0');

        for (std::size_t index = 0; index < size; ++index) {
            const auto byteValue =
                    static_cast<unsigned char>(
                            data[index]
                    );

            stream << std::setw(2)
                   << static_cast<unsigned int>(
                           byteValue
                   );
        }

        return stream.str();
    }

    bool token_piece_to_hex(
            const llama_vocab* vocabulary,
            const llama_token token,
            std::string& outputHex
    ) {
        std::vector<char> pieceBuffer(
                256
        );

        int32_t pieceSize =
                llama_token_to_piece(
                        vocabulary,
                        token,
                        pieceBuffer.data(),
                        static_cast<int32_t>(
                                pieceBuffer.size()
                        ),
                        0,
                        true
                );

        if (pieceSize < 0) {
            const int32_t requiredPieceSize =
                    -pieceSize;

            if (requiredPieceSize <= 0) {
                return false;
            }

            pieceBuffer.resize(
                    static_cast<std::size_t>(
                            requiredPieceSize
                    )
            );

            pieceSize =
                    llama_token_to_piece(
                            vocabulary,
                            token,
                            pieceBuffer.data(),
                            requiredPieceSize,
                            0,
                            true
                    );
        }

        if (pieceSize < 0) {
            return false;
        }

        outputHex =
                bytes_to_hex(
                        pieceBuffer.data(),
                        static_cast<std::size_t>(
                                pieceSize
                        )
                );

        return true;
    }

    jstring to_jstring(
            JNIEnv* environment,
            const char* value
    ) {
        return environment->NewStringUTF(
                value
        );
    }

    llama_sampler* create_sampling_chain(
            const float temperature,
            const int32_t topK,
            const float topP,
            const float minP,
            const float repeatPenalty,
            const int32_t seed
    ) {
        llama_sampler_chain_params chainParams =
                llama_sampler_chain_default_params();

        chainParams.no_perf = true;

        llama_sampler* chain =
                llama_sampler_chain_init(
                        chainParams
                );

        if (chain == nullptr) {
            return nullptr;
        }

        llama_sampler* topKSampler =
                llama_sampler_init_top_k(
                        topK
                );

        llama_sampler* topPSampler =
                llama_sampler_init_top_p(
                        topP,
                        SAMPLING_MIN_KEEP
                );

        llama_sampler* minPSampler =
                llama_sampler_init_min_p(
                        minP,
                        SAMPLING_MIN_KEEP
                );

        llama_sampler* penaltiesSampler =
                llama_sampler_init_penalties(
                        SAMPLING_PENALTY_LAST_N,
                        repeatPenalty,
                        0.0F,
                        0.0F
                );

        llama_sampler* temperatureSampler =
                llama_sampler_init_temp(
                        temperature
                );

        llama_sampler* distributionSampler =
                llama_sampler_init_dist(
                        static_cast<uint32_t>(
                                seed
                        )
                );

        if (
                topKSampler == nullptr ||
                topPSampler == nullptr ||
                minPSampler == nullptr ||
                penaltiesSampler == nullptr ||
                temperatureSampler == nullptr ||
                distributionSampler == nullptr
                ) {
            if (topKSampler != nullptr) {
                llama_sampler_free(topKSampler);
            }

            if (topPSampler != nullptr) {
                llama_sampler_free(topPSampler);
            }

            if (minPSampler != nullptr) {
                llama_sampler_free(minPSampler);
            }

            if (penaltiesSampler != nullptr) {
                llama_sampler_free(penaltiesSampler);
            }

            if (temperatureSampler != nullptr) {
                llama_sampler_free(temperatureSampler);
            }

            if (distributionSampler != nullptr) {
                llama_sampler_free(distributionSampler);
            }

            llama_sampler_free(
                    chain
            );

            return nullptr;
        }

        llama_sampler_chain_add(
                chain,
                topKSampler
        );

        llama_sampler_chain_add(
                chain,
                topPSampler
        );

        llama_sampler_chain_add(
                chain,
                minPSampler
        );

        llama_sampler_chain_add(
                chain,
                penaltiesSampler
        );

        llama_sampler_chain_add(
                chain,
                temperatureSampler
        );

        llama_sampler_chain_add(
                chain,
                distributionSampler
        );

        return chain;
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

    bool apply_user_chat_template(
            const llama_model* model,
            const std::string& userPrompt,
            std::string& formattedPrompt
    ) {
        if (model == nullptr) {
            return false;
        }

        const char* chatTemplate =
                llama_model_chat_template(
                        model,
                        nullptr
                );

        if (chatTemplate == nullptr) {
            return false;
        }

        const llama_chat_message message = {
                "user",
                userPrompt.c_str()
        };

        int32_t formattedSize =
                llama_chat_apply_template(
                        chatTemplate,
                        &message,
                        1,
                        true,
                        nullptr,
                        0
                );

        if (formattedSize <= 0) {
            return false;
        }

        std::vector<char> formattedBuffer(
                static_cast<std::size_t>(
                        formattedSize
                )
        );

        const int32_t writtenSize =
                llama_chat_apply_template(
                        chatTemplate,
                        &message,
                        1,
                        true,
                        formattedBuffer.data(),
                        static_cast<int32_t>(
                                formattedBuffer.size()
                        )
                );

        if (
                writtenSize <= 0 ||
                writtenSize > static_cast<int32_t>(
                        formattedBuffer.size()
                )
                ) {
            return false;
        }

        formattedPrompt.assign(
                formattedBuffer.data(),
                static_cast<std::size_t>(
                        writtenSize
                )
        );

        return true;
    }

    jstring native_version(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        return to_jstring(
                environment,
                "phishingawareness-native-13-chat-template"
        );
    }

    jstring native_system_info(
            JNIEnv* environment,
            jobject /* instance */
    ) {
        const char* systemInfo =
                llama_print_system_info();

        if (systemInfo == nullptr) {
            return to_jstring(
                    environment,
                    SYSTEM_INFO_UNAVAILABLE
            );
        }

        const std::string result =
                "OK|SYSTEM_INFO|" +
                std::string(
                        systemInfo
                );

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_context_runtime_info(
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

        std::ostringstream result;

        result
                << "OK|CONTEXT_RUNTIME_INFO"
                << "|CONTEXT_SIZE|"
                << llama_n_ctx(
                        inferenceContext
                )
                << "|GENERATION_THREADS|"
                << llama_n_threads(
                        inferenceContext
                )
                << "|BATCH_THREADS|"
                << llama_n_threads_batch(
                        inferenceContext
                )
                << "|MMAP_SUPPORTED|"
                << (
                        llama_supports_mmap()
                        ? 1
                        : 0
                )
                << "|GPU_OFFLOAD_SUPPORTED|"
                << (
                        llama_supports_gpu_offload()
                        ? 1
                        : 0
                )
                << "|RPC_SUPPORTED|"
                << (
                        llama_supports_rpc()
                        ? 1
                        : 0
                );

        const std::string resultValue =
                result.str();

        return environment->NewStringUTF(
                resultValue.c_str()
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
        contextParams.n_threads =
                DEFAULT_GENERATION_THREADS;

        contextParams.n_threads_batch =
                DEFAULT_BATCH_THREADS;

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

    jstring native_tokenize_prompt(
            JNIEnv* environment,
            jobject /* instance */,
            jstring prompt,
            jboolean addSpecial
    ) {
        if (prompt == nullptr) {
            return to_jstring(
                    environment,
                    TOKENIZE_PROMPT_NULL
            );
        }

        const char* promptChars =
                environment->GetStringUTFChars(
                        prompt,
                        nullptr
                );

        if (promptChars == nullptr) {
            return to_jstring(
                    environment,
                    TOKENIZE_FAILED
            );
        }

        const std::string promptValue(
                promptChars
        );

        environment->ReleaseStringUTFChars(
                prompt,
                promptChars
        );

        if (promptValue.empty()) {
            return to_jstring(
                    environment,
                    TOKENIZE_PROMPT_EMPTY
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    TOKENIZE_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    TOKENIZE_CONTEXT_NOT_CREATED
            );
        }

        const llama_vocab* vocabulary =
                llama_model_get_vocab(
                        loadedModel
                );

        if (vocabulary == nullptr) {
            return to_jstring(
                    environment,
                    TOKENIZE_FAILED
            );
        }

        const bool shouldAddSpecial =
                addSpecial == JNI_TRUE;

        const int32_t tokenizationProbe =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        nullptr,
                        0,
                        shouldAddSpecial,
                        false
                );

        if (tokenizationProbe >= 0) {
            return to_jstring(
                    environment,
                    TOKENIZE_FAILED
            );
        }

        const int32_t requiredTokenCount =
                -tokenizationProbe;

        if (requiredTokenCount <= 0) {
            return to_jstring(
                    environment,
                    TOKENIZE_FAILED
            );
        }

        std::vector<llama_token> tokens(
                static_cast<std::size_t>(
                        requiredTokenCount
                )
        );

        const int32_t tokenCount =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        tokens.data(),
                        requiredTokenCount,
                        shouldAddSpecial,
                        false
                );

        if (tokenCount < 0) {
            return to_jstring(
                    environment,
                    TOKENIZE_FAILED
            );
        }

        const uint32_t availableContextSize =
                llama_n_ctx(
                        inferenceContext
                );

        if (
                static_cast<uint32_t>(
                        tokenCount
                ) > availableContextSize
                ) {
            return to_jstring(
                    environment,
                    TOKENIZE_CONTEXT_EXCEEDED
            );
        }

        const std::string result =
                "OK|TOKEN_COUNT|" +
                std::to_string(
                        tokenCount
                );

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_decode_prompt_probe(
            JNIEnv* environment,
            jobject /* instance */,
            jstring prompt,
            jboolean addSpecial
    ) {
        if (prompt == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_PROMPT_NULL
            );
        }

        const char* promptChars =
                environment->GetStringUTFChars(
                        prompt,
                        nullptr
                );

        if (promptChars == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_TOKENIZATION_FAILED
            );
        }

        const std::string promptValue(
                promptChars
        );

        environment->ReleaseStringUTFChars(
                prompt,
                promptChars
        );

        if (promptValue.empty()) {
            return to_jstring(
                    environment,
                    DECODE_PROMPT_EMPTY
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_CONTEXT_NOT_CREATED
            );
        }

        const llama_vocab* vocabulary =
                llama_model_get_vocab(
                        loadedModel
                );

        if (vocabulary == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_TOKENIZATION_FAILED
            );
        }

        const bool shouldAddSpecial =
                addSpecial == JNI_TRUE;

        const int32_t tokenizationProbe =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        nullptr,
                        0,
                        shouldAddSpecial,
                        false
                );

        if (tokenizationProbe >= 0) {
            return to_jstring(
                    environment,
                    DECODE_TOKENIZATION_FAILED
            );
        }

        const int32_t requiredTokenCount =
                -tokenizationProbe;

        if (requiredTokenCount <= 0) {
            return to_jstring(
                    environment,
                    DECODE_TOKENIZATION_FAILED
            );
        }

        const uint32_t availableContextSize =
                llama_n_ctx(
                        inferenceContext
                );

        if (
                static_cast<uint32_t>(
                        requiredTokenCount
                ) > availableContextSize
                ) {
            return to_jstring(
                    environment,
                    DECODE_CONTEXT_EXCEEDED
            );
        }

        std::vector<llama_token> tokens(
                static_cast<std::size_t>(
                        requiredTokenCount
                )
        );

        const int32_t tokenCount =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        tokens.data(),
                        requiredTokenCount,
                        shouldAddSpecial,
                        false
                );

        if (tokenCount <= 0) {
            return to_jstring(
                    environment,
                    DECODE_TOKENIZATION_FAILED
            );
        }


        llama_memory_t contextMemory =
                llama_get_memory(
                        inferenceContext
                );

        if (contextMemory == nullptr) {
            return to_jstring(
                    environment,
                    DECODE_FAILED
            );
        }

        llama_memory_clear(
                contextMemory,
                true
        );

        llama_batch batch =
                llama_batch_get_one(
                        tokens.data(),
                        tokenCount
                );

        const int32_t decodeResult =
                llama_decode(
                        inferenceContext,
                        batch
                );

        if (decodeResult != 0) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    DECODE_FAILED
            );
        }

        llama_memory_clear(
                contextMemory,
                true
        );

        const std::string result =
                "OK|PROMPT_DECODED|TOKEN_COUNT|" +
                std::to_string(
                        tokenCount
                );

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_generate_first_token_greedy(
            JNIEnv* environment,
            jobject /* instance */,
            jstring prompt,
            jboolean addSpecial
    ) {
        if (prompt == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_PROMPT_NULL
            );
        }

        const char* promptChars =
                environment->GetStringUTFChars(
                        prompt,
                        nullptr
                );

        if (promptChars == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_TOKENIZATION_FAILED
            );
        }

        const std::string promptValue(
                promptChars
        );

        environment->ReleaseStringUTFChars(
                prompt,
                promptChars
        );

        if (promptValue.empty()) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_PROMPT_EMPTY
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_CONTEXT_NOT_CREATED
            );
        }

        const llama_vocab* vocabulary =
                llama_model_get_vocab(
                        loadedModel
                );

        if (vocabulary == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_TOKENIZATION_FAILED
            );
        }

        const bool shouldAddSpecial =
                addSpecial == JNI_TRUE;

        const int32_t tokenizationProbe =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        nullptr,
                        0,
                        shouldAddSpecial,
                        false
                );

        if (tokenizationProbe >= 0) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_TOKENIZATION_FAILED
            );
        }

        const int32_t requiredTokenCount =
                -tokenizationProbe;

        if (requiredTokenCount <= 0) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_TOKENIZATION_FAILED
            );
        }

        const uint32_t availableContextSize =
                llama_n_ctx(
                        inferenceContext
                );

        if (
                static_cast<uint32_t>(
                        requiredTokenCount + 1
                ) > availableContextSize
                ) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_CONTEXT_EXCEEDED
            );
        }

        std::vector<llama_token> promptTokens(
                static_cast<std::size_t>(
                        requiredTokenCount
                )
        );

        const int32_t promptTokenCount =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        promptTokens.data(),
                        requiredTokenCount,
                        shouldAddSpecial,
                        false
                );

        if (promptTokenCount <= 0) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_TOKENIZATION_FAILED
            );
        }

        llama_memory_t contextMemory =
                llama_get_memory(
                        inferenceContext
                );

        if (contextMemory == nullptr) {
            return to_jstring(
                    environment,
                    FIRST_TOKEN_PROMPT_DECODE_FAILED
            );
        }

        llama_memory_clear(
                contextMemory,
                true
        );

        llama_batch promptBatch =
                llama_batch_get_one(
                        promptTokens.data(),
                        promptTokenCount
                );

        const int32_t promptDecodeResult =
                llama_decode(
                        inferenceContext,
                        promptBatch
                );

        if (promptDecodeResult != 0) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    FIRST_TOKEN_PROMPT_DECODE_FAILED
            );
        }

        llama_sampler* greedySampler =
                llama_sampler_init_greedy();

        if (greedySampler == nullptr) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    FIRST_TOKEN_SAMPLER_FAILED
            );
        }

        const llama_token sampledToken =
                llama_sampler_sample(
                        greedySampler,
                        inferenceContext,
                        -1
                );

        llama_sampler_free(
                greedySampler
        );

        const bool isEndOfGeneration =
                llama_vocab_is_eog(
                        vocabulary,
                        sampledToken
                );

        std::vector<char> pieceBuffer(
                256
        );

        int32_t pieceSize =
                llama_token_to_piece(
                        vocabulary,
                        sampledToken,
                        pieceBuffer.data(),
                        static_cast<int32_t>(
                                pieceBuffer.size()
                        ),
                        0,
                        true
                );

        if (pieceSize < 0) {
            const int32_t requiredPieceSize =
                    -pieceSize;

            if (requiredPieceSize <= 0) {
                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        FIRST_TOKEN_PIECE_FAILED
                );
            }

            pieceBuffer.resize(
                    static_cast<std::size_t>(
                            requiredPieceSize
                    )
            );

            pieceSize =
                    llama_token_to_piece(
                            vocabulary,
                            sampledToken,
                            pieceBuffer.data(),
                            requiredPieceSize,
                            0,
                            true
                    );
        }

        if (pieceSize < 0) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    FIRST_TOKEN_PIECE_FAILED
            );
        }

        if (!isEndOfGeneration) {
            llama_token tokenToDecode =
                    sampledToken;

            llama_batch tokenBatch =
                    llama_batch_get_one(
                            &tokenToDecode,
                            1
                    );

            const int32_t tokenDecodeResult =
                    llama_decode(
                            inferenceContext,
                            tokenBatch
                    );

            if (tokenDecodeResult != 0) {
                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        FIRST_TOKEN_DECODE_FAILED
                );
            }
        }

        const std::string pieceHex =
                bytes_to_hex(
                        pieceBuffer.data(),
                        static_cast<std::size_t>(
                                pieceSize
                        )
                );

        llama_memory_clear(
                contextMemory,
                true
        );

        const std::string result =
                "OK|FIRST_TOKEN|TOKEN_ID|" +
                std::to_string(
                        sampledToken
                ) +
                "|EOG|" +
                (
                        isEndOfGeneration
                        ? "1"
                        : "0"
                ) +
                "|PIECE_HEX|" +
                pieceHex;

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_generate_greedy_sequence(
            JNIEnv* environment,
            jobject /* instance */,
            jstring prompt,
            jboolean addSpecial,
            jint maxGeneratedTokens
    ) {
        if (prompt == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_NULL
            );
        }

        if (
                maxGeneratedTokens <= 0 ||
                maxGeneratedTokens > MAX_GREEDY_PROBE_TOKENS
                ) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_INVALID_MAX_TOKENS
            );
        }

        const char* promptChars =
                environment->GetStringUTFChars(
                        prompt,
                        nullptr
                );

        if (promptChars == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const std::string promptValue(
                promptChars
        );

        environment->ReleaseStringUTFChars(
                prompt,
                promptChars
        );

        if (promptValue.empty()) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_EMPTY
            );
        }

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_CONTEXT_NOT_CREATED
            );
        }

        const llama_vocab* vocabulary =
                llama_model_get_vocab(
                        loadedModel
                );

        if (vocabulary == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const bool shouldAddSpecial =
                addSpecial == JNI_TRUE;

        const int32_t tokenizationProbe =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        nullptr,
                        0,
                        shouldAddSpecial,
                        false
                );

        if (tokenizationProbe >= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const int32_t requiredPromptTokens =
                -tokenizationProbe;

        if (requiredPromptTokens <= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const uint32_t availableContextSize =
                llama_n_ctx(
                        inferenceContext
                );

        const uint64_t requiredContextSize =
                static_cast<uint64_t>(
                        requiredPromptTokens
                ) +
                static_cast<uint64_t>(
                        maxGeneratedTokens
                );

        if (
                requiredContextSize >
                static_cast<uint64_t>(
                        availableContextSize
                )
                ) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_CONTEXT_EXCEEDED
            );
        }

        std::vector<llama_token> promptTokens(
                static_cast<std::size_t>(
                        requiredPromptTokens
                )
        );

        const int32_t promptTokenCount =
                llama_tokenize(
                        vocabulary,
                        promptValue.c_str(),
                        static_cast<int32_t>(
                                promptValue.size()
                        ),
                        promptTokens.data(),
                        requiredPromptTokens,
                        shouldAddSpecial,
                        false
                );

        if (promptTokenCount <= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        llama_memory_t contextMemory =
                llama_get_memory(
                        inferenceContext
                );

        if (contextMemory == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_DECODE_FAILED
            );
        }

        llama_memory_clear(
                contextMemory,
                true
        );

        llama_batch promptBatch =
                llama_batch_get_one(
                        promptTokens.data(),
                        promptTokenCount
                );

        const int32_t promptDecodeResult =
                llama_decode(
                        inferenceContext,
                        promptBatch
                );

        if (promptDecodeResult != 0) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_DECODE_FAILED
            );
        }

        llama_sampler* greedySampler =
                llama_sampler_init_greedy();

        if (greedySampler == nullptr) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_SAMPLER_FAILED
            );
        }

        std::vector<llama_token> generatedTokens;
        generatedTokens.reserve(
                static_cast<std::size_t>(
                        maxGeneratedTokens
                )
        );

        std::string generatedHex;
        bool reachedEndOfGeneration = false;

        for (
                int32_t generatedIndex = 0;
                generatedIndex < maxGeneratedTokens;
                ++generatedIndex
                ) {
            const llama_token sampledToken =
                    llama_sampler_sample(
                            greedySampler,
                            inferenceContext,
                            -1
                    );

            generatedTokens.push_back(
                    sampledToken
            );

            std::string pieceHex;

            if (
                    !token_piece_to_hex(
                            vocabulary,
                            sampledToken,
                            pieceHex
                    )
                    ) {
                llama_sampler_free(
                        greedySampler
                );

                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        GREEDY_SEQUENCE_PIECE_FAILED
                );
            }

            generatedHex += pieceHex;

            if (
                    llama_vocab_is_eog(
                            vocabulary,
                            sampledToken
                    )
                    ) {
                reachedEndOfGeneration = true;
                break;
            }

            llama_token tokenToDecode =
                    sampledToken;

            llama_batch generatedBatch =
                    llama_batch_get_one(
                            &tokenToDecode,
                            1
                    );

            const int32_t generatedDecodeResult =
                    llama_decode(
                            inferenceContext,
                            generatedBatch
                    );

            if (generatedDecodeResult != 0) {
                llama_sampler_free(
                        greedySampler
                );

                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        GREEDY_SEQUENCE_TOKEN_DECODE_FAILED
                );
            }
        }

        llama_sampler_free(
                greedySampler
        );

        llama_memory_clear(
                contextMemory,
                true
        );

        std::ostringstream tokenIdsStream;

        for (
                std::size_t index = 0;
                index < generatedTokens.size();
                ++index
                ) {
            if (index > 0) {
                tokenIdsStream << ",";
            }

            tokenIdsStream << generatedTokens[index];
        }

        const std::string result =
                "OK|GREEDY_SEQUENCE" +
                std::string(
                        "|REQUESTED_TOKEN_COUNT|"
                ) +
                std::to_string(
                        maxGeneratedTokens
                ) +
                "|GENERATED_TOKEN_COUNT|" +
                std::to_string(
                        generatedTokens.size()
                ) +
                "|EOG|" +
                (
                        reachedEndOfGeneration
                        ? "1"
                        : "0"
                ) +
                "|TOKEN_IDS|" +
                tokenIdsStream.str() +
                "|OUTPUT_HEX|" +
                generatedHex;

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_generate_configured_sequence(
            JNIEnv* environment,
            jobject /* instance */,
            jstring prompt,
            jboolean addSpecial,
            jint maxGeneratedTokens,
            jfloat temperature,
            jint topK,
            jfloat topP,
            jfloat minP,
            jfloat repeatPenalty,
            jint seed
    ) {
        if (prompt == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_NULL
            );
        }

        if (
                maxGeneratedTokens <= 0 ||
                maxGeneratedTokens > MAX_CONFIGURED_GENERATION_TOKENS
                ) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_INVALID_MAX_TOKENS
            );
        }

        if (
                !std::isfinite(temperature) ||
                temperature <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TEMPERATURE
            );
        }

        if (topK <= 0) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_K
            );
        }

        if (
                !std::isfinite(topP) ||
                topP <= 0.0F ||
                topP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_P
            );
        }

        if (
                !std::isfinite(minP) ||
                minP < 0.0F ||
                minP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_MIN_P
            );
        }

        if (
                !std::isfinite(repeatPenalty) ||
                repeatPenalty <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_REPEAT_PENALTY
            );
        }

        const char* promptChars =
                environment->GetStringUTFChars(
                        prompt,
                        nullptr
                );

        if (promptChars == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const std::string promptValue(
                promptChars
        );

        environment->ReleaseStringUTFChars(
                prompt,
                promptChars
        );

        if (promptValue.empty()) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_EMPTY
            );
        }

        std::string formattedPromptValue;

        std::lock_guard<std::mutex> lock(
                modelMutex
        );

        if (loadedModel == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_MODEL_NOT_LOADED
            );
        }

        if (inferenceContext == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_CONTEXT_NOT_CREATED
            );
        }

        const NativeTimePoint totalStartedAt =
                NativeSteadyClock::now();

        const NativeTimePoint templateStartedAt =
                NativeSteadyClock::now();

        const char* chatTemplate =
                llama_model_chat_template(
                        loadedModel,
                        nullptr
                );

        if (chatTemplate == nullptr) {
            return to_jstring(
                    environment,
                    CHAT_TEMPLATE_NOT_AVAILABLE
            );
        }

        if (
                !apply_user_chat_template(
                        loadedModel,
                        promptValue,
                        formattedPromptValue
                )
                ) {
            return to_jstring(
                    environment,
                    CHAT_TEMPLATE_APPLY_FAILED
            );
        }

        const int64_t templateMilliseconds =
                elapsed_milliseconds(
                        templateStartedAt,
                        NativeSteadyClock::now()
                );

        const llama_vocab* vocabulary =
                llama_model_get_vocab(
                        loadedModel
                );

        if (vocabulary == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const bool shouldAddSpecial =
                addSpecial == JNI_TRUE;

        const NativeTimePoint tokenizationStartedAt =
                NativeSteadyClock::now();

        const int32_t tokenizationProbe =
                llama_tokenize(
                        vocabulary,
                        formattedPromptValue.c_str(),
                        static_cast<int32_t>(
                                formattedPromptValue.size()
                        ),
                        nullptr,
                        0,
                        shouldAddSpecial,
                        true
                );

        if (tokenizationProbe >= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const int32_t requiredPromptTokens =
                -tokenizationProbe;

        if (requiredPromptTokens <= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const uint32_t availableContextSize =
                llama_n_ctx(
                        inferenceContext
                );

        const uint64_t requiredContextSize =
                static_cast<uint64_t>(
                        requiredPromptTokens
                ) +
                static_cast<uint64_t>(
                        maxGeneratedTokens
                );

        if (
                requiredContextSize >
                static_cast<uint64_t>(
                        availableContextSize
                )
                ) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_CONTEXT_EXCEEDED
            );
        }

        std::vector<llama_token> promptTokens(
                static_cast<std::size_t>(
                        requiredPromptTokens
                )
        );

        const int32_t promptTokenCount =
                llama_tokenize(
                        vocabulary,
                        formattedPromptValue.c_str(),
                        static_cast<int32_t>(
                                formattedPromptValue.size()
                        ),
                        promptTokens.data(),
                        requiredPromptTokens,
                        shouldAddSpecial,
                        true
                );

        if (promptTokenCount <= 0) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_TOKENIZATION_FAILED
            );
        }

        const int64_t tokenizationMilliseconds =
                elapsed_milliseconds(
                        tokenizationStartedAt,
                        NativeSteadyClock::now()
                );

        llama_memory_t contextMemory =
                llama_get_memory(
                        inferenceContext
                );

        if (contextMemory == nullptr) {
            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_DECODE_FAILED
            );
        }

        llama_memory_clear(
                contextMemory,
                true
        );

        llama_batch promptBatch =
                llama_batch_get_one(
                        promptTokens.data(),
                        promptTokenCount
                );

        const NativeTimePoint promptDecodeStartedAt =
                NativeSteadyClock::now();

        const int32_t promptDecodeResult =
                llama_decode(
                        inferenceContext,
                        promptBatch
                );

        const int64_t promptDecodeMilliseconds =
                elapsed_milliseconds(
                        promptDecodeStartedAt,
                        NativeSteadyClock::now()
                );

        if (promptDecodeResult != 0) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    GREEDY_SEQUENCE_PROMPT_DECODE_FAILED
            );
        }

        const NativeTimePoint samplerCreationStartedAt =
                NativeSteadyClock::now();

        llama_sampler* samplingChain =
                create_sampling_chain(
                        temperature,
                        topK,
                        topP,
                        minP,
                        repeatPenalty,
                        seed
                );

        const int64_t samplerCreationMilliseconds =
                elapsed_milliseconds(
                        samplerCreationStartedAt,
                        NativeSteadyClock::now()
                );

        if (samplingChain == nullptr) {
            llama_memory_clear(
                    contextMemory,
                    true
            );

            return to_jstring(
                    environment,
                    SAMPLING_CHAIN_CREATE_FAILED
            );
        }

        /*
         * Registra nella memoria del repeat penalty anche i token
         * del prompt. I token generati vengono invece accettati
         * automaticamente da llama_sampler_sample().
         */


        std::vector<llama_token> generatedTokens;

        generatedTokens.reserve(
                static_cast<std::size_t>(
                        maxGeneratedTokens
                )
        );

        std::string generatedHex;
        bool reachedEndOfGeneration = false;

        const NativeTimePoint generationStartedAt =
                NativeSteadyClock::now();

        for (
                int32_t generatedIndex = 0;
                generatedIndex < maxGeneratedTokens;
                ++generatedIndex
                ) {
            const llama_token sampledToken =
                    llama_sampler_sample(
                            samplingChain,
                            inferenceContext,
                            -1
                    );

            const int64_t generationMilliseconds =
                    elapsed_milliseconds(
                            generationStartedAt,
                            NativeSteadyClock::now()
                    );

            generatedTokens.push_back(
                    sampledToken
            );

            if (
                    llama_vocab_is_eog(
                            vocabulary,
                            sampledToken
                    )
                    ) {
                reachedEndOfGeneration = true;
                break;
            }

            std::string pieceHex;

            if (
                    !token_piece_to_hex(
                            vocabulary,
                            sampledToken,
                            pieceHex
                    )
                    ) {
                llama_sampler_free(
                        samplingChain
                );

                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        GREEDY_SEQUENCE_PIECE_FAILED
                );
            }

            generatedHex += pieceHex;

            llama_token tokenToDecode =
                    sampledToken;

            llama_batch generatedBatch =
                    llama_batch_get_one(
                            &tokenToDecode,
                            1
                    );

            const int32_t generatedDecodeResult =
                    llama_decode(
                            inferenceContext,
                            generatedBatch
                    );

            if (generatedDecodeResult != 0) {
                llama_sampler_free(
                        samplingChain
                );

                llama_memory_clear(
                        contextMemory,
                        true
                );

                return to_jstring(
                        environment,
                        GREEDY_SEQUENCE_TOKEN_DECODE_FAILED
                );
            }
        }

        const int64_t generationMilliseconds =
                elapsed_milliseconds(
                        generationStartedAt,
                        NativeSteadyClock::now()
                );

        llama_sampler_free(
                samplingChain
        );

        llama_memory_clear(
                contextMemory,
                true
        );

        const int64_t totalNativeMilliseconds =
                elapsed_milliseconds(
                        totalStartedAt,
                        NativeSteadyClock::now()
                );

        log_configured_generation_timings(
                promptTokenCount,
                maxGeneratedTokens,
                generatedTokens.size(),
                reachedEndOfGeneration,
                DEFAULT_GENERATION_THREADS,
                DEFAULT_BATCH_THREADS,
                templateMilliseconds,
                tokenizationMilliseconds,
                promptDecodeMilliseconds,
                samplerCreationMilliseconds,
                generationMilliseconds,
                totalNativeMilliseconds
        );

        std::ostringstream tokenIdsStream;

        for (
                std::size_t index = 0;
                index < generatedTokens.size();
                ++index
                ) {
            if (index > 0) {
                tokenIdsStream << ",";
            }

            tokenIdsStream << generatedTokens[index];
        }

        const std::string result =
                "OK|GREEDY_SEQUENCE" +
                std::string(
                        "|REQUESTED_TOKEN_COUNT|"
                ) +
                std::to_string(
                        maxGeneratedTokens
                ) +
                "|GENERATED_TOKEN_COUNT|" +
                std::to_string(
                        generatedTokens.size()
                ) +
                "|EOG|" +
                (
                        reachedEndOfGeneration
                        ? "1"
                        : "0"
                ) +
                "|TOKEN_IDS|" +
                tokenIdsStream.str() +
                "|OUTPUT_HEX|" +
                generatedHex;

        return environment->NewStringUTF(
                result.c_str()
        );
    }

    jstring native_validate_sampling_configuration(
            JNIEnv* environment,
            jobject /* instance */,
            jint maxGeneratedTokens,
            jfloat temperature,
            jint topK,
            jfloat topP,
            jfloat minP,
            jfloat repeatPenalty,
            jint seed
    ) {
        if (maxGeneratedTokens <= 0) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_MAX_TOKENS
            );
        }

        if (
                !std::isfinite(temperature) ||
                temperature <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TEMPERATURE
            );
        }

        if (topK <= 0) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_K
            );
        }

        if (
                !std::isfinite(topP) ||
                topP <= 0.0F ||
                topP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_P
            );
        }

        if (
                !std::isfinite(minP) ||
                minP < 0.0F ||
                minP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_MIN_P
            );
        }

        if (
                !std::isfinite(repeatPenalty) ||
                repeatPenalty <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_REPEAT_PENALTY
            );
        }

        std::ostringstream result;

        result
                << "OK|SAMPLING_CONFIGURATION"
                << "|MAX_GENERATED_TOKENS|"
                << maxGeneratedTokens
                << "|TEMPERATURE|"
                << temperature
                << "|TOP_K|"
                << topK
                << "|TOP_P|"
                << topP
                << "|MIN_P|"
                << minP
                << "|REPEAT_PENALTY|"
                << repeatPenalty
                << "|SEED|"
                << seed;

        const std::string resultValue =
                result.str();

        return to_jstring(
                environment,
                resultValue.c_str()
        );
    }

    jstring native_probe_sampling_chain(
            JNIEnv* environment,
            jobject /* instance */,
            jint maxGeneratedTokens,
            jfloat temperature,
            jint topK,
            jfloat topP,
            jfloat minP,
            jfloat repeatPenalty,
            jint seed
    ) {
        if (maxGeneratedTokens <= 0) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_MAX_TOKENS
            );
        }

        if (
                !std::isfinite(temperature) ||
                temperature <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TEMPERATURE
            );
        }

        if (topK <= 0) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_K
            );
        }

        if (
                !std::isfinite(topP) ||
                topP <= 0.0F ||
                topP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_TOP_P
            );
        }

        if (
                !std::isfinite(minP) ||
                minP < 0.0F ||
                minP > 1.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_MIN_P
            );
        }

        if (
                !std::isfinite(repeatPenalty) ||
                repeatPenalty <= 0.0F
                ) {
            return to_jstring(
                    environment,
                    SAMPLING_INVALID_REPEAT_PENALTY
            );
        }

        llama_sampler* samplingChain =
                create_sampling_chain(
                        temperature,
                        topK,
                        topP,
                        minP,
                        repeatPenalty,
                        seed
                );

        if (samplingChain == nullptr) {
            return to_jstring(
                    environment,
                    SAMPLING_CHAIN_CREATE_FAILED
            );
        }

        llama_sampler_free(
                samplingChain
        );

        return to_jstring(
                environment,
                SAMPLING_CHAIN_CREATE_SUCCESS
        );
    }

    JNINativeMethod NATIVE_METHODS[] = {
            {
                    const_cast<char*>("nativeVersion"),
                    const_cast<char*>("()Ljava/lang/String;"),
                    reinterpret_cast<void*>(
                            native_version
                    )
            },
            {
                    const_cast<char*>("systemInfo"),
                    const_cast<char*>(
                            "()Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_system_info
                    )
            },
            {
                    const_cast<char*>("contextRuntimeInfo"),
                    const_cast<char*>(
                            "()Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_context_runtime_info
                    )
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
                    const_cast<char*>(
                            "(I)Ljava/lang/String;"
                    ),
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
            },
            {
                    const_cast<char*>("tokenizePrompt"),
                    const_cast<char*>(
                            "(Ljava/lang/String;Z)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_tokenize_prompt
                    )
            },
            {
                    const_cast<char*>("decodePromptProbe"),
                    const_cast<char*>(
                            "(Ljava/lang/String;Z)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_decode_prompt_probe
                    )
            },
            {
                    const_cast<char*>("generateFirstTokenGreedy"),
                    const_cast<char*>(
                            "(Ljava/lang/String;Z)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_generate_first_token_greedy
                    )
            },
            {
                    const_cast<char*>("generateGreedySequence"),
                    const_cast<char*>(
                            "(Ljava/lang/String;ZI)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_generate_greedy_sequence
                    )
            },

            {
                    const_cast<char*>(
                            "generateConfiguredSequence"
                    ),
                    const_cast<char*>(
                            "(Ljava/lang/String;ZIFIFFFI)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_generate_configured_sequence
                    )
            },

            {
                    const_cast<char*>(
                            "validateSamplingConfiguration"
                    ),
                    const_cast<char*>(
                            "(IFIFFFI)Ljava/lang/String;"
                    ),
                    reinterpret_cast<void*>(
                            native_validate_sampling_configuration
                    )
            },

            {
                const_cast<char*>(
                        "probeSamplingChain"
                ),
                        const_cast<char*>(
                                "(IFIFFFI)Ljava/lang/String;"
                        ),
                        reinterpret_cast<void*>(
                                native_probe_sampling_chain
                        )
            }

    };

} // namespace

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
