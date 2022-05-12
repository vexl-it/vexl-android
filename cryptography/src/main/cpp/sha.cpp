#include <jni.h>
#include <string>
#include <SHA.h>

#include "converters.h"

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_ShaCryptoLib_hash(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray dataArray,
        jint dataArrayLen) {

    const char *data = byteArrayToChar(env, dataArray, dataArrayLen);

    const char *digest = sha256_hash(data, dataArrayLen);
    jbyteArray digestArray = charToByteArray(env, digest);

    free((void *) digest);
    free((void *) data);

    return digestArray;
}