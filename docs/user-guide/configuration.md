---
title: Configuration
layout: doc
edit_link: https://github.com/willowtreeapps/conductor-mobile/edit/master/docs/user-guide/configuration.md
sidebar: "user-guide"
grouping: "configuration"
navigation: false
order: 2
---

# Configuration

Conductor Mobile can be configured using a [yaml](https://en.wikipedia.org/wiki/YAML) file. By default, Conductor looks for a `config.yaml` at the root of embedded resources.

## Config.yaml

The config file has these main sections: 
* `platformName` (required)
* `currentSchemes` (optional)
* `defaults` (required)
* `schemes` (optional)

#### Defaults
The defaults section contains both general defaults and defaults per platform. Platforms can override defaults just be re-specifying them in the platform section.

```yaml
defaults:
  retries: 3
  timeout: 10
  screenshotOnFail: false
  autoGrantPermissions: true

  ios:
    platformVersion: 11.0
    deviceName: iPhone 8

  android:
    timeout: 15
    platformVersion: 8.0
```

#### Schemes
Sometimes it is useful to specify properties under specific circumstances, and this is what `schemes` are for. Schemes override properties in the configuarion _in the order_ they are specified in the currentSchemes section of the configuration file.

You might, for example, have a scheme for running on a specific device or specific remote testing tool. It's a pain to have to re-specify these so, Conductor Mobile makes this easy with schemes.

Example:
```yaml
currentSchemes:
  - ios_sauce_labs
  - shorter_timeouts

defaults:
  ...

ios_sauce_labs:
  hub: http://saucelabs-hub
  appFile: sauce-storage:app.zip
  automationName: XCUITest
  platformVersion: 11.1
  deviceName: iPhone 8

shorter_timeouts:
  timeout: 1
  retries: 2
```

## Properties

Conductor Mobile supports most of the commonly used properties required by Appium and native platforms, as well as give you the flexibility to add custom capabilities. For a full list of the capabilities refer to the [ConductorConfig](https://github.com/willowtreeapps/conductor-mobile/blob/develop/src/main/java/com/joss/conductor/mobile/ConductorConfig.java).

#### General
- `platformName` - ANDROID or IOS (note all caps)
- `deviceName` - name of the device
- `appFile` - path to apk, ipa, or app
- `platformVersion` - string: version iOS or Android
- `udid` - string: iOS device's UDID or Android's device name from ADB, or auto to use the first connected device
- `noReset` - boolean: true or false
- `fullReset` - boolean: true or false
- `timeout` - int: default equals 5 seconds per call
- `retries` - int: default equals 5 retries
- `screenshotsOnFail` - boolean: true or false
- `autoGrantPermissions` - boolean: true or false

### Android
- `appPackageName` - android app package name
- `automationName` - usually just set to `uiautomator2` on Android
- `avd` - the name of the avd to boot
- `appActivity` - the name of the activity that starts the app
- `appWaitActivity` - the name of the activity to wait for
- `intentCategory` - name of intent category (e.g `android.intent.category.LEANBACK_LAUNCHER`)

### iOS
- `appPackageName` - iOS bundle id
- `automationName` - usually just set to `xcuitest` on iOS
- `xcodeSigningId` - signing id to use to load the app on a device (e.g. "iPhone Developer")
- `xcodeOrgId` - org id to use to sign the app

Example:
```yaml
platformName: ANDROID
currentSchemes:
  - longer_timeouts

defaults:
  noReset: false
  appiumVersion: 1.7.1
  timeout: 8
  retries: 10
  android:
    retries: 4
    appFile: ./apps/android.apk
    appActivity: com.android.activity
  ios:
    retries: 4
    appFile: ./apps/ios.app
    xcodeSigningId: iPhone Developer
    xcodeOrgId: TEAMID

shorter_timeouts:
  retries: 8
  timeout: 5

longer_timeouts:
  retries: 5
  timeout: 20

android_device:
  udid: auto

ios_device:
  appFile: ./apps/ios.ipa
  udid: auto
```

## Environment Variables
TODO - The yaml config also supports environments...`${PLATFORM}`

Example
```yaml
platformName: IOS

defaults:
  noReset: false
  appiumVersion: 1.7.1
  timeout: 8
  retries: 10
  udid: ${FOO_PROPERTY}
  platformVersion: ${PLATFORM_MAJOR}.${PLATFORM_MINOR}
```

## Custom Capabilities
TODO - Support for `customCapabilities`...

Example
```yaml
platformName: ANDROID

defaults:
  noReset: false
  appiumVersion: 1.7.1
  timeout: 8
  retries: 10
  customCapabilities:
    foo: bar
    fizz: buzz
    truty: true

  android:
    deviceName: device
    retries: 4
    appFile: ./apps/android.apk
    appActivity: com.android.activity

  ios:
    retries: 4
    appFile: ./apps/ios.app
    xcodeSigningId: iPhone Developer
    xcodeOrgId: TEAMID
```