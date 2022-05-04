#include <jni.h>
#include <string>
#include <ECIES.h>
#include <android/log.h>

#include "converters.h"

//
// Created by Martin Egermajer on 19.04.2022.
//
#define APPNAME "module_name jni"
#define LOGD(TEXT) __android_log_print(ANDROID_LOG_DEBUG , APPNAME, TEXT);


extern "C"
JNIEXPORT void JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_init(JNIEnv *env, jobject /* this */) {
    ecies_init();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jstring publicKeyArg,
        jstring messageArg) {

    LOGD("ASDX encryptLog 1")
    const char *public_key = env->GetStringUTFChars(publicKeyArg, nullptr);
    LOGD("ASDX encryptLog 2");
    const char *message = env->GetStringUTFChars(messageArg, nullptr);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3 %s", public_key);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.1 %s", message);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.2 %d", strlen(public_key));

    const char *result = ecies_encrypt(public_key, message);
    LOGD("ASDX encryptLog 4");
    return env->NewStringUTF(result);

}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_encrypt2(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray publicKeyArg,
        jbyteArray messageArg) {

    LOGD("ASDX encryptLog 1");
    const char *public_key = (const char *) env->GetByteArrayElements(publicKeyArg, nullptr);
    LOGD("ASDX encryptLog 2");
    const char *message = (const char *) env->GetByteArrayElements(messageArg, nullptr);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3 %s", public_key);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.1 %s", message);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.2 %d", strlen(public_key));
    const char *result = ecies_encrypt(public_key, message);
    LOGD("ASDX encryptLog 4");
    jbyteArray someJbyteArray = env->NewByteArray((jsize) 10000);
    LOGD("ASDX encryptLog 5");
    env->SetByteArrayRegion(someJbyteArray, 0, (jsize) strlen(result), reinterpret_cast<const jbyte *>(result));
    LOGD("ASDX encryptLog 6");
    return someJbyteArray;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jobject keysArg,
        jstring encodedMessageArg) {

    LOGD("ASDX decryptLog 1");
    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    LOGD("ASDX decryptLog 2");
    const char *message = env->GetStringUTFChars(encodedMessageArg, nullptr);

    LOGD("ASDX decryptLog 3");
    const char *result = ecies_decrypt(keys.pemPublicKey, keys.pemPrivateKey, message);
    LOGD("ASDX decryptLog 4");
    return env->NewStringUTF(result);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_decrypt2(
        JNIEnv *env,
        jobject /* this */,
        jobject keysArg,
        jbyteArray encodedMessageArg) {

    LOGD("ASDX decryptLog 1");
    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    LOGD("ASDX decryptLog 2");
    const char *message = (const char *) env->GetByteArrayElements(encodedMessageArg, nullptr);
    LOGD("ASDX decryptLog 3");

    const char *result = ecies_decrypt(keys.pemPublicKey, keys.pemPrivateKey, message);
    LOGD("ASDX decryptLog 4");
    jbyteArray someJbyteArray = env->NewByteArray((jsize) 1000000);
    LOGD("ASDX decryptLog 5");
    env->SetByteArrayRegion(someJbyteArray, 0, (jsize) strlen(result), reinterpret_cast<const jbyte *>(result));
    LOGD("ASDX decryptLog 6");
    return someJbyteArray;
}