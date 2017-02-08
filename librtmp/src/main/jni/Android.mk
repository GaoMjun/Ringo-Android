LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := librtmp

LOCAL_SRC_FILES := \
	amf.c \
	error.c \
	hashswf.c \
	log.c \
	parseurl.c \
	rtmp.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/pili-librtmp

include $(BUILD_SHARED_LIBRARY)


