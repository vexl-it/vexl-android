#include <jni.h>
#include <string>
#include <model/Curve.h>
#include <model/KeyPair.h>

//
// Created by Martin Egermajer on 19.04.2022.
//

#include "converters.h"

extern "C"
JNIEXPORT jobject JNICALL
Java_com_cleevio_vexl_cryptography_KeyPairCryptoLib_generateKeyPair(JNIEnv *env) {
    KeyPair keyPair = generate_key_pair(DEFAULT_CURVE);
    return keyPairToJObject(env, &keyPair);
}