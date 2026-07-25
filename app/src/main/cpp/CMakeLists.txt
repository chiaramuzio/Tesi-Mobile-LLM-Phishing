cmake_minimum_required(VERSION 3.22.1)

project(
    phishingawareness_native
    LANGUAGES
    C
    CXX
)

# I test strumentali Android usano il build type Debug.
# Manteniamo i simboli di debug, ma abilitiamo l'ottimizzazione
# del codice nativo per ottenere misure di inferenza realistiche.
add_compile_options(
        "$<$<CONFIG:Debug>:-O3>"
)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(
    BUILD_SHARED_LIBS
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_COMMON
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_TESTS
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_TOOLS
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_EXAMPLES
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_SERVER
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_APP
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_BUILD_UI
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    LLAMA_OPENSSL
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_BUILD_TESTS
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_BUILD_EXAMPLES
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_NATIVE
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_OPENMP
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_BLAS
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_CUDA
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_VULKAN
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_OPENCL
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_RPC
    OFF
    CACHE BOOL
    ""
    FORCE
)

set(
    GGML_CPU_KLEIDIAI
    OFF
    CACHE BOOL
    ""
    FORCE
)

if(
        ANDROID
        AND
        CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a"
)
    add_compile_options(
            -march=armv8.2-a+dotprod
    )
endif()

add_subdirectory(
    third_party/llama.cpp
    llama.cpp-build
    EXCLUDE_FROM_ALL
)

add_library(
    phishingawareness_native
    SHARED
    native_runtime_bridge.cpp
)

target_compile_features(
    phishingawareness_native
    PRIVATE
    cxx_std_17
)

target_include_directories(
    phishingawareness_native
    PRIVATE
    third_party/llama.cpp/include
)

find_library(
    android_log_library
    log
)

target_link_libraries(
        phishingawareness_native
        PRIVATE
        llama
        log
)