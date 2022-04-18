//
//  OpenSSLHelper.h
//  vexl-cryptography
//
//  Created by Adam Salih on 23.02.2022.
//

#ifndef OPENSSLHELPER_h
#define OPENSSLHELPER_h

#ifdef BUILD_FOR_LIBRARY

#include <openssl/bio.h>

char *_BIO_read_chars(BIO *bio);

#endif

#endif