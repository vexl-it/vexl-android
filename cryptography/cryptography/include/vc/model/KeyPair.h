//
//  KeyPair.h
//  vexl-cryptography
//
//  Created by Adam Salih on 16.03.2022.
//

#ifndef KEYPAIR_h
#define KEYPAIR_h

#include <stdio.h>

#ifdef BUILD_FOR_LIBRARY

#include <openssl/err.h>
#include <openssl/pem.h>
#include <openssl/bio.h>

#endif

#include "Curve.h"
#include "../common/Log.h"
#include "../common/OpenSSLHelper.h"
#include "../common/Base64.h"

# ifdef __cplusplus
extern "C" {
# endif

typedef struct {
    char *pemPrivateKey;
    char *pemPublicKey;
    Curve curve;
} KeyPair;

#ifdef BUILD_FOR_LIBRARY

KeyPair _EVP_PKEY_get_KeyPair(const EVP_PKEY *pkey);
void _base64_keys_get_EC_KEY(const char *base64_public_key, const char *base64_private_key, EC_KEY **eckey);

#endif

// This function generates only public key. This function is implemented by request from backend for testing purposes.
char *generate_key_public_key(const int curve);

KeyPair generate_key_pair(Curve curve);
void KeyPair_free(KeyPair KeyPair);

# ifdef  __cplusplus
}
# endif

#endif
