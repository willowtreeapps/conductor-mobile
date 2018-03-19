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

# Configuration
Conductor can be configured using a [yaml](https://en.wikipedia.org/wiki/YAML) file. By default, Conductor looks for a "config.yaml" at the root of embedded resources.

The file has 3 sections: `current configuration`, `defaults`, and `schemes`.

## General Configuration
The general configuration only has two properties: `platformName` (which must be either IOS or ANDROID) and optionally `currentSchemes`, which will be discussed later

## Defaults
The defaults section contains both general defaults and defaults per platform. It looks like this:
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
Platforms can override defaults just be re-specifying them in the platform section

## Schemes

Sometimes it is useful to specify properties under specific circumstances, and this is what "schemes" are for.  Schemes override properties in the configuarion *in the order they are specified in the `currentSchemes` section of the configuration file.*

You might, for example, have a scheme for running on a specific device or specific remote testing tool. It's a pain to have to re-specify these so, Conductor makes this easy with schemes. Some example schemes:

```yaml
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
You can see a variety of example configuration files in the unit tests for conductor [here](src/test/resources/test_yaml)

# Supported Properties
## General (Common)
- `platformName` = {string: ANDROID or IOS} (note all capps)
- `deviceName` = {name of the device}
- `appPackageName` = {android app package name or iOS bundle id}
- `appFile` = {path to apk, ipa, or app}
- `platformVersion` = {string: version iOS or Android}
- `udid` = {string: iOS device's UDID or Android's device name from ADB, or auto to use the first connected device}
- `noReset` = {boolean: true or false}
- `fullReset` = {boolean: true or false}
- `timeout` = {int: default equals 5 seconds per call}
- `retries` = {int: default equals 5 retries}
- `screenshotsOnFail` = {boolean: true or false}
- `autoGrantPermissions` = {boolean: true or false}
- `automationName` = {string: i.e. uiautomator2 or xcuitest}


## General (less common, usually not required)
- `language` = {string: }
- `locale` = {string: }
- `orientation` = {string: portrait or landscape}
- `hub` = {string: url}

## Android specific
- `avd` = {string: the name of the avd to boot}
- `appActivity` = {string: the name of the activity that starts the app}
- `appWaitActivity` = {string: the name of the activity to wait for}
- `intentCategory` = {string: i.e. android.intent.category.LEANBACK_LAUNCHER}

## iOS specific
- `xcodeSigningId` = {string: the signing id to use to load the app on a device, usually "iPhone Developer"}
- `xcodeOrgId` = {string: the org id to use to sign the app}


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

# Platform Identifier Annotation
Support for grouping your platform (android, ios) IDs into one place via annotations:
```java 
@AndroidId("google")
@IOSId("apple")
public By Item;
```

Default locator is `By.id` but there is also support for `By.xpath`: 
```java 
@AndroidId(xpath = "//*[@text='Knock Knock']")
@IOSId(xpath = "//*[@text='Who's there?']")
public By XpathItem;
```
String types are not supported, must be type `By`.

Initialize once in the BasePage constructor i.e.:
```java
public BasePage(Locomotive driver) {
    this.driver = driver;
    PlatformFindByHelper.initIds(this, driver.configuration.getPlatformName());
}   
```

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
*Note: it is recommended you create a scheme for sauce labs testing when possible*

 1. get an API token for your sauce labs account
 2. upload the .app file as a zip to temporary [sauce storage](https://wiki.saucelabs.com/display/DOCS/Uploading+Mobile+Applications+to+Sauce+Storage+for+Testing)
 3. set the hub property to connect to saucelabs `https://<login-name>:<API-token>@ondemand.saucelabs.com:443/wd/hub`
 4. set the appFile property to `sauce-storage:<zip-filename>.zip`
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
