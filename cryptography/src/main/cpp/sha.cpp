#include <jni.h>
#include <string>
#include <SHA.h>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_cleevio_vexl_cryptography_ShaCryptoLib_hash(
        JNIEnv *env,
        jobject /* this */,
        jstring jdata,
        jint jdata_length) {

    const char *data = env->GetStringUTFChars(jdata, nullptr);
    const int data_length = (int) jdata_length;

    const char *result = sha256_hash(data, data_length);
    return env->NewStringUTF(result);
}