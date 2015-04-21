LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := TesterApplication
LOCAL_SRC_FILES := TesterApplication.cpp

include $(BUILD_SHARED_LIBRARY)
