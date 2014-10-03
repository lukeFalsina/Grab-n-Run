Discussion of an example project
================================

Before digging into this section, you are **strongly** encouraged to read :doc:`tutorial` for an **introductory description** on the features of Grab`n Run library.

The **aim of the sample application** is to give you some *hints on how to use the classes in Grab'n Run and how they will behave across different contexts*. The **source code** of the example can be found `here <http://fill.link.com>`_.

Different extracts of code will be considered and explained in the following sections of this page.

MainActivity.java
-----------------

`MainActivity <http://fill.link.com>`_ is the **entry point** of the sample application. In its overloaded method ``onCreate()`` it initializes through a ``ListView`` a set of buttons used to select the *different test cases* present in the application.

DexClassLoader (.apk) vs SecureDexClassLoader (.apk)
----------------------------------------------------

In this first scenario you will consider how to retrieve an `Activity <http://developer.android.com/reference/android/app/Activity.html>`_ class, whose name is ``NasaDailyImage``, stored in the *.apk* container, called *test.apk*, through the use of `DexClassLoader <http://developer.android.com/reference/dalvik/system/DexClassLoader.html>`_ and ``SecureDexClassLoader``.

The relevant **code** in this case is the one of the two methods ``setUpDexClassLoader()`` and ``setUpSecureDexClassLoader()``, which are triggered by tapping the related two buttons on the ``MainActivity`` view.

setUpDexClassLoader()
~~~~~~~~~~~~~~~~~~~~~

In this method a standard initialization of a ``DexClassLoader`` is applied.
So at first the usual **application-private, writable directory** for caching loaded *.dex* classes must be set up.

Then a ``DexClassLoader`` object is initialized using *test.apk*, a container located directly in the phone external storage ( as described by ``exampleTestAPKPath``), as its *jar path* for the classes to load.

Finally the ``NasaDailyImage`` Activity is loaded. If such an operation is successful the **simple name** of the **loaded class** is shown to the user through a *toast message*; otherwise different **exceptions** are raised and show again through a toast message an appropriate helper message.

setUpSecureDexClassLoader()
~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this method **repeated** ``loadClass()`` **calls** are performed on differently initialized ``SecureDexClassLoader`` instances in order to *show different behaviors* of the loader class while retrieving the usual ``NasaDailyImage`` Activity.

At first a ``SecureLoaderFactory`` object is created. Then this instance is used to generate three ``SecureDexClassLoader`` that covers different cases and ends up with different results on the load operation:

1.	**Test case 1:** Load a class through ``SecureDexClassLoader`` without providing an associative map for certificates location

	This first test case shows a **common error** that a developer may encounter when using this library for the first time.
	If you want to have the location of the certificate being computed by `reversing the package name <http://fill.it>`_ you still need to **populate an associative map** with entries like (*"any.package.name"*, **null**) and use it as a parameter of the method ``createDexClassLoader()``. To understand why the class works in this way think of this system as a kind of `white listing <http://en.wikipedia.org/wiki/Whitelist>`_. Only those classes inside packages which are *declared into the associative map* will be considered as possible valid ones, while all classes belonging to a **not listed package** will be **immediately rejected**.

	And this is exactly what happens in this test case where **no associative map is provided** and so all the classes in the two containers, including the target ``NasaDailyImage``, are **prevented from being loaded** since there is *no clue on the certificate location*.

2.	**Test case 2:** Failed load of a class through ``SecureDexClassLoader`` with an associative map 

	In the second test case you can see different ways to **populate** the associative map ``packageNamesToCertMap``, used to *link packages with certificates location*.

	.. note::
		Always keep in mind that **prior** to **downloading** a certificate from the **web** the certificate for that package will be **searched inside the application-private directory** reserved for certificates and then possibly at the remote location. If you wish to *just look at the remote URL* without considering cached certificates, always remember to **wipe out private application data** through the invocation of the method ``wipeOutPrivateAppCachedData()`` **before dismissing** your ``SecureDexClassLoader``.


	The first ``put()`` *call* inserts the package name *headfirstlab.nasadailyimage* of the class that we would like to load later in the example and associates it with a **valid remote URL**. What you can immediately notice by pointing your browser to that URL is that the *remote certificate* in this case is a **self-signed developer** one since the **subject** of the certificate is **also** the **issuer** of it but, as it is mentioned in the :doc:`tutorial` this is perfectly fine.

	The *second  and the third entry* inserted into the associative map provide *remote URLs* to an **inexistent certificate** (once again you can try to point there your browser to easy spot this out). More over since *no certificate for those two package names is already inside the application-private cache directory*, then **no certificate** is **available** for them and that is the reason why *any class* belonging to one of these two packages will be **rejected and prevented from being loaded** by ``SecureDexClassLoader``.

	Lastly the fourth ``put()`` call on the associative map will insert a package name that will be also used to *construct the remote URL* (**reverse package name**). Once again the final remote URL points to no certificate so any class, whose package name is *it.polimi.example3*, will be rejected from being loaded.

	In the end a ``SecureDexClassLoader`` is generated using as container class a valid *apk* file containing the target class but **signed with a different certificate**, the *Debug Android Certificate*, from the one of the developer. The result of the ``loadClass()`` method is in this case that *no class object will be returned* since the apk is **not signed** with the **required certificate**.

3.	**Test case 3:** Successful load of a class through ``SecureDexClassLoader`` with an associative map

	In this last test case a **successful example** of dynamic code loading is shown. This time ``SecureDexClassLoader`` is initialized with a **valid** *apk* container, **signed** with the **correct developer certificate**, and with the associative map previously initialized in *Test case 2*. The whole process works fine since this associative map contains the necessary key entry *headfirstlab.nasadailyimage* and the related developer **certificate** has been **already cached** during *Test case 2*. Finally during the **signature verification step** inside the ``loadClass()`` method all the entries inside the container match properly with their signature and the certificate used for that signing process is exactly the one linked to *headfirstlab.nasadailyimage* package. That is the reason why *dynamic loading* of ``NasaDailyImage`` activity is **allowed**.

DexClassLoader (.jar) vs SecureDexClassLoader (.jar)
----------------------------------------------------

An other different example to show the power of dynamic code loading and the **security weakness** of the standard ``DexClassLoader`` is represented by the following example. In this scenario we have another activity (the source code is contained into *DexClassSampleActivity.java*) which instantiates a certain number of **GUI components** (a couple of buttons, a text view, a switch..) and then **customize** them according to the methods of an object belonging to the **external** class ``ComponentModifier``, which is **dynamically loaded** at run time.

Depending on the user choice (tapping one button in stead of the other) a different extension class of ``ComponentModifier`` is loaded and a different behavior is shown to the user. This loading operation can be realized easily by means of ``DexClassLoader`` as shown in the method ``retrieveComponentModifier()`` of the source code..

That's just a pity that the container used by ``DexClassLoader`` in this example is actually a **repackaged version** of the original *.apk* and so malicious code could have been possibly executed without the user sake!

On the other hand if we repeat the same experiment with ``SecureDexClassLoader`` the repackaged *.apk* container is this time detected and erased during the **signature verification procedure** with the developer certificate in the ``loadClass()`` method because *malicious modified entries will not succeed in the verification with the initial signature stored inside the container and the developer certificate* retrieved from the associative map. Because of this ``SecureDexClassLoader`` **won't load** the customization extension classes and it will just **end up the activity**, which is exactly the **secure** behavior that you, *as a developer*, would like to obtain :)  

Create PackageContext
---------------------

Coming soon.. More or less ;)