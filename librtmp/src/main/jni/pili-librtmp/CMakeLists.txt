# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(librtmp_include_DIR ${CMAKE_SOURCE_DIR})
include_directories(${librtmp_include_DIR})

add_library( rtmp
             SHARED
             ${librtmp_include_DIR}/amf.c
             ${librtmp_include_DIR}/error.c
             ${librtmp_include_DIR}/hashswf.c
             ${librtmp_include_DIR}/log.c
             ${librtmp_include_DIR}/parseurl.c
             ${librtmp_include_DIR}/rtmp.c
)

target_link_libraries( rtmp
                       log
                       android)
