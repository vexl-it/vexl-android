//
// Created by Martin Egermajer on 19.04.2022.
//

#include "converters.h"

Curve DEFAULT_CURVE = secp224r1;

jobject keyPairToJObject(JNIEnv *env, KeyPair *keyPair) {

    jclass cls = env->FindClass("com/cleevio/vexl/cryptography/model/KeyPairInternal");
    jmethodID constructor = env->GetMethodID(cls, "<init>", "([B[B)V");
    jvalue args[2];
    jbyteArray privArray = charToByteArray(env, keyPair->pemPrivateKey);
    jbyteArray pubArray = charToByteArray(env, keyPair->pemPublicKey);

    args[0].l = (jobject) privArray;
    args[1].l = (jobject) pubArray;

    return env->NewObjectA(cls, constructor, args);
}

KeyPair jObjectToKeyPair(JNIEnv *env, jobject *jObj) {

    jclass cls = env->GetObjectClass(*jObj);

    auto privateKey = (jstring) env->GetObjectField(
            *jObj,
            env->GetFieldID(
                    cls,
                    "privateKey",
                    "Ljava/lang/String;"
            )
    );

    auto publicKey = (jstring) env->GetObjectField(
            *jObj,
            env->GetFieldID(
                    cls,
                    "publicKey",
                    "Ljava/lang/String;"
            )
    );

    KeyPair keys;
    keys.pemPrivateKey = (char *) env->GetStringUTFChars(privateKey, nullptr);
    keys.pemPublicKey = (char *) env->GetStringUTFChars(publicKey, nullptr);
    keys.curve = DEFAULT_CURVE;

    return keys;
}

char *byteArrayToChar(JNIEnv *env, jbyteArray array, jint arrayLen) {
    const char *weirdTerminatedString = (char *) env->GetByteArrayElements(array, nullptr);
    char *nullTerminatedString = (char *) malloc(arrayLen + 1);
    memcpy(nullTerminatedString, weirdTerminatedString, arrayLen);
    nullTerminatedString[arrayLen] = 0;
    free((void *) weirdTerminatedString);
    return nullTerminatedString;
}

jbyteArray charToByteArray(JNIEnv *env, const char *str) {
    if (str == NULL) {
        return env->NewByteArray(0);
    }
    jsize strLen = strlen(str);
    jbyteArray byteArray = env->NewByteArray(strLen);
    env->SetByteArrayRegion(byteArray, 0, strLen, reinterpret_cast<const jbyte *>(str));
    return byteArray;
}

