#include <jni.h>
#include <string>
#include <AES.h>

#include "converters.h"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_AesCryptoLib_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray passwordArray,
        jint passwordArrayLen,
        jbyteArray messageArray,
        jint messageArrayLen) {

    const char *password = byteArrayToChar(env, passwordArray, passwordArrayLen);
    const char *message = byteArrayToChar(env, messageArray, messageArrayLen);

    const char *cipher = aes_encrypt(password, message);
    jbyteArray cipherArray = charToByteArray(env, cipher);

    free((void *) password);
    free((void *) message);
    free((void *) cipher);

    return cipherArray;

}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_cleevio_vexl_cryptography_AesCryptoLib_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray passwordArray,
        jint passwordArrayLen,
        jbyteArray cipherArray,
        jint cipherArrayLen) {

    const char *password = byteArrayToChar(env, passwordArray, passwordArrayLen);
    const char *cipher = byteArrayToChar(env, cipherArray, cipherArrayLen);

    const char *message = aes_decrypt(password, cipher);
    jbyteArray messageArray = charToByteArray(env, message);

    free((void *) password);
    free((void *) message);
    free((void *) cipher);

    return messageArray;

}