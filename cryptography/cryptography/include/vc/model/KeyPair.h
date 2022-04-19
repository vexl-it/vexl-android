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
EC_KEY *_KeyPair_get_EC_KEY(const KeyPair keys);

#endif

KeyPair generate_key_pair(Curve curve);

void KeyPair_free(KeyPair KeyPair);

# ifdef  __cplusplus
}
# endif

#endif
