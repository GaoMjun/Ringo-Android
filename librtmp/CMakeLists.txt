# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(${CMAKE_SOURCE_DIR}/src/main/jni/pili-librtmp)

set(librtmp_include_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/pili-librtmp)
include_directories(${librtmp_include_DIR})

set(rtmpnative_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)
include_directories(${rtmpnative_DIR})

add_library( rtmpnative
             SHARED
             ${rtmpnative_DIR}/rtmpnative.cpp
)

set(library_DIR ${CMAKE_SOURCE_DIR}/build/intermediates/cmake/release/obj/${ANDROID_ABI})

add_library( librtmp SHARED IMPORTED )
set_target_properties( librtmp PROPERTIES IMPORTED_LOCATION ${library_DIR}/librtmp.so )

target_link_libraries( rtmpnative
                       librtmp
                       log
                       android)
