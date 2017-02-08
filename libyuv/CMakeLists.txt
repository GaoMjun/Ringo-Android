# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(libyuv_include_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/include)
include_directories(${libyuv_include_DIR})

set(yuvnative_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)
include_directories(${yuvnative_DIR})

add_library( yuvnative
             SHARED
             ${yuvnative_DIR}/yuvnative.cpp
)

set(library_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

add_library( libyuv SHARED IMPORTED )
set_target_properties( libyuv PROPERTIES IMPORTED_LOCATION ${library_DIR}/libyuv.so )

target_link_libraries( yuvnative
                       libyuv
                       log
                       android)
