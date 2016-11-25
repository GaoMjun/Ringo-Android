//
// Created by qq on 22/11/2016.
//

#include <jni.h>
#include <CMT.h>

#ifdef __cplusplus
extern "C" {
#endif

using namespace std;
using namespace cv;

static bool CMTinitiated = false;
static CMT *cmt = new CMT();

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_OpenCMT(JNIEnv *env, jclass thiz,
                                                     jlong matAddrGr,
                                                     jint x1, jint y1, jint x2, jint y2,
                                                     jboolean isFrontCamera) {

    Mat &im_gray = *(Mat *) matAddrGr;
    if (isFrontCamera)
        flip(im_gray, im_gray, 1);

    Point p1(x1, y1);
    Point p2(x2, y2);

    CMTinitiated = false;
    if (cmt->initialise(im_gray, p1, p2))
        CMTinitiated = true;

}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_ProcessCMT(JNIEnv *env, jclass thiz,
                                                        jlong matAddrGr,
                                                        jboolean isFrontCamera) {

    if (!CMTinitiated)
        return;

    Mat& im_gray  = *(Mat*)matAddrGr;
    if (isFrontCamera)
        flip(im_gray, im_gray, 1);

    cmt->processFrame(im_gray);
}

JNIEXPORT jintArray JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetRect(JNIEnv *env, jclass thiz) {

    if (!CMTinitiated)
        return NULL;

    jintArray result;
    result = env->NewIntArray(4);

    jint fill[4];

    {
        fill[0] = cmt->boundingbox.x;
        fill[1] = cmt->boundingbox.y;
        fill[2] = cmt->boundingbox.width;
        fill[3] = cmt->boundingbox.height;

        env->SetIntArrayRegion(result, 0, 4, fill);
        return result;
    }

    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetResult(JNIEnv *env, jclass type) {

    return cmt->hasResult;

}

#ifdef __cplusplus
}
#endif