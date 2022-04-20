//
// Created by Martin Egermajer on 19.04.2022.
//

#include "convertors.h"

Curve DEFAULT_CURVE = secp224r1;

jobject keyPairToJObject(JNIEnv *env, KeyPair *keyPair) {

    jclass cls = env->FindClass("com/cleevio/vexl/cryptography/model/KeyPair");
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    jvalue args[2];
    args[0].l = env->NewStringUTF(keyPair->pemPrivateKey);
    args[1].l = env->NewStringUTF(keyPair->pemPublicKey);

    return env->NewObjectA(cls, constructor, args);
}

KeyPair jObjectToKeyPair(JNIEnv *env, jobject *jObj) {

    jclass cls = env->GetObjectClass(*jObj);

    jstring privateKey = (jstring) env->GetObjectField(
            *jObj,
            env->GetFieldID(
                    cls,
                    "privateKey",
                    "Ljava/lang/String;"
            )
    );

    jstring publicKey = (jstring) env->GetObjectField(
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