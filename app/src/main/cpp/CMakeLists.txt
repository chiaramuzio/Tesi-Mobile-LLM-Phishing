cmake_minimum_required(VERSION 3.22.1)

project(
    phishingawareness_native
    LANGUAGES
    CXX
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

find_library(
    android_log_library
    log
)

target_link_libraries(
    phishingawareness_native
    PRIVATE
    ${android_log_library}
)