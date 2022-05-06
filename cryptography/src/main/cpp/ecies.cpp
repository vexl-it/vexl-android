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
        jint publicKeyLenArg,
        jbyteArray messageArg,
        jint messageLenArg) {

    LOGD("ASDX encryptLog 1");
    const char *public_key = (char *) env->GetByteArrayElements(publicKeyArg, nullptr);
    LOGD("ASDX encryptLog 2");
    const char *message = (char *) env->GetByteArrayElements(messageArg, nullptr);

    char nulled_message[messageLenArg + 1];
    memcpy(nulled_message, message, messageLenArg);
    nulled_message[messageLenArg] = 0;

    char nulled_publicKey[publicKeyLenArg + 1];
    memcpy(nulled_publicKey, public_key, publicKeyLenArg);
    nulled_publicKey[publicKeyLenArg] = 0;

//    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3 %s", nulled_publicKey);
//    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.1 %s", nulled_message);
//    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.2 %d", strlen(nulled_message));
//    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX encryptLog 3.3 %d", messageLenArg);

    //char *cipher = nullptr;
    const char *result = ecies_encrypt(nulled_publicKey, nulled_message);
    LOGD("ASDX encryptLog 4");
    jbyteArray someJbyteArray = env->NewByteArray((jsize) strlen(result));
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
        jbyteArray publicKeyArg,
        jint publicKeyLenArg,
        jbyteArray privKeyArg,
        jint privKeyLenArg,
        jbyteArray encodedMessageArg,
        jint encodedMessageLenArg) {

    LOGD("ASDX decryptLog 1");
    //KeyPair keys = jObjectToKeyPair(env, &keysArg);
    const char *message = (char *) env->GetByteArrayElements(encodedMessageArg, nullptr);
    LOGD("ASDX decryptLog 2");
    const char *publicKey = (char *) env->GetByteArrayElements(publicKeyArg, nullptr);
    LOGD("ASDX decryptLog 2.1");
    const char *privKey = (char *) env->GetByteArrayElements(privKeyArg, nullptr);
    LOGD("ASDX decryptLog 3");

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX decryptLog 3.0.1 %d", encodedMessageLenArg);
    char nulled_message[encodedMessageLenArg + 1];
    memcpy(nulled_message, message, encodedMessageLenArg);
    nulled_message[encodedMessageLenArg] = 0;
    LOGD("ASDX decryptLog 3.1");

    char nulled_public_key[publicKeyLenArg + 1];
    memcpy(nulled_public_key, publicKey, publicKeyLenArg);
    nulled_public_key[publicKeyLenArg] = 0;
    LOGD("ASDX decryptLog 3.2");

    char nulled_priv_key[privKeyLenArg + 1];
    memcpy(nulled_priv_key, privKey, privKeyLenArg);
    nulled_priv_key[privKeyLenArg] = 0;
    LOGD("ASDX decryptLog 3.3");

//    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX decryptLog 3.4 %s", nulled_message);
//    for (int i = 250; i < strlen(nulled_message); i++) {
//        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "%x", nulled_message[i]);
//    }
//    LOGD("END_MESSAGE");
//    for (int i = 0; i < strlen(nulled_public_key); i++) {
//        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "%x", nulled_public_key[i]);
//    }
//    LOGD("END_PUBLIC_KEY");
//    for (int i = 0; i < strlen(nulled_priv_key); i++) {
//        __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "%x", nulled_priv_key[i]);
//    }
//    LOGD("END_PRIV_KEY");

    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX decryptLog 3.4 %s", nulled_message);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX decryptLog 3.5 %s", nulled_public_key);
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "ASDX decryptLog 3.6 %s", nulled_priv_key);


    const char *result = ecies_decrypt(nulled_public_key, nulled_priv_key, nulled_message);
    LOGD("ASDX decryptLog 4");
    jbyteArray someJbyteArray = env->NewByteArray((jsize) strlen(result));
    LOGD("ASDX decryptLog 5");
    env->SetByteArrayRegion(someJbyteArray, 0, (jsize) strlen(result), reinterpret_cast<const jbyte *>(result));
    LOGD("ASDX decryptLog 6");

    return someJbyteArray;
}