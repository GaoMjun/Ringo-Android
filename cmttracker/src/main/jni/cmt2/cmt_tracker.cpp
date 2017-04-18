//
// Created by qq on 22/11/2016.
//

#include <jni.h>
#include "CMT.h"
#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

static const char *TAG = "CMT_Tracker";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

static bool CMTinitiated = false;
static size_t initTrackedPoints = 0;
static cmt::CMT *cmtTracker;

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_OpenCMT(JNIEnv *env, jclass thiz,
                                                     jlong matAddrGr,
                                                     jint x1, jint y1, jint x2, jint y2,
                                                     jboolean isFrontCamera) {

    const Mat *im_gray = (cv::Mat *)matAddrGr;
    if (isFrontCamera)
        flip(*im_gray, *im_gray, 1);

    cv::Point p1(x1, y1);
    cv::Point p2(x2, y2);
    cv::Rect initCTBox(p1, p2);

    CMTinitiated = false;
    if (cmtTracker != nullptr) {
        delete cmtTracker;
    }
    cmtTracker = new cmt::CMT();
    cmtTracker->initialize(*im_gray, initCTBox);
    initTrackedPoints = cmtTracker->points_active.size();
//        LOGD("initTrackingPoints %d", initTrackedPoints);
    CMTinitiated = true;
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_ProcessCMT(JNIEnv *env, jclass thiz,
                                                        jlong matAddrGr,
                                                        jboolean isFrontCamera) {

    if (!CMTinitiated)
        return;

    cv::Mat &im_gray  = *(cv::Mat*)matAddrGr;
    if (isFrontCamera)
        flip(im_gray, im_gray, 1);

    cmtTracker->processFrame(im_gray);
}

JNIEXPORT jintArray JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetRect(JNIEnv *env, jclass thiz) {

    if (!CMTinitiated)
        return NULL;

    jintArray result;
    result = env->NewIntArray(4);

    jint fill[4];

    {
        float centerX = cmtTracker->bb_rot.center.x;
        float centerY = cmtTracker->bb_rot.center.y;
        float w = cmtTracker->bb_rot.size.width;
        float h = cmtTracker->bb_rot.size.height;

        fill[0] = (jint) (centerX - w / 2);
        fill[1] = (jint) (centerY - h / 2);
        fill[2] = (jint) w;
        fill[3] = (jint) h;

        env->SetIntArrayRegion(result, 0, 4, fill);
        return result;
    }

    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetResult(JNIEnv *env, jclass type) {

//    LOGD("initTrackingPoints %d %d", initTrackedPoints, cmt->trackedKeypoints.size());
    return (jboolean) (cmtTracker->points_active.size() > (initTrackedPoints / 4));

}

#ifdef __cplusplus
}
#endif