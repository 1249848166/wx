#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_su_wx_core_Core_getAppId(
        JNIEnv *env,
        jobject /* this */) {
    std::string appId = "vS8EOFmoooV6s8mpaPdD7wjg-gzGzoHsz";
    return env->NewStringUTF(appId.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_su_wx_core_Core_getClientKey(
        JNIEnv *env,
        jobject /* this */) {
    std::string clientKey = "BNMdW7IbU47h7K2YOcwdSLv7";
    return env->NewStringUTF(clientKey.c_str());
}
