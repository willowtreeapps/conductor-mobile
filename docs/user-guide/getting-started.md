---
title: User Guide
layout: doc
edit_link: https://github.com/willowtreeapps/conductor-mobile/edit/master/docs/user-guide/getting-started.md
sidebar: "user-guide"
grouping: "getting-started-user"
navigation: true
order: 1
---

# Getting Started with Conductor Mobile

Conductor Mobile is a testing framework built on top of Appium that takes care of a lot of boilerplate and heavylifting of native UI testing.

* Conductor Mobile supports [TestNG](http://testng.org/doc/) and [JUnit](http://junit.org/junit5/).
* Conductor Mobile provides Implicit Waiting by default on most of it's features to provide a smoother syntax and avoid most `ElementNotFound` exceptions.

## Installation and Usage
Adding Conductor Mobile to your project is easy, just add the [jitpack.io](https://jitpack.io) to your list of repositories, include the `conductor-mobile` dependency, and extend `Locomotive` from your test classes.

### Add jitpack.io to your repositories

```xml
<repositories>
    
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>

</repositories>
```

### Add the Conductor Mobile dependency

```xml
<dependencies>

    <dependency>
        <groupId>com.github.willowtreeapps</groupId>
        <artifactId>conductor-mobile</artifactId>
        <version>v1.0</version>
    </dependency>
    
</dependencies>
```

### Test Classes
Just extend `Locomotive` on your test classes and Conductor Mobile will take care of launching and managing the Appium server.

```java
public class HomeTest extends Locomotive {
    ...
}
```