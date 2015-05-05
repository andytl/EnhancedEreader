LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
include $(OPENCV_PACKAGE_DIR)/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := TesterApplication
LOCAL_SRC_FILES := TesterApplication.cpp 

include $(BUILD_SHARED_LIBRARY)
