
Quick start and tutorial
========================

In this section you will see how to *retrieve and include Grab'n Run library* into your project (either by using Android Studio ot the Android Development Tool). After this setup step a **brief tutorial** will explain how to use classes in the library to **secure** the *dynamic code loading operations*.

Since this section is **introductory** and more descriptive, it should be read by those who are not familiar with this library or more in general with *class loading* in Android. On the other hand the :doc:`complementary` section provides a more complete and detailed view on *Grab'n Run* library and its insights, while :doc:`example` shows a simple use case of the concepts introduced here.

Quick Setup
-----------

Setting up GNR as an **additional library** for your *Android application* is very easy:

Android Studio (AS)
~~~~~~~~~~~~~~~~~~~
..	highlight:: groovy

1. Modify the *build.gradle* file in the *app* module of your Android project by adding the following *compile* line in the *dependencies* body::

	dependencies {
		// Grab'n Run will be imported from JCenter.
		// Verify that the string "jcenter()" is included in your repositories block!
		compile 'it.necst.grabnrun:grabnrun:1.0.2'
	}

2. Resync your project to apply changes.

Android Development Tool (ADT)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1. Download the latest `version <https://github.com/lukeFalsina/Grab-n-Run/raw/master/downloads/gnr-1.0.2.jar>`_ of the *JAR* container of Grab'n Run.

2. Include the *JAR* in the **libs** subfolder of your Android project.

Adding missing permissions (Both AS and ADT)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
..	highlight:: xml

Finally it is required to modify the *Android Manifest* of your application by adding a couple of required permissions if they are not already in place::

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

..	highlight:: java

Tutorial
--------

This tutorial assumes that you have **already retrieved Grab'n Run and linked it** to one of your existing Android projects.

Using standard DexClassLoader to load code dynamically
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Let us pretend that you want to dynamically load an external class through `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_, a class in the *Android API* used to load classes from *jar* and *apk* files containing a **classes.dex** entry. This is a convenient way to execute code not installed as part of an application package.

Let's assume, for example, that you want to load an instance of ``com.example.MyClass`` located in the container *exampleJar.jar*, stored in the *Download* folder of the sd_card on the target phone. Note that this scenario may potentially lead to a **code injection** attack when you use the standard ``DexClassLoader`` since you are choosing to load code from a container which is stored in a **world writable location** of your phone. Notice that this kind of attack would be prevented with ``SecureDexClassLoader``.
Anyway a snippet of code to achieve this task is the following::

		MyClass myClassInstance = null;
		String jarContainerPath = 	Environment.getExternalStorageDirectory().getAbsolutePath() 
						+ "/Download/exampleJar.jar";
		File dexOutputDir = getDir("dex", MODE_PRIVATE);
		DexClassLoader mDexClassLoader = new DexClassLoader(	jarContainerPath, 
									dexOutputDir.getAbsolutePath(), 
									null, 
									getClass().getClassLoader());
		
		try {
			Class<?> loadedClass = mDexClassLoader.loadClass("com.example.MyClass");
			myClassInstance = (MyClass) loadedClass.newInstance();

			// Do something with the loaded object myClassInstance
			// i.e. myClassInstance.doSomething();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

The String ``jarContainerPath`` contains the path to *examplejar*, while ``dexOutputDir`` is an **application-private**, writable directory to cache optimized *dex* classes into *examplejar*. As reported in ``DexClassLoader`` documentation, you can retrieve ``dexOutputDir`` in different ways but it is fundamental that this cache folder is application-private; otherwise your application may be subjected to **code injection attacks**. And by the way this kind of attack is *prevented* if you choose to use ``SecureDexClassLoader`` as explained later on in this guide.

The object ``mDexClassLoader`` is then initialized as a ``DexClassLoader`` instance, which loads all the classes
into *examplejar* and caches their optimized version into ``dexOutputDir``. No native library is included
since the third parameter of the constructor is ``null`` and the `ClassLoader <http://developer.android.com/reference/java/lang/ClassLoader.html>`_ of the current activity is passed as parent class loader.

