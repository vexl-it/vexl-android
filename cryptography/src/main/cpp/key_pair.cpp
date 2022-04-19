#include <jni.h>
#include <string>
#include <model/Curve.h>
#include <model/KeyPair.h>

//
// Created by Martin Egermajer on 19.04.2022.
//

#include "convertors.h"

extern "C"
JNIEXPORT jobject JNICALL
Java_com_cleevio_vexl_cryptography_KeyPairCryptoLib_generateKeyPair(JNIEnv *env) {
    KeyPair keyPair = generate_key_pair(secp192k1);
    return keyPairToJObject(env, &keyPair);
}