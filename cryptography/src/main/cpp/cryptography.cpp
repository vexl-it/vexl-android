#include <jni.h>
#include <string>
#include <AES.h>
#include <HMAC.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_cleevio_vexl_cryptography_AesCryptoLib_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jstring jpassword,
        jstring jmessage) {

    const char *password = env->GetStringUTFChars(jpassword, nullptr);
    const char *message = env->GetStringUTFChars(jmessage, nullptr);

    const char *result = aes_encrypt(password, message);
    return env->NewStringUTF(result);

}