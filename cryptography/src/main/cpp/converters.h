//
// Created by Martin Egermajer on 19.04.2022.
//

#include <jni.h>
#include <model/KeyPair.h>

#ifndef VEXL_ANDROID_CONVERTERS_H
#define VEXL_ANDROID_CONVERTERS_H

#endif //VEXL_ANDROID_CONVERTERS_H

extern Curve DEFAULT_CURVE;

jobject keyPairToJObject(JNIEnv *env, KeyPair *keyPair);

KeyPair jObjectToKeyPair(JNIEnv *env, jobject *jObject);

char *byteArrayToChar(JNIEnv *env, jbyteArray array, jint publicKeyLenArg);

jbyteArray charToByteArray(JNIEnv *env, const char *str);