//
// Created by Adam Salih on 04.04.2022.
//

#ifndef CONSTANTS_H
#define CONSTANTS_H

#define SALT (const unsigned char*)"vexlvexl"
#define SALT_LEN (int)8
#define PBKDF2ITER 2000
#define MAX_DATA_SIZE_LIMIT (int)15000000
#define MAX_CIPHER_SIZE_LIMIT ((double)MAX_DATA_SIZE_LIMIT)*(4.0/3.0)

#endif
