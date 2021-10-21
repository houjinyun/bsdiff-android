LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := bsdiff
LOCAL_SRC_FILES :=  bsdiff.c
LOCAL_C_INCLUDES += \ $(JNI_H_INCLUDE) bzip2
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := bspatch
LOCAL_SRC_FILES :=  bspatch.c
LOCAL_C_INCLUDES += \ $(JNI_H_INCLUDE) bzip2
include $(BUILD_SHARED_LIBRARY)