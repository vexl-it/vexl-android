//
//  Log.h
//  vexl-cryptography
//
//  Created by Adam Salih on 16.03.2022.
//

#ifndef LOG_h
#define LOG_h

#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>

void _log(char *message, ...);
void _error(int code, char *message, ...);

#endif
