//
// Created by qq on 20/1/2017.
//

#include <jni.h>
#include <android/log.h>
#include <cstddef>
#include "libyuv.h"
#include <math.h>

static const char *TAG = "YUVNative";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_io_github_gaomjun_libyuv_YUVNative_SCALE(JNIEnv *env, jclass type,
                                              jbyteArray i_yuv_, jint i_width, jint i_height,
                                              jbyteArray o_yuv_, jint o_width, jint o_height) {

    jbyte *i_yuv = env->GetByteArrayElements(i_yuv_, NULL);
    jbyte *o_yuv = env->GetByteArrayElements(o_yuv_, NULL);

//    LOGD("%s", "SCALE");

    int src_stride_y = (ceil(i_width/16.0))*16;
    int src_stride_u = (ceil(src_stride_y>>1)/16.0)*16;
    int src_stride_v = src_stride_u;

//    LOGD("SCALE_SRC src_stride_y %d src_stride_u %d src_stride_v %d", src_stride_y, src_stride_u, src_stride_v);

    int src_y_size = src_stride_y * i_height;
    int src_v_size = src_stride_v * i_height >> 1;

//    LOGD("SCALE_SRC y_size %d u_size %d v_size %d", src_y_size, src_u_size, src_v_size);

    const uint8 *src_y = (unsigned char *)i_yuv;
    const uint8 *src_v = (unsigned char *)(i_yuv + src_y_size);
    const uint8 *src_u = (unsigned char *)(i_yuv + src_y_size + src_v_size);

    /*************************************************************/

    int dst_stride_y = (ceil(o_width/16.0))*16;
    int dst_stride_u = (ceil((dst_stride_y>>1)/16.0))*16;
    int dst_stride_v = dst_stride_u;

//    LOGD("SCALE_DST dst_stride_y %d dst_stride_u %d dst_stride_v %d", dst_stride_y, dst_stride_u, dst_stride_v);

    int dst_y_size = dst_stride_y * o_height;
    int dst_u_size = dst_stride_u * o_height >> 1;

//    LOGD("SCALE_DST y_size %d u_size %d v_size %d", dst_y_size, dst_u_size, dst_v_size);

    uint8 *dst_y = (unsigned char *)o_yuv;
    uint8 *dst_u = (unsigned char *)(o_yuv + dst_y_size);
    uint8 *dst_v = (unsigned char *)(o_yuv + dst_y_size + dst_u_size);

    libyuv::Scale(src_y, src_u, src_v, src_stride_y, src_stride_u, src_stride_v, i_width, i_height,
                  dst_y, dst_u, dst_v, dst_stride_y, dst_stride_u, dst_stride_v, o_width, o_height,
                  LIBYUV_FALSE);

    env->ReleaseByteArrayElements(i_yuv_, i_yuv, 0);
    env->ReleaseByteArrayElements(o_yuv_, o_yuv, 0);
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_libyuv_YUVNative_ROTATE(JNIEnv *env, jclass type,
                                               jbyteArray i_yuv_,
                                               jint i_width, jint i_height,
                                               jint degree,
                                               jbyteArray o_yuv_) {

    jbyte *i_yuv = env->GetByteArrayElements(i_yuv_, NULL);
    jbyte *o_yuv = env->GetByteArrayElements(o_yuv_, NULL);

    // TODO

    env->ReleaseByteArrayElements(i_yuv_, i_yuv, 0);
    env->ReleaseByteArrayElements(o_yuv_, o_yuv, 0);
}


JNIEXPORT void JNICALL
Java_io_github_gaomjun_libyuv_YUVNative_NV21ToI420(JNIEnv *env, jclass type,
                                                   jbyteArray i_nv21_,
                                                   jint i_width, jint i_height,
                                                   jbyteArray o_i420_) {

    jbyte *i_yv12 = env->GetByteArrayElements(i_nv21_, NULL);
    jbyte *o_i420 = env->GetByteArrayElements(o_i420_, NULL);

    // TODO

    env->ReleaseByteArrayElements(i_nv21_, i_yv12, 0);
    env->ReleaseByteArrayElements(o_i420_, o_i420, 0);
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_libyuv_YUVNative_CONVERT(JNIEnv *env, jclass type,
                                                jbyteArray i_nv21_,
                                                jint i_width, jint i_height,
                                                jbyteArray o_i420_,
                                                jint o_width, jint o_height) {

    jbyte *i_nv21 = env->GetByteArrayElements(i_nv21_, NULL);
    jbyte *o_i420 = env->GetByteArrayElements(o_i420_, NULL);

    // 1. scale
//    LOGD("%s", "SCALE");
    Java_io_github_gaomjun_libyuv_YUVNative_SCALE(env, type, i_nv21_, i_width, i_height, o_i420_, o_width, o_height);

    // 2. nv21 to i420
//    LOGD("%s", "NV21ToI420");
    Java_io_github_gaomjun_libyuv_YUVNative_NV21ToI420(env, type, o_i420_, o_width, o_height, o_i420_);

    env->ReleaseByteArrayElements(i_nv21_, i_nv21, 0);
    env->ReleaseByteArrayElements(o_i420_, o_i420, 0);
}

#ifdef __cplusplus
}
#endif