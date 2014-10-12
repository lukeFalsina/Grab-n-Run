Quick start and tutorial
========================

In this section you will learn how to *retrieve and include Grab'n Run library* into your project (either by using Android Development Tool or Android Studio). After this setup a **brief tutorial** will explain how to use different classes in the library to **secure** the *dynamic code loading operations*.

Since this section is **introductory** and more descriptive, it should be read by those who are not familiar with this library or more in general with *class loading* in Android. On the other hand the :doc:`javaAPI/packages` section provides a more complete and detailed view on Grab'n Run library and its insights, while :doc:`example` shows a simple use case of the concepts introduced here.

Retrieve Grab'n Run
-------------------

TODO

Include Grab'n Run in your project
----------------------------------
TODO

Android Development Tool (ADT)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Text here

Android Studio
~~~~~~~~~~~~~~

Text here

Tutorial
--------

This tutorial assumes that you have *already retrieved and imported Grab'n Run* into your Android project.

Using standard DexClassLoader to load code dynamically
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Let us pretend that you want to dynamically load an external class through `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_, a class in the *Android API* used to load classes from *jar* and *apk* files containing a **classes.dex** entry. This is a convenient way to execute code not installed as part of an application package.

Let's assume, for example, that you want to load an instance of ``com.example.MyClass`` located in the container *examplejar*, stored in the *Download* folder of the sd_card on the target phone.
A snippet of code to achieve this goal is the following::

		MyClass myClassInstance = null;
		String jarContainerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/examplejar";
		File dexOutputDir = getDir("dex", MODE_PRIVATE);
		DexClassLoader mDexClassLoader = new DexClassLoader(	jarContainerPath, 
									dexOutputDir.getAbsolutePath(), 
									null, 
									getClass().getClassLoader());
		
		try {
			Class<?> loadedClass = mDexClassLoader.loadClass("com.example.MyClass");
			myClassInstance = (MyClass) loadedClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

The String ``jarContainerPath`` contains the path to *examplejar*, while ``dexOutputDir`` is an **application-private, writable** directory to cache optimized classes into *examplejar*.

The object ``mDexClassLoader`` is then initialized as a ``DexClassLoader`` instance, which loads all the classes
into *examplejar* and caches their optimized version into ``dexOutputDir``. No native library is included
since the third parameter of the constructor is ``null`` and the `ClassLoader <http://developer.android.com/reference/java/lang/ClassLoader.html>`_ of the current activity is passed as parent class loader.

Finally the designated class is, at first, loaded by invoking the ``loadClass()`` method on ``mDexClassLoader`` with the **full class name** provided as a parameter and, secondly, instantiated through the ``newInstance()`` method and the forced
casting to ``MyClass``. The three different **catch blocks** are used to handle different exceptions that may be raised during the process.

.. note::
	Notice that a **full class name** is required and so the complete package name separated by dots must precede the class name.
	So in the example the full class name is ``com.example.MyClass`` and not just the short class name ``MyClass``.
	In case that a short class name is provided, it is likely that a ``ClassNotFoundException`` will be raised at runtime.

This snippet of code is perfectly fine and working but it is **not completely secure** since neither integrity on the container of the classes, neither authentication on the developer of the container are checked before executing the code.
And here comes ``SecureDexClassLoader`` to solve all of these issues.  

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

**developer certificate**
	a certificate, which in Android can be even *self-signed*, used to sign all the entries
	contained in a *jar* or in an *apk* container. Notice that in the Android environment in order to run 
	an application on a smart phone or to publish it on a store, the *signing step* is **mandatory** and can be 
	used to check that an *apk* was actually written and approved by the issuer of the certificate.
	For more details on signing applications and certificate, please check `here <http://developer.android.com/tools/publishing/app-signing.html#cert>`_.

So in this example we assume that all the classes belonging to the package ``com.example`` have been signed 
with a self-signed certificate, stored at ``https://something.somethelse.com/example_cert.pem``.
Since here you just want to load ``com.example.MyClass`` the following snippet of code is enough::

		Map<String, String> packageNamesToCertMap = new HashMap<String, String>();
		packageNamesToCertMap.put("com.example", "https://something.somethelse.com/example_cert.pem");

.. note::
	Any *self-signed certificate* can be used to validate classes to load as long as it is not 
	expired and it suits the standard `X509 Certificate <http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html>`_ format. The only exception is
	represented by the **Android Debug Certificate**, a certificate used to sign applications before
	running them in debug mode and not safe to use during production phase.
	``SecureDexClassLoader`` has been instructed to automatically reject class loading for classes 
	whose package name has been associated to the **Android Debug Certificate** and so **DO NOT USE IT**
	to check the signature of your containers.

.. note::
	You may want to insert more than one entry into the associative map. This is useful whenever you want to
	use the same ``SecureDexClassLoader`` to load classes which belong to different packages. Still 
	remember that each class can only be associated with **one and only one** certificate location.
	Pushing into the associative map an entry with an already existing package name will simply overwrite 
	the previously chosen location of the certificate for that package name.

