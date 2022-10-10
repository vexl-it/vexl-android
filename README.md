# TODO - Update all according to your needs, don´t keep this unchanged

# Project name

[![minSdk 23](https://img.shields.io/badge/minSdk-23-brightgreen.svg)]()
[![targetsdk 31](https://img.shields.io/badge/targetSdk-31-brightgreen.svg)]()
[![Kotlin 1.6.20](https://img.shields.io/badge/Kotlin-1.5.31-brightgreen.svg)]()

[![Figma](https://img.shields.io/badge/Figma-%23F24E1E.svg?logo=figma&logoColor=white&label=design)]()
[![Play Store](https://img.shields.io/badge/Google_Play-414141?logo=google-play&label=store&logoColor=white)](https://play.google.com/store/apps/details?id=it.vexl)
[![Jira](https://img.shields.io/badge/jira-%230A0FFF.svg?logo=jira&logoColor=white&label=tasks)]()


`https://vexl.it`

## Technologies

* App is completely written in ***Kotlin***
* App is completely reactive (we use ***coroutines*** and ***flow***)
* For API communication, this app uses ***REST API***, specifically Retrofit and OkHttp3 SDK libraries for Android
* App uses ***MVVM*** architecture with multiple modules
* App uses ***App Distribution*** from Firebase to distribute app to testers
* App respects ***Material design***, don´t use anything from old Appcompat libraries unless you absolutely have to

## App and project settings

* App supports only ***portrait mode*** (no landscape or tablets)

## Used libraries

* **Retrofit** - network library
* **Coil** - Coroutine image loader
* **Koin** - dependency injection
* **Moshi** - parsing json
* **Navigation Components**
* **Room** - database
* **Timber** - logging

## Build variants

Use the Android Studio *Build Variants* button to choose from flavors:

* **development** - development server
* **staging** - staging server
* **production** - production server combined with debug and release build types.

## Detekt + KTLint

It is necessary to add newly created module paths to build.gradle for detekt input files.

- Configuration file is 'detekt.yml'
