#include <jni.h>
#include <string>
#include <ECIES.h>

#include "convertors.h"

//
// Created by Martin Egermajer on 19.04.2022.
//

extern "C"
JNIEXPORT void JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_init(JNIEnv *env, jobject thiz) {
    ecies_init();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_encrypt(JNIEnv *env, jobject thiz, jobject keysArg, jstring messageArg) {

    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *message = env->GetStringUTFChars(messageArg, nullptr);

    const char *result = ecies_encrypt(keys, message);
    return env->NewStringUTF(result);

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_decrypt(JNIEnv *env, jobject thiz, jobject keysArg, jstring encodedMessageArg) {

    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *message = env->GetStringUTFChars(encodedMessageArg, nullptr);

    const char *result = ecies_decrypt(keys, message);
    return env->NewStringUTF(result);
}