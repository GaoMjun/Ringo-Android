//
// Created by qq on 22/11/2016.
//

#include <jni.h>
#include "CMT.h"
#include <android/log.h>
#include <opencv2/imgproc.hpp>

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
                                                     jint x, jint y, jint w, jint h) {

    const Mat &im_gray = *(cv::Mat *)matAddrGr;

    cv::Rect initCTBox(x, y, w, h);

    CMTinitiated = false;
    if (cmtTracker != nullptr) {
        delete cmtTracker;
    }
    cmtTracker = new cmt::CMT();
    cmtTracker->initialize(im_gray, initCTBox);
    initTrackedPoints = cmtTracker->points_active.size();
    LOGD("initTrackingPoints %d", initTrackedPoints);
    CMTinitiated = true;
}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_ProcessCMT(JNIEnv *env, jclass thiz,
                                                        jlong matAddrGr) {

    if (!CMTinitiated)
        return;

    cv::Mat &im_gray  = *(cv::Mat*)matAddrGr;

    cmtTracker->processFrame(im_gray);


//    int w = (int) cmtTracker->bb_rot.size.width;
//    int h = (int) cmtTracker->bb_rot.size.height;
//    int x = (int) (cmtTracker->bb_rot.center.x - w / 2);
//    int y = (int) (cmtTracker->bb_rot.center.y - h / 2);
//    cv::rectangle(im_gray, cv::Rect(x, y, w, h), cv::Scalar(255, 0, 0));
}

JNIEXPORT jintArray JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetRect(JNIEnv *env, jclass thiz) {

    if (!CMTinitiated)
        return NULL;

    jintArray result;
    result = env->NewIntArray(4);

    jint fill[4];

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

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTgetResult(JNIEnv *env, jclass type) {

//    LOGD("initTrackingPoints %d %d", initTrackedPoints, cmt->trackedKeypoints.size());
    return (jboolean) (cmtTracker->points_active.size() > (initTrackedPoints / 4));

}

#ifdef __cplusplus
}
#endif