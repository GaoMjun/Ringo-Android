//
// Created by qq on 3/2/2017.
//

#include <jni.h>
#include <android/log.h>
#include <cstddef>
#include <rtmp.h>
#include <cstdio>
#include <log.h>
#include <string.h>

static const char *TAG = "RTMPNative";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

static const AVal av_setDataFrame = AVC("@setDataFrame");
static const AVal av_SDKVersion = AVC("ringo");

#define SAVC(x)    static const AVal av_ ## x = AVC(#x)

SAVC(onMetaData);
SAVC(duration);
SAVC(fileSize);
SAVC(width);
SAVC(height);
SAVC(videocodecid);
SAVC(avc1);
SAVC(videodatarate);
SAVC(framerate);

SAVC(audiocodecid);
SAVC(mp4a);
SAVC(audiodatarate);
SAVC(audiosamplerate);
SAVC(audiosamplesize);
SAVC(stereo);
SAVC(encoder);

static void RTMP_LOG_ENABLE();

#ifdef __cplusplus
extern "C" {
#endif

static PILI_RTMP *rtmp = NULL;
static RTMPError error;
static jobject connectStateCallbackGlobalRef;

JNIEXPORT jboolean JNICALL
Java_io_github_gaomjun_rtmpclient_RTMPNative_CONNECT(JNIEnv *env, jclass type,
                                                     jstring publishUrl_) {

    const char *publishUrl = env->GetStringUTFChars(publishUrl_, 0);

    LOGD("%s", "PILI_RTMP_Alloc()");
    rtmp = PILI_RTMP_Alloc();

    LOGD("%s", "PILI_RTMP_Init()");
    PILI_RTMP_Init(rtmp);

    rtmp->Link.timeout = 5;
    rtmp->Link.send_timeout = 5;

    LOGD("%s", "RTMP_LOG_ENABLE()");
//    RTMP_LOG_ENABLE();

    LOGD("%s %s", "PILI_RTMP_SetupURL()", publishUrl);
    if (PILI_RTMP_SetupURL(rtmp, publishUrl, &error) == FALSE) {
        LOGD("%s", "PILI_RTMP_SetupURL() failed!");
        goto Failed;
    }

    //    rtmp->m_errorCallback = RTMPErrorCallback;
    //    rtmp->m_connCallback = ConnectionTimeCallback;
    //    rtmp->m_userData = (__bridge void *)self;
    //    rtmp->m_msgCounter = 1;
    //    rtmp->Link.timeout = 2;

    LOGD("%s", "PILI_RTMP_EnableWrite()");
    PILI_RTMP_EnableWrite(rtmp);

    LOGD("%s", "PILI_RTMP_Connect()");
    if (PILI_RTMP_Connect(rtmp, NULL, &error) == FALSE) {
        LOGD("%s %s %s", "PILI_RTMP_Connect() failed!", error.code, error.message);
        goto Failed;
    }

    LOGD("%s", "PILI_RTMP_ConnectStream()");
    if (PILI_RTMP_ConnectStream(rtmp, 0, &error) == FALSE) {
        LOGD("%s", "PILI_RTMP_ConnectStream() failed!");
        goto Failed;
    }

    env->ReleaseStringUTFChars(publishUrl_, publishUrl);

    //    _isConnected = YES;
    //    _isConnecting = NO;
    //    _isReconnecting = NO;
    //    _isSending = NO;
//    _state = LIVING;
//    success(YES);
    return JNI_TRUE;

//    connectStateCallbackGlobalRef = env->NewGlobalRef(connectStateCallback);
//
//    jclass connectStateCallbackInterface;
//    jmethodID connectStateMethodID;
//
//    LOGD("%s", "get java interface");
//    connectStateCallbackInterface = env->GetObjectClass(connectStateCallbackGlobalRef);
//    LOGD("%s", "get java method");
//    connectStateMethodID = env->GetMethodID(connectStateCallbackInterface, "connectState", "(Ljava/lang/Boolean;)V");
//
//    LOGD("%s", "call java method");
//    env->CallVoidMethod(connectStateCallbackInterface, connectStateMethodID, JNI_TRUE);

    Failed:
        PILI_RTMP_Close(rtmp, NULL);
        PILI_RTMP_Free(rtmp);
        rtmp = NULL;
        return JNI_FALSE;
//        _state = STOP;
//        success(NO);
//        LOGD("%s", "call java method");
//        env->CallVoidMethod(connectStateCallbackInterface, connectStateMethodID, JNI_FALSE);

}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_rtmpclient_RTMPNative_CLOSE(JNIEnv *env, jclass type) {

    PILI_RTMP_Close(rtmp, NULL);
    PILI_RTMP_Free(rtmp);
    rtmp = NULL;

}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_rtmpclient_RTMPNative_SEND_1METADATA(JNIEnv *env, jclass type,
                                                            jint videoWidth, jint videoHeight, jint videoBitrate, jint videoFps,
                                                            jint audioSampleRate, jint audioBitrate) {

    PILI_RTMPPacket packet;

    char pbuf[2048], *pend = pbuf + sizeof(pbuf);

    packet.m_nChannel = 0x03;
    packet.m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet.m_packetType = RTMP_PACKET_TYPE_INFO;
    packet.m_nTimeStamp = 0;
    packet.m_nInfoField2 = rtmp->m_stream_id;
    packet.m_hasAbsTimestamp = TRUE;
    packet.m_body = pbuf + RTMP_MAX_HEADER_SIZE;

    char *enc = packet.m_body;
    enc = AMF_EncodeString(enc, pend, &av_setDataFrame);
    enc = AMF_EncodeString(enc, pend, &av_onMetaData);

    *enc++ = AMF_OBJECT;

    enc = AMF_EncodeNamedNumber(enc, pend, &av_duration, 0.0);
    enc = AMF_EncodeNamedNumber(enc, pend, &av_fileSize, 0.0);

    // videosize
    enc = AMF_EncodeNamedNumber(enc, pend, &av_width, videoWidth);
    enc = AMF_EncodeNamedNumber(enc, pend, &av_height, videoHeight);

    // video
    enc = AMF_EncodeNamedString(enc, pend, &av_videocodecid, &av_avc1);

    enc = AMF_EncodeNamedNumber(enc, pend, &av_videodatarate, videoBitrate / 1000.f);
    enc = AMF_EncodeNamedNumber(enc, pend, &av_framerate, videoFps);

    // audio
    enc = AMF_EncodeNamedString(enc, pend, &av_audiocodecid, &av_mp4a);
    enc = AMF_EncodeNamedNumber(enc, pend, &av_audiodatarate, audioBitrate);

    enc = AMF_EncodeNamedNumber(enc, pend, &av_audiosamplerate, audioSampleRate);
    enc = AMF_EncodeNamedNumber(enc, pend, &av_audiosamplesize, 16.0);
    enc = AMF_EncodeNamedBoolean(enc, pend, &av_stereo, 1);

    // sdk version
    enc = AMF_EncodeNamedString(enc, pend, &av_encoder, &av_SDKVersion);

    *enc++ = 0;
    *enc++ = 0;
    *enc++ = AMF_OBJECT_END;

    packet.m_nBodySize = (uint32_t)(enc - packet.m_body);
    if (!PILI_RTMP_SendPacket(rtmp, &packet, FALSE, NULL)) {
        return;
    }

}

JNIEXPORT void JNICALL
Java_io_github_gaomjun_rtmpclient_RTMPNative_SEND_1PACKET(JNIEnv *env, jclass type,
                                                          jint packetType,
                                                          jbyteArray data_,
                                                          jint size,
                                                          jlong timestamp) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    PILI_RTMPPacket rtmp_pack;
    PILI_RTMPPacket_Reset(&rtmp_pack);
    PILI_RTMPPacket_Alloc(&rtmp_pack, size);

    rtmp_pack.m_nBodySize = size;
    memcpy(rtmp_pack.m_body, data, size);
    rtmp_pack.m_hasAbsTimestamp = 0;
    rtmp_pack.m_packetType = packetType;
    rtmp_pack.m_nInfoField2 = rtmp->m_stream_id;
    rtmp_pack.m_nChannel = 0x04;
    if ((packetType == RTMP_PACKET_TYPE_AUDIO) && (size != 4)) {
        rtmp_pack.m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    } else {
        rtmp_pack.m_headerType = RTMP_PACKET_SIZE_LARGE;
    }
    rtmp_pack.m_nTimeStamp = timestamp;

    PILI_RTMP_SendPacket(rtmp, &rtmp_pack, 0, NULL);

    PILI_RTMPPacket_Free(&rtmp_pack);

    env->ReleaseByteArrayElements(data_, data, 0);
}

static void log_callback(int level, const char *msg, va_list args) {
    char log[1024];
    vsprintf(log, msg, args);
    LOGD("%s", log);
}

static void RTMP_LOG_ENABLE() {
    RTMP_LogSetLevel(RTMP_LOGINFO);
    RTMP_LogSetCallback(log_callback);
}

#ifdef __cplusplus
}
#endif