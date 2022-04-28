//
// Created by user2859193 and ryyst https://stackoverflow.com/questions/342409/how-do-i-base64-encode-decode-in-c
//

#ifndef BASE64_H
#define BASE64_H

#include <stdint.h>
#include <stdlib.h>


#ifdef BUILD_FOR_LIBRARY
void base64_build_decoding_table();
size_t base64_calculate_encoding_lenght(size_t input_length);
#endif

void base64_encode(const unsigned char *data, size_t input_length, size_t *output_length, char **output);
void base64_decode(const char *data, size_t input_length, size_t *output_length, unsigned char **output);


#endif
