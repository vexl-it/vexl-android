# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("cryptography")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(cryptography_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../cryptography)

add_library(libcrypto STATIC IMPORTED)
set_target_properties(libcrypto PROPERTIES IMPORTED_LOCATION
        ${cryptography_DIR}/${ANDROID_ABI}/libcrypto.a)

add_library(libssl STATIC IMPORTED)
set_target_properties(libssl PROPERTIES IMPORTED_LOCATION
        ${cryptography_DIR}/${ANDROID_ABI}/libssl.a)

add_library(libvc STATIC IMPORTED)
set_target_properties(libvc PROPERTIES IMPORTED_LOCATION
        ${cryptography_DIR}/${ANDROID_ABI}/libvc.a)

# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++14")

# include all .cpp files in our bridge
file(GLOB SOURCES "*.cpp")
add_library(libcryptography SHARED ${SOURCES})

target_include_directories(libcryptography PRIVATE
        ${cryptography_DIR}/include/vc)

target_link_libraries(libcryptography
        android
        libcrypto
        libssl
        libvc)