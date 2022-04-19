//
// Created by Martin Egermajer on 19.04.2022.
//

#include "convertors.h"

jobject keyPairToJObject(JNIEnv *env, KeyPair *keyPair) {

    jclass cls = env->FindClass("com/cleevio/vexl/cryptography/model/KeyPair");
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    jvalue args[2];
    args[0].l = env->NewStringUTF(keyPair->pemPrivateKey);
    args[1].l = env->NewStringUTF(keyPair->pemPublicKey);

    return env->NewObjectA(cls, constructor, args);
}
