//
// Created by Adam Salih on 29.03.2022.
//

#ifndef ECDSA_H
#define ECDSA_H

#include <stdio.h>
#include <stdbool.h>

#ifdef BUILD_FOR_LIBRARY

#include <openssl/ec.h>
#include <openssl/ecdsa.h>
#include <openssl/obj_mac.h>

#endif

#include "model/KeyPair.h"
#include "SHA.h"

# ifdef __cplusplus
extern "C" {
# endif

char *ecdsa_sign(const KeyPair keys, const void *data, const int data_len);

bool ecdsa_verify(const KeyPair pubkey, const void *data, const int data_len, char *signature);

# ifdef  __cplusplus
}
# endif

#endif