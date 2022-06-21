#include <jni.h>
#include <string>
#include <HMAC.h>

#include "converters.h"
#include <jni.h>
#include <jni.h>

//
// Created by Martin Egermajer on 19.04.2022.
//

extern "C"
JNIEXPORT jbyteArray

JNICALL
Java_com_cleevio_vexl_cryptography_HmacCryptoLib_digest(JNIEnv *env,
                                                        jobject /* thiz */,
                                                        jbyteArray passwordArray,
                                                        jint passwordArrayLen,
                                                        jbyteArray messageArray,
                                                        jint messageArrayLen) {

    const char *password = byteArrayToChar(env, passwordArray, passwordArrayLen);
    const char *message = byteArrayToChar(env, messageArray, messageArrayLen);

    const char *digest = hmac_digest(password, message);
    jbyteArray digestArray = charToByteArray(env, digest);

    free((void *) password);
    free((void *) message);
    free((void *) digest);

    return digestArray;
}

extern "C"
JNIEXPORT jboolean

JNICALL
Java_com_cleevio_vexl_cryptography_HmacCryptoLib_verify(JNIEnv *env,
                                                        jobject /* thiz */,
                                                        jbyteArray passwordArray,
                                                        jint passwordArrayLen,
                                                        jbyteArray messageArray,
                                                        jint messageArrayLen,
                                                        jbyteArray digestArray,
                                                        jint digestArrayLen) {

    const char *password = byteArrayToChar(env, passwordArray, passwordArrayLen);
    const char *message = byteArrayToChar(env, messageArray, messageArrayLen);
    const char *digest = byteArrayToChar(env, digestArray, digestArrayLen);

    bool result = hmac_digest(password, message);

    free((void *) password);
    free((void *) message);
    free((void *) digest);

    return (jboolean)
    result;
}