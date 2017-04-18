# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(opencv_include_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/opencv/include)
include_directories(${opencv_include_DIR})

set(cmt_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/cmt2)
include_directories(${cmt_DIR})

add_library( cmt_tracker
             SHARED
             ${cmt_DIR}/CMT.cpp
             ${cmt_DIR}/cmt_tracker.cpp
             ${cmt_DIR}/common.cpp
             ${cmt_DIR}/Consensus.cpp
             ${cmt_DIR}/fastcluster.cpp
             ${cmt_DIR}/Fusion.cpp
             ${cmt_DIR}/Matcher.cpp
             ${cmt_DIR}/Tracker.cpp
)

set(library_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

add_library( opencv_java3 SHARED IMPORTED )
set_target_properties( opencv_java3 PROPERTIES IMPORTED_LOCATION ${library_DIR}/libopencv_java3.so )

target_link_libraries( cmt_tracker
                       opencv_java3
                       log
                       android)
