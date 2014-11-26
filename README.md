# ![Logo](https://github.com/lukeFalsina/Grab-n-Run/raw/master/gnr/logo.png) Grab'n Run

*Grab’n Run* (aka **GNR**) is a **simple** and **effective** Java Library that you can easily add to your Android projects to *secure dynamic class loading* operations over standard [DexClassLoader](http://developer.android.com/reference/dalvik/system/DexClassLoader.html).

For a **brief presentation** of the library and some of its features you can give a look at these [slides](http://goo.gl/oiYAZB). On the other hand if you prefer a more **structured and complete description** with *set up information, tutorials, examples, tips&tricks and a full presentation of the API* you should definitely check the [documentation](https://readthedocs.com/something).

*Grab'n Run* is currently a work in progress so, if you desire to suggest new *features, improvements, criticisms* or whatever, I would be more than glad to hear **any kind of constructive feedback** :D 

You can contact me either by dropping an email at luca.falsina@mail.polimi.it or by pinging on Twitter [@lfalsina](https://twitter.com/lfalsina).

Moreover if you have just spent a bit of time playing with *Grab'n Run*, why don't you try to **fill in** this [evaluation form](http://goo.gl/forms/k500h7cYiv)? This will help once again to **make GNR a better tool** to use for other developers like you :)

## News

- *11/26/2014* - **Grab'n Run is on line!**

## Main features
Securely load code dynamically into your Android application from **APK** containers or **JAR** libraries translated to be *executable by the Dalvik Virtual Machine* (don't worry a [section]() of the docs explains how to do it).

- *JAR* and *APK* containers can be either already stored on the device or **automatically fetched from remote locations** by GNR.
- Retrieved containers signatures are compared against a **valid developer certificate**. Only containers that are **correctly signed** are allowed to have their classes loaded dynamically. This ensures **integrity** and **developer authentication** on all the retrieved containers.
- Developer certificates are retrieved from remote locations securely and cached on the mobile phone for future verifications.
- *Cached classes, containers and certificates* used for the signature verification are stored into *application-private* folders. This **prevents** your application **from code injection attacks** at runtime.
- GNR implements an **effective caching system** that speeds up its execution and at the same time enables it to *work in most cases also when no connectivity is available*.
- Transition to GNR is **smooth** for the application developer since its **API** where thought to be *as close as possible to the standard API* provided by the Android framework.
- When *many containers* are provided as sources for class loading, Grab'n Run performs a **concurrent multi-thread signature verification** in order to *limit the performance overhead*.
- GNR helps the application developer to **implement silent updating** on *remote third-party libraries in a secure and concise way*. 

## Quick Setup

This setup explains how to simply add *Grab'n Run* as a library for your Android applications.

#### 1. Include library

* [Download JAR](https://github.com/lukeFalsina/Grab-n-Run/raw/master/downloads/gnr-1.0.jar)
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

This quick use case gives you a taste on how to use GNR once that you have added it to your project.

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

You can publish the certificate in many places as long as **HTTPS** protocol is used and **everyone can access this location** from the web.
As a **test** example you could store the *certificate.pem* in your "Public" *Dropbox* folder and then retrieve the **associated public link**, which could be for example something like "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem". You will need this URL soon.

#### 3. Export an unsigned container and sign it with your developer key

Let's say that in your IDE (i.e. the *Android Development Tool (ADT)*) you have an Android project called **"LoaderApp"** from which you want to load some of its classes dynamically in another project.

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
#### 4. Publish the signed and aligned version of the source container

Once that you have obtained *LoaderAppAligned.apk* you need to make also this resource **available on line**. Notice that in this case both remote locations that uses **HTTP** or **HTTPS** protocols are fine as long as they are accessible from the web. As an example again you can store the container in your "Public" *Dropbox* folder and get back a **public URL** like "https://dl.dropboxusercontent.com/u/28681922/LoaderAppAligned.apk".

#### 5. Set up dynamic code loading with GNR in the application

In the end it is time to set up a *SecureDexClassLoader* instance to **fetch your remote container and developer certificate**, **store it in a safe place** and **perform a signature verification** before dynamically loading your code.

**Copy and paste** the code below in one of the Activity in your target Android project, where you have *already imported GNR*, to **dynamically and securely load** an instance of the class *"com.example.MyClass"*:
``` java

MyClass myClassInstance = null;
jarContainerPath = "https://dl.dropboxusercontent.com/u/28681922/LoaderAppAligned.apk";

try {
	Map<String, URL> packageNamesToCertMap = new HashMap<String, URL>();
	packageNamesToCertMap.put("com.example", new URL("https://dl.dropboxusercontent.com/u/28681922/test_cert.pem"));

	SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);
	SecureDexClassLoader mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
												null, 
												packageNamesToCertMap, 
												getClass().getClassLoader());
		
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

* If you want to learn how to use *Grab'n Run* I suggest to start from the [tutorial]() and then moving on by analyzing the [example application]().
* If you are interested in understanding what are the **security threats** of *improper dynamic code loading* fixed by GNR check out the [security resume]().
* If you would like to implement cool features of GNR like **silent updates**, **handling more containers**, **concurrent code loading** or **dynamically loading JAR libraries in your applications** you should give a look at the [complementary topics]().
* Finally you may also need to **consult** the *JavaDoc-like* [API documentation](). 

## License

*Grab'n Run* is released under the *Apache* license. Check the COPYRIGHT file for further details.