#include <jni.h>
#include <string>
#include <ECDSA.h>

#include "converters.h"

//
// Created by Martin Egermajer on 19.04.2022.
//

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_EcdsaCryptoLib_sign(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray publicKeyArray,
        jint publicKeyArrayLen,
        jbyteArray privateKeyArray,
        jint privateKeyArrayLen,
        jbyteArray dataArray,
        jint dataArrayLen) {

    const char *publicKey = byteArrayToChar(env, publicKeyArray, publicKeyArrayLen);
    const char *privateKey = byteArrayToChar(env, privateKeyArray, privateKeyArrayLen);
    const char *data = byteArrayToChar(env, dataArray, dataArrayLen);

    const char *signature = ecdsa_sign(publicKey, privateKey, data, dataArrayLen);
    jbyteArray signatureArray = charToByteArray(env, signature);

    free((void *) publicKey);
    free((void *) privateKey);
    free((void *) data);
    free((void *) signature);

    return signatureArray;

}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cleevio_vexl_cryptography_EcdsaCryptoLib_verify(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray publicKeyArray,
        jint publicKeyArrayLen,
        jbyteArray dataArray,
        jint dataArrayLen,
        jbyteArray signatureArray,
        jint signatureArrayLen) {

    const char *publicKey = byteArrayToChar(env, publicKeyArray, publicKeyArrayLen);
    const char *data = byteArrayToChar(env, dataArray, dataArrayLen);
    const char *signature = byteArrayToChar(env, signatureArray, signatureArrayLen);

    bool result = ecdsa_verify(publicKey, data, dataArrayLen, signature);

    free((void *) publicKey);
    free((void *) data);
    free((void *) signature);

    return (jboolean) result;
}