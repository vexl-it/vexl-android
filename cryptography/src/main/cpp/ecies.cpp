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
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray publicKeyArray,
        jint publicKeyArrayLen,
        jbyteArray messageArray,
        jint messageArrayLen) {

    const char *publicKey = byteArrayToChar(env, publicKeyArray, publicKeyArrayLen);
    const char *message = byteArrayToChar(env, messageArray, messageArrayLen);

    const char *cipher = ecies_encrypt(publicKey, message);
    jbyteArray cipherArray = charToByteArray(env, cipher);

    free((void *) publicKey);
    free((void *) message);
    free((void *) cipher);

    return cipherArray;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray publicKeyArray,
        jint publicKeyArrayLen,
        jbyteArray privateKeyArray,
        jint privateKeyArrayLen,
        jbyteArray messageArray,
        jint messageArrayLen) {

    const char *publicKey = byteArrayToChar(env, publicKeyArray, publicKeyArrayLen);
    const char *privateKey = byteArrayToChar(env, privateKeyArray, privateKeyArrayLen);
    const char *message = byteArrayToChar(env, messageArray, messageArrayLen);

    const char *cipher = ecies_decrypt(publicKey, privateKey, message);
    jbyteArray cipherArray = charToByteArray(env, cipher);

    free((void *) publicKey);
    free((void *) privateKey);
    free((void *) message);
    free((void *) cipher);

    return cipherArray;
}