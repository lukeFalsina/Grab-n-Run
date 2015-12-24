# ![Logo](https://github.com/lukeFalsina/Grab-n-Run/raw/master/gnr/app/src/main/res/drawable-mdpi/logo_with_name.png)

## Research paper

We present the findings of this work in a research paper:

**Grab'n Run: Secure and Practical Dynamic Code Loading for Android Applications**  
Luca Falsina, Yanick Fratantonio, Stefano Zanero, Christopher Kruegel, Giovanni Vigna, Federico Maggi.  
*In Proceedings of the Annual Computer Security Applications Conference (ACSAC). Los Angeles, CA December, 2015*
[[PDF](http://cs.ucsb.edu/~yanick/publications/2015_acsac_grabandrun.pdf)]
[[Bibtex](https://github.com/lukeFalsina/Grab-n-Run/raw/master/grabandrun.bib)]

If you use *Grab'n Run* in a scientific publication, we would appreciate citations to the previous paper.  
Please use this **Bibtex** entry:
``` tex
@InProceedings{falsina15:grabandrun,
  author = {Luca Falsina and Yanick Fratantonio and Stefano Zanero and Christopher Kruegel and Giovanni Vigna and Federico Maggi},
  title = {{Grab'n Run: Secure and Practical Dynamic Code Loading for Android Applications}},
  booktitle = {Proceedings of the Annual Computer Security Applications Conference (ACSAC)},
  month = {December},
  year = {2015},
  address = {Los Angeles, CA}
}
``` 

## News

- *10/10/2015* - The **repackaging tool** is now **[online](http://grab-n-run.readthedocs.org/en/latest/repackaging.html)**. Use it to patch automatically your applications to use Grab'n Run APIs. 
- *01/17/2015* - **Grab'n Run** is now **available** on [JCenter](https://bintray.com/bintray/jcenter?filterByPkgName=grab-n-run)
- *01/16/2015* - **Grab'n Run** project migrates to [Android Studio](http://developer.android.com/tools/studio/index.html), the official *IDE* for **Android application development**. However, you can still use the library also with your *ADT* projects! (*see below the "Quick Setup" section for further details*)
- *11/26/2014* - **Grab'n Run is on line!**

## Introduction

*Grab’n Run* (aka **GNR**) is a **simple** and **effective** Java Library that you can easily add to your Android projects to perform *secure dynamic class loading* operations over standard [DexClassLoader](http://developer.android.com/reference/dalvik/system/DexClassLoader.html).

Previous research has shown that many applications often need to perform dynamic class loading to implement, for example, non-invasive self-update features. However, research has also shown that it is really challenging to *safely* implement these features. This is of particular importance as, in this context, **one single mistake** could open the application (and, therefore, the entire device) to **serious security vulnerabilities**, such as *remote code execution*.  

The main goal of *Grab's Run* is to offer an alternative to the native Android APIs, and its design enforces that even the most inexperienced developer cannot perform well-known, serious mistakes.

For a **brief presentation** of the library and some of its features you can give a look at these [slides](https://goo.gl/QrwWey), while if you prefer a more **structured and complete description** with *set up information, tutorials, examples, tips&tricks, and a full presentation of the API* you should definitely check the [documentation](http://grab-n-run.readthedocs.org/en/latest/).

Note that *Grab'n Run* is currently a work in progress: if you desire to suggest new *features, improvements, criticisms* or whatever, I would be more than glad to hear **any kind of constructive feedback** :D  
You can contact me either by dropping an email at [lfalsina@gmail.com](mailto:lfalsina@gmail.com) or by pinging me on Twitter [@lfalsina](https://twitter.com/lfalsina).

## Main features
Securely load code dynamically into your Android application from **APK** containers or **JAR** libraries translated to be *executable by both the Dalvik Virtual Machine (DVM) and the Android Runtime (ART)* (don't worry a [section](http://grab-n-run.readthedocs.org/en/latest/complementary.html#on-library-developer-side-how-to-prepare-a-valid-library-container-compatible-with-gnr) of the docs explains step-by-step how to do it).

- *JAR* and *APK* containers can be either already stored on the device or **automatically fetched from remote locations** by GNR.
- Retrieved containers signatures are compared against a **valid developer certificate**. Only containers that are **correctly signed** are allowed to have their classes loaded dynamically. This ensures **integrity** and **developer authentication** on all the retrieved containers.
- Developer certificates are retrieved from remote locations securely and cached on the mobile phone for future verifications.
- *Cached classes, containers and certificates* used for the signature verification are stored into *application-private* folders. This **prevents** your application **from code injection attacks** at runtime.
- GNR implements an **effective caching system** that speeds up its execution and at the same time enables it to *work in most cases also when no connectivity is available*.
- Transition to GNR is **smooth** for the application developer since its **API** where thought to be *as close as possible to the standard API* provided by the Android framework.
- When *many containers* are provided as sources for class loading, *Grab'n Run* performs a **concurrent multi-thread signature verification** in order to *limit the performance overhead*.
- GNR helps the application developer to **implement silent updating** on *remote third-party libraries in a secure and concise way*. 

## Quick Setup

This section explains how to setup *Grab'n Run* as a library for your Android applications.

#### 1. Include library

###### a. Android Studio (AS)

* Modify the *build.gradle* file in the *app* module of your Android project by adding the following *compile* line in the *dependencies* body:
``` gradle
dependencies {
    // Grab'n Run will be imported from JCenter.
    // Verify that the string "jcenter()" is included in your repositories block!
    compile 'it.necst.grabnrun:grabnrun:1.0.2'
}
``` 
* Resync your project to apply changes.

###### b. Android Development Tool (ADT)

* [Download JAR](https://github.com/lukeFalsina/Grab-n-Run/raw/master/downloads/gnr-1.0.2.jar)
* Put the JAR in the **libs** subfolder of your Android project

#### 2. Android Manifest

Modify the *Android Manifest* of your application by adding a couple of **required permissions**:
``` xml
<manifest>
	<!-- 	Include following permission to be able to download remote resources 
			like containers and certificates -->
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<!-- 	Include following permission to be able to download remote resources 
			like containers and certificates -->
	<uses-permission android:name="android.permission.INTERNET" />
	<!-- 	Include following permission to be able to import local containers 
			on SD card -->
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	...
</manifest>
```

## Quick example of use

This quick use case gives you a taste on how to use GNR once you have added it to your project.

#### 1. Create a key pair to sign your code and export your developer certificate

* Open a terminal and type the following command to **generate a keystore** and a **keypair**:
``` bash
$ 	keytool -genkey -v -keystore my-tests-key.keystore -alias test_dev_key 
	-keyalg RSA -keysize 2048 -validity 10000
```
* Next **export** the public key **into a certificate** that will be *used to verify your library code* before dynamically loading it:
``` bash
$	keytool -exportcert -keystore my-tests-key.keystore -alias test_dev_key 
	-file certificate.pem
```
* You should now see in the folder a **certificate file** called *certificate.pem*

#### 2. Publish your developer certificate on line at a remote location which uses HTTPS protocol

You can publish the certificate wherever you like as long as **HTTPS** protocol is used and **everyone can access this location** from the web.
As a **test** example you could store the *certificate.pem* in your "Public" *Dropbox* folder and then retrieve the **associated public link**, which could be for example something like "https://dl.dropboxusercontent.com/u/00000000/certificate.pem". Note this URL down, you will need it soon.

#### 3. Export an unsigned container and sign it with your developer key

Let's say that in your IDE (i.e., the *Android Development Tools (ADT)*) you have an Android project called **"LoaderApp"** from which you want to load some of its classes dynamically in another project.

* In the *ADT Package Explorer* **right** click on **"LoaderApp"** -> Android Tools -> Export Unsigned Application Package...
![Screenshot](https://github.com/lukeFalsina/Grab-n-Run/raw/master/docs/images/ExportUnsignedContainer.png)
* Next select the **same folder** where you have previously saved the keystore and the keypair as the *destination folder* and press OK.
* Open a terminal which points to the destination folder and **sign the apk container** with the previously created key:
``` bash
$	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 
	-keystore my-tests-key.keystore LoaderApp.apk test_dev_key
```
* Finally **align** the apk container to optimize access time to its resources:
``` bash
$	<path_to_your_sdk>/sdk/build-tools/<sdk_version_number>/zipalign -v 4 
	LoaderApp.apk LoaderAppAligned.apk
```

**P.S.** *Step 3* can also be directly performed by means of your favorite *IDE*. In **ADT** you would have to select the option *"Android Tools -> Export Signed Application Package..."* and, when it is required, navigate to the location of your keystore and inserting its password, the key id and the key password. On the other hand in **Android Studio** the signature process can be automatized by setting up a proper **signing configuration** as described [here](http://developer.android.com/tools/publishing/app-signing.html#release-mode).

#### 4. Publish the signed and aligned version of the source container

Once you have obtained *LoaderAppAligned.apk*, you need to make also this resource **available on line**. Notice that, in this case, both remote locations that use **HTTP** or **HTTPS** protocols are fine as long as they are accessible from the web. Again, as an example, you can store the container in your "Public" *Dropbox* folder and get back a **public URL** like "https://dl.dropboxusercontent.com/u/00000000/LoaderAppAligned.apk".

#### 5. Set up dynamic code loading with GNR in the application

In the end, it is time to set up a *SecureDexClassLoader* instance to **fetch your remote container and developer certificate**, **store it in a safe place** and **perform a signature verification** before dynamically loading your code.

**Copy and paste** the code below in one of the Activity in your target Android project, where you have *already imported GNR*, to **dynamically and securely load** an instance of the class *"com.example.MyClass"*:
``` java

MyClass myClassInstance = null;
jarContainerPath = "https://dl.dropboxusercontent.com/u/00000000/LoaderAppAligned.apk";

try {
	Map<String, URL> packageNamesToCertMap = new HashMap<String, URL>();
	packageNamesToCertMap.put("com.example", new URL("https://dl.dropboxusercontent.com/u/00000000/certificate.pem"));

	SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);
	SecureDexClassLoader mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
												null, 
												getClass().getClassLoader(),
												packageNamesToCertMap);
		
	Class<?> loadedClass = mSecureDexClassLoader.loadClass("com.example.MyClass");

	// Check whether the signature verification process succeeded
	if (loadedClass != null) {

		// No security constraints were violated and so
		// class loading was successful.
		myClassInstance = (MyClass) loadedClass.newInstance();
				
		// Do something with the loaded object myClassInstance
		// i.e. myClassInstance.doSomething();
	}

} catch (ClassNotFoundException e) {
	// This exception will be raised when the container of the target class
	// is genuine but this class file is missing..
	e.printStackTrace();
} catch (InstantiationException e) {
	e.printStackTrace();
} catch (IllegalAccessException e) {
	e.printStackTrace();
} catch (MalformedURLException e) {
	// The previous URL used for the packageNamesToCertMap entry was a malformed one.
	Log.e("Error", "A malformed URL was provided for a remote certificate location");
}
```

*Et voilá..* now you have an instance of *"MyClass"* loaded in a **secure way** at **run time**!

## Next steps :)

* If you want to learn how to use *Grab'n Run* I suggest to start from the [tutorial](http://grab-n-run.readthedocs.org/en/latest/tutorial.html) and then moving on by analyzing the [example application](http://grab-n-run.readthedocs.org/en/latest/example.html).
* If you are interested in understanding what are the **security threats** of *improper dynamic code loading* fixed by GNR check out the [security resume](http://grab-n-run.readthedocs.org/en/latest/security.html).
* If you would like to implement cool features of GNR like **silent updates**, **handling more containers**, **concurrent code loading** or **dynamically loading JAR libraries in your applications** you should give a look at the [complementary topics](http://grab-n-run.readthedocs.org/en/latest/complementary.html).
* You may also need to **consult** the *JavaDoc-like* [API documentation](https://rawgit.com/lukeFalsina/Grab-n-Run/master/docs/javaDoc/index.html).
* Finally, you may want to convert automatically your applications to use Grab'n Run APIs for secure dynamic code loading. Give a try at the [repackaging tool](http://grab-n-run.readthedocs.org/en/latest/repackaging.html).

## License

*Grab'n Run* is released under the *Apache* license. Check the *COPYRIGHT* file for further details.

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Grab--n--Run-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1185)
