//
//  ECIES.h
//  vexl-cryptography
//
//  Created by Adam Salih on 23.02.2022.
//

#ifndef ECIES_h
#define ECIES_h

#include <stdio.h>
#include <string.h>

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

#include "common/Log.h"
#include "common/Constants.h"
#include "common/OpenSSLHelper.h"
#include "model/Curve.h"
#include "model/KeyPair.h"
#include "model/Cipher.h"
#include "AES.h"
#include "HMAC.h"

# ifdef __cplusplus
extern "C" {
# endif

void ecies_init();

char *ecies_encrypt(const char *base64_public_key, const char *message);
char *ecies_decrypt(const char *base64_public_key, const char *base64_private_key, const char *encoded_cipher);

#ifdef BUILD_FOR_LIBRARY
void _ecies_encrypt(const char *base64_public_key, const char *message, const int message_len, char **encoded_cipher);
void _ecies_decrypt(const char *base64_public_key, const char *base64_private_key, const char *encoded_cipher, char **message, int *message_len);
#endif

# ifdef  __cplusplus
}
# endif

#endif