Finally the designated class is, at first, loaded by invoking the ``loadClass()`` method on ``mDexClassLoader`` with the **full class name** provided as a parameter and, secondly, instantiated through the ``newInstance()`` method and the forced
casting to ``MyClass``. The three different **catch blocks** are used to handle different exceptions that may be raised during the process.

**Package Name**
	In Java every class is associated to a **package name**. A **package** is a *grouping of related classes, interfaces and enumerations* providing **access protection** and **name space management**. In particular in *Grab'n Run* packages names are accepted if and only if they are *a sequence of at least two not-empty, dot-separated words, which ends with a word and not with a dot*. This implies that the following are all examples of **invalid** package names: ``com``, ``com..application``, ``com.application.``, while **suitable** package names are ``com.application`` or ``it.polimi.necst.gnr``. As you will see later on in this tutorial, package names perform a **relevant functionality** in **GNR** system since they *link containers to be verified with the certificate used to do so*.

.. warning::
	Notice that a **full class name** is required to successfully load a class and so the **complete package name** separated by dots must **precede** the **class name**.
	Referred to the example, full class name is ``com.example.MyClass`` and not just the short class name ``MyClass``, which would produce a failure in the class loading operation.
	In particular if it is the case that a short class name is provided in stead of a full one, it is likely that a ``ClassNotFoundException`` will be raised at runtime.

This snippet of code is perfectly fine and working but it is **not completely secure** since neither integrity on the container of the classes, neither authentication on the developer of the container are checked before executing the code.
And here comes ``SecureDexClassLoader`` to solve these issues.  

.. _Using SecureDexClassLoader to load dynamic code securely:

Using SecureDexClassLoader to load dynamic code securely 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In order to improve the security of the snippet of code shown in `Using standard DexClassLoader to load code dynamically`_
a new version of the code is presented through the use of ``SecureDexClassLoader`` and ``SecureLoaderFactory``.

At first you should create a ``SecureLoaderFactory`` object as shown here::

		SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);

This is an helper class necessary to generate a ``SecureDexClassLoader`` object.
But before performing this step you have to initialize and provide to ``mSecureLoaderFactory`` an **associative map** 
which links all the package names of the classes that you want to dynamically load to one *developer certificate*,
which is stored at a **secure web location** (i.e. an HTTPS link) and which was previously used 
to sign the *jar* or *apk* container which holds those classes.

**Developer Certificate**
	a certificate, which in Android can be even *self-signed*, used to sign all the entries
	contained in a *jar* or in an *apk* container. Notice that in the Android environment in order to run 
	an application on a smart phone or to publish it on a store, the *signing step* is **mandatory** and can be 
	used to check that an *apk* was actually written and approved by the issuer of the certificate.
	For more details on signing applications and certificate, please check `here <http://developer.android.com/tools/publishing/app-signing.html#cert>`_.

So in this example we assume that all the classes belonging to the package ``com.example`` have been signed 
with a self-signed certificate, stored at ``https://something.com/example_cert.pem``.
Since here you just want to load ``com.example.MyClass`` the following snippet of code is enough::

		Map<String, URL> packageNamesToCertMap = new HashMap<String, URL>();
		try {
			packageNamesToCertMap.put(	"com.example",
							new URL("https://something.com/example_cert.pem"));

		} catch (MalformedURLException e) {
			// The previous URL used for the packageNamesToCertMap entry was a malformed one.
			Log.e("Error", "A malformed URL was provided for a remote certificate location");
		}
		

.. note::
	Any *self-signed certificate* can be used to validate classes to load as long as it is not 
	expired and it suits the standard `X509 Certificate <http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html>`_ format. The only exception is
	represented by the **Android Debug Certificate**, a certificate used to sign applications before
	running them in debug mode and not safe to use during production phase.
	``SecureDexClassLoader`` has been instructed to automatically reject class loading for classes 
	whose package name has been associated for signature verification to the **Android Debug Certificate** 
	and so **DO NOT USE IT** to check the signature of your containers.

