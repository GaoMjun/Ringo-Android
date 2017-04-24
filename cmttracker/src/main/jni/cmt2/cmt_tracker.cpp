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

static void pre_process(void *frameBuffer, int frameWidth, int frameHeight, cv::Mat *matImage, bool isFront);

static bool CMTinitiated = false;
static size_t initTrackedPoints = 0;
static cmt::CMT *cmtTracker;
static cv::Mat *grayBuffer;


JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_OpenCMT(JNIEnv *, jclass,
                                                     jlong matAddrGr,
                                                     jint x, jint y, jint w, jint h) {

    const cv::Mat &im_gray = *(cv::Mat *)matAddrGr;

    cv::Rect initCTBox(x, y, w, h);

    CMTinitiated = false;
    if (cmtTracker != nullptr) {
        delete cmtTracker;
    }
    cmtTracker = new cmt::CMT();
    cv::equalizeHist(im_gray, im_gray);
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

    cv::equalizeHist(im_gray, im_gray);
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

JNIEXPORT void JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTInit(JNIEnv *env, jclass,
                                                     jobject _frameBuffer,
                                                     jint frameWidth, jint frameHeight,
                                                     jint x, jint y, jint w, jint h,
                                                     jboolean isFront) {

    void *frameBuffer = env->GetDirectBufferAddress(_frameBuffer);

    if (grayBuffer == nullptr) grayBuffer = new cv::Mat(72, 128, CV_8UC1);
    pre_process(frameBuffer, frameWidth, frameHeight, grayBuffer, isFront);

    cv::Rect initCTBox(x, y, w, h);

    CMTinitiated = false;
    if (cmtTracker != nullptr) {
        delete cmtTracker;
    }
    cmtTracker = new cmt::CMT();
    cmtTracker->initialize(*grayBuffer, initCTBox);
    initTrackedPoints = cmtTracker->points_active.size();
    LOGD("initTrackingPoints %d", initTrackedPoints);
    CMTinitiated = true;
}

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_cmttracker_CMTTracker_CMTProcessing(JNIEnv *env, jclass,
                                                     jobject _frameBuffer,
                                                     jint frameWidth, jint frameHeight,
                                                     jintArray rect,
                                                     jboolean isFront) {

    void *frameBuffer = env->GetDirectBufferAddress(_frameBuffer);

    if (grayBuffer == nullptr) grayBuffer = new cv::Mat(72, 128, CV_8UC1);
    pre_process(frameBuffer, frameWidth, frameHeight, grayBuffer, isFront);

    if (!CMTinitiated)
        return JNI_FALSE;

    cmtTracker->processFrame(*grayBuffer);

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
}

static void pre_process(void *frameBuffer, int frameWidth, int frameHeight, cv::Mat *matImage, bool isFront) {
    cv::Mat matBuffer(frameHeight, frameWidth, CV_8UC1, frameBuffer);
    cv::resize(matBuffer, *matImage, (*matImage).size(), 0, 0, cv::INTER_NEAREST);
    if (isFront) cv::flip(*matImage, *matImage, 1);
}

#ifdef __cplusplus
}
#endif