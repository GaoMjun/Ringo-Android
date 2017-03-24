//
// Created by qq on 13/3/2017.
//

#include <jni.h>
#include <android/log.h>
#include <cstddef>

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned int Color32;

static inline Color32 Color32ReverseARGB2RGBA(Color32 x)
{

    return
            ((x & 0xFF000000) >> 24) | //______AA
            ((x & 0x00FF0000) <<  8) | //RR______
            ((x & 0x0000FF00) <<  8) | //__GG____
            ((x & 0x000000FF) <<  8);  //____BB__
}

static inline Color32 Color32ReverseRGBA2ARGB(Color32 x)
{

    return
            ((x & 0xFF000000) >>  8) | //__RR____
            ((x & 0x00FF0000) >>  8) | //____GG__
            ((x & 0x0000FF00) >>  8) | //______BB
            ((x & 0x000000FF) << 24);  //AA______
}

static inline Color32 Color32ReverseABGR2ARGB(Color32 x)
{

    return
            ((x & 0xFF000000) >>  0) | //AA______
            ((x & 0x00FF0000) >> 16) | //__RR____
            ((x & 0x0000FF00) >>  0) | //____GG__
            ((x & 0x000000FF) << 16);  //______BB
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_gl_ColorConverter_RGBA2ARGB(JNIEnv *env, jobject instance,
                                                   jintArray rgba_,
                                                   jintArray argb_) {
    jint *rgba = env->GetIntArrayElements(rgba_, NULL);
    jint *argb = env->GetIntArrayElements(argb_, NULL);
    jsize length = env->GetArrayLength(rgba_);

    for (int i = 0; i < length; ++i) {
        argb[i] = Color32ReverseRGBA2ARGB((Color32)rgba[i]);
    }

    env->ReleaseIntArrayElements(rgba_, rgba, 0);
    env->ReleaseIntArrayElements(argb_, argb, 0);
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_gl_ColorConverter_ARGB2RGBA(JNIEnv *env, jobject instance,
                                                   jintArray argb_,
                                                   jintArray rgba_) {
    jint *argb = env->GetIntArrayElements(argb_, NULL);
    jint *rgba = env->GetIntArrayElements(rgba_, NULL);
    jsize length = env->GetArrayLength(argb_);

    for (int i = 0; i < length; ++i) {
        rgba[i] = Color32ReverseARGB2RGBA((Color32)argb[i]);
    }

    env->ReleaseIntArrayElements(argb_, argb, 0);
    env->ReleaseIntArrayElements(rgba_, rgba, 0);
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_gl_ColorConverter_ABGR2ARGB(JNIEnv *env, jobject instance,
                                                   jintArray abgr_,
                                                   jintArray rgba_) {
    jint *abgr = env->GetIntArrayElements(abgr_, NULL);
    jint *rgba = env->GetIntArrayElements(rgba_, NULL);
    jsize length = env->GetArrayLength(abgr_);

    for (int i = 0; i < length; ++i) {
        rgba[i] = Color32ReverseABGR2ARGB((Color32)abgr[i]);
    }

    env->ReleaseIntArrayElements(abgr_, abgr, 0);
    env->ReleaseIntArrayElements(rgba_, rgba, 0);
}

#ifdef __cplusplus
}
#endif