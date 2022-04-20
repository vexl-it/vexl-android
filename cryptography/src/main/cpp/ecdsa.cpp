#include <jni.h>
#include <string>
#include <ECDSA.h>

#include "converters.h"

//
// Created by Martin Egermajer on 19.04.2022.
//

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EcdsaCryptoLib_sign(JNIEnv *env, jobject thiz, jobject keysArg, jstring dataArg) {

    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *data = env->GetStringUTFChars(dataArg, nullptr);
    const int dataLength = strlen(data);

    const char *result = ecdsa_sign(keys, data, dataLength);
    return env->NewStringUTF(result);

}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cleevio_vexl_cryptography_EcdsaCryptoLib_verify(JNIEnv *env, jobject thiz, jobject keysArg, jstring dataArg, jstring signatureArg) {

    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *data = env->GetStringUTFChars(dataArg, nullptr);
    const int dataLength = strlen(data);
    const char *signature = env->GetStringUTFChars(signatureArg, nullptr);

    bool result = ecdsa_verify(keys, data, dataLength, (char *) signature);
    return (jboolean) result;
}