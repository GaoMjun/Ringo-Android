//
// Created by qq on 22/12/2016.
//

#include <jni.h>
#include <android/log.h>
#include <cstddef>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_io_github_gaomjun_yuvdecoder_YUVDecoder_YUVToARGB(JNIEnv *env, jclass type,
                                                        jbyteArray yuv420sp_,
                                                        jint width,
                                                        jint height,
                                                        jintArray argb_) {

    jbyte *yuv420sp = env->GetByteArrayElements(yuv420sp_, NULL);
    jint *argb = env->GetIntArrayElements(argb_, NULL);


    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    for(j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for(i = 0; i < w; i++) {
            Y = yuv420sp[pixPtr];
            if(Y < 0)
                Y += 255;
            if((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv420sp[cOff];
                if(Cb < 0)
                    Cb += 127;
                else
                    Cb -= 128;
                Cr = yuv420sp[cOff + 1];
                if(Cr < 0)
                    Cr += 127;
                else
                    Cr -= 128;
            }

            //ITU-R BT.601 conversion
            //
            //R = 1.164*(Y-16) + 2.018*(Cr-128);
            //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
            //B = 1.164*(Y-16) + 1.596*(Cb-128);
            //
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if(R < 0)
                R = 0;
            else if(R > 255)
                R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if(G < 0)
                G = 0;
            else if(G > 255)
                G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if(B < 0)
                B = 0;
            else if(B > 255)
                B = 255;
            argb[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
        }
    }

    env->ReleaseByteArrayElements(yuv420sp_, yuv420sp, 0);
    env->ReleaseIntArrayElements(argb_, argb, 0);
}

//JNIEXPORT void JNICALL
//Java_io_github_gaomjun_yuvdecoder_YUVDecoder_YUVToARGB(JNIEnv *env, jclass type,
//                                                       jbyteArray yuv420sp_,
//                                                       jint width,
//                                                       jint height,
//                                                       jbyteArray argb_) {
//
//    jbyte *yuv420sp = env->GetByteArrayElements(yuv420sp_, NULL);
//    jbyte *argb = env->GetByteArrayElements(argb_, NULL);
//
//
//    int             sz;
//    int             i;
//    int             j;
//    int             Y;
//    int             Cr = 0;
//    int             Cb = 0;
//    int             pixPtr = 0;
//    int             jDiv2 = 0;
//    int             R = 0;
//    int             G = 0;
//    int             B = 0;
//    int             cOff;
//    int w = width;
//    int h = height;
//    sz = w * h;
//
//    for(j = 0; j < h; j++) {
//        pixPtr = j * w;
//        jDiv2 = j >> 1;
//        for(i = 0; i < w; i++) {
//            Y = yuv420sp[pixPtr];
//            if(Y < 0)
//                Y += 255;
//            if((i & 0x1) != 1) {
//                cOff = sz + jDiv2 * w + (i >> 1) * 2;
//                Cb = yuv420sp[cOff];
//                if(Cb < 0)
//                    Cb += 127;
//                else
//                    Cb -= 128;
//                Cr = yuv420sp[cOff + 1];
//                if(Cr < 0)
//                    Cr += 127;
//                else
//                    Cr -= 128;
//            }
//
//            //ITU-R BT.601 conversion
//            //
//            //R = 1.164*(Y-16) + 2.018*(Cr-128);
//            //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
//            //B = 1.164*(Y-16) + 1.596*(Cb-128);
//            //
//            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
//            R = Y + (Cr << 1) + (Cr >> 6);
//            if(R < 0)
//                R = 0;
//            else if(R > 255)
//                R = 255;
//            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
//            if(G < 0)
//                G = 0;
//            else if(G > 255)
//                G = 255;
//            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
//            if(B < 0)
//                B = 0;
//            else if(B > 255)
//                B = 255;
//            argb[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
//        }
//    }
//
//    env->ReleaseByteArrayElements(yuv420sp_, yuv420sp, 0);
//    env->ReleaseByteArrayElements(argb_, argb, 0);
//}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_yuvdecoder_YUVDecoder_YUVToABGR(JNIEnv *env, jclass type,
                                                       jbyteArray yuv420sp_,
                                                       jint width,
                                                       jint height, jintArray abgr_) {

    jbyte *yuv420sp = env->GetByteArrayElements(yuv420sp_, NULL);
    jint *abgr = env->GetIntArrayElements(abgr_, NULL);

    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    for(j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for(i = 0; i < w; i++) {
            Y = yuv420sp[pixPtr];
            if(Y < 0)
                Y += 255;
            if((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv420sp[cOff];
                if(Cb < 0)
                    Cb += 127;
                else
                    Cb -= 128;
                Cr = yuv420sp[cOff + 1];
                if(Cr < 0)
                    Cr += 127;
                else
                    Cr -= 128;
            }

            //ITU-R BT.601 conversion
            //
            //R = 1.164*(Y-16) + 2.018*(Cr-128);
            //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
            //B = 1.164*(Y-16) + 1.596*(Cb-128);
            //
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if(R < 0)
                R = 0;
            else if(R > 255)
                R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if(G < 0)
                G = 0;
            else if(G > 255)
                G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if(B < 0)
                B = 0;
            else if(B > 255)
                B = 255;
            abgr[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
        }
    }

    env->ReleaseByteArrayElements(yuv420sp_, yuv420sp, 0);
    env->ReleaseIntArrayElements(abgr_, abgr, 0);
}

#ifdef __cplusplus
}
#endif