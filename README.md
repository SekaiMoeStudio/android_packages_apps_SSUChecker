SakitinSU Checker
===

An Android application that provides package installation control without modifying the framework fot LineageOS.

## Overview
SakitinSU Checker(named "Checker") is a system application that allows you to intercept and control package installations on LineageOS. It runs as a privileged system app and provides a flexible way to manage package installations without requiring framework modifications or root access.

## Features

- Block specific package installations
- Persistent blocking rules across reboots
- System-level integration
- No framework modifications required
- Configuration through Settings or ADB
- Detailed logging system
- Support for all LineageOS versions

## Installation

### Building from Source

1. Clone this repository into your LineageOS source tree:
```bash
cd packages/apps/
git clone https://github.com/SekaiMoeStudio/android_packages_apps_SSUChecker
```

2. Add the package to your device's makefile (`device/<manufacturer>/<device>/device.mk`):
```makefile
PRODUCT_PACKAGES += \
    SSUChecker
```

3. Build LineageOS as usual:
```bash
source build/envsetup.sh
lunch lineage_<device>-userdebug
mka SSUChecker
```

### Pre-built Installation

1. Download the latest release from the releases page
2. Push to system partition:
```bash
adb root
adb remount
adb push SSUChecker.apk /system_ext/priv-app/SSUChecker/
adb push privapp-permissions-ssuchecker.xml /system_ext/etc/permissions/
adb reboot
```

## Usage

### Through Settings UI

1. Navigate to Settings -> System -> SSUChecker
2. Add packages to the blocklist
3. Enable/disable blocking functionality

### Trough device.mk

Some usage:
```makefile
# Pre-blocked packages
PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    ro.pmhook.blocked_packages=com.example.app1:com.example.app2:com.example.app3
```

### Through ADB

Block a package:
```bash
adb shell cmd ssuchecker block com.example.package
```

Unblock a package:
```bash
adb shell cmd ssuchecker unblock com.example.package
```

List blocked packages:
```bash
adb shell cmd ssuchecker list
```

### Log Viewing

View logs:
```bash
adb logcat -s SSUCheckerService:* PMController:*
```

## Permissions

The application requires the following permissions:
- `android.permission.INSTALL_PACKAGES`
- `android.permission.DELETE_PACKAGES`
- `android.permission.INTERACT_ACROSS_USERS`
- `android.permission.MANAGE_USERS`
- `android.permission.OBSERVE_GRANT_REVOKE_PERMISSIONS`

For details, see [privapp-permissions-ssuchecker.xml](privapp-permissions-ssuchecker.xml)

## Project Structure

```
android_packages_apps_ssuchecker/
├── Android.bp                 # Build configuration
├── AndroidManifest.xml        # App manifest
├── src/                       # Source code
│   └── org/lineageos/ssuchecker/
│       ├── PMHookService.java
│       ├── PMController.java
│       └── activities/
├── res/                       # Resources
└── privapp-permissions-ssuchecker.xml
```

## Development

### Building Locally

```bash
m SSUChecker
```

### Testing

```bash
adb install -r $OUT/system_ext/priv-app/SSUChecker/SSUChecker.apk
```

### Debugging

```bash
adb logcat -s SSUChecker:*
```

## Note

This application is intended for system-level integration only. It requires system privileges and must be built as part of the ROM or installed on a rooted device with system partition write access.
