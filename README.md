[![Build Status](https://travis-ci.org/willowtreeapps/conductor-mobile.svg?branch=master)](https://travis-ci.org/jossjacobo/conductor-mobile)
[![GitHub release](https://img.shields.io/github/release/willowtreeapps/conductor-mobile.svg)](https://github.com/willowtreeapps/conductor-mobile)

Conductor Mobile
================

Conductor Mobile is a port of the [Conductor](https://github.com/conductor-framework/conductor) Web Framework for iOS and Android, instead of wrapping around [Selenium](http://www.seleniumhq.org/) it wraps the [Appium Framework](http://appium.io/). Thanks to [@ddavison]

# Getting Started
Using maven, add jitpack.io to your repositories and include it as a dependency:
```xml
<repositories>
    
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    
</repositories>
```
```xml
<dependencies>

    <dependency>
        <groupId>com.github.willowtreeapps</groupId>
        <artifactId>conductor-mobile</artifactId>
        <version>v1.0</version>
    </dependency>
    
</dependencies>
```
Create a Java Class, and extend it from `com.joss.conductor.mobile.Locomotive`

# Goals
Same as the original conductor, the primary goals of this project are to...
- Take advantage of method chaining, to create a fluent interface.
- Abstract the programmer from bloated scripts resulting from using too many css selectors, and too much code.
- Provide a quick and easy framework in Selenium 2 using Java, to get started writing scripts.
- Provide a free to use framework for any starting enterprise, or individual programmer.
- Automatic detection of connected iOS devices

# Default Properties
- `deviceName` = {name of the device}
- `appPackageName` = {android app package name}
- `apk` = {path to apk}
- `ipa` = {path to ipa}
- `platformName` = {string: android or ios}
- `platformVersion` = {string: appium platform version}
- `udid` = {string: iOS device's UDID or Android's device name from ADB}
- `language` = {string: }
- `locale` = {string: }
- `orientation` = {string: portrait or landscape}
- `autoWebview` = {boolean: true or false}
- `noReset` = {boolean: true or false}
- `fullReset` = {boolean: true or false}
- `hub` = {string: url}
- `timeout` = {int: default equals 5 seconds per call}
- `retries` = {int: default equals 5 retries}
- `screenshotsOnFail` = {boolean: true or false}
- `autoGrantPermissions` = {boolean: true or false}
- `automationName` = {string: i.e. uiautomator2 or xcuitest}
- `intentCategory` = {string: i.e. android.intent.category.LEANBACK_LAUNCHER}

# Inline Actions
- ```click(By)```
- ```setText(By, text)```
- ```getText(By)```
- ```isPresent(By)```
- ```getAttribute(By, attribute)```
- ```swipe(SwipeElementDirection, By)```
- etc.

# Inline validations
This is one of the most important features that I want to _*accentuate*_.
- ```validateText```
- ```validateTextNot```
- ```validatePresent```
- ```validateNotPresent```
- ```validateTextPresent```
- ```validateTextNotPresent```

All of these methods are able to be called in-line, and fluently without ever having to break your tests.

# Implicit Waiting
The ```AutomationTest``` class extends on this concept by implenting a sort of ```waitFor``` functionality which ensures that an object appears before interacting with it.  This rids of most ```ElementNotFound``` exceptions that Appium will cough up.

# Pull requests
If you have an idea for the framework, fork it and submit a pull-request!

# Release process
 We follow gitflow branch management [reference graphic](http://nvie.com/posts/a-successful-git-branching-model/). The
 steps to make a new release are:
 1. Create a release branch from the develop branch named `release/x.x.x`.
 2. Create a new pull request from the release branch to the master branch.
 3. If approved merge release branch into master.
 4. Tag the merge (with release notes) in the master branch with `x.x.x` (this will make this version available in jitpack).
 5. Create a new pull request from master to develop so all changes are back in develop.
 6. If approved merge master branch into develop.

# Use with Sauce Labs
 1. get an API token for your sauce labs account
 2. upload the .app file as a zip to temporary [sauce storage](https://wiki.saucelabs.com/display/DOCS/Uploading+Mobile+Applications+to+Sauce+Storage+for+Testing)
 3. set the hub property to connect to saucelabs `https://<login-name>:<API-token>@ondemand.saucelabs.com:443/wd/hub`
 4. set the ipa/apk property to `sauce-storage:<zip-filename>.zip`
 5. run the test

License
-------

    Copyright 2016 Jossay Jacobo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
