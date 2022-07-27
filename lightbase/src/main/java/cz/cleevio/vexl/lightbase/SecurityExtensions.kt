package cz.cleevio.vexl.lightbase

import android.content.Context
import android.content.pm.ApplicationInfo

fun Context.isDebuggable(): Boolean =
	applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
