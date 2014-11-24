# Grab'n Run

*Grab’n Run* (aka **GNR**) is a **simple** and **effective** Java Library that you can easily add to your Android projects to *secure dynamic class loading* operations.

For a **concise presentation** on the library and some of its features you can give a look at these [slides](http://goo.gl/oiYAZB). On the other hand if you prefer a more **structured and complete description** with *set up information, tutorials, examples, tips&tricks and a full presentation of the API* you should definitely check the [documentation](https://readthedocs.com/something).

*Grab'n Run* is currently a work in progress so if you desire to suggest new features, improvements, criticisms or whatever I would be more than glad to hear **any kind of feedback**. You can contact me either by dropping an email at luca.falsina@mail.polimi.it or by pinging on Twitter [@lfalsina](https://twitter.com/lfalsina).

If you have also played a bit with the whole system it would be great if you decide to spend 5 minutes of your time by filling in this [evaluation form](), which once again could help to improve the current state of *Grab'n Run*.

## News

- *11/25/2014* - Grab'n Run is on line!

## Main features
Securely load code dynamically into your Android application from *APK* containers or *JAR* libraries translated to be executable by the Dalvik Virtual Machine (see [here]() ).
- *JAR* and *APK* containers can be either already stored on the device or **automatically fetched from remote locations** by GNR.
- Retrieved containers signatures are compared against a valid developer certificate. Only containers that are correctly signed are allowed to have their classes loaded dynamically. This ensures **integrity** and **developer** authentication on all the retrieved containers.
- Developer certificates are retrieved from remote locations securely and cached on the mobile phone for later on uses.
- Cached classes, containers and certificates used for the signature verification are stored into application-private folders. This **prevents** your application **from code injection attacks** at runtime.
- GNR implements an **effective caching system** that speeds up its execution and at the same time enables it to work in most cases also when no connectivity is available.
- Transition to GNR is *smooth* for the application developer since its API where thought to be as close as possible to the standard API provided by the Android framework.

## Quick Setup

This setup explains how to simply add *Grab'n Run* as a library for your Android applications.

#### 1. Include library
* [Download JAR](https://github.com/lukeFalsina/Grab-n-Run/raw/master/downloads/gnr-1.0.jar)
* Put the JAR in the **libs** subfolder of your Android project

#### 2. Android Manifest
Modify the *Android Manifest* of your application by adding a couple of required permissions:
``` xml
<manifest>
	<!-- Include following permission to be able to download remote resources like containers and certificates -->
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<!-- Include following permission to be able to download remote resources like containers and certificates -->
	<uses-permission android:name="android.permission.INTERNET" />
	<!-- Include following permission to be able to import local containers on SD card -->
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	...
</manifest>
```

## License

*Grab'n Run* is released under the Apache license. Check the COPYRIGHT file for further details.