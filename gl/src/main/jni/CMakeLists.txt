# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(color_converter_DIR ${CMAKE_SOURCE_DIR})
include_directories(${color_converter_DIR})

add_library( color_converter
             SHARED
             ${color_converter_DIR}/ColorConverter.cpp
)

target_link_libraries( color_converter
                       log
                       android)