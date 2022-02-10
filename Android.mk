LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, src)

LOCAL_PACKAGE_NAME := SoterService

LOCAL_VENDOR_MODULE := true
#LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_CERTIFICATE := platform

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android.hidl.base-V1.0-java \
    vendor.sprd.hardware.soter-V1.0-java

include $(BUILD_PACKAGE)

