//
//  Curve.h
//  vexl-cryptography
//
//  Created by Adam Salih on 16.03.2022.
//

#ifndef CURVE_h
#define CURVE_h

#include <stdio.h>

# ifdef __cplusplus
extern "C" {
# endif

typedef enum {
    secp112r1,
    secp112r2,
    secp128r1,
    secp128r2,
    secp160r1,
    secp160k1,
    secp160r2,
    secp192k1,
    secp224k1,
    secp224r1,
    secp256k1,
    secp384r1,
    secp521r1,
    prime192v1,
    prime192v2,
    prime192v3,
    prime239v1,
    prime239v2,
    prime239v3,
    prime256v1,
    sect113r1,
    sect113r2,
    sect131r1,
    sect131r2,
    sect163k1,
    sect163r1,
    sect163r2,
    sect193r1,
    sect193r2,
    sect233k1,
    sect233r1,
    sect239k1,
    sect283k1,
    sect283r1,
    sect409k1,
    sect409r1,
    sect571k1,
    sect571r1,
    c2pnb163v1,
    c2pnb163v2,
    c2pnb163v3,
    c2pnb176v1,
    c2tnb191v1,
    c2tnb191v2,
    c2tnb191v3,
    c2pnb208w1,
    c2tnb239v1,
    c2tnb239v2,
    c2tnb239v3,
    c2pnb272w1,
    c2pnb304w1,
    c2tnb359v1,
    c2pnb368w1,
    c2tnb431r1,
    brainpoolP160r1,
    brainpoolP160t1,
    brainpoolP192r1,
    brainpoolP192t1,
    brainpoolP224r1,
    brainpoolP224t1,
    brainpoolP256r1,
    brainpoolP256t1,
    brainpoolP320r1,
    brainpoolP320t1,
    brainpoolP384r1,
    brainpoolP384t1,
    brainpoolP512r1,
    brainpoolP512t1
} Curve;

char *_get_group_name(Curve curve);

# ifdef  __cplusplus
}
# endif

#endif
