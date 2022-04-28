#include <jni.h>
#include <string>
#include <ECIES.h>

#include "converters.h"

//
// Created by Martin Egermajer on 19.04.2022.
//

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

    const char *public_key = env->GetStringUTFChars(publicKeyArg, nullptr);
    const char *message = env->GetStringUTFChars(messageArg, nullptr);

    const char *result = ecies_encrypt(public_key, message);
    return env->NewStringUTF(result);

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_EciesCryptoLib_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jobject keysArg,
        jstring encodedMessageArg) {

    KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *message = env->GetStringUTFChars(encodedMessageArg, nullptr);

    const char *result = ecies_decrypt(keys.pemPublicKey, keys.pemPrivateKey, message);
    return env->NewStringUTF(result);
}