.. note::
	For each entry of the map only an **HTTPS** link will be accepted. This is necessary in order to 
	**avoid MITM (Man-In-The-Middle)** attacks while retrieving the certificate. In case that an **HTTP**
	link is inserted, ``SecureLoaderFactory`` will enforce HTTPS protocol on it and in any case whenever 
	no certificate is found at the provided URL, no dynamic class loading will succeed for any class of 
	the related package so **take care to verify** that certificate URL is correctly spelled and working.

Now it comes the time to initialize a ``SecureDexClassLoader`` object through the method ``createDexClassLoader()``
of ``SecureLoaderFactory``::

		SecureDexClassLoader mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	jarContainerPath, 
													null, 
													packageNamesToCertMap, 
													getClass().getClassLoader());

``mSecureDexClassLoader`` will be able to load the classes whose container path is listed in ``jarContainerPath`` and 
it will use the ``packageNamesToCertMap`` to retrieve all the required certificate from the web and import them into 
an application private certificate folder. Also notice that in this case no directory to cache output classes is needed
since ``SecureDexClassLoader`` will automatically reserve such a folder.

.. note::
	As stated in the API documentation ``jarContainerPath`` may link many different containers separated by ``:`` and 
	for such a reason the **developer is responsible** of filling the associative map of the certificates location
	accordingly with all the entries needed to cover all the package names of the classes to be loaded.

.. note::
	``DexClassLoader``, the standard class from Android API, is able to parse and import only those *jar* and *apk* 
	containers listed in ``jarContainerPath`` as resources stored on the mobile device. In addition to this 
	``SecureDexClassLoader`` is also capable of **downloading containers** directly stored on the web 
	(i.e. **HTTP or HTTPS URL**) and to import them into an application-private directory to avoid code injections 
	from attackers.
	
	Example::

		jarContainerPath = "http://something.somethingelse.com/dev/examplejar"

	This ``jarContainerPath`` will retrieve no resource when used in the constructor of ``DexClassLoader`` but it 
	is perfectly fine as first parameter of the ``mSecureLoaderFactory.createDexClassLoader()`` call.

Finally you can use the resulting ``mSecureDexClassLoader`` to load the desired class by means of this call::

	 	try {
			Class<?> loadedClass = mSecureDexClassLoader.loadClass("com.example.MyClass");
			myClassInstance = (MyClass) loadedClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// Handle this exception since now it is not necessary an error
			// but it may be a security constraint being violated..
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

It is important to remember that the ``mSecureDexClassLoader.loadClass()`` call will return ``null`` not only if
no class matching the provided name is found (as it happens in ``DexClassLoader``) but also whenever **at least one 
of the following security constraints is violated**:

* The *package name* of the class used as a parameter of ``loadClass()`` was **not previously included in the associative
  map** and so it do not exist any certificate that could be used to validate this class.
* The *package name* of the class used as a parameter of ``loadClass()`` was previously included in the associative map
  but the **related certificate** was **not found** (improper URL or no connectivity) or **not valid** 
  (i.e. expired certificate, use of the Android Debug Certificate).
* The *container file* of the required class was **not signed**.
* The *container file* of the required class was **not signed with the certificate associated** to the package name 
  of the class. [Missing trusted certificate]
* At least one of the **entry** of the *container file* do **not match its signature** even if the certificate used to sign
  the container file is the trusted one. [Possibility of repackaged container]

For all of these reasons you should always pay attention in **handling exceptions** thrown in this case since they may 
be a clue to **establish security violation**. *Informative and debug messages* will be generated in the logs by the 
classes of the Grab'n Run library in order to help you figure out what it is happening.

.. note::
	Every time that ``SecureDexClassLoader`` finds out a repackaged container, it will immediately delete this file
	from the device since a fresh and genuine copy of the container should be retrieved instead.

Wiping out cached containers and certificates
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In order to *improve performance* and offer the possibility to *partially work also when connectivity is limited*, 
``SecureDexClassLoader`` will store certificates retrieved from the web into an **application-private directory**.

Every time that a certificate is needed to verify a class, ``SecureDexClassLoader`` will at first look for it
inside this directory and then, if no match is found, possibly download it from the web.

It was also stated into `Using SecureDexClassLoader to load dynamic code securely`_ that, differently from
``DexClassLoader``, ``SecureDexClassLoader`` is also able to **download and import remote containers** into an
*application-private folder*.

Because of this features it may come useful to the developer the possibility to easily delete either downloaded containers 
or certificates or both of them imported by a ``SecureDexClassLoader`` object. In order to do so a call to the
``wipeOutPrivateAppCachedData()`` is sufficient.

Let us consider again the previous scenario: after having tried to load ``com.example.MyClass``, if you want to *cancel
both the certificates and the containers* used by the related ``mSecureDexClassLoader``, the code to insert is::

		mSecureDexClassLoader.wipeOutPrivateAppCachedData(true, true);

.. note::
	After that you *have canceled at least one between the certificates and the containers*, ``mSecureDexClassLoader``
	will always return ``null`` to any invocation of the ``loadClass()`` method. So it will be **necessary** for you
	to require a **new** ``SecureDexClassLoader`` instance to ``SecureLoaderFactory``.