.. note::
	You may want to insert more than one entry into the associative map. This is useful whenever you want to
	use the same ``SecureDexClassLoader`` to load classes which belong to different packages. Still 
	remember that each package name can only be associated with **one and only one** certificate location.
	Pushing into the associative map an entry with an already existing package name will simply overwrite 
	the previously chosen location of the certificate for that package name.

.. warning::
	For each entry of the map only an **HTTPS** link will be accepted. This is necessary in order to 
	**avoid MITM (Man-In-The-Middle)** attacks while retrieving the *trusted* certificate. In case that an **HTTP**
	link is inserted, ``SecureLoaderFactory`` will enforce *HTTPS protocol* on it and in any case whenever 
	no certificate is found at the provided URL, no dynamic class loading will succeed for any class of 
	the related package so **take care to verify** that certificate URL is correctly spelled and working via **HTTPS** protocol.

Now it comes the time to initialize a ``SecureDexClassLoader`` object through the method ``createDexClassLoader()``
of ``SecureLoaderFactory``::

		SecureDexClassLoader mSecureDexClassLoader = 
			mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
									null, 
									getClass().getClassLoader(),
									packageNamesToCertMap);

``mSecureDexClassLoader`` will be able to load the classes whose container path is listed in ``jarContainerPath`` and 
it will use the ``packageNamesToCertMap`` to retrieve all the required certificate from the web and import them into 
an application private certificate folder. Also notice that in this case no directory to cache output classes is needed
since ``SecureDexClassLoader`` will automatically reserve such a folder.

.. warning::
	As stated in the `API documentation <http://developer.android.com/reference/dalvik/system/DexClassLoader.html#DexClassLoader(java.lang.String, java.lang.String, java.lang.String, java.lang.ClassLoader)>`_ ``jarContainerPath`` may link many *different containers* separated by ``:`` and 
	for such a reason the **developer is responsible** of filling the associative map of the certificates location
	accordingly with all the entries needed to cover all the package names of the classes to be loaded.

.. note::
	``DexClassLoader``, the standard class from Android API, is able to parse and import only those *jar* and *apk* 
	containers listed in ``jarContainerPath`` which are directly saved on the mobile device storage. In addition to this 
	``SecureDexClassLoader`` is also capable of **downloading remote containers** from the web 
	(i.e. **HTTP or HTTPS URL**) and to import them into an application-private directory to avoid code injections 
	from attackers.
	
	Example::

		jarContainerPath = "http://something.com/dev/exampleJar.jar";

	This ``jarContainerPath`` will retrieve no resource when used in the constructor of ``DexClassLoader`` but it 
	is perfectly fine as a first parameter of the ``mSecureLoaderFactory.createDexClassLoader()`` call, as long as
	a *jar* container is actually stored at the remote location.

