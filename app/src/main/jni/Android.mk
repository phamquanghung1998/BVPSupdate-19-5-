LOCAL_PATH:= $(call my-dir)  
include $(CLEAR_VARS)

APP_STL := gnustl_shared

LOCAL_MODULE:= hello-android-jni

LOCAL_SRC_FILES:= native-lib.cpp

include $(BUILD_STATIC_LIBRARY)  
