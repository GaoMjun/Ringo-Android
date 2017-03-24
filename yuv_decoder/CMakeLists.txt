# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(yuv_decoder_include_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/yuv_decoder)
include_directories(${yuv_decoder_include_DIR})

set(yuv_decoder_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/yuv_decoder)
include_directories(${yuv_decoder_DIR})

add_library( yuv_decoder
             SHARED
             ${yuv_decoder_DIR}/yuv_decoder.cpp
)

target_link_libraries( yuv_decoder
                       log
                       android)