Finally you can use the resulting ``mSecureDexClassLoader`` to load the desired class in a similar fashion to ``DexClassLoader``::

	 	try {
			Class<?> loadedClass = mSecureDexClassLoader.loadClass("com.example.MyClass");

			// Check whether the signature verification process succeeds
			if (loadedClass == null) {

				// One of the security constraints was violated so no class
				// loading was allowed..
			}
			else {

				// Class loading was successful and performed in a safe way.
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
		}

It is important to notice that, differently from ``DexClassLoader``, the ``mSecureDexClassLoader.loadClass()`` call will 
return ``null``  whenever **at least one of the following security constraints is violated**:

* The *package name* of the class used as a parameter of ``loadClass()`` was **not previously included in the associative
  map** and so it do not exist any certificate that could be used to validate this class.
* The *package name* of the class used as a parameter of ``loadClass()`` was previously included in the associative map
  but the **related certificate** was **not found** (URL with no certificate file attached or no connectivity) or **not valid** 
  (i.e. expired certificate, use of the Android Debug Certificate).
* The *container file* of the required class was **not signed**.
* The *container file* of the required class was **not signed with the certificate associated** to the package name 
  of the class. [Missing trusted certificate]
* At least one of the **entry** of the *container file* do **not match its signature** even if the certificate used to sign
  the container file is the trusted one. [Possibility of **repackaged container**]

For all of these reasons you should always check and pay attention when a **null** pointer is returned after a 
``mSecureDexClassLoader.loadClass()`` call since this is a clear clue to establish either a wrong set up of 
``SecureLoaderFactoty`` and ``SecureDexClassLoader`` or a security violation. 
*Informative and debug messages* will be generated in the logs by the classes of the Grab'n Run library in order 
to help you figure out what it is happening.

.. note::
	Every time that ``SecureDexClassLoader`` finds out a (possibly repackaged) **invalid container**, it will immediately 
	**delete** this file from its **application-private directory**. Nevertheless if this container is *stored on your device* 
	it may be a good idea for you, as a developer, after having double checked that you have properly set up ``SecureDexClassLoader``, 
	to **look for a fresh copy** of the container or at least **not to trust** and delete this container from the phone.

Please notice, on the other hand, that the three exceptions caught in the try-catch block surrounding the ``loadClass()`` method 
behaves and are thrown in the same way as it would happen with ``DexClassLoader``.

Finally for clarity the **full snippet of code** presented in this section is reported here::

		MyClass myClassInstance = null;
		jarContainerPath = "http://something.com/dev/exampleJar.jar";

		try {
			Map<String, URL> packageNamesToCertMap = new HashMap<String, URL>();
			packageNamesToCertMap.put(	"com.example",
							new URL("https://something.com/example_cert.pem"));

			SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);
			SecureDexClassLoader mSecureDexClassLoader = 
				mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
										null, 
										getClass().getClassLoader(),
										packageNamesToCertMap);
		
			Class<?> loadedClass = mSecureDexClassLoader.loadClass("com.example.MyClass");

			// Check whether the signature verification process succeeds
			if (loadedClass == null) {

				// One of the security constraints was violated so no class
				// loading was allowed..
			}
			else {

				// Class loading was successful and performed in a safe way.
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


Wiping out cached containers and certificates
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In order to *improve performance* and offer the possibility to *partially work also when connectivity is limited*, 
``SecureDexClassLoader`` will store certificates retrieved from the web and all containers into specific **application-private directories**.

Every time that a **resource** (container or certificate) is needed to load or verify a class, ``SecureDexClassLoader`` will at first 
look for it inside its private directories and then, if no match is found, possibly attempt to download it from the web or found it 
at a specified location on the device (this last option is applicable only for containers).

.. It was also stated into `Using SecureDexClassLoader to load dynamic code securely`_ that, differently from
.. ``DexClassLoader``, ``SecureDexClassLoader`` is also able to **download and import remote containers** into an
.. *application-private folder*.

Even if these **caching features** may come really useful and *speed up* significantly ``SecureDexClassLoader`` execution,
it would be also nice for the developer to have the possibility to **choose** whether a **fresh or cached copy** of either a 
certificate or a container should be used for the *dynamic loading operations*. And that is the reason why ``SecureDexClassLoader``
provides a method called ``wipeOutPrivateAppCachedData()`` to manage this choice.

To present this method let us consider again the previous scenario shown in `Using SecureDexClassLoader to load dynamic code securely`_: 
after having tried to load ``com.example.MyClass``, if you want to *delete both the cached certificates and the containers* used by the 
related ``mSecureDexClassLoader``, in order to impose for the next loading operation the retrieval of **fresh resources**, the call to 
perform is the following::

		mSecureDexClassLoader.wipeOutPrivateAppCachedData(true, true);

.. warning::
	After that you *have erased at least one cached resource between the certificates and the containers*, ``mSecureDexClassLoader``
	will always return ``null`` for **consistency reason** to any invocation of the ``loadClass()`` method. 
	So it will be **necessary** for you to require a **new** ``SecureDexClassLoader`` instance to ``SecureLoaderFactory``
	through the invocation of the ``createDexClassLoader()`` method before being able to dynamically and securely load other classes.
