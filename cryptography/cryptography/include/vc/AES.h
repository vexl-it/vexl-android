//
// Created by Adam Salih on 04.04.2022.
//

#ifndef PBKDF2_H
#define PBKDF2_H

#include <stdio.h>

#ifdef BUILD_FOR_LIBRARY

#include <openssl/err.h>
#include <openssl/ec.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rand.h>
#include <openssl/bio.h>
#include <openssl/bn.h>
#include <openssl/hmac.h>

#endif

#include "common/Constants.h"
#include "common/Base64.h"

# ifdef __cplusplus
extern "C" {
# endif

char *aes_encrypt(const char *password, const char *message);
char *aes_decrypt(const char *password, const char *cipher);

#ifdef BUILD_FOR_LIBRARY
void _aes_encrypt(const char *password, const int password_len, const char *message, const int message_len, char **cipher, int *cipher_len);
void _aes_decrypt(const char *password, const int password_len, const char *cipher, const int cipher_len, char **message, int *message_len);
#endif

# ifdef  __cplusplus
}
# endif

#endif
