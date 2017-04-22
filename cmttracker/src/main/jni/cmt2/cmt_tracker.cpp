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
Java_io_github_gaomjun_cmttracker_CMTTracker_OpenCMT(JNIEnv *, jclass,
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

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_ProcessCMT(JNIEnv *env, jclass,
                                                        jlong matAddrGr,
                                                        jintArray rect) {

    if (!CMTinitiated)
        return JNI_FALSE;

    cv::Mat &im_gray  = *(cv::Mat*)matAddrGr;

    cmtTracker->processFrame(im_gray);

    bool result = cmtTracker->points_active.size() > (initTrackedPoints / 4);

    if (result) {
        float centerX = cmtTracker->bb_rot.center.x;
        float centerY = cmtTracker->bb_rot.center.y;
        float w = cmtTracker->bb_rot.size.width;
        float h = cmtTracker->bb_rot.size.height;

        jint fill[4];
        fill[0] = (jint) (centerX - w / 2);
        fill[1] = (jint) (centerY - h / 2);
        fill[2] = (jint) w;
        fill[3] = (jint) h;
        env->SetIntArrayRegion(rect, 0, 4, fill);

        return JNI_TRUE;
    }

    return JNI_FALSE;

/*    int w = (int) cmtTracker->bb_rot.size.width;
    int h = (int) cmtTracker->bb_rot.size.height;
    int x = (int) (cmtTracker->bb_rot.center.x - w / 2);
    int y = (int) (cmtTracker->bb_rot.center.y - h / 2);
    cv::rectangle(im_gray, cv::Rect(x, y, w, h), cv::Scalar(255, 0, 0));*/
}

#ifdef __cplusplus
}
#